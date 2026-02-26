package com.flmhospitals.service.impl;

import java.time.LocalDate;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.flmhospitals.builder.AppointmentBuilder;
import com.flmhospitals.builder.AppointmentDTOBuilder;
import com.flmhospitals.clients.DoctorClient;
import com.flmhospitals.clients.NotificationClient;
import com.flmhospitals.clients.PatientClient;
import com.flmhospitals.dao.AppointmentRepository;
import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.dto.AppointmentResponseDTO;
import com.flmhospitals.dto.EmailRequestDto;
import com.flmhospitals.dto.PatientResponseDto;
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
	
	public final NotificationClient notificationClient;

	public AppointmentServiceImpl(AppointmentRepository appointmentRepository, DoctorClient doctorClient,PatientClient patientClient, NotificationClient notificationClient) {

		this.appointmentRepository = appointmentRepository;
		
		this.doctorClient = doctorClient;
		
		this.patientClient = patientClient;
		
		this.notificationClient = notificationClient;
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
						
						sendAppointmentNotification(savedAppointment);
						

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

	private void sendAppointmentNotification(Appointment savedAppointment) {
		System.out.println("Entered Into sendAppointmentNotification");
		ResponseEntity<PatientResponseDto> patientDetails = patientClient.getPatientById(savedAppointment.getPatientId());
		
		EmailRequestDto emailRequest = new EmailRequestDto();
		emailRequest.setTo(patientDetails.getBody().getPatientEmail());
		emailRequest.setSubject("Appointment Confirmation - MedSync");

		emailRequest.setBody(
		        "<h2>Dear " + patientDetails.getBody().getPatientName() + ",</h2>"
		        + "<p>Your appointment has been successfully scheduled.</p>"
		        + "<p><b>Appointment ID:</b> " + savedAppointment.getAppointmentId() + "</p>"
		        + "<p><b>Date :</b> " + savedAppointment.getAppointmentDate()+ "</p>"
		        + "<p><b>Time :</b> " + savedAppointment.getStartTime() +" - "+savedAppointment.getEndTime() + "</p>"
		        + "<p>Thank you for choosing MedSync.</p>"
		);
		notificationClient.sendEmail(emailRequest);
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
		log.info("InSide getAllFutureAppointmentsOfDoctor method currentDateAndTime", LocalDate.now());
		List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId)
											.stream()
											.filter(appointment  -> appointment.getAppointmentDate().isAfter(LocalDate.now()))
											.toList();
		System.out.println("appointments = "+appointments);
		if(appointments.isEmpty()) {
			throw new AppointmentNotFoundException("No appointments found for doctorId " + doctorId + " after " + LocalDate.now());
		}
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

		appointment.setAppointmentDate(rescheduleAppointmentDTO.getNewDate());
		appointment.setStartTime(rescheduleAppointmentDTO.getNewStartTime());
		appointment.setEndTime(rescheduleAppointmentDTO.getNewEndTime());
		
		if (!rescheduleAppointmentDTO.getNewDate().isBefore(LocalDate.now())) {
			
			if (doctorAvailablity) {

				if (doctorAppointments.isEmpty()) {
					
					if (patientAppointments.isEmpty()) {
						
						Appointment savedAppointment = appointmentRepository.save(appointment);
						
						sendRescheduleAppointmentNotification(savedAppointment); 

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

		
	}

	private void sendRescheduleAppointmentNotification(Appointment savedAppointment) {
		ResponseEntity<PatientResponseDto> patientDetails =
		        patientClient.getPatientById(savedAppointment.getPatientId());

		PatientResponseDto patient = patientDetails.getBody();

		if (patient == null) {
		    throw new RuntimeException("Patient not found");
		}

		EmailRequestDto emailRequest = new EmailRequestDto();
		emailRequest.setTo(patient.getPatientEmail());
		emailRequest.setSubject("Appointment Rescheduled - MedSync");

		emailRequest.setBody(
		        "<h2>Dear " + patient.getPatientName() + ",</h2>"
		        + "<p>Your appointment has been <b>successfully rescheduled</b>.</p>"
		        + "<p><b>Appointment ID:</b> " + savedAppointment.getAppointmentId() + "</p>"
		        + "<p><b>New Date :</b> " + savedAppointment.getAppointmentDate() + "</p>"
		        + "<p><b>New Time :</b> " + savedAppointment.getStartTime() +" - "+savedAppointment.getEndTime() + "</p>"
		        + "<p>Please make a note of the updated schedule.</p>"
		        + "<p>Thank you for choosing MedSync.</p>"
		);
		
		notificationClient.sendEmail(emailRequest);
	}	

	@Override
	public boolean cancelAppointment(String appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id "+appointmentId));
		appointment.setStatus("CANCELLED");
		appointmentRepository.save(appointment);
		appointmentCancelledNotification(appointment);
		return true;
	}

	private void appointmentCancelledNotification(Appointment appointment) {
		ResponseEntity<PatientResponseDto> patientDetails =
		        patientClient.getPatientById(appointment.getPatientId());

		PatientResponseDto patient = patientDetails.getBody();

		if (patient == null) {
		    throw new RuntimeException("Patient not found");
		}

		EmailRequestDto emailRequest = new EmailRequestDto();
		emailRequest.setTo(patient.getPatientEmail());
		emailRequest.setSubject("Appointment Cancelled - MedSync");

		emailRequest.setBody(
		        "<h2>Dear " + patient.getPatientName() + ",</h2>"
		        + "<p>Your appointment has been <b>successfully cancelled</b>.</p>"
		        + "<p><b>Appointment ID:</b> " + appointment.getAppointmentId() + "</p>"
		        + "<p>If this was a mistake, please book a new appointment.</p>"
		        + "<p>Thank you for choosing MedSync.</p>"
		);

		notificationClient.sendEmail(emailRequest);
	}
	
	@Override
	public AppointmentResponseDTO getAppointmentDetails(String appointmentId) {
		
		Appointment appointment = appointmentRepository.findById(appointmentId)
		.orElseThrow(()->new AppointmentNotFoundException("no appointment found with the the Id :" + appointmentId));
		
		AppointmentResponseDTO appointmentResponseDTO = AppointmentDTOBuilder.buildAppointmentResponseDTO(appointment);
		
		String doctorName = doctorClient.getDoctorName(appointment.getDoctorId());
		
		String PatientName = patientClient.getPatientName(appointment.getPatientId());
		
		appointmentResponseDTO.setDoctorName(doctorName);
		
		appointmentResponseDTO.setPatientName(PatientName);
		
		 return appointmentResponseDTO;

	}

}
