package com.driton.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

@Component
@ConditionalOnProperty(name = "com.driton.camel.SocketClientRoute.enabled", havingValue = "true")
public class SocketClientRoute extends RouteBuilder {
    @Autowired
    protected CamelContext context;
    @Autowired
    protected SocketMessageBean socketMsgBean;

    @Override
    public void configure() {
        StringDecoder stringDecoder = new StringDecoder();
        StringEncoder stringEncoder = new StringEncoder();
        context.getRegistry().bind("stringEncoder", stringEncoder);
        context.getRegistry().bind("stringDecoder", stringDecoder);

        //
        // This route establishes a TCP connection to a listening TCP server socket
        // and waits for the server to send the data. The client doesn't send back
        // any data to the server by setting the parameter sync=false.
        //
        // sync=false allows not to send back a response upon the receipt of data from
        // the server
        // sync=true - allows the route to send back the Message body to the server upon
        // the receipt of data from the server.
        // clientMode=true - allows this endpoint to connect to the server and act as a
        // consumer (receive messages from the server).
        from("netty:tcp://localhost:3000?sync=false&allowDefaultCodec=false&encoders=#stringEncoder&decoders=#stringDecoder&clientMode=true")
                .log("${body}")
                .process(exchange -> {
                    // Process the Message here.
                })
                .bean(socketMsgBean)
                // forward (multicast) the message to many receivers in parallel
                .multicast().parallelProcessing()
                .to("netty:tcp://localhost:3001?sync=true&allowDefaultCodec=false&encoders=#stringEncoder&decoders=#stringDecoder")
                .to("netty:tcp://localhost:3002?sync=true&allowDefaultCodec=false&encoders=#stringEncoder&decoders=#stringDecoder")
                .end();
    }
}

@Component
class SocketMessageBean {
    public void handleSocketMessage(Exchange exchange) {
        exchange.getIn().setBody("Forwarding received data: " + exchange.getMessage().getBody());
    }
}
