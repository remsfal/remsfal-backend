package de.remsfal.common.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConfigSource that loads secrets from Azure Key Vault.
 * Supports both Client Secret authentication (dev) and Managed Identity (prod).
 * 
 * Configuration (checked in order: application.properties, then environment variables):
 * - azure.keyvault.endpoint: Key Vault URL (in application.properties)
 * - AZURE_KEYVAULT_ENDPOINT: Key Vault URL (as environment variable)
 * - AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, AZURE_TENANT_ID: For dev authentication (optional)
 */
public class AzureKeyVaultConfigSource implements ConfigSource {

    private static final String VAULT_ENDPOINT_PROPERTY = "azure.keyvault.endpoint";
    private static final String VAULT_ENDPOINT_ENV = "AZURE_KEYVAULT_ENDPOINT";
    private static final String DEFAULT_VAULT_ENDPOINT = "";
    private static final int ORDINAL = 270;
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private volatile SecretClient secretClient;

    @Override
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(cache);
    }

    @Override
    public Set<String> getPropertyNames() {
        return cache.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        // Check cache first
        if (cache.containsKey(propertyName)) {
            return cache.get(propertyName);
        }

        // Only load secrets for specific known properties
        if (!isKnownSecret(propertyName)) {
            return null;
        }

        try {
            if (secretClient == null) {
                synchronized (this) {
                    if (secretClient == null) {
                        secretClient = createSecretClient();
                    }
                }
            }

            String value = secretClient.getSecret(propertyName).getValue();
            cache.put(propertyName, value);
            // Uncomment for debugging: System.out.println("✓ Loaded secret from Key Vault: " + propertyName);
            return value;
        } catch (Exception e) {
            System.err.println("✗ Failed to load secret: " + propertyName + " - " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getName() {
        return "AzureKeyVaultConfigSource";
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }

    private boolean isKnownSecret(String propertyName) {
        // Common secrets used across services
        return propertyName.equals("postgres-connection-string") ||
               propertyName.equals("eventhub-bootstrap-server") ||
               propertyName.equals("eventhub-connection-string") ||
               propertyName.equals("cosmos-contact-point") ||
               propertyName.equals("cosmos-username") ||
               propertyName.equals("cosmos-password") ||
               propertyName.equals("storage-connection-string");
    }

    private SecretClient createSecretClient() {
        String vaultEndpoint = getVaultEndpoint();

        String clientId = System.getenv("AZURE_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
        String tenantId = System.getenv("AZURE_TENANT_ID");

        SecretClientBuilder builder = new SecretClientBuilder().vaultUrl(vaultEndpoint);

        if (clientId != null && clientSecret != null && tenantId != null) {
            // Dev: Use Client Secret authentication
            ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .tenantId(tenantId)
                    .build();
            builder.credential(credential);
        } else {
            // Prod: Use Managed Identity or Azure CLI
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        return builder.buildClient();
    }

    /**
     * Gets the vault endpoint from (in order of priority):
     * 1. application.properties (azure.keyvault.endpoint)
     * 2. Environment variable (AZURE_KEYVAULT_ENDPOINT)
     * 3. Default value (empty - disables Key Vault)
     */
    private String getVaultEndpoint() {
        // 1. Check application.properties (loaded directly since ConfigSource API not available here)
        String endpoint = loadFromApplicationProperties(VAULT_ENDPOINT_PROPERTY);
        if (endpoint != null && !endpoint.isEmpty()) {
            return endpoint;
        }

        // 2. Check environment variable
        endpoint = System.getenv(VAULT_ENDPOINT_ENV);
        if (endpoint != null && !endpoint.isEmpty()) {
            return endpoint;
        }

        // 3. Fall back to default (empty = disabled)
        return DEFAULT_VAULT_ENDPOINT;
    }

    /**
     * Loads a property directly from application.properties on the classpath.
     * This is necessary because ConfigSource implementations cannot use the ConfigProvider API.
     */
    private String loadFromApplicationProperties(String key) {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty(key);
            }
        } catch (Exception e) {
            // Ignore - fall back to other sources
        }
        return null;
    }
}
