package com.flmhospitals.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.flmhospitals.builder.AppointmentBuilder;
import com.flmhospitals.builder.AppointmentDTOBuilder;
import com.flmhospitals.clients.DoctorClient;
import com.flmhospitals.clients.PatientClient;
import com.flmhospitals.dao.AppointmentRepository;
import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.dto.AppointmentResponseDTO;
import com.flmhospitals.dto.RescheduleAppointmentDTO;
import com.flmhospitals.exception.AppointmentAlreadyExistsException;
import com.flmhospitals.exception.AppointmentNotFoundException;
import com.flmhospitals.exception.DoctorUnAvailableException;
import com.flmhospitals.exception.InvalidTimeException;
import com.flmhospitals.model.Appointment;
import com.flmhospitals.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

	public final AppointmentRepository appointmentRepository;

	public final DoctorClient doctorClient;
	
	public final PatientClient patientClient;

	public AppointmentServiceImpl(AppointmentRepository appointmentRepository, DoctorClient doctorClient,PatientClient patientClient) {

		this.appointmentRepository = appointmentRepository;
		
		this.doctorClient = doctorClient;
		
		this.patientClient = patientClient;
	}

	@Override
	public List<String> getPatientsByDoctor(String staffId, LocalDate startDate, LocalDate endDate) {

		List<String> patientsByStaffId = appointmentRepository.findPatientsByStaffId(staffId, startDate, endDate);

		return patientsByStaffId;
	}

	@Override
	public AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDto) {
		log.info("Booking Appointment for the user {}", appointmentRequestDto.getPatientId());

		LocalDate appointmentDate = appointmentRequestDto.getAppointmentDate();
		
		String date = appointmentDate.toString();

		Boolean doctorAvailablity = doctorClient.isDoctorAvailable(appointmentRequestDto.getDoctorId(), date);

		List<Appointment> doctorAppointments = appointmentRepository.findAppointmentsByDoctorId(
				appointmentRequestDto.getDoctorId(), appointmentRequestDto.getAppointmentDate(),
				appointmentRequestDto.getStartTime(), appointmentRequestDto.getEndTime());

		List<Appointment> patientAppointments = appointmentRepository.findByPatientId(
				appointmentRequestDto.getPatientId(), appointmentRequestDto.getAppointmentDate(),
				appointmentRequestDto.getStartTime(), appointmentRequestDto.getEndTime());
		

		Appointment appointment = AppointmentBuilder.buildAppointmentFromAppointmentRequestDTO(appointmentRequestDto);
		
		if (!appointmentRequestDto.getAppointmentDate().isBefore(LocalDate.now())) {
			
			if (doctorAvailablity) {

				if (doctorAppointments.isEmpty()) {
					
					if (patientAppointments.isEmpty()) {
						
						Appointment savedAppointment = appointmentRepository.save(appointment);

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

	@Override
	public List<Appointment> getAllAppointmentsForAllDoctors(LocalDate date) {
		List<Appointment> appointments = appointmentRepository.findByAppointmentDate(date);
		if(appointments.isEmpty()) {
			throw new AppointmentNotFoundException("No appointments found for date :"+date);
		}
		return appointments;
	}

	@Override
	public List<Appointment> getAllAppointmentsOfDoctor(String doctorId, LocalDate date) {
		List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
		if(appointments.isEmpty()) {
			throw new AppointmentNotFoundException("No appointments found for doctorId " + doctorId + " on " + date);
		}
		return appointments;
	}
	
	@Override
	public List<Appointment> getAllFutureAppointmentsOfDoctor(String doctorId) {
		//LocalDateTime currentDateAndTime = LocalDateTime.now();
		System.out.println("InSide getAllFutureAppointmentsOfDoctor method");
		System.out.println("currentDateAndTime = "+LocalDate.now());
//		List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
		List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId)
											.stream()
											.filter(appointment  -> appointment.getAppointmentDate().isAfter(LocalDate.now()))
											.toList();
		System.out.println("appointments = "+appointments);
		if(appointments.isEmpty()) {
			throw new AppointmentNotFoundException("No appointments found for doctorId " + doctorId + " after " + LocalDate.now());
		}
//		else {
//			List<Appointment> futureAppointments = new ArrayList<>();
//			appointments.stream()
//						.filter(appointment  -> appointment.getAppointmentDate().isAfter(LocalDate.now()))
//						.toList();
//		}
		return appointments;
	}
	
	@Override
	public AppointmentResponseDTO reScheduleAppointment(String appointmentId,RescheduleAppointmentDTO rescheduleAppointmentDTO) {

		Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId);

		LocalDate newAppointmentDate = rescheduleAppointmentDTO.getNewDate();
		
		String date = newAppointmentDate.toString();

		Boolean doctorAvailablity = doctorClient.isDoctorAvailable(appointment.getDoctorId(), date);

		List<Appointment> doctorAppointments = appointmentRepository.findAppointmentsByDoctorId(
				appointment.getDoctorId(), rescheduleAppointmentDTO.getNewDate(),
				rescheduleAppointmentDTO.getNewStartTime(), rescheduleAppointmentDTO.getNewEndTime());

		List<Appointment> patientAppointments = appointmentRepository.findByPatientId(
				appointment.getPatientId(), rescheduleAppointmentDTO.getNewDate(),
				rescheduleAppointmentDTO.getNewStartTime(), rescheduleAppointmentDTO.getNewEndTime());	

//		Appointment appointment = AppointmentBuilder.buildAppointmentFromAppointmentRequestDTO(appointmentRequestDto);
		appointment.setAppointmentDate(rescheduleAppointmentDTO.getNewDate());
		appointment.setStartTime(rescheduleAppointmentDTO.getNewStartTime());
		appointment.setEndTime(rescheduleAppointmentDTO.getNewEndTime());
		
		if (!rescheduleAppointmentDTO.getNewDate().isBefore(LocalDate.now())) {
			
			if (doctorAvailablity) {

				if (doctorAppointments.isEmpty()) {
					
					if (patientAppointments.isEmpty()) {
						
						Appointment savedAppointment = appointmentRepository.save(appointment);

						String doctorName = doctorClient.getDoctorName(appointment.getDoctorId());
						
						String PatientName = patientClient.getPatientName(appointment.getPatientId());
						
						AppointmentResponseDTO appointmentResponseDTO = AppointmentDTOBuilder
								.buildAppointmentResponseDTO(savedAppointment);
						
						appointmentResponseDTO.setDoctorName(doctorName);
						
						appointmentResponseDTO.setPatientName(PatientName);
						
						appointmentResponseDTO.setStatus("Booked");

						log.info("AppointmentBooked Successfully {} ", rescheduleAppointmentDTO.getNewDate());

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
			log.info("Invalid Date hence throwing an Exception {}", rescheduleAppointmentDTO.getNewDate());

			throw new InvalidTimeException("In valid Date and time, please enter the correct Date and time");
		}
		
//		return null;
		
	}	

}
