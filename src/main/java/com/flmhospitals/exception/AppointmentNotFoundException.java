package com.flmhospitals.exception;

public class AppointmentNotFoundException extends RuntimeException {

	public AppointmentNotFoundException(String msg) {
		super(msg);
	}
}
