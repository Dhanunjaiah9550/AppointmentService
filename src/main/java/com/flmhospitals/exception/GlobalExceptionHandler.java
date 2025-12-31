package com.flmhospitals.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AppointmentNotFoundException.class)
	public ResponseEntity<String> handleAppointmentNotFoundException(AppointmentNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(AppointmentAlreadyExistsException.class)
	public ResponseEntity<String> handleAppointmentAlreadyExistsException(AppointmentAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
	}
	
	@ExceptionHandler(DoctorUnAvailableException.class)
	public ResponseEntity<String> handleAppointmentAlreadyExistsException(DoctorUnAvailableException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
	}
	
	@ExceptionHandler(InvalidTimeException.class)
	public ResponseEntity<String> handleInvalidTimeException(InvalidTimeException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
	}
	
}
