locals {
  # Naming convention: {project_name_short}-{location_short}-{env}-{resource_short}
  base_name = "${var.project_name_short}-${var.environment}-${var.location_short}"

  # Common tags
  common_tags = merge(
    {
      project_name  = var.project_name
      environment   = var.environment
      maintained_by = var.maintained_by
      managed_by    = "terraform"
    },
    var.tags
  )

  # Resource names
  resource_group_name             = "${local.base_name}-rg"
  container_registry_name         = replace("${local.base_name}-acr", "-", "") # ACR names can't contain hyphens
  storage_account_name            = replace("${local.base_name}sa", "-", "")   # SA names can't contain hyphens
  postgres_server_name            = "${local.base_name}-psql"
  cosmos_account_name             = "${local.base_name}-cosmos"
  eventhub_namespace_name         = "${local.base_name}-ehns"
  key_vault_name                  = "${local.base_name}-${random_string.kv_suffix.result}-kv"
  container_apps_environment_name = "${local.base_name}-cae"
  log_analytics_workspace_name    = "${local.base_name}-law"
  application_insights_name       = "${local.base_name}-ai"

  # Event Hub topic names (matching Kafka topics from REMSFAL)
  eventhub_topics = [
    "user-notification",
    "ocr.documents.to_process",
    "ocr.documents.processed"
  ]

  # Storage container names
  storage_containers = [
    "remsfal-ticketing",
    "documents",
    "test-bucket"
  ]

  # Cosmos DB Cassandra configuration
  cosmos_keyspace_name = "REMSFAL"
  cosmos_tables = {
    issues = {
      schema = {
        columns = [
          { name = "project_id", type = "uuid" },
          { name = "issue_id", type = "uuid" },
          { name = "type", type = "text" },
          { name = "title", type = "text" },
          { name = "status", type = "text" },
          { name = "reporter_id", type = "uuid" },
          { name = "tenancy_id", type = "uuid" },
          { name = "owner_id", type = "uuid" },
          { name = "description", type = "text" },
          { name = "blocked_by", type = "uuid" },
          { name = "related_to", type = "uuid" },
          { name = "duplicate_of", type = "uuid" },
          { name = "created_by", type = "uuid" },
          { name = "created_at", type = "timestamp" },
          { name = "modified_at", type = "timestamp" }
        ]
        partition_keys = [
          { name = "project_id" }
        ]
        cluster_keys = [
          { name = "issue_id", order_by = "Asc" }
        ]
      }
    }
    chat_sessions = {
      schema = {
        columns = [
          { name = "project_id", type = "uuid" },
          { name = "issue_id", type = "uuid" },
          { name = "session_id", type = "uuid" },
          { name = "participants", type = "text" },
          { name = "created_at", type = "timestamp" },
          { name = "modified_at", type = "timestamp" }
        ]
        partition_keys = [
          { name = "project_id" },
          { name = "issue_id" }
        ]
        cluster_keys = [
          { name = "session_id", order_by = "Asc" }
        ]
      }
    }
    chat_messages = {
      schema = {
        columns = [
          { name = "session_id", type = "uuid" },
          { name = "message_id", type = "uuid" },
          { name = "sender_id", type = "uuid" },
          { name = "content_type", type = "text" },
          { name = "content", type = "text" },
          { name = "url", type = "text" },
          { name = "created_at", type = "timestamp" },
          { name = "modified_at", type = "timestamp" }
        ]
        partition_keys = [
          { name = "session_id" }
        ]
        cluster_keys = [
          { name = "message_id", order_by = "Asc" }
        ]
      }
    }
  }
}
