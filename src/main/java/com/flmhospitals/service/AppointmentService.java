package com.flmhospitals.service;

import java.time.LocalDate;
import java.util.List;
import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.dto.AppointmentResponseDTO;
import com.flmhospitals.dto.RescheduleAppointmentDTO;
import com.flmhospitals.model.Appointment;

public interface AppointmentService {

	List<String> getPatientsByDoctor(String staffId,LocalDate startDate, LocalDate endDate);

	AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDto);

	List<Appointment> getAllAppointmentsForAllDoctors(LocalDate date);
	
	List<Appointment> getAllAppointmentsOfDoctor(String doctorId,LocalDate date);
	
	List<Appointment> getAllFutureAppointmentsOfDoctor(String doctorId);

	AppointmentResponseDTO reScheduleAppointment(String appointmentId,RescheduleAppointmentDTO rescheduleAppointmentDTO);

	boolean cancelAppointment(String appointmentId);
	
	AppointmentResponseDTO getAppointmentDetails(String appointmentId);
}
