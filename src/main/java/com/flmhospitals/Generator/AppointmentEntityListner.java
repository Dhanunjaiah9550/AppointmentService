package com.flmhospitals.Generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flmhospitals.model.Appointment;

import jakarta.persistence.PrePersist;

@Component
public class AppointmentEntityListner {

	public static AppointmentIdGenerator appointmentIdGenerator;

	@Autowired
	public void init(AppointmentIdGenerator appointmentIdGenerator) {

		this.appointmentIdGenerator = appointmentIdGenerator;
	}

	@PrePersist
	public void generateStaffId(Appointment appointment) {
		if (appointment.getAppointmentId() == null || appointment.getAppointmentId().isEmpty()) {
			appointment.setAppointmentId(appointmentIdGenerator.generateNextAppointmentId());
		}
	}

}
