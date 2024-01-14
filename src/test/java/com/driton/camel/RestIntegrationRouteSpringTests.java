package com.driton.camel;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.util.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import net.minidev.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;

@SpringBootTest(classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration
@CamelSpringBootTest
class RestIntegrationRouteSpringTests {
    private Logger log = LoggerFactory.getLogger(RestIntegrationRouteSpringTests.class);
    Map<String, Object> headers = new HashMap<String, Object>();
    @Autowired
    private CamelContext context;
    @EndpointInject("mock:finishGetDataRoute")
    MockEndpoint mockFinishEndpoint;
    @EndpointInject("mock:finishPostEmployeeRoute")
    MockEndpoint mockFinishPostEmployeeRoute;
    @Autowired
    protected ProducerTemplate template;
    @Autowired
    protected TestRestTemplate restTemplate;
    protected JSONObject json;

    // Spring context fixtures
    @Configuration
    static class TestConfig {
        @Bean
        RoutesBuilder route() {
            return new RestIntegrationRoute();
        }
    }

    @Test
    void testGetFileDataRoute() throws Exception {
        Route route = context.getRoute("get-file-data");
        adviceWith(route.getId(), context,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() {
                        weaveAddLast().to("mock:finishGetDataRoute");
                    }
                });
        context.start();
        mockFinishEndpoint.expectedMessageCount(1);
        this.headers.put("fileName", "employees-basic.csv");
        log.info("Sending #1 message.");
        // <T> T requestBodyAndHeaders(
        // String endpointUri,
        // Object body,
        // Map<String,Object> headers,
        // Class<T> type)
        // throws CamelExecutionException;
        ListEmployeesResponse res = template.requestBodyAndHeaders(
                "direct:get-file-data",
                null,
                headers,
                ListEmployeesResponse.class);

        assertNotNull(res);
        assertNotNull(res.getMetadata());
        assertNotNull(res.getEmployees());
        JsonObject expectedMetadata = new JsonObject();
        expectedMetadata.put("size", res.getEmployees().size());
        expectedMetadata.put("start", 0);
        expectedMetadata.put("limit", res.getEmployees().size());
        assertEquals(expectedMetadata, res.getMetadata());
        mockFinishEndpoint.assertIsSatisfied();
        this.headers.clear();
    }

    @Test
    void testWrongFileGetFileDataRoute() throws Exception {
        Route route = context.getRoute("get-file-data");
        adviceWith(route.getId(), context,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() {
                        weaveAddLast().to("mock:finishGetDataRoute");
                    }
                });
        context.start();
        mockFinishEndpoint.expectedMessageCount(0);
        this.headers.put("fileName", "employees-wrong-file.csv");
        log.info("Sending #1 message.");
        // <T> T requestBodyAndHeaders(
        // String endpointUri,
        // Object body,
        // Map<String,Object> headers,
        // Class<T> type)
        // throws CamelExecutionException;
        Object res = template.requestBodyAndHeaders(
                "direct:get-file-data",
                null,
                headers);

        assertNotNull(res);
        assertEquals("File not found: employees-wrong-file.csv", res);
        mockFinishEndpoint.assertIsSatisfied();
        this.headers.clear();
    }

    @Test
    void testPostEmployeeRoute() throws Exception {
        Route route = context.getRoute("post-employee-route");
        adviceWith(route.getId(), context,
                new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() {
                        weaveAddLast().to("mock:finishPostEmployeeRoute");
                    }
                });
        context.start();
        log.info("Sending #1 message.");
        Employee empl = new Employee().setFirstName("Unit").setLastName("Test").setEmail("unit.test@company.com")
                .setPhoneNumber("514-331-2376");
        Employee res = template.requestBody("direct:post-employee", empl, Employee.class);

        // test mock endpoint
        // we have added a mock endpoint to the end of the route using adviceWith
        // so anything that is returned by the post-employee-route is also sent to the
        // mock:finishPostEmployeeRoute
        // Here we can expect the body that should be receeived by the mock endpoint.
        mockFinishPostEmployeeRoute.expectedMessageCount(1);
        mockFinishPostEmployeeRoute.expectedBodiesReceived(res);
        mockFinishPostEmployeeRoute.assertIsSatisfied();

        // validate the result returned from the route.
        assertNotNull(res);
        assertNotNull(res.getFirstName());
        assertNotNull(res.getLastName());
        assertNotNull(res.getEmail());
        assertNotNull(res.getPhoneNumber());
        // Employee class overrides the method equals, so we can just compare the two
        // objects if they are equal.
        assertEquals(empl, res);
    }
}
