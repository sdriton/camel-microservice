package com.driton.camel;

import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.SSLContextServerParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLContextParamsConfiguration {
    @Value("${app.security.truststore.file}")
    private String trustStoreFile;
    @Value("${app.security.truststore.password}")
    private String trustStorePassword;

    @Bean(name = "sslContextParameters")
    public SSLContextParameters setSSLContextParameters() {
        KeyStoreParameters ksp = new KeyStoreParameters();
        ksp.setResource(trustStoreFile);
        ksp.setPassword(trustStorePassword);
        ksp.setType("JKS");

        // We only need the trust store to call a SSL endpoint,
        // but camel seems to need the keymanager parameters and
        // not the trust manager parameters.
        KeyManagersParameters kmp = new KeyManagersParameters();
        kmp.setKeyStore(ksp);
        kmp.setKeyPassword(trustStorePassword);

        SSLContextServerParameters scsp = new SSLContextServerParameters();
        scsp.setClientAuthentication("NONE");

        // trust manager
        // TrustManagersParameters trustManagerParams = new TrustManagersParameters();
        // trustManagerParams.setKeyStore(ksp);

        SSLContextParameters scp = new SSLContextParameters();
        scp.setServerParameters(scsp);
        scp.setKeyManagers(kmp);
        // scp.setTrustManagers(trustManagerParams);

        return scp;
    }
}
