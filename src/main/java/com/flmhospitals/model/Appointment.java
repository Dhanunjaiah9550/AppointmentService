package com.flmhospitals.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.flmhospitals.Generator.AppointmentIdGenerator;

import jakarta.persistence.Entity;

import jakarta.persistence.Id;

import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;
    
    @Transient
    private AppointmentIdGenerator appointmentIdGenerator;

   
    public void generateAppointmentId() {
        if (this.appointmentId == null || this.appointmentId.isEmpty()) {
            this.appointmentId = appointmentIdGenerator.generateNextAppointmentId();
        }
    }

	
}

