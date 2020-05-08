package org.camunda.bpm.extension.keycloak;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.camunda.bpm.engine.impl.identity.IdentityProviderException;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * Keycloak Identity Provider Session Factory.
 */
public class KeycloakIdentityProviderFactory implements SessionFactory {

    protected KeycloakConfiguration keycloakConfiguration;
    protected KeycloakContextProvider keycloakContextProvider;

    protected RestTemplate restTemplate = new RestTemplate();

    /**
     * Creates a new Keycloak session factory.
     * @param keycloakConfiguration the Keycloak configuration
     */
    public KeycloakIdentityProviderFactory(KeycloakConfiguration keycloakConfiguration) {
        this.keycloakConfiguration = keycloakConfiguration;

        // Create REST template with pooling HTTP client
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        HttpClientBuilder httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy());
        if (keycloakConfiguration.isDisableSSLCertificateValidation()) {
            try {
                TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
                SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                        .loadTrustMaterial(null, acceptingTrustStrategy)
                        .build();

                SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
                CloseableHttpClient httpClient2 = HttpClients.custom()
                        .setSSLSocketFactory(csf)
                        .build();

                factory.setHttpClient(httpClient2);

//                HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
//                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
//                        .<ConnectionSocketFactory> create().register("https", new SSLConnectionSocketFactory(sslContext, allowAllHosts))
//                        .build();
//                final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//                connectionManager.setMaxTotal(keycloakConfiguration.getMaxHttpConnections());
//                httpClient.setConnectionManager(connectionManager);
            } catch (GeneralSecurityException e) {
                throw new IdentityProviderException("Disabling SSL certificate validation failed", e);
            }
        } else {
            final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(keycloakConfiguration.getMaxHttpConnections());
            httpClient.setConnectionManager(connectionManager);
            factory.setHttpClient(httpClient.build());
        }
//        factory.setHttpClient(httpClient.build());
        restTemplate.setRequestFactory(factory);

        // replace ISO-8859-1 encoding with configured charset (default: UTF-8)
        for (int i = 0; i < restTemplate.getMessageConverters().size(); i++) {
            if (restTemplate.getMessageConverters().get(i) instanceof StringHttpMessageConverter) {
                restTemplate.getMessageConverters().set(i, new StringHttpMessageConverter(Charset.forName(keycloakConfiguration.getCharset())));
                break;
            }
        }

        // Create Keycloak context provider for access token handling
        keycloakContextProvider = new KeycloakContextProvider(keycloakConfiguration, restTemplate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getSessionType() {
        return ReadOnlyIdentityProvider.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session openSession() {
        return new KeycloakIdentityProviderSession(keycloakConfiguration, restTemplate, keycloakContextProvider);
    }

}
