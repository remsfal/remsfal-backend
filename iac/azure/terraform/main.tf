# Random suffix for globally unique Key Vault name
resource "random_string" "kv_suffix" {
  length  = 4
  special = false
  upper   = false
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = local.resource_group_name
  location = var.location
  tags     = local.common_tags
}

# Log Analytics Workspace
resource "azurerm_log_analytics_workspace" "main" {
  name                = local.log_analytics_workspace_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "PerGB2018"
  retention_in_days   = 30
  tags                = local.common_tags
}

# Application Insights
resource "azurerm_application_insights" "main" {
  name                = local.application_insights_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  workspace_id        = azurerm_log_analytics_workspace.main.id
  application_type    = "web"
  tags                = local.common_tags
}

# Key Vault
resource "azurerm_key_vault" "main" {
  name                       = local.key_vault_name
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = false
  enable_rbac_authorization  = true

  tags = local.common_tags
}

# Grant Key Vault Administrator to Terraform service principal
resource "azurerm_role_assignment" "terraform_kv_admin" {
  scope                = azurerm_key_vault.main.id
  role_definition_name = "Key Vault Administrator"
  principal_id         = data.azurerm_client_config.current.object_id
}

# Container Registry
resource "azurerm_container_registry" "main" {
  name                = local.container_registry_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Basic"
  admin_enabled       = true
  tags                = local.common_tags
}

# Storage Account for Blob Storage
resource "azurerm_storage_account" "main" {
  name                     = local.storage_account_name
  resource_group_name      = azurerm_resource_group.main.name
  location                 = azurerm_resource_group.main.location
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
  tags                     = local.common_tags
}

# Storage Containers
resource "azurerm_storage_container" "containers" {
  for_each              = toset(local.storage_containers)
  name                  = each.value
  storage_account_id    = azurerm_storage_account.main.id
  container_access_type = "private"
}

# PostgreSQL Flexible Server
resource "azurerm_postgresql_flexible_server" "main" {
  name                   = local.postgres_server_name
  resource_group_name    = azurerm_resource_group.main.name
  location               = azurerm_resource_group.main.location
  version                = "16"
  administrator_login    = var.postgres_admin_username
  administrator_password = var.postgres_admin_password
  storage_mb             = var.postgres_storage_mb
  sku_name               = var.postgres_sku
  backup_retention_days  = 7
  zone                   = "1"

  authentication {
    active_directory_auth_enabled = true
    password_auth_enabled         = true
    tenant_id                     = data.azurerm_client_config.current.tenant_id
  }

  tags = local.common_tags
}

# PostgreSQL Firewall Rule - Allow Azure Services
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_azure" {
  name             = "allow-azure-services"
  server_id        = azurerm_postgresql_flexible_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# PostgreSQL Firewall Rule - Allow all (for development/testing)
resource "azurerm_postgresql_flexible_server_firewall_rule" "allow_all" {
  count            = var.environment == "dev" ? 1 : 1 # Adjust as needed for other environments
  name             = "allow-all"
  server_id        = azurerm_postgresql_flexible_server.main.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "255.255.255.255"
}

# PostgreSQL Database
resource "azurerm_postgresql_flexible_server_database" "platform" {
  name      = "REMSFAL"
  server_id = azurerm_postgresql_flexible_server.main.id
  charset   = "UTF8"
  collation = "en_US.utf8"
}

# Note: PostgreSQL AD Administrator can be configured manually after deployment
# Azure AD authentication is enabled, but admin must be set via Azure Portal or CLI:
# az postgres flexible-server ad-admin create --resource-group <rg> --server-name <server> \
#   --object-id <user-object-id> --display-name <admin-name> --type User

# Note: PostgreSQL role assignments for Managed Identity must be done manually via SQL
# After deployment, run: GRANT ALL PRIVILEGES ON DATABASE REMSFAL TO "<managed_identity_name>";


# Cosmos DB Account with Cassandra API
resource "azurerm_cosmosdb_account" "main" {
  name                = local.cosmos_account_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  offer_type          = "Standard"
  kind                = "GlobalDocumentDB"

  capabilities {
    name = "EnableCassandra"
  }

  consistency_policy {
    consistency_level = "Session"
  }

  geo_location {
    location          = azurerm_resource_group.main.location
    failover_priority = 0
  }

  tags = local.common_tags
}

# Cosmos DB Cassandra Keyspace
resource "azurerm_cosmosdb_cassandra_keyspace" "main" {
  name                = local.cosmos_keyspace_name
  resource_group_name = azurerm_cosmosdb_account.main.resource_group_name
  account_name        = azurerm_cosmosdb_account.main.name
  throughput          = var.cosmos_throughput
}

# Cosmos DB Cassandra Tables
resource "azurerm_cosmosdb_cassandra_table" "tables" {
  for_each              = local.cosmos_tables
  name                  = each.key
  cassandra_keyspace_id = azurerm_cosmosdb_cassandra_keyspace.main.id

  schema {
    dynamic "column" {
      for_each = each.value.schema.columns
      content {
        name = column.value.name
        type = column.value.type
      }
    }

    dynamic "partition_key" {
      for_each = each.value.schema.partition_keys
      content {
        name = partition_key.value.name
      }
    }

    dynamic "cluster_key" {
      for_each = each.value.schema.cluster_keys
      content {
        name     = cluster_key.value.name
        order_by = cluster_key.value.order_by
      }
    }
  }
}
# Store PostgreSQL connection string in Key Vault
resource "azurerm_key_vault_secret" "postgres_connection_string" {
  name         = "postgres-connection-string"
  value        = "jdbc:postgresql://${azurerm_postgresql_flexible_server.main.fqdn}:5432/${azurerm_postgresql_flexible_server_database.platform.name}?user=${var.postgres_admin_username}&password=${var.postgres_admin_password}&sslmode=require"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

# Store Storage Account connection string in Key Vault
resource "azurerm_key_vault_secret" "storage_connection_string" {
  name         = "storage-connection-string"
  value        = "DefaultEndpointsProtocol=https;AccountName=${azurerm_storage_account.main.name};AccountKey=${azurerm_storage_account.main.primary_access_key};EndpointSuffix=core.windows.net"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

# Store Cosmos DB connection details in Key Vault
resource "azurerm_key_vault_secret" "cosmos_contact_point" {
  name         = "cosmos-contact-point"
  value        = "${azurerm_cosmosdb_account.main.name}.cassandra.cosmos.azure.com:10350"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

resource "azurerm_key_vault_secret" "cosmos_username" {
  name         = "cosmos-username"
  value        = azurerm_cosmosdb_account.main.name
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

resource "azurerm_key_vault_secret" "cosmos_password" {
  name         = "cosmos-password"
  value        = azurerm_cosmosdb_account.main.primary_key
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

# Event Hub Namespace
resource "azurerm_eventhub_namespace" "main" {
  name                = local.eventhub_namespace_name
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  sku                 = "Standard"
  capacity            = var.eventhub_capacity
  tags                = local.common_tags
}

# Event Hubs (Kafka topics)
resource "azurerm_eventhub" "topics" {
  for_each          = toset(local.eventhub_topics)
  name              = each.value
  namespace_id      = azurerm_eventhub_namespace.main.id
  partition_count   = 2
  message_retention = 1
}

# Event Hub Authorization Rule for applications (fallback)
resource "azurerm_eventhub_namespace_authorization_rule" "app_access" {
  name                = "app-access"
  namespace_name      = azurerm_eventhub_namespace.main.name
  resource_group_name = azurerm_resource_group.main.name
  listen              = true
  send                = true
  manage              = false
}

# Store Event Hub connection string in Key Vault (JAAS format for Kafka)
resource "azurerm_key_vault_secret" "eventhub_connection_string" {
  name         = "eventhub-connection-string"
  value        = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"${azurerm_eventhub_namespace_authorization_rule.app_access.primary_connection_string}\";"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

resource "azurerm_key_vault_secret" "eventhub_sasl_username" {
  name         = "eventhub-sasl-username"
  value        = "$ConnectionString"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

resource "azurerm_key_vault_secret" "eventhub_sasl_password" {
  name         = "eventhub-sasl-password"
  value        = azurerm_eventhub_namespace_authorization_rule.app_access.primary_connection_string
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

# Store Event Hub bootstrap server in Key Vault
resource "azurerm_key_vault_secret" "eventhub_bootstrap_server" {
  name         = "eventhub-bootstrap-server"
  value        = "${azurerm_eventhub_namespace.main.name}.servicebus.windows.net:9093"
  key_vault_id = azurerm_key_vault.main.id

  depends_on = [
    azurerm_key_vault.main,
    azurerm_role_assignment.terraform_kv_admin
  ]
}

# Container Apps Environment
resource "azurerm_container_app_environment" "main" {
  name                       = local.container_apps_environment_name
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
  tags                       = local.common_tags
}

# Container Apps
resource "azurerm_container_app" "apps" {
  for_each                     = var.container_apps
  name                         = "${local.base_name}-ca-${each.key}"
  container_app_environment_id = azurerm_container_app_environment.main.id
  resource_group_name          = azurerm_resource_group.main.name
  revision_mode                = "Single"

  identity {
    type = "SystemAssigned"
  }

  registry {
    server               = azurerm_container_registry.main.login_server
    username             = azurerm_container_registry.main.admin_username
    password_secret_name = "acr-password"
  }

  secret {
    name  = "acr-password"
    value = azurerm_container_registry.main.admin_password
  }

  # Secrets for Platform service (PostgreSQL, Event Hub)
  dynamic "secret" {
    for_each = each.key == "platform" ? [1] : []
    content {
      name  = "postgres-connection-string"
      value = "jdbc:postgresql://${azurerm_postgresql_flexible_server.main.fqdn}:5432/${azurerm_postgresql_flexible_server_database.platform.name}?user=${var.postgres_admin_username}&password=${var.postgres_admin_password}&sslmode=require"
    }
  }

  # Secrets for Ticketing service (Cosmos DB)
  dynamic "secret" {
    for_each = each.key == "ticketing" ? [1] : []
    content {
      name  = "cosmos-contact-point"
      value = "${azurerm_cosmosdb_account.main.name}.cassandra.cosmos.azure.com"
    }
  }

  dynamic "secret" {
    for_each = each.key == "ticketing" ? [1] : []
    content {
      name  = "cosmos-username"
      value = azurerm_cosmosdb_account.main.name
    }
  }

  dynamic "secret" {
    for_each = each.key == "ticketing" ? [1] : []
    content {
      name  = "cosmos-password"
      value = azurerm_cosmosdb_account.main.primary_key
    }
  }

  # Event Hub secrets for all services
  secret {
    name  = "eventhub-bootstrap-server"
    value = "${azurerm_eventhub_namespace.main.name}.servicebus.windows.net:9093"
  }

  secret {
    name  = "eventhub-connection-string"
    value = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$ConnectionString\" password=\"${azurerm_eventhub_namespace_authorization_rule.app_access.primary_connection_string}\";"
  }

  template {
    min_replicas = each.value.min_replicas
    max_replicas = each.value.max_replicas

    container {
      name   = each.key
      image  = "${azurerm_container_registry.main.login_server}/${each.value.image}"
      cpu    = each.value.cpu
      memory = each.value.memory

      # Application Insights for monitoring
      env {
        name  = "APPLICATIONINSIGHTS_CONNECTION_STRING"
        value = azurerm_application_insights.main.connection_string
      }

      # Azure Key Vault endpoint for AzureKeyVaultConfigSource
      # The ConfigSource loads secrets directly from Key Vault using Managed Identity
      env {
        name  = "AZURE_KEYVAULT_ENDPOINT"
        value = azurerm_key_vault.main.vault_uri
      }

      # PostgreSQL connection string for platform service
      dynamic "env" {
        for_each = each.key == "platform" ? [1] : []
        content {
          name        = "POSTGRES_CONNECTION_STRING"
          secret_name = "postgres-connection-string"
        }
      }

      # Cosmos DB credentials for ticketing service
      dynamic "env" {
        for_each = each.key == "ticketing" ? [1] : []
        content {
          name        = "COSMOS_CONTACT_POINT"
          secret_name = "cosmos-contact-point"
        }
      }

      dynamic "env" {
        for_each = each.key == "ticketing" ? [1] : []
        content {
          name        = "COSMOS_USERNAME"
          secret_name = "cosmos-username"
        }
      }

      dynamic "env" {
        for_each = each.key == "ticketing" ? [1] : []
        content {
          name        = "COSMOS_PASSWORD"
          secret_name = "cosmos-password"
        }
      }

      # Event Hub config for all services (Kafka-compatible)
      env {
        name        = "EVENTHUB_BOOTSTRAP_SERVER"
        secret_name = "eventhub-bootstrap-server"
      }

      env {
        name        = "EVENTHUB_CONNECTION_STRING"
        secret_name = "eventhub-connection-string"
      }

      # Platform service URL for JWT verification (ticketing/notification need this)
      env {
        name  = "PLATFORM_JWKS_URL"
        value = "https://${local.base_name}-ca-platform.${azurerm_container_app_environment.main.default_domain}/api/v1/authentication/jwks"
      }

      # Frontend URL base for email links etc. (points to remsfal-frontend container app)
      env {
        name  = "DE_REMSFAL_FRONTEND_URL_BASE"
        value = "https://${local.base_name}-ca-frontend.${azurerm_container_app_environment.main.default_domain}"
      }

      # Quarkus profile for production (not for OCR/frontend)
      dynamic "env" {
        for_each = contains(["platform", "ticketing", "notification"], each.key) ? [1] : []
        content {
          name  = "QUARKUS_PROFILE"
          value = "prod"
        }
      }

      # OCR-specific environment variables
      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "KAFKA_PROVIDER"
          value = "AZURE"
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "KAFKA_TOPIC_IN"
          value = "ocr.documents.to_process"
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "KAFKA_TOPIC_OUT"
          value = "ocr.documents.processed"
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "KAFKA_GROUP_ID"
          value = "ocr-service"
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "SECRETS_PROVIDER"
          value = "AZURE_KEYVAULT"
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "KEYVAULT_URL"
          value = azurerm_key_vault.main.vault_uri
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "STORAGE_PROVIDER"
          value = "AZURE"
        }
      }

      dynamic "env" {
        for_each = each.key == "ocr" ? [1] : []
        content {
          name  = "PYTHONUNBUFFERED"
          value = "1"
        }
      }

      # Frontend-specific: Backend API URLs for nginx proxy
      dynamic "env" {
        for_each = each.key == "frontend" ? [1] : []
        content {
          name  = "PLATFORM_API_URL"
          value = "https://${local.base_name}-ca-platform.${azurerm_container_app_environment.main.default_domain}"
        }
      }

      dynamic "env" {
        for_each = each.key == "frontend" ? [1] : []
        content {
          name  = "TICKETING_API_URL"
          value = "https://${local.base_name}-ca-ticketing.${azurerm_container_app_environment.main.default_domain}"
        }
      }
    }
  }

  dynamic "ingress" {
    for_each = each.value.ingress_enabled ? [1] : []
    content {
      external_enabled = each.value.external_enabled
      target_port      = each.value.target_port
      traffic_weight {
        percentage      = 100
        latest_revision = true
      }
    }
  }

  tags = local.common_tags
}

# Grant ACR Pull permissions to each Container App's System-Assigned Managed Identity
resource "azurerm_role_assignment" "container_app_acr_pull" {
  for_each             = var.container_apps
  scope                = azurerm_container_registry.main.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_container_app.apps[each.key].identity[0].principal_id
}

# Grant Storage Blob Data Contributor to each Container App's System-Assigned Managed Identity
resource "azurerm_role_assignment" "container_app_storage_blob_contributor" {
  for_each             = var.container_apps
  scope                = azurerm_storage_account.main.id
  role_definition_name = "Storage Blob Data Contributor"
  principal_id         = azurerm_container_app.apps[each.key].identity[0].principal_id
}

# Grant Event Hub Data Owner to each Container App's System-Assigned Managed Identity
resource "azurerm_role_assignment" "container_app_eventhub_data_owner" {
  for_each             = var.container_apps
  scope                = azurerm_eventhub_namespace.main.id
  role_definition_name = "Azure Event Hubs Data Owner"
  principal_id         = azurerm_container_app.apps[each.key].identity[0].principal_id
}

# Note: Cosmos DB Cassandra API does NOT support Managed Identity authentication
# Applications must use username/password (primary key) from Key Vault
# The following role assignment is for SQL API only and will not work with Cassandra API
# Commenting out to avoid confusion:
# resource "azurerm_cosmosdb_sql_role_assignment" "container_app_cosmos_data_contributor" {
#   for_each            = var.container_apps
#   resource_group_name = azurerm_resource_group.main.name
#   account_name        = azurerm_cosmosdb_account.main.name
#   role_definition_id  = "${azurerm_cosmosdb_account.main.id}/sqlRoleDefinitions/00000000-0000-0000-0000-000000000002"
#   principal_id        = azurerm_container_app.apps[each.key].identity[0].principal_id
#   scope               = azurerm_cosmosdb_account.main.id
# }

# Grant Key Vault Secrets User to each Container App's System-Assigned Managed Identity
resource "azurerm_role_assignment" "container_app_kv_secrets_user" {
  for_each             = var.container_apps
  scope                = azurerm_key_vault.main.id
  role_definition_name = "Key Vault Secrets User"
  principal_id         = azurerm_container_app.apps[each.key].identity[0].principal_id
}

# Data source for current client config
data "azurerm_client_config" "current" {}
