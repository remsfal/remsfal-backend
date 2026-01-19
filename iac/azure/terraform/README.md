# Infrastructure as Code - Azure Terraform

Diese Dokumentation beschreibt die Terraform-basierte Infrastruktur für die REMSFAL-Anwendung auf Microsoft Azure.

**Repository:** [GitHub - IaC Terraform](https://github.com/enricogoerlitz/remsfal-backend/tree/Enrico-Goerlitz%23644/iac/azure/terraform)

---

## Inhaltsverzeichnis

1. [Projektstruktur](#projektstruktur)
2. [Verwendete Versionen](#verwendete-versionen)
3. [Naming Convention](#naming-convention)
4. [Azure-Ressourcen im Überblick](#azure-ressourcen-im-überblick)
5. [Detaillierte Ressourcen-Konfiguration](#detaillierte-ressourcen-konfiguration)
6. [Umgebungskonfiguration](#umgebungskonfiguration)
7. [Secret Management](#secret-management)
8. [Managed Identity & RBAC](#managed-identity--rbac)

---

## Projektstruktur

```
iac/azure/terraform/
├── main.tf              # Hauptkonfiguration aller Azure-Ressourcen
├── variables.tf         # Variablendefinitionen mit Validierung
├── locals.tf            # Lokale Werte und Naming-Logik
├── outputs.tf           # Output-Werte nach dem Deployment
├── providers.tf         # Provider-Konfiguration und Backend
├── README.md            # Terraform-spezifische Dokumentation
├── env/                 # Umgebungsspezifische Konfigurationen
│   ├── dev.tfvars       # Development-Umgebung
│   ├── tst.tfvars       # Test-Umgebung
│   └── prd.tfvars       # Production-Umgebung
└── scripts/             # Hilfs-Skripte
```

### Dateibeschreibungen

| Datei | Beschreibung |
|-------|--------------|
| `main.tf` | Definiert alle Azure-Ressourcen: Container Apps, Datenbanken, Event Hubs, Key Vault, Storage, Monitoring |
| `variables.tf` | Deklariert alle Eingabevariablen mit Typen, Beschreibungen und Standardwerten |
| `locals.tf` | Berechnet abgeleitete Werte wie Ressourcennamen, Tags und Cosmos DB Tabellenschemata |
| `outputs.tf` | Exportiert wichtige Werte nach dem Deployment (URLs, Connection Strings, Principal IDs) |
| `providers.tf` | Konfiguriert Azure Provider und Remote Backend für State-Management |

---

## Verwendete Versionen

### Terraform

| Komponente | Version | Beschreibung |
|------------|---------|--------------|
| **Terraform** | >= 1.5 | Infrastructure as Code Tool |
| **azurerm Provider** | ~> 4.57.0 | Azure Resource Manager Provider |
| **azapi Provider** | ~> 2.8.0 | Azure API Direct Access Provider |
| **random Provider** | ~> 3.6 | Zufallsgenerierung für eindeutige Namen |

### Backend-Konfiguration

Der Terraform State wird remote in Azure Blob Storage gespeichert:

```hcl
backend "azurerm" {
  resource_group_name  = "remsfal-iac-rg"
  storage_account_name = "engobaremsfalsa"
  container_name       = "tfstate"
  key                  = "terraform.tfstate"
}
```

**Begründung:** Remote State ermöglicht Team-Kollaboration und verhindert State-Konflikte. Azure Blob Storage bietet integrierte Versionierung und Locking.

---

## Naming Convention

Alle Ressourcen folgen einem einheitlichen Namensschema:

```
{project_name_short}-{environment}-{location_short}-{resource_short}
```

**Beispiel:** `rmsfl-dev-weu-rg` (Resource Group für Development in West Europe)

### Namenszusammensetzung

| Komponente | Wert | Beschreibung |
|------------|------|--------------|
| `project_name_short` | `rmsfl` | Kurzform von "remsfal" |
| `environment` | `dev`, `tst`, `prd` | Umgebungsbezeichnung |
| `location_short` | `weu` | West Europe |
| `resource_short` | `rg`, `acr`, `kv`, etc. | Ressourcentyp-Kürzel |

### Sonderregeln

- **Azure Container Registry (ACR):** Keine Bindestriche erlaubt → `rmsfldevweuacr`
- **Storage Account:** Keine Bindestriche, max. 24 Zeichen → `rmsfldevweusa`
- **Key Vault:** Eindeutiger Name erforderlich → Suffix mit `random_string`

---

## Azure-Ressourcen im Überblick

### Architekturdiagramm

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Azure Resource Group                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    Container Apps Environment                       │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │    │
│  │  │ Frontend │ │ Platform │ │Ticketing │ │  OCR     │ │Notific.  │   │    │
│  │  │ (Vue.js) │ │ (Quarkus)│ │(Quarkus) │ │ (Python) │ │(Quarkus) │   │    │
│  │  │ Port 80  │ │ Port 8080│ │Port 8081 │ │Port 8000 │ │Port 8082 │   │    │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘   │    │
│  └───────┼────────────┼────────────┼────────────┼────────────┼─────────┘    │
│          │            │            │            │            │              │
│  ┌───────▼────────────▼────────────▼────────────▼────────────▼─────────┐    │
│  │                         Azure Key Vault                             │    │
│  │         (Secrets: DB Credentials, Event Hub, Storage)               │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌───────────────────────────┐    │
│  │   PostgreSQL    │  │    Cosmos DB    │  │     Azure Event Hubs      │    │
│  │ Flexible Server │  │  (Cassandra API)│  │    (Kafka-kompatibel)     │    │
│  │   (Platform)    │  │   (Ticketing)   │  │ Topics: user-notif.,      │    │
│  │                 │  │                 │  │ ocr.documents.*           │    │
│  └─────────────────┘  └─────────────────┘  └───────────────────────────┘    │
│                                                                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌───────────────────────────┐    │
│  │  Blob Storage   │  │ Container       │  │   Application Insights    │    │
│  │  (Documents,    │  │ Registry (ACR)  │  │   + Log Analytics         │    │
│  │   Attachments)  │  │                 │  │                           │    │
│  └─────────────────┘  └─────────────────┘  └───────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Ressourcenübersicht

| Ressource | Azure Service | Zweck |
|-----------|---------------|-------|
| **Compute** | Container Apps | Hosting der 5 Microservices |
| **Container Images** | Container Registry (ACR) | Private Docker Registry |
| **Relationale DB** | PostgreSQL Flexible Server | Platform-Service Datenbank |
| **NoSQL DB** | Cosmos DB (Cassandra API) | Ticketing-Service Datenbank |
| **Messaging** | Event Hubs | Kafka-kompatibler Message Broker |
| **Object Storage** | Blob Storage | Dokumente und Attachments |
| **Secrets** | Key Vault | Credentials und Connection Strings |
| **Monitoring** | Application Insights | APM und Distributed Tracing |
| **Logs** | Log Analytics Workspace | Zentralisierte Log-Aggregation |

---

## Detaillierte Ressourcen-Konfiguration

### 1. Container Apps Environment

**Ressource:** `azurerm_container_app_environment`

Das Container Apps Environment ist die serverlose Hosting-Plattform für alle Microservices.

```hcl
resource "azurerm_container_app_environment" "main" {
  name                       = local.container_apps_environment_name
  resource_group_name        = azurerm_resource_group.main.name
  location                   = azurerm_resource_group.main.location
  log_analytics_workspace_id = azurerm_log_analytics_workspace.main.id
}
```

**Begründung:**
- **Serverless:** Automatische Skalierung basierend auf Last
- **Integriertes Logging:** Direkte Anbindung an Log Analytics
- **KEDA-Integration:** Event-driven Autoscaling für Kafka-Consumer

### 2. Container Apps (Microservices)

**Ressource:** `azurerm_container_app`

Jeder Microservice wird als eigene Container App deployt. Die Konfiguration verwendet eine **User-Assigned Managed Identity** für ACR-Zugriff und eine **System-Assigned Identity** für weitere Ressourcenzugriffe.

#### Ressourcen-Konfiguration (Development)

| Service | CPU | Memory | Min Replicas | Max Replicas | Ingress | Port |
|---------|-----|--------|--------------|--------------|---------|------|
| **Frontend** | 0.25 | 0.5 Gi | 0 | 2 | External | 80 |
| **Platform** | 0.25 | 0.5 Gi | 0 | 3 | External | 8080 |
| **Ticketing** | 0.25 | 0.5 Gi | 0 | 3 | External | 8081 |
| **Notification** | 0.25 | 0.5 Gi | 0 | 2 | Internal | 8082 |
| **OCR** | 0.5 | 1 Gi | 0 | 2 | Kein Ingress | 8000 |

**Konfigurationsentscheidungen:**

- **min_replicas=0 (Scale-to-Zero):** Alle Services in Dev/Test skalieren auf 0 bei Inaktivität. Dies spart erhebliche Kosten, führt aber zu **Cold-Start-Latenz** von ca. 10-30 Sekunden beim ersten Request.
- **Minimale Ressourcen:** CPU=0.25 und Memory=0.5Gi sind die Minimalwerte für Container Apps. Der OCR-Service benötigt mehr Ressourcen (0.5 CPU, 1Gi Memory) wegen der ML-Modelle.
- **OCR ohne HTTP-Ingress:** Der OCR-Service kommuniziert ausschließlich über Event Hubs (Kafka), daher kein HTTP-Ingress erforderlich.

> **⚠️ Production-Empfehlung:** Für kritische Services (Platform, Ticketing) sollte `min_replicas >= 1` gesetzt werden, um Cold-Starts zu vermeiden und die Verfügbarkeit zu gewährleisten.

#### KEDA Event Hub Scaling (OCR-Service)

```hcl
eventhub_scaling = {
  enabled               = true
  consumer_group        = "ocr-service"  # Dedizierte Consumer Group!
  event_hub_name        = "ocr.documents.to_process"
  message_lag_threshold = 10
}
```

**Begründung:** Der OCR-Service skaliert automatisch basierend auf der Anzahl unverarbeiteter Nachrichten in der Event Hub Queue. Bei 10 oder mehr wartenden Dokumenten wird eine zusätzliche Instanz gestartet.

**Wichtig zur Consumer Group:**
- **NICHT `$Default` verwenden!** Die `$Default` Consumer Group ist für allgemeine Zwecke reserviert.
- Dedizierte Consumer Group `ocr-service` gewährleistet korrekte Offset-Verfolgung und verhindert Konflikte mit anderen Konsumenten.
- Die Consumer Group wird automatisch via Terraform auf den Topics `ocr.documents.to_process` und `ocr.documents.processed` erstellt.

### 3. Azure Database for PostgreSQL Flexible Server

**Ressource:** `azurerm_postgresql_flexible_server`

```hcl
resource "azurerm_postgresql_flexible_server" "main" {
  name                   = local.postgres_server_name
  version                = "16"
  administrator_login    = var.postgres_admin_username
  administrator_password = var.postgres_admin_password
  storage_mb             = var.postgres_storage_mb  # 32 GB
  sku_name               = var.postgres_sku         # B_Standard_B1ms
  backup_retention_days  = 7
  zone                   = "1"
  
  authentication {
    active_directory_auth_enabled = true
    password_auth_enabled         = true
  }
}
```

**Konfigurationsentscheidungen:**

| Einstellung | Wert | Begründung |
|-------------|------|------------|
| **Version** | 16 | Neueste PostgreSQL LTS-Version |
| **SKU** | B_Standard_B1ms | Burstable Tier - kostengünstig für Dev/Test |
| **Storage** | 32 GB | Minimum für Flexible Server |
| **Backup Retention** | 7 Tage | Standard für automatische Backups |
| **Zone** | 1 | Keine Zone Redundancy für Kostenoptimierung |
| **AD Auth** | Aktiviert | Ermöglicht Managed Identity Authentifizierung |

#### Verfügbare PostgreSQL SKUs

| SKU | vCores | RAM | Tier | Empfohlen für |
|-----|--------|-----|------|---------------|
| **B_Standard_B1ms** | 1 | 2 GB | Burstable | Development, minimale Kosten |
| **B_Standard_B2s** | 2 | 4 GB | Burstable | Test, leichte Last |
| **B_Standard_B2ms** | 2 | 8 GB | Burstable | Test mit mehr Memory |
| **GP_Standard_D2s_v3** | 2 | 8 GB | General Purpose | Production (empfohlen) |
| **GP_Standard_D4s_v3** | 4 | 16 GB | General Purpose | Production, höhere Last |
| **GP_Standard_D8s_v3** | 8 | 32 GB | General Purpose | Production, hohe Last |

> **💡 Hinweis:** Burstable SKUs (B_*) sind für variable Workloads konzipiert und günstiger. General Purpose (GP_*) bieten konsistente Performance für Production.

**Firewall-Regeln:**
- `allow-azure-services`: Erlaubt Zugriff von Azure-Services (Container Apps)
- `allow-all` (nur Dev): Erlaubt externen Zugriff für Debugging

### 4. Azure Cosmos DB mit Cassandra API

**Ressource:** `azurerm_cosmosdb_account`

```hcl
resource "azurerm_cosmosdb_account" "main" {
  name       = local.cosmos_account_name
  offer_type = "Standard"
  kind       = "GlobalDocumentDB"

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
}
```

#### Warum Cosmos DB mit Cassandra API?

- **Migration ohne Code-Änderungen:** Der Ticketing-Service wurde ursprünglich für Apache Cassandra entwickelt. Die Cassandra API ermöglicht die Migration ohne Änderungen am Datenmodell oder CQL-Queries.
- **Managed Service:** Kein Betrieb von Cassandra-Clustern erforderlich, automatische Patches und Backups.
- **Globale Verteilung:** Multi-Region Replikation möglich (nicht in dieser Konfiguration aktiviert).

#### Throughput-Konfiguration (Request Units)

| Umgebung | Throughput | Beschreibung |
|----------|------------|--------------|
| **Dev** | 400 RU/s | Minimum für Cassandra API |
| **Test** | 400 RU/s | Ausreichend für Integrationstests |
| **Prod** | 1000 RU/s | Höhere Kapazität für Production |

> **💡 Request Units (RU/s):** Eine RU entspricht ungefähr einer Leseoperation für ein 1KB-Dokument. Komplexere Operationen (Writes, Queries) verbrauchen mehr RUs.

#### Manual vs. Autoscale Throughput

Die aktuelle Konfiguration verwendet **Manual (Provisioned) Throughput**. Alternativ bietet Cosmos DB **Autoscale**, das automatisch zwischen 10% und 100% des konfigurierten Maximums skaliert.

| Aspekt | Manual (Provisioned) | Autoscale |
|--------|---------------------|-----------|
| **Minimum** | 400 RU/s (Cassandra API) | 10% des Max-Werts (z.B. 100 bei max 1000) |
| **Preis pro RU/s** | 1× Basispreis | ~1,5× Basispreis (50% teurer) |
| **Abrechnung** | Immer volle RU/s | Nur genutzter Anteil |
| **Skalierungsbereich** | Fest | Automatisch 10%-100% |

**Wann lohnt sich Autoscale?**

Autoscale kann günstiger sein, obwohl der Preis pro RU/s höher ist:
- **Bei stark variierender Last:** Wenn nachts/am Wochenende kaum Nutzung erfolgt
- **Faustregel:** Autoscale ist günstiger, wenn durchschnittliche Nutzung **< 66%** der provisionierten RU/s liegt

**Beispielrechnung:**
- **Manual 400 RU/s:** Zahlt immer für 400 RU/s (100%)
- **Autoscale max 1000 RU/s:** Skaliert zwischen 100-1000 RU/s
  - Bei 10% Last (Nacht): 100 RU/s × 1,5 = Kosten wie 150 RU/s manual
  - Bei Peaks: Bis zu 1000 RU/s verfügbar

> **💡 Empfehlung für Dev/Test:** Bei sporadischer Nutzung (Entwicklung, gelegentliche Tests) kann Autoscale günstiger sein, da außerhalb der Arbeitszeiten nur minimale Kosten anfallen. Für Production mit konstanter Last ist Manual oft kosteneffizienter.

#### Consistency Level

**Session Consistency** wurde gewählt, da sie den besten Kompromiss bietet:
- **Monotonic Reads:** Ein Client sieht nie ältere Daten als zuvor gelesene.
- **Read Your Writes:** Eigene Schreiboperationen sind sofort lesbar.
- **Performance:** Geringere Latenz als Strong Consistency.

Andere verfügbare Level: `Eventual`, `ConsistentPrefix`, `BoundedStaleness`, `Strong`

#### Tabellenschema und Partition Keys

```hcl
# Definiert in locals.tf
cosmos_tables = {
  issues = {
    schema = {
      columns = [
        { name = "project_id", type = "uuid" },
        { name = "issue_id", type = "uuid" },
        { name = "title", type = "text" },
        # ... weitere Spalten
      ]
      partition_keys = [{ name = "project_id" }]
      cluster_keys   = [{ name = "issue_id", order_by = "asc" }]
    }
  }
  chat_sessions = {
    schema = {
      partition_keys = [{ name = "project_id" }, { name = "issue_id" }]
      cluster_keys   = [{ name = "session_id", order_by = "asc" }]
    }
  }
  chat_messages = {
    schema = {
      partition_keys = [{ name = "session_id" }]
      cluster_keys   = [{ name = "created_at", order_by = "desc" }]
    }
  }
}
```

**Begründung für Partition Key Design:**

| Tabelle | Partition Key | Begründung |
|---------|---------------|------------|
| **issues** | `project_id` | Alle Issues eines Projekts werden zusammen gespeichert. Ermöglicht effiziente Abfragen wie "Alle Issues für Projekt X". |
| **chat_sessions** | `project_id` + `issue_id` | Composite Key, da Chat-Sessions immer im Kontext eines Issues abgefragt werden. |
| **chat_messages** | `session_id` | Nachrichten werden immer pro Session geladen. `created_at` als Cluster Key ermöglicht chronologische Sortierung. |

> **⚠️ Wichtiger Hinweis:** Cosmos DB Cassandra API unterstützt **keine Managed Identity**. Die Authentifizierung erfolgt über Username/Password, die im Key Vault gespeichert werden. Dies erfordert SSL-Konfiguration im CassandraExecutor (siehe [REFACTORING.md](REFACTORING.md#cosmos-db-cassandra-api-ssl-konfiguration)).

### 5. Azure Event Hubs (Kafka-Ersatz)

**Ressource:** `azurerm_eventhub_namespace`

```hcl
resource "azurerm_eventhub_namespace" "main" {
  name     = local.eventhub_namespace_name
  sku      = "Standard"
  capacity = var.eventhub_capacity  # 1 TU
}
```

> **⚠️ WICHTIG:** Für **Kafka-Kompatibilität ist mindestens Standard SKU erforderlich**! Die REMSFAL-Services verwenden das Kafka-Protokoll (SASL_SSL, Port 9093), daher ist Basic SKU keine Option.

#### Throughput Units (TU)

| Umgebung | TUs | Kapazität |
|----------|-----|-----------|
| **Dev/Test** | 1 | 1 MB/s ingress, 2 MB/s egress |
| **Production** | 2 | 2 MB/s ingress, 4 MB/s egress |

**Konfigurierte Topics (Event Hubs):**

| Topic | Partitions | Retention | Zweck |
|-------|------------|-----------|-------|
| `user-notification` | 2 | 1 Tag | Benutzerbenachrichtigungen |
| `ocr.documents.to_process` | 2 | 1 Tag | Eingangswarteschlange für OCR |
| `ocr.documents.processed` | 2 | 1 Tag | Verarbeitete OCR-Ergebnisse |

**Begründung für Event Hubs statt Kafka:**
- **Vollständig Kafka-kompatibel:** SASL_SSL, Port 9093, Standard Kafka-Clients funktionieren
- **Managed Service:** Kein Betrieb von Kafka-Clustern/Zookeeper nötig
- **Automatische Skalierung:** Throughput Units je nach Bedarf
- **Native Integration:** KEDA-Scaler für Container Apps, Azure Functions Trigger

**Consumer Groups:**

```hcl
resource "azurerm_eventhub_consumer_group" "ocr_service" {
  for_each = toset([
    "ocr.documents.to_process",
    "ocr.documents.processed"
  ])
  name           = "ocr-service"
  eventhub_name  = each.value
  namespace_name = azurerm_eventhub_namespace.main.name
}
```

- `ocr-service`: Dedizierte Consumer Group für den OCR-Service
- `$Default`: Standard Consumer Group (für andere Services/Debugging)

### 6. Azure Blob Storage

**Ressource:** `azurerm_storage_account`

```hcl
resource "azurerm_storage_account" "main" {
  name                     = local.storage_account_name
  account_tier             = "Standard"
  account_replication_type = "LRS"
  account_kind             = "StorageV2"
}
```

**Konfigurierte Container:**

| Container | Zweck |
|-----------|-------|
| `remsfal-ticketing` | Dokumente für Ticketing-Service |
| `documents` | Allgemeine Dokumentenspeicherung |
| `test-bucket` | Test-Daten |
| `eventhub-checkpoints` | KEDA Checkpoint Storage |

**Begründung für LRS (Locally Redundant Storage):**
- Kostenoptimierung für Dev/Test-Umgebungen
- Für Production empfohlen: ZRS oder GRS

### 7. Azure Key Vault

**Ressource:** `azurerm_key_vault`

```hcl
resource "azurerm_key_vault" "main" {
  name                       = local.key_vault_name
  tenant_id                  = data.azurerm_client_config.current.tenant_id
  sku_name                   = "standard"
  soft_delete_retention_days = 7
  purge_protection_enabled   = false
  rbac_authorization_enabled = true
}
```

**Gespeicherte Secrets:**

| Secret | Beschreibung |
|--------|--------------|
| `postgres-connection-string` | JDBC Connection String für PostgreSQL |
| `storage-connection-string` | Azure Storage Connection String |
| `cosmos-contact-point` | Cassandra Contact Point (Host:Port) |
| `cosmos-username` | Cosmos DB Account Name |
| `cosmos-password` | Cosmos DB Primary Key |
| `eventhub-connection-string` | JAAS-Format für Kafka-Konfiguration |
| `eventhub-sasl-username` | `$ConnectionString` |
| `eventhub-sasl-password` | Event Hub Connection String |
| `eventhub-bootstrap-server` | Kafka Bootstrap Server URL |

**Begründung für RBAC:**
- RBAC statt Access Policies ermöglicht feinere Zugriffssteuerung
- Konsistent mit Azure-weitem Identity Management

### 8. Monitoring (Application Insights & Log Analytics)

**Ressourcen:** `azurerm_application_insights`, `azurerm_log_analytics_workspace`

```hcl
resource "azurerm_log_analytics_workspace" "main" {
  name              = local.log_analytics_workspace_name
  sku               = "PerGB2018"
  retention_in_days = 30
}

resource "azurerm_application_insights" "main" {
  name             = local.application_insights_name
  workspace_id     = azurerm_log_analytics_workspace.main.id
  application_type = "web"
}
```

**Begründung:**
- **Log Analytics Workspace:** Zentrale Log-Aggregation für alle Services
- **Application Insights:** APM mit Distributed Tracing, Dependency Mapping
- **30 Tage Retention:** Ausreichend für Debugging, kostenoptimiert

---

## Umgebungskonfiguration

Die Infrastruktur unterstützt drei Umgebungen, die sich in Ressourcenausstattung, Skalierung und Kosten unterscheiden:

| Umgebung | Datei | Beschreibung |
|----------|-------|--------------|
| **Development** | `env/dev.tfvars` | Minimale Ressourcen, Scale-to-Zero, günstigste Konfiguration |
| **Test** | `env/tst.tfvars` | Mittlere Ressourcen für Integrationstests und QA |
| **Production** | `env/prd.tfvars` | Hochverfügbarkeit, ausreichende Ressourcen für Produktionslast |

### Umgebungsvergleich

| Einstellung | Dev | Test | Production |
|-------------|-----|------|------------|
| **PostgreSQL SKU** | B_Standard_B1ms | B_Standard_B2s | GP_Standard_D2s_v3 |
| **PostgreSQL Storage** | 32 GB | 64 GB | 128 GB |
| **Cosmos Throughput** | 400 RU/s | 400 RU/s | 1000 RU/s |
| **Event Hub TUs** | 1 | 1 | 2 |
| **Container min_replicas** | 0 (alle) | 0 (alle) | 1-2 (kritische) |
| **Container max_replicas** | 2-3 | 3-5 | 5-10 |
| **Container CPU** | 0.25-0.5 | 0.25-0.5 | 0.5-1.0 |
| **Container Memory** | 0.5-1 Gi | 0.5-1 Gi | 1-2 Gi |
| **Image Tag** | `:latest` | `:latest` | `:stable` |
| **Storage Redundancy** | LRS | LRS | LRS (GRS empfohlen) |

### Development-Umgebung (dev.tfvars)

**Ziel:** Minimale Kosten bei Entwicklung und lokalem Testing.

```hcl
# Alle Services: Scale-to-Zero für Kostenoptimierung
# CPU: 0.25 (Minimum), Memory: 0.5Gi (Minimum)
# OCR: 0.5 CPU, 1Gi wegen ML-Modellen

container_apps = {
  platform = {
    cpu          = 0.25
    memory       = "0.5Gi"
    min_replicas = 0  # Scale-to-Zero → Cold-Start bei erstem Request
    max_replicas = 3
  }
  # ...
}
```

**Eigenschaften:**
- ✅ Günstigste Konfiguration
- ✅ Scale-to-Zero spart Kosten bei Inaktivität
- ⚠️ Cold-Start-Latenz: 10-30 Sekunden beim ersten Request
- ⚠️ Minimale Ressourcen können bei komplexen Operationen langsam sein

### Test-Umgebung (tst.tfvars)

**Ziel:** Realistische Umgebung für Integrationstests und QA.

```hcl
# Gleiche Ressourcen wie Dev, aber höhere max_replicas für Lasttests
# PostgreSQL: B_Standard_B2s für mehr Performance bei parallelen Tests

postgres_sku = "B_Standard_B2s"  # 2 vCores, 4GB RAM

container_apps = {
  platform = {
    min_replicas = 0
    max_replicas = 5  # Höher für Lasttests
  }
}
```

**Eigenschaften:**
- ✅ Größere PostgreSQL-Instanz für parallele Tests
- ✅ Höhere max_replicas für Skalierungstests
- ✅ Scale-to-Zero für Kostenoptimierung außerhalb der Testzeiten
- ⚠️ Cold-Start bei Tests nach längerer Inaktivität

### Production-Umgebung (prd.tfvars)

**Ziel:** Hochverfügbarkeit und konsistente Performance.

```hcl
# Kritische Services: min_replicas > 0 für Verfügbarkeit
# Image-Tag: :stable für kontrollierte Deployments

container_apps = {
  platform = {
    image        = "remsfal-platform:stable"
    cpu          = 1.0
    memory       = "2Gi"
    min_replicas = 2  # Hochverfügbar
    max_replicas = 10
  }
}

postgres_sku = "GP_Standard_D2s_v3"  # General Purpose für konsistente Performance
cosmos_throughput = 1000              # Höhere Kapazität
eventhub_capacity = 2                 # Mehr Durchsatz
```

**Eigenschaften:**
- ✅ Keine Cold-Starts für kritische Services (Platform, Ticketing)
- ✅ Ausreichende Ressourcen für Produktionslast
- ✅ General Purpose PostgreSQL für konsistente Performance
- ✅ `:stable` Image-Tags für kontrollierte Releases
- 💰 Höhere Kosten, aber notwendig für Produktionsbetrieb

> **💡 Empfehlung für Production:** Zusätzlich Zone Redundancy für PostgreSQL aktivieren und Storage Redundancy auf GRS (Geo-Redundant) ändern.

---

## Secret Management

### AzureKeyVaultConfigSource

Die Quarkus-Services laden Secrets direkt aus dem Key Vault mittels der `AzureKeyVaultConfigSource`:

```java
// Quarkus lädt automatisch Secrets aus Key Vault
// wenn AZURE_KEYVAULT_ENDPOINT gesetzt ist
@ConfigProperty(name = "quarkus.datasource.jdbc.url")
String jdbcUrl;  // Wird aus Key Vault Secret "postgres-connection-string" geladen
```

**Vorteile:**
- Keine Secrets in Environment Variables oder Configs
- Automatische Rotation möglich
- Zentrale Secret-Verwaltung

### Secret-Mapping für Services

| Service | Benötigte Secrets |
|---------|-------------------|
| **Platform** | `postgres-connection-string` |
| **Ticketing** | `cosmos-*`, `storage-connection-string` |
| **Notification** | `eventhub-*` |
| **OCR** | `eventhub-*`, `storage-connection-string` |

---

## Managed Identity & RBAC

### Dual Identity Pattern

Die Container Apps verwenden ein **Dual Identity Pattern** mit zwei verschiedenen Managed Identities:

```hcl
# User-Assigned Identity für ACR Pull (erstellt VOR den Container Apps)
resource "azurerm_user_assigned_identity" "container_apps" {
  name = "${local.base_name}-ca-identity"
}

# Grant ACR Pull BEVOR Container Apps erstellt werden
resource "azurerm_role_assignment" "container_apps_acr_pull" {
  scope                = data.azurerm_container_registry.main.id
  role_definition_name = "AcrPull"
  principal_id         = azurerm_user_assigned_identity.container_apps.principal_id
}

# Container App mit beiden Identity-Typen
resource "azurerm_container_app" "apps" {
  identity {
    type         = "SystemAssigned, UserAssigned"
    identity_ids = [azurerm_user_assigned_identity.container_apps.id]
  }
  
  # User-Assigned Identity für ACR Pull
  registry {
    identity = azurerm_user_assigned_identity.container_apps.id
    server   = data.azurerm_container_registry.main.login_server
  }
}
```

**Begründung für Dual Identity:**

| Identity-Typ | Verwendungszweck | Warum? |
|--------------|------------------|--------|
| **User-Assigned** | ACR Pull | Identity existiert bevor Container App erstellt wird |
| **System-Assigned** | Storage, Event Hubs, Key Vault | Automatisch verwaltet, pro Container App eindeutig |

### Zugewiesene Rollen

| Rolle | Scope | Identity | Zweck |
|-------|-------|----------|-------|
| `AcrPull` | Container Registry | **User-Assigned** | Container Images aus ACR ziehen |
| `Storage Blob Data Contributor` | Storage Account | System-Assigned | Lesen/Schreiben von Blobs |
| `Azure Event Hubs Data Owner` | Event Hub Namespace | System-Assigned | Kafka Produce/Consume |
| `Key Vault Secrets User` | Key Vault | System-Assigned | Secrets aus Key Vault lesen |

**Vorteile von Managed Identity:**
- Keine Credentials im Code oder Config
- Automatische Credential-Rotation
- Zentrale Zugriffskontrolle über Azure RBAC
- Kein manuelles Secret-Management für Azure-Services

> **⚠️ Ausnahme Cosmos DB:** Cosmos DB Cassandra API unterstützt keine Managed Identity. Credentials werden aus Key Vault gelesen.

---

## Hinweise

> **⚠️ Produktionsempfehlung:** Diese Dokumentation beschreibt die IaC-Konfiguration für Development/Test. Für Production sollten zusätzliche Maßnahmen wie Zone Redundancy, Geo-Replikation und erweiterte Backup-Strategien implementiert werden.

> **📝 Repository-Hinweis:** Idealerweise sollte Infrastructure as Code in einem eigenen Repository verwaltet werden. Da für die REMSFAL GitHub-Organisation keine neuen Repositories erstellt werden können, befindet sich der IaC-Code im Backend-Repository unter `/iac/azure/terraform`.
