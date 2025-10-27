package com.flmhospitals.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "appointments")
public class Appointment {

	@Id
	private String appointmentId;
	
	private Long patientId;
	
	private Long doctorId;
	
	private LocalDate appointmentDate;
	
	private LocalTime startTime;
	
	private LocalTime endTime;
	
	private String status;
	
	private String notes;
}

