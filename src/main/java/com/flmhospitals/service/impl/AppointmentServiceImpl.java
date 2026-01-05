package com.flmhospitals.service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.flmhospitals.builder.AppointmentBuilder;
import com.flmhospitals.builder.AppointmentDTOBuilder;
import com.flmhospitals.clients.DoctorClient;
import com.flmhospitals.clients.PatientClient;
import com.flmhospitals.dao.AppointmentRepository;
import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.dto.AppointmentResponseDTO;
import com.flmhospitals.exception.AppointmentAlreadyExistsException;
import com.flmhospitals.exception.DoctorUnAvailableException;
import com.flmhospitals.exception.InvalidTimeException;
import com.flmhospitals.model.Appointment;
import com.flmhospitals.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

	public final AppointmentRepository appointementRepository;

	public final DoctorClient doctorClient;
	
	public final PatientClient patientClient;

	public AppointmentServiceImpl(AppointmentRepository appointementRepository, DoctorClient doctorClient,PatientClient patientClient) {

		this.appointementRepository = appointementRepository;
		
		this.doctorClient = doctorClient;
		
		this.patientClient = patientClient;
	}

	@Override
	public List<String> getPatientsByDoctor(String staffId, LocalDate startDate, LocalDate endDate) {

		List<String> patientsByStaffId = appointementRepository.findPatientsByStaffId(staffId, startDate, endDate);

		return patientsByStaffId;
	}

	@Override
	public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDto) {
		log.info("Booking Appointment for the user {}", appointmentRequestDto.getPatientId());

		LocalDate appointmentDate = appointmentRequestDto.getAppointmentDate();
		
		String date = appointmentDate.toString();

		Boolean doctorAvailablity = doctorClient.isDoctorAvailable(appointmentRequestDto.getDoctorId(), date);

		List<Appointment> doctorAppointments = appointementRepository.findByDoctorId(
				appointmentRequestDto.getDoctorId(), appointmentRequestDto.getAppointmentDate(),
				appointmentRequestDto.getStartTime(), appointmentRequestDto.getEndTime());

		List<Appointment> patientAppointments = appointementRepository.findByPatientId(
				appointmentRequestDto.getPatientId(), appointmentRequestDto.getAppointmentDate(),
				appointmentRequestDto.getStartTime(), appointmentRequestDto.getEndTime());
		

		Appointment appointment = AppointmentBuilder.buildAppointmentFromAppointmentRequestDTO(appointmentRequestDto);
		
		if (!appointmentRequestDto.getAppointmentDate().isBefore(LocalDate.now())) {
			
			if (doctorAvailablity) {

				if (doctorAppointments.isEmpty()) {
					
					if (patientAppointments.isEmpty()) {
						
						Appointment savedAppointment = appointementRepository.save(appointment);

						String doctorName = doctorClient.getDoctorName(appointment.getDoctorId());
						
						String PatientName = patientClient.getPatientName(appointment.getPatientId());
						
						AppointmentResponseDTO appointmentResponseDTO = AppointmentDTOBuilder
								.buildAppointmentResponseDTO(savedAppointment);
						
						appointmentResponseDTO.setDoctorName(doctorName);
						
						appointmentResponseDTO.setPatientName(PatientName);
						
						appointmentResponseDTO.setStatus("Booked");

						log.info("AppointmentBooked Successfully {} ", appointmentRequestDto.getAppointmentDate());

						return appointmentResponseDTO;

					} else
						throw new AppointmentAlreadyExistsException(
								"Alreay an appointment is booked for Patient in the Date & Time Limits");

				} else
					throw new AppointmentAlreadyExistsException(
							"Alreay an appointment is booked for Doctor in the Date & Time Limits");

			} else
				throw new DoctorUnAvailableException("Doctor NotAvailable on this Date");
		} else {
			log.info("Invalid Date hence throwing an Exception {}", appointmentRequestDto.getAppointmentDate());

			throw new InvalidTimeException("In valid Date and time, please enter the correct Date and time");
		}
	}

}
