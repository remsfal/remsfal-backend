output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "location" {
  description = "Azure region"
  value       = azurerm_resource_group.main.location
}

output "environment" {
  description = "Environment name"
  value       = var.environment
}

# Container Registry (pre-created)
output "container_registry_name" {
  description = "Container Registry name"
  value       = data.azurerm_container_registry.main.name
}

output "container_registry_login_server" {
  description = "Container Registry login server"
  value       = data.azurerm_container_registry.main.login_server
}

output "container_registry_admin_username" {
  description = "Container Registry admin username"
  value       = data.azurerm_container_registry.main.admin_username
  sensitive   = true
}

# Storage Account
output "storage_account_name" {
  description = "Storage Account name"
  value       = azurerm_storage_account.main.name
}

output "storage_blob_endpoint" {
  description = "Blob Storage endpoint"
  value       = azurerm_storage_account.main.primary_blob_endpoint
}

# PostgreSQL
output "postgres_fqdn" {
  description = "PostgreSQL server FQDN"
  value       = azurerm_postgresql_flexible_server.main.fqdn
}

output "postgres_database_name" {
  description = "PostgreSQL database name"
  value       = azurerm_postgresql_flexible_server_database.platform.name
}

# Cosmos DB
output "cosmos_endpoint" {
  description = "Cosmos DB endpoint"
  value       = azurerm_cosmosdb_account.main.endpoint
}

output "cosmos_cassandra_contact_point" {
  description = "Cosmos DB Cassandra contact point"
  value       = "${azurerm_cosmosdb_account.main.name}.cassandra.cosmos.azure.com:10350"
}

output "cosmos_keyspace_name" {
  description = "Cosmos DB Cassandra keyspace name"
  value       = azurerm_cosmosdb_cassandra_keyspace.main.name
}

output "cosmos_table_names" {
  description = "Cosmos DB Cassandra table names"
  value       = [for table in azurerm_cosmosdb_cassandra_table.tables : table.name]
}

# Event Hub
output "eventhub_namespace_name" {
  description = "Event Hub namespace name"
  value       = azurerm_eventhub_namespace.main.name
}

output "eventhub_namespace_fqdn" {
  description = "Event Hub namespace FQDN (Kafka bootstrap servers)"
  value       = "${azurerm_eventhub_namespace.main.name}.servicebus.windows.net:9093"
}

# output "eventhub_topics" {
#   description = "Event Hub topic names"
#   value       = [for hub in azurerm_eventhub.topics : hub.name]
# }

# Key Vault
output "key_vault_name" {
  description = "Key Vault name"
  value       = azurerm_key_vault.main.name
}

output "key_vault_uri" {
  description = "Key Vault URI"
  value       = azurerm_key_vault.main.vault_uri
}

# Container Apps
output "container_app_urls" {
  description = "Container App URLs"
  value = {
    for name, app in azurerm_container_app.apps :
    name => app.ingress != null && length(app.ingress) > 0 ? "https://${app.ingress[0].fqdn}" : "No ingress configured"
  }
}

# Container Apps System-Assigned Managed Identities
output "container_app_principal_ids" {
  description = "System-Assigned Managed Identity Principal IDs for each Container App"
  value = {
    for name, app in azurerm_container_app.apps :
    name => app.identity[0].principal_id
  }
}

output "container_app_tenant_ids" {
  description = "System-Assigned Managed Identity Tenant IDs for each Container App"
  value = {
    for name, app in azurerm_container_app.apps :
    name => app.identity[0].tenant_id
  }
}

# Application Insights
output "application_insights_instrumentation_key" {
  description = "Application Insights instrumentation key"
  value       = azurerm_application_insights.main.instrumentation_key
  sensitive   = true
}

output "application_insights_connection_string" {
  description = "Application Insights connection string"
  value       = azurerm_application_insights.main.connection_string
  sensitive   = true
}

# Log Analytics
output "log_analytics_workspace_id" {
  description = "Log Analytics Workspace ID"
  value       = azurerm_log_analytics_workspace.main.id
}
