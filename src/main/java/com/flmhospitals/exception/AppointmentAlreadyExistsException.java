package com.flmhospitals.exception;

public class AppointmentAlreadyExistsException extends RuntimeException {

	public AppointmentAlreadyExistsException(String msg) {
		super(msg);
	}
}
