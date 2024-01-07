package com.driton.camel;

import java.util.List;

import org.apache.camel.util.json.JsonObject;

public class ListEmployeesResponse {
	private JsonObject metadata;
	private List<Employee> employees;

	public JsonObject getMetadata() {
		return metadata;
	}

	public ListEmployeesResponse setMetadata(JsonObject metadata) {
		this.metadata = metadata;
		return this;
	}

	public List<Employee> getEmployees() {
		return employees;
	}

	public ListEmployeesResponse setEmployees(List<Employee> employees) {
		this.employees = employees;
		return this;
	}

	@Override
	public String toString() {
		return "ListEmployeesResponse [metadata=" + metadata + ", employees=" + employees + "]";
	}
}
