package com.driton.camel;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "com.driton.camel.FtpTransferRoute.enabled", havingValue = "true")
public class FtpTransferRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("file:/tmp?noop=true&fileName=testfile.csv")
                .log("${body}")
                .to("ftps:host:990/directory?passiveMode=true&noop=true&implicit=true&username=UserName&password=Password");
    }
}
