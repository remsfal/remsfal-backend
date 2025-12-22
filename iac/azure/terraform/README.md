# REMSFAL Azure Infrastructure - Terraform

This Terraform project provisions the complete Azure infrastructure for the REMSFAL application based on a microservices architecture.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Resource Naming Convention](#resource-naming-convention)
- [Setup Instructions](#setup-instructions)
- [Environment Management](#environment-management)
- [Deployed Resources](#deployed-resources)
- [Post-Deployment Steps](#post-deployment-steps)
- [Maintenance](#maintenance)
- [Troubleshooting](#troubleshooting)

## 🏗️ Architecture Overview

The infrastructure deploys a cloud-native microservices architecture with the following components:

### Compute & Container Services
- **Azure Container Apps**: Hosting for 4 microservices (Frontend, Platform, Ticketing, OCR)
- **Azure Container Registry (ACR)**: Private Docker image registry

### Data Storage Services
- **Azure Blob Storage**: Object storage for documents and attachments (MinIO replacement)
- **Azure Database for PostgreSQL Flexible Server**: Relational database for Platform service
- **Azure Cosmos DB (Cassandra API)**: NoSQL database for Ticketing service (Cassandra replacement)

### Event Streaming & Messaging
- **Azure Event Hubs**: Kafka-compatible event streaming (Kafka replacement)
  - Topics: `user-notification`, `ocr-documents-inbox`, `ocr-documents-outbox`

### Security & Identity
- **Azure Key Vault**: Secret and credential management
- **Azure Managed Identity**: Service-to-service authentication without credentials

⚠️ **Important Note on Managed Identity Support:**
- **Cosmos DB Cassandra API**: Does NOT support Managed Identity authentication. Requires username/password (primary key) stored in Key Vault.
- **PostgreSQL Flexible Server**: Supports Azure AD authentication with Managed Identity, but requires manual SQL configuration after deployment. Password authentication is used by default.

### Monitoring & Observability
- **Azure Monitor & Application Insights**: Centralized logging and APM
- **Azure Log Analytics**: Log aggregation and analysis

## ✅ Prerequisites

### Required Tools
1. **Azure CLI** (version 2.50+)
   ```bash
   # Install on macOS
   brew install azure-cli
   
   # Verify installation
   az --version
   ```

2. **Terraform** (version 1.5+)
   ```bash
   # Install on macOS
   brew install terraform
   
   # Verify installation
   terraform --version
   ```

3. **Git** (for version control)
   ```bash
   brew install git
   ```

4. **(Optional) cqlsh** - For Cosmos DB Cassandra index creation
   ```bash
   pip install cassandra-driver
   ```

### Azure Setup

1. **Azure Subscription**
   - Subscription ID: `012e925b-f538-41ef-8d23-b0c85e7dbe7b`
   - Ensure you have **Contributor** or **Owner** role

2. **Azure CLI Login**
   ```bash
   # Login to Azure
   az login
   
   # Set the correct subscription
   az account set --subscription "012e925b-f538-41ef-8d23-b0c85e7dbe7b"
   
   # Verify
   az account show
   ```

3. **Terraform Backend Storage** (One-time setup)
   
   The backend storage account must exist before running Terraform:
   
   ```bash
   # Create resource group for Terraform state
   az group create \
     --name remsfal-iac-rg \
     --location germanywestcentral
   
   # Create storage account
   az storage account create \
     --name engobaremsfalsa \
     --resource-group remsfal-iac-rg \
     --location germanywestcentral \
     --sku Standard_LRS \
     --encryption-services blob
   
   # Create container for Terraform state
   az storage container create \
     --name tfstate \
     --account-name engobaremsfalsa
   ```

## 📁 Project Structure

```
iac/azure/terraform/
├── providers.tf           # Provider and backend configuration
├── main.tf               # Main resource definitions
├── variables.tf          # Variable declarations
├── locals.tf             # Local values and naming logic
├── outputs.tf            # Output values
├── .gitignore            # Git ignore rules
├── env/                  # Environment-specific configurations
│   ├── dev.tfvars       # Development environment
│   ├── tst.tfvars       # Test environment
│   └── prd.tfvars       # Production environment
├── scripts/              # Post-deployment scripts
│   ├── setup-cosmos-indexes.sh    # Bash script for Cosmos DB indexes
│   └── setup-cosmos-indexes.ps1   # PowerShell script for Windows
└── README.md             # This file
```

## 🏷️ Resource Naming Convention

All resources follow the naming pattern:

```
{project_name_short}-{location_short}-{env}-{resource_short}
```

**Example for Dev Environment:**
- Resource Group: `rmsfl-gwc-dev-rg`
- PostgreSQL Server: `rmsfl-gwc-dev-psql`
- Cosmos DB Account: `rmsfl-gwc-dev-cosmos`
- Key Vault: `rmsfl-gwc-dev-kv`

**Abbreviations:**
- `rmsfl` = REMSFAL (project_name_short)
- `gwc` = Germany West Central (location_short)
- `dev/tst/prd` = Environment

## 🚀 Setup Instructions

### Initial Setup

1. **Clone the repository**
   ```bash
   cd /Users/enricogoerlitz/Developer/repos/remsfal-backend/iac/azure/terraform
   ```

2. **Initialize Terraform**
   ```bash
   terraform init
   ```
   
   This will:
   - Download required providers (azurerm, azapi)
   - Initialize the remote backend (Azure Storage)
   - Create `.terraform` directory

3. **Select Environment**
   
   Choose which environment to deploy (dev/tst/prd):
   ```bash
   # For development
   export TF_VAR_FILE="env/dev.tfvars"
   
   # For test
   export TF_VAR_FILE="env/tst.tfvars"
   
   # For production
   export TF_VAR_FILE="env/prd.tfvars"
   ```

### Deploy Infrastructure

1. **Review the Terraform Plan**
   ```bash
   terraform plan -var-file=$TF_VAR_FILE
   ```
   
   Review the output to understand what resources will be created.

2. **Apply the Configuration**
   ```bash
   terraform apply -var-file=$TF_VAR_FILE
   ```
   
   Type `yes` when prompted to confirm.
   
   ⏱️ **Expected Duration:** 15-25 minutes

3. **Save Outputs**
   ```bash
   terraform output > outputs.txt
   ```

### Post-Deployment Configuration

After Terraform completes, run the Cosmos DB index setup script:

```bash
# macOS/Linux
cd scripts
./setup-cosmos-indexes.sh

# Windows (PowerShell)
.\scripts\setup-cosmos-indexes.ps1
```

This script will:
- Retrieve Cosmos DB connection details from Terraform outputs
- Get credentials from Azure Key Vault
- Create required indexes on Cassandra tables

## 🌍 Environment Management

### Development (dev)
```bash
terraform plan -var-file=env/dev.tfvars
terraform apply -var-file=env/dev.tfvars
```

**Characteristics:**
- Lowest cost configuration (Burstable tiers)
- Scale-to-zero enabled for some services
- Public firewall rules enabled
- Minimal redundancy

### Test (tst)
```bash
terraform plan -var-file=env/tst.tfvars
terraform apply -var-file=env/tst.tfvars
```

**Characteristics:**
- Medium-tier configuration
- Higher resource limits than dev
- Suitable for load testing
- No scale-to-zero

### Production (prd)
```bash
terraform plan -var-file=env/prd.tfvars
terraform apply -var-file=env/prd.tfvars
```

**Characteristics:**
- Production-grade tiers (General Purpose)
- High availability configuration
- Increased throughput and capacity
- Multiple replicas

⚠️ **Important:** Change the PostgreSQL password in `env/prd.tfvars` before deploying to production!

## 📦 Deployed Resources

### By Resource Type

| Resource Type | Count | Purpose |
|--------------|-------|---------|
| Resource Group | 1 | Container for all resources |
| Container Registry | 1 | Docker image storage |
| Container Apps | 4 | Microservices hosting |
| Container App Environment | 1 | Shared runtime environment |
| Storage Account | 1 | Blob storage for documents |
| PostgreSQL Flexible Server | 1 | Platform service database |
| Cosmos DB Account | 1 | Ticketing service database |
| Cosmos DB Cassandra Keyspace | 1 | Logical database |
| Cosmos DB Cassandra Tables | 3 | `issues`, `chat_sessions`, `chat_messages` |
| Event Hub Namespace | 1 | Kafka-compatible messaging |
| Event Hubs | 3 | Individual topics/queues |
| Key Vault | 1 | Secret management |
| Managed Identity | 1 | Service authentication |
| Log Analytics Workspace | 1 | Log aggregation |
| Application Insights | 1 | Application monitoring |

### Container Apps

| Service | Port | External | Min Replicas | Max Replicas |
|---------|------|----------|--------------|--------------|
| Frontend | 80 | Yes | 0-2 (dev) | 10 (prd) |
| Platform | 8080 | Yes | 1 | 10 (prd) |
| Ticketing | 8081 | Yes | 1 | 10 (prd) |
| OCR | 8082 | No (internal) | 0-1 | 5 (prd) |

### Event Hub Topics

- `user-notification`: User notification events
- `ocr-documents-inbox`: Documents to be processed by OCR
- `ocr-documents-outbox`: OCR processing results

## 🔧 Post-Deployment Steps

### 1. Push Container Images to ACR

```bash
# Get ACR credentials
ACR_NAME=$(terraform output -raw container_registry_login_server)
az acr login --name $(terraform output -raw container_registry_login_server | cut -d'.' -f1)

# Build and push images (example)
docker build -t $ACR_NAME/remsfal/platform:latest ./remsfal-services/remsfal-platform
docker push $ACR_NAME/remsfal/platform:latest

# Repeat for ticketing, frontend, and ocr services
```

### 2. Configure Application Settings

Update Container Apps with environment variables:

```bash
# Get Key Vault name
KV_NAME=$(terraform output -raw key_vault_name)

# Container Apps will automatically use Managed Identity to access:
# - Key Vault secrets
# - Blob Storage
# - PostgreSQL (with Azure AD authentication)
# - Cosmos DB
```

### 3. Configure Connection Strings in Applications

**PostgreSQL Connection (Platform Service):**
```properties
quarkus.datasource.jdbc.url=jdbc:postgresql://{postgres_fqdn}:5432/remsfal_platform
quarkus.datasource.username=psqladmin
quarkus.datasource.password=${POSTGRES_PASSWORD} # From Key Vault
```

**Cosmos DB Cassandra (Ticketing Service):**
```properties
quarkus.cassandra.contact-points=${COSMOS_CONTACT_POINT}
quarkus.cassandra.local-datacenter=Germany West Central
quarkus.cassandra.keyspace=remsfal_ticketing
quarkus.cassandra.auth.username=${COSMOS_USERNAME}
quarkus.cassandra.auth.password=${COSMOS_PASSWORD}
```

**Event Hub / Kafka (All Services):**
```properties
kafka.bootstrap.servers=${EVENTHUB_NAMESPACE}.servicebus.windows.net:9093
kafka.security.protocol=SASL_SSL
kafka.sasl.mechanism=PLAIN
kafka.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$ConnectionString" password="${EVENTHUB_CONNECTION_STRING}";
```

### 4. Set up Monitoring Alerts

```bash
# Example: Create alert for high error rate
az monitor metrics alert create \
  --name high-error-rate \
  --resource-group $(terraform output -raw resource_group_name) \
  --scopes $(terraform output -raw application_insights_id) \
  --condition "avg requests/failed > 10" \
  --description "Alert when error rate exceeds 10 per minute"
```

## 🛠️ Maintenance

### Update Infrastructure

```bash
# Pull latest changes
git pull

# Plan changes
terraform plan -var-file=env/dev.tfvars

# Apply updates
terraform apply -var-file=env/dev.tfvars
```

### Scale Container Apps

Modify `container_apps` in the respective `env/*.tfvars` file:

```hcl
container_apps = {
  platform = {
    # ... other settings ...
    min_replicas = 3  # Increase minimum replicas
    max_replicas = 15 # Increase maximum replicas
  }
}
```

Then apply:
```bash
terraform apply -var-file=env/prd.tfvars
```

### View Logs

```bash
# Log Analytics query
az monitor log-analytics query \
  --workspace $(terraform output -raw log_analytics_workspace_id) \
  --analytics-query "ContainerAppConsoleLogs_CL | where ContainerAppName_s == 'platform' | limit 100"

# Application Insights
az monitor app-insights query \
  --app $(terraform output -raw application_insights_name) \
  --resource-group $(terraform output -raw resource_group_name) \
  --analytics-query "requests | where timestamp > ago(1h) | summarize count() by resultCode"
```

### Backup and Disaster Recovery

**PostgreSQL:**
- Automated daily backups with 7-day retention
- Point-in-time restore available

**Cosmos DB:**
- Continuous backup (automatic)
- Point-in-time restore within last 30 days

**Blob Storage:**
- Enable soft delete:
  ```bash
  az storage blob service-properties delete-policy update \
    --account-name $(terraform output -raw storage_account_name) \
    --enable true \
    --days-retained 30
  ```

## 🔍 Troubleshooting

### Common Issues

**1. Backend Initialization Failed**
```
Error: Failed to get existing workspaces
```
**Solution:** Ensure the backend storage account exists and you have access:
```bash
az storage account show --name engobaremsfalsa --resource-group remsfal-iac-rg
```

**2. Insufficient Permissions**
```
Error: authorization failed
```
**Solution:** Verify your Azure role assignment:
```bash
az role assignment list --assignee $(az account show --query user.name -o tsv)
```

**3. Resource Name Conflicts**
```
Error: A resource with the ID already exists
```
**Solution:** Resource names must be globally unique (ACR, Storage Account, Cosmos DB). Modify `project_name_short` in `variables.tf`.

**4. Cosmos DB Connection Issues**
```
Error: SSL handshake failed
```
**Solution:** Ensure you're using port 10350 and SSL is enabled in your connection string.

**5. Container Apps Not Starting**
```
Error: Failed to pull image
```
**Solution:** 
- Verify ACR credentials are configured
- Check Managed Identity has `AcrPull` role
- Ensure image exists in ACR

### Get Help

```bash
# View Terraform state
terraform show

# List all resources in resource group
az resource list --resource-group $(terraform output -raw resource_group_name) -o table

# Check Container App logs
az containerapp logs show \
  --name <container-app-name> \
  --resource-group $(terraform output -raw resource_group_name) \
  --follow
```

### Destroy Infrastructure

⚠️ **Warning:** This will permanently delete all resources!

```bash
# Destroy specific environment
terraform destroy -var-file=env/dev.tfvars

# Confirm with 'yes' when prompted
```

## 📊 Cost Estimation

### Development (dev)
- **Estimated Monthly Cost:** €50-80
  - Container Apps (Burstable): €20
  - PostgreSQL (B1ms): €15
  - Cosmos DB (400 RU/s): €20
  - Storage, Event Hub, etc.: €10-15

### Production (prd)
- **Estimated Monthly Cost:** €300-500
  - Container Apps (Production tier): €150
  - PostgreSQL (GP D2s_v3): €100
  - Cosmos DB (1000 RU/s): €50
  - Event Hub, Storage, Monitoring: €50-100

💡 **Cost Optimization Tips:**
- Use scale-to-zero for dev/test environments
- Enable auto-pause for PostgreSQL in dev
- Use Cosmos DB autoscale
- Set up budget alerts in Azure

## 📚 Additional Resources

- [Azure Container Apps Documentation](https://learn.microsoft.com/azure/container-apps/)
- [Azure Cosmos DB Cassandra API](https://learn.microsoft.com/azure/cosmos-db/cassandra/)
- [Azure Event Hubs as Kafka](https://learn.microsoft.com/azure/event-hubs/event-hubs-for-kafka-ecosystem-overview)
- [Terraform Azure Provider](https://registry.terraform.io/providers/hashicorp/azurerm/latest/docs)

## 🏷️ Tags

All resources are tagged with:
- `project_name`: remsfal
- `environment`: dev/tst/prd
- `maintained_by`: Team name
- `managed_by`: terraform

---

**Maintained by:** Platform Team  
**Last Updated:** December 2025  
**Terraform Version:** >= 1.5  
**Azure Provider Version:** ~> 3.0
