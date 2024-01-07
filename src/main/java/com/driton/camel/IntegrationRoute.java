package com.driton.camel;

import java.time.LocalDateTime;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "com.driton.camel.IntegrationRoute.enabled", havingValue = "true")
public class IntegrationRoute extends RouteBuilder {
	@Autowired
	private GetCurrentTimeBean getCurrentTimeBean;
	@Autowired
	private SimpleLoggingProcessingComponent loggingComponent;

	@Override
	public void configure() throws Exception {

		from("timer:first-timer?delay=10000&repeatCount=1")
				.routeId("Start Route From Timer")
				.log("\n\nBody is: ${body}")
				.transform().constant("My constant message!")
				.log("Body after transform is: ${body}")
				.bean(getCurrentTimeBean)
				.log("Body after getCurrentTimeBean is: ${body}")
				.bean(loggingComponent)
				.process(exchange -> {
					String bodyIn = exchange.getIn().getBody(String.class);
					exchange.getMessage().setBody("Changed body in Process method1");
					String bodyOut = exchange.getMessage().getBody(String.class);

					log.info("In process handler Body In is: {} ", bodyIn);
					log.info("In process handler Body Out is: {}", bodyOut);
				})
				.process(new SimpleLoggingProcessor())
				.to("log:first-timer");
	}
}

@Component
class GetCurrentTimeBean {
	public String getCurrentTime() {
		return "Datetime is: " + LocalDateTime.now();
	}
}

@Component
class SimpleLoggingProcessingComponent {
	private Logger logger = LoggerFactory.getLogger(SimpleLoggingProcessingComponent.class);

	public void process(String message) {
		logger.info("SimpleLoggingProcessingComponent {}", message);
	}
}

class SimpleLoggingProcessor implements Processor {
	private Logger logger = LoggerFactory.getLogger(SimpleLoggingProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("SimpleLoggingProcessingComponent {}", exchange.getMessage().getBody());
	}
}
