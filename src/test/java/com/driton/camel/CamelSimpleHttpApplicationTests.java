package com.driton.camel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
@CamelSpringBootTest
@SpringBootTest(properties = { "rest.get.file = direct:start" })
class CamelSimpleHttpApplicationTests {
	private Logger log = LoggerFactory.getLogger(CamelSimpleHttpApplicationTests.class);
	@Autowired
	protected CamelContext camelContext;
	@EndpointInject("mock:get-file-data")
	protected MockEndpoint mockA;
	@Autowired
	protected ProducerTemplate template;
	Map<String, Object> headers = new HashMap<String, Object>();

	@Test
	void shouldAutowireProducerTemplate() {
		assertNotNull(template);
	}

	@Test
	void shouldAutowireMockA() {
		assertNotNull(mockA);
	}

	@Test
	void shouldInjectEndpoint() throws InterruptedException {
		mockA.setExpectedMessageCount(1);
		template.sendBodyAndHeader(mockA, "mockA", "fileName", "employees-basic.csv");
		mockA.assertIsSatisfied();
		mockA.expectedHeaderReceived("Content-Type", "application/json");
	}

	@Test
	void requestDirectGetFileDataTest() throws Exception {
		// <T> T requestBodyAndHeader(Endpoint endpoint, Object body, String header,
		// Object headerValue, Class<T> type)
		ListEmployeesResponse ret = template.requestBodyAndHeader("direct:get-file-data", "World", "fileName",
				"employees-basic.csv",
				ListEmployeesResponse.class);
		assertNotNull(ret);
		assertNotNull(ret.getMetadata());
		assertNotNull(ret.getEmployees());
		ret.getEmployees().forEach(employee -> {
			assertNotNull(employee.getFirstName());
			assertNotNull(employee.getLastName());
			assertNotNull(employee.getEmail());
			assertNotNull(employee.getPhoneNumber());
		});
		assertEquals(10, ret.getEmployees().size());
	}

	@Test
	void testPositive() throws Exception {
		assertEquals(ServiceStatus.Started, camelContext.getStatus());
		this.headers.put("fileName", "employees-basic.csv");
		Object obj = template.sendBodyAndHeaders("direct:get-file-data", ExchangePattern.InOut, "", headers);
		assertNotNull(obj);
		log.info("The returned mock object: {}", obj);
		this.headers.clear();
	}
}
