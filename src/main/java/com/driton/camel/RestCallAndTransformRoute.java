package com.driton.camel;

import java.util.ArrayList;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.cluster.ClusteredRoutePolicy;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spi.RoutePolicy;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "com.driton.camel.RestCallAndTransformRoute.enabled", havingValue = "true")
public class RestCallAndTransformRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        RoutePolicy policy = ClusteredRoutePolicy.forNamespace("rest-call-namespace");

        from("timer:callHttpService?fixedRate=true&period=6000&repeatCount=2")
                // The instances of the same route can be clustered together in a active/passive
                // mode where only one instance at a time executes the business logic.
                //
                // Add route policy to make only one instance of this route to process the
                // business logic. It is a file based policy, but it could be implemented
                // using hazelcast, consul or zookeeper.
                .routePolicy(policy)
                .to("direct:enrichFromHttpBin");

        from("direct:enrichFromHttpBin")
                .to("undertow:https://httpbin.org/json?sslContextParameters=#sslContextParameters&ssl=true")
                .unmarshal().json(JsonLibrary.Jackson, Content.class)
                .process(exchange -> {
                    Content response = exchange.getIn().getBody(Content.class);
                    log.info("Received response: {}", response);
                })
                .log("The message body:\n ${body}")
                .marshal().json(JsonLibrary.Jackson, true)
                .log("After marshalling:\n ${body}")
                .convertBodyTo(String.class)
                .to("mock:funnel");
        // to("netty:tcp://localhost:3001?sync=false&allowDefaultCodec=false&encoders=#stringEncoder&decoders=#stringDecoder");
    }
}

class Content {
    public Slideshow slideshow;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

class Slide {
    public String title;
    public String type;
    public ArrayList<String> items;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

class Slideshow {
    public String author;
    public String date;
    public ArrayList<Slide> slides;
    public String title;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}