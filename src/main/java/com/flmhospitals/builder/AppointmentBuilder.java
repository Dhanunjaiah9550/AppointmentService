package com.flmhospitals.builder;

import java.time.LocalDateTime;

import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.model.Appointment;

public class AppointmentBuilder {
	
	public static Appointment buildAppointmentFromAppointmentRequestDTO(AppointmentRequestDTO appointmentRequestDTO){
		
		return Appointment.builder()
					.patientId(appointmentRequestDTO.getPatientId())
					.doctorId(appointmentRequestDTO.getDoctorId())
					.appointmentDate(appointmentRequestDTO.getAppointmentDate())
		            .startTime(LocalDateTime.of(
		                    appointmentRequestDTO.getAppointmentDate(),
		                    appointmentRequestDTO.getStartTime()))
		             .endTime(LocalDateTime.of(
		                    appointmentRequestDTO.getAppointmentDate(),
		                    appointmentRequestDTO.getEndTime()))
		             .notes(appointmentRequestDTO.getNotes())
					 .build();	
		
	}
	
}
