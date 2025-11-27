package com.flmhospitals.builder;

import com.flmhospitals.dto.AppointmentResponseDTO;

import com.flmhospitals.model.Appointment;

public class AppointmentDTOBuilder {

	 public static AppointmentResponseDTO buildAppointmentResponseDTO(Appointment appointment) {
	        return AppointmentResponseDTO.builder()
	                .appointmentId(appointment.getAppointmentId())
	                .appointmentDate(appointment.getAppointmentDate())
	                .startTime(appointment.getStartTime())
	                .endTime(appointment.getEndTime())
	                .status(appointment.getStatus())
	                .notes(appointment.getNotes())
	                .build();
	    }
}
