# Terraform Commands - REMSFAL Azure Infrastructure

## Initial Setup
```sh
# Login to Azure
az login

# Set subscription
az account set --subscription <subscription-id>

# Initialize Terraform
terraform init
```

## Workspace Management
```sh
# Create/Select workspace (dev, tst, prd)
terraform workspace select -or-create=true dev
terraform workspace select -or-create=true tst
terraform workspace select -or-create=true prd

# List workspaces
terraform workspace list

# Show current workspace
terraform workspace show
```

## Deployment
```sh
# Format code
terraform fmt -recursive

# Validate configuration
terraform validate

# Plan deployment
terraform plan -var-file="env/$(terraform workspace show).tfvars"

# Apply changes
terraform apply -var-file="env/$(terraform workspace show).tfvars"

# Apply with auto-approve
terraform apply -var-file="env/$(terraform workspace show).tfvars" --auto-approve

# Destroy infrastructure
terraform destroy -var-file="env/$(terraform workspace show).tfvars"
```

## Quick Deploy (dev)
```sh
terraform workspace select -or-create=true dev
terraform plan -var-file=env/dev.tfvars
terraform apply -var-file=env/dev.tfvars
```

## Troubleshooting
```sh
# Show state
terraform state list

# Show specific resource
terraform state show azurerm_container_app.apps[\"frontend\"]

# Refresh state
terraform refresh -var-file="env/$(terraform workspace show).tfvars"

# View outputs
terraform output

# Enable detailed logging
export TF_LOG=DEBUG
terraform plan -var-file=env/dev.tfvars
```
