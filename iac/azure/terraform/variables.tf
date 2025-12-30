variable "subscription_id" {
  type        = string
  description = "Azure Subscription ID"
  default     = "012e925b-f538-41ef-8d23-b0c85e7dbe7b"
}

variable "location" {
  type        = string
  description = "Azure Region"
  default     = "westeurope"
}

variable "location_short" {
  type        = string
  description = "Short name for location"
  default     = "weu"
}

variable "project_name" {
  type        = string
  description = "Project name"
  default     = "remsfal"
}

variable "project_name_short" {
  type        = string
  description = "Short project name for resource naming"
  default     = "rmsfl"
}

variable "environment" {
  type        = string
  description = "Environment name (dev, tst, prd)"
  validation {
    condition     = contains(["dev", "tst", "prd"], var.environment)
    error_message = "Environment must be dev, tst, or prd."
  }
}

variable "maintained_by" {
  type        = string
  description = "Team or person maintaining the infrastructure"
  default     = "Enrico Goerlitz"
}

# PostgreSQL Configuration
variable "postgres_admin_username" {
  type        = string
  description = "PostgreSQL admin username"
  default     = "psqladmin"
}

variable "postgres_admin_password" {
  type        = string
  description = "PostgreSQL admin password"
  sensitive   = true
}

variable "postgres_sku" {
  type        = string
  description = "PostgreSQL SKU"
  default     = "B_Standard_B1ms" # Burstable tier for lowest cost
}

variable "postgres_storage_mb" {
  type        = number
  description = "PostgreSQL storage in MB"
  default     = 32768 # 32 GB minimum
}

# Cosmos DB Configuration
variable "cosmos_throughput" {
  type        = number
  description = "Cosmos DB throughput (RU/s)"
  default     = 400 # Minimum for manual throughput
}

# Event Hub Configuration
variable "eventhub_capacity" {
  type        = number
  description = "Event Hub namespace capacity (Throughput Units)"
  default     = 1
}

# Container Apps Configuration
variable "container_apps" {
  type = map(object({
    image            = string
    cpu              = number
    memory           = string
    min_replicas     = number
    max_replicas     = number
    ingress_enabled  = bool
    target_port      = number
    external_enabled = bool
    # KEDA Event Hub scaling for Kafka consumers (optional)
    eventhub_scaling = optional(object({
      enabled             = bool
      consumer_group      = string
      event_hub_name      = string
      message_lag_threshold = optional(number, 100) # Messages before scaling up
    }))
  }))
  description = "Container Apps configuration"
  default = {
    frontend = {
      image            = "remsfal-frontend:latest"
      cpu              = 0.25
      memory           = "0.5Gi"
      min_replicas     = 0
      max_replicas     = 3
      ingress_enabled  = true
      target_port      = 80
      external_enabled = true
    }
    platform = {
      image            = "remsfal-platform:latest"
      cpu              = 0.5
      memory           = "1Gi"
      min_replicas     = 1
      max_replicas     = 5
      ingress_enabled  = true
      target_port      = 8080
      external_enabled = true
    }
    ticketing = {
      image            = "remsfal-ticketing:latest"
      cpu              = 0.5
      memory           = "1Gi"
      min_replicas     = 1
      max_replicas     = 5
      ingress_enabled  = true
      target_port      = 8081
      external_enabled = true
    }
    notification = {
      image            = "remsfal-notification:latest"
      cpu              = 0.25
      memory           = "0.5Gi"
      min_replicas     = 0
      max_replicas     = 3
      ingress_enabled  = true
      target_port      = 8082
      external_enabled = false
    }
    ocr = {
      image            = "remsfal-ocr:latest"
      cpu              = 0.5
      memory           = "1Gi"
      min_replicas     = 0
      max_replicas     = 3
      ingress_enabled  = false
      target_port      = 8000
      external_enabled = false
      # KEDA scaling based on Event Hub message lag
      eventhub_scaling = {
        enabled             = true
        consumer_group      = "$Default"
        event_hub_name      = "ocr.documents.to_process"
        message_lag_threshold = 10
      }
    }
  }
}

# Tags
variable "tags" {
  type        = map(string)
  description = "Common resource tags"
  default     = {}
}
