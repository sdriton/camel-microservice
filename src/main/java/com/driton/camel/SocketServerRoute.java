package com.driton.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

@Component
@ConditionalOnProperty(name = "com.driton.camel.SocketServerRoute.enabled", havingValue = "true")
public class SocketServerRoute extends RouteBuilder {
    @Autowired
    protected CamelContext context;

    @Override
    public void configure() throws Exception {
        StringDecoder stringDecoder = new StringDecoder();
        StringEncoder stringEncoder = new StringEncoder();
        context.getRegistry().bind("stringEncoder", stringEncoder);
        context.getRegistry().bind("stringDecoder", stringDecoder);

        from("netty:tcp://localhost:3001?sync=true&allowDefaultCodec=false&encoders=#stringEncoder&decoders=#stringDecoder")
                .log("Received on port 3001 ${body}");

        from("netty:tcp://localhost:3002?sync=true&allowDefaultCodec=false&encoders=#stringEncoder&decoders=#stringDecoder")
                .log("Received on port 3002 ${body}");
    }
}
