package com.driton.camel;

import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.SSLContextServerParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLContextParamsConfiguration {
    @Value("${app.security.truststore.file}")
    private String trustStoreFile;
    @Value("${app.security.truststore.password}")
    private String trustStorePassword;
    
    @Value("${app.security.keystore.file}")
    private String keyStoreFile;
    @Value("${app.security.keystore.password}")
    private String keyStorePassword;

    @Bean(name = "sslContextParameters")
    public SSLContextParameters setSSLContextParameters() {
        // Setup KeyStore parameters keystore/camel-keystore.jks
        // This is needed only when serving secured ssl endpoints.
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(keyStoreFile);
        ksp.setPassword(keyStorePassword);
        ksp.setType("JKS");


        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);
        kmp.setKeyPassword(keyStorePassword);

        SSLContextServerParameters scsp = new SSLContextServerParameters();
        scsp.setClientAuthentication("NONE");

        // Setup TrustStore parameters using the truststore file keystore/camel-certs.jks
        // in order to trust server certificates that are in camel-certs.jks
        // which in this case is a copy of java runtime cacerts file.
        KeyStoreParameters tsp = new KeyStoreParameters();
        tsp.setResource(trustStoreFile);
        tsp.setPassword(trustStorePassword);
        tsp.setType("JKS");

        // We need the trust store to call SSL endpoints,
        // but when creating a ssl enpoint we need the keymanager parameters 
        TrustManagersParameters trustManagerParams = new TrustManagersParameters();
        trustManagerParams.setKeyStore(tsp);

        SSLContextParameters scp = new SSLContextParameters();
        scp.setServerParameters(scsp);
        scp.setKeyManagers(kmp);
        scp.setTrustManagers(trustManagerParams);

        return scp;
    }
}
