package com.driton.camel;

import java.util.Objects;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true)
public class Employee { // #1

	@DataField(pos = 1) // #2
	private String firstName;

	@DataField(pos = 2) // #2
	private String lastName;

	@DataField(pos = 3) // #2
	private String email;

	@DataField(pos = 4) // #2
	private String phoneNumber;

	// #1 Maps to CSV record
	// #2 Maps to column in CSV record

	public Employee() {
	}

	public Employee(String firstName, String lastName, String email, String phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phoneNumber = phoneNumber;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public Employee setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}

	public String getLastName() {
		return this.lastName;
	}

	public Employee setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}

	public String getEmail() {
		return this.email;
	}

	public Employee setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public Employee setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Employee)) {
			return false;
		}
		Employee employee = (Employee) o;
		return Objects.equals(firstName, employee.firstName) && Objects.equals(lastName, employee.lastName)
				&& Objects.equals(email, employee.email) && Objects.equals(phoneNumber, employee.phoneNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(firstName, lastName, email, phoneNumber);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append("{").append(this.firstName)
				.append(", " + this.lastName).append(",").append(this.email)
				.append(", ").append(this.phoneNumber).append("}").toString();
	}
}
