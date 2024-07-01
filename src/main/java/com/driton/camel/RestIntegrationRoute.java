package com.driton.camel;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.apache.camel.model.rest.RestParamType.query;
import static org.apache.camel.model.rest.RestParamType.body;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.util.json.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "com.driton.camel.RestIntegrationRoute.enabled", havingValue = "true")
public class RestIntegrationRoute extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		onException(Exception.class)
				.handled(true)
				.log("Exception occurred due: ${exception.message}")
				.log("Stacktrace: ${exception.stacktrace}")
				.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
				.setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
				.setBody().simple("Error ${exception.message}");

		restConfiguration()
				.component("servlet")
				.bindingMode(RestBindingMode.auto)
				.dataFormatProperty("prettyPrint", "true")
				.enableCORS(true)
				.apiContextPath("/openapi")
				.apiProperty("api.title", "User API")
				.apiProperty("api.version", "1.0.0")
				.apiVendorExtension("true")
				.enableNoContentResponse("true");

		// call this endpoint as follows:
		// http://localhost:8090/api/v1/file?fileName=employees-basic.csv
		//
		// Actuator metrics
		// http://localhost:8090/actuator
		rest().produces("application/json")
				.get("/file").routeId("get-employees-rest-endpoint").produces("application/json")
				.description("Get employees data")
				.responseMessage("200", "Successful.")
				.param().name("fileName").type(query).description("The name of the csv file").dataType("string")
				.endParam()
				// In order to return a proper response it is required to set
				// bindingMode(RestBindingMode.json)
				// otherwise the response is not accepted by a browser or Postman.
				.outType(ListEmployeesResponse.class).bindingMode(RestBindingMode.json)
				.to("direct:get-file-data")

				.post("/file").routeId("post-employee-rest-endpoint").description("Creates new employee.")
				.responseMessage("201", "Created.")
				.type(Employee.class).bindingMode(RestBindingMode.json)
				.outType(Employee.class).bindingMode(RestBindingMode.json)
				.param().name("body").type(body).description("The user to create").endParam()
				.to("direct:post-employee");

		from("direct:get-file-data").routeId("get-file-data")
				.bean(FileService.class, "processFile(${exchange})")
				// Instead of using a bean,
				// pollEnrich can also be used to read the contents
				// of a source into the exchange.
				// .pollEnrich().simple("file:///tmp?fileName=${header.fileName}&noop=true&idempotent=false")
				.log("${body}")
				.unmarshal().bindy(BindyType.Csv, Employee.class)
				.process(exchange -> {
					// Bindy returns an array list of Employee objects.
					@SuppressWarnings("unchecked")
					ArrayList<Employee> e = exchange.getIn().getBody(ArrayList.class);
					JsonObject metadata = new JsonObject();
					metadata.put("size", e.size());
					metadata.put("start", 0);
					metadata.put("limit", e.size());
					ListEmployeesResponse res = new ListEmployeesResponse().setEmployees(e).setMetadata(metadata);
					exchange.getMessage().setBody(res, ListEmployeesResponse.class);
				});

		from("direct:post-employee").routeId("post-employee-route")
				.process(exchange -> {
					Employee body = exchange.getIn().getBody(Employee.class);
					log.info("Body: {}", body);
					exchange.getIn().setBody(body, Employee.class);
				})
				.marshal().bindy(BindyType.Csv, Employee.class)
				.to("file:///tmp?fileName=employees-post.csv&fileExist=Append")
				.process(exchange -> {
					String body = exchange.getIn().getBody(String.class);
					log.info("Body: {}", body);
					String[] csvRecord = body.replace(System.lineSeparator(), "").split(",");
					Employee e = new Employee().setFirstName(csvRecord[0]).setLastName(csvRecord[1])
							.setEmail(csvRecord[2]).setPhoneNumber(csvRecord[3]);
					exchange.getMessage().setBody(e, Employee.class);
				});
	}
}

@Component
class FileService {
	@Value("${app.employee-file-directory}")
	private String employeeFilePath;

	public void processFile(Exchange exchange) throws Exception {
		String fileName = exchange.getIn().getHeader("fileName").toString();
		File file = new File(employeeFilePath + File.separator + fileName);
		if (!file.exists()) {
			Message m = exchange.getMessage();
			m.setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
			m.setBody("File not found: " + file.getAbsolutePath());
			// Stop processing the message and return the response.
			exchange.setRouteStop(true);
			return;
		}
		exchange.getMessage().setBody(getContentFromFile(file));
	}

	private String getContentFromFile(File file) {
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()), StandardCharsets.UTF_8)) {
			reader.lines().map(line -> line + System.lineSeparator())
					.forEach(content::append);
		} catch (IOException ex) {
			content.delete(0, content.length());
			ex.printStackTrace();
		}
		return content.toString();
	}
}
