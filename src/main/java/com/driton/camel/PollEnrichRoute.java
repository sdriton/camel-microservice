package com.driton.camel;

import java.io.File;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.cluster.ClusteredRoutePolicy;
import org.apache.camel.spi.RoutePolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "com.driton.camel.PollEnrichRoute.enabled", havingValue = "true")
public class PollEnrichRoute extends RouteBuilder {
	@Autowired
	PollEnrichAggregationStrategy pollEnrichAggregationStrategy;
	@Autowired
	EnrichAggregationStrategy enrichAggregationStrategy;

	@Override
	public void configure() throws Exception {

		onException(Exception.class)
				.handled(true)
				.log("Exception occurred due: ${exception.message}")
				.log("Stacktrace: ${exception.stacktrace}")
				.transform().simple("Error ${exception.message}")
				.to("mock:error");

		RoutePolicy policy = ClusteredRoutePolicy.forNamespace("poll-enrich-call-namespace");

		// "timer:enrichContent?repeatCount=1&delay=1000"
		from("timer:enrichContent?fixedRate=true&period=5000&repeatCount=3")
				// Only one instance of this route should process messages.
				.routePolicy(policy)
				.to("direct:pollEnrichDoTheJob");

		from("direct:pollEnrichDoTheJob")
				.routeId("poll-enrich-from-file")
				.transform().constant("Initial body.")
				.pollEnrich(
						"file:///tmp?fileName=data.txt&noop=true&idempotent=false",
						1000,
						this.pollEnrichAggregationStrategy)

				.enrich("undertow:https://httpbin.org/json?sslContextParameters=#sslContextParameters&ssl=true",
						this.enrichAggregationStrategy)

				// .enrich()
				// .simple("undertow:https://httpbin.org/json?sslContextParameters=#sslContextParameters&ssl=true")

				.process(exchange -> {
					String body = exchange.getIn().getBody(String.class);
					log.info("After PollEnrich Body is: {}\n", body);
				})
				.log(LoggingLevel.DEBUG, "Body is:\n ${body}")
				.to("file:///tmp?fileName=data-result.txt&autoCreate=true&bridgeErrorHandler=true");
	}
}

@Component
class PollEnrichAggregationStrategy implements AggregationStrategy {
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		if (newExchange == null) {
			return oldExchange;
		}
		if (oldExchange == null) {
			return newExchange;
		}

		Object oldBody = oldExchange.getIn().getBody();
		File resourceFile = newExchange.getIn().getBody(File.class);
		String newBody = newExchange.getContext().getTypeConverter().convertTo(String.class, resourceFile);
		newExchange.getIn().setBody(oldBody + ":" + newBody);
		return newExchange;
	}
}

@Component
class EnrichAggregationStrategy implements AggregationStrategy {
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		if (newExchange == null) {
			return oldExchange;
		}
		if (oldExchange == null) {
			return newExchange;
		}

		Object oldBody = oldExchange.getIn().getBody(String.class);
		String newBody = newExchange.getIn().getBody(String.class);
		oldExchange.getIn().setBody(oldBody + ":" + newBody);
		return oldExchange;
	}
}
