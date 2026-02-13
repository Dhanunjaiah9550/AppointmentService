package com.flmhospitals.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.dto.AppointmentResponseDTO;
import com.flmhospitals.dto.RescheduleAppointmentDTO;
import com.flmhospitals.model.Appointment;
import com.flmhospitals.service.AppointmentService;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
	
	public final AppointmentService appointmentSerivce;
	
	public AppointmentController(AppointmentService appointmentService) {
		
		this.appointmentSerivce = appointmentService;
	}

	@GetMapping("/getDoctorPatients/{staffId}")
	public List<String> getPatientsVisitedByDoctor(@PathVariable(name="staffId") String staffId, @RequestParam("startDate") String startdate,@RequestParam("endDate") String enddate){
		
		LocalDate startDate = LocalDate.parse(startdate);
		
		LocalDate endDate = LocalDate.parse(enddate);
		
		return appointmentSerivce.getPatientsByDoctor(staffId,startDate,endDate);
	}
	
	@PostMapping("/bookAppointment")
	public ResponseEntity<AppointmentResponseDTO> bookAppointment(@RequestBody AppointmentRequestDTO appointmentRequestDto){
		
		AppointmentResponseDTO ResponseDto = appointmentSerivce.bookAppointment(appointmentRequestDto);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto);
	}
	
	@GetMapping("/{date}")
	public ResponseEntity<List<Appointment>> getAllAppointmentsForAllDoctors(@PathVariable("date") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)  LocalDate date){
		return ResponseEntity.ok(appointmentSerivce.getAllAppointmentsForAllDoctors(date));
	}
	
	@GetMapping("/{doctorid}/{date}")
	public ResponseEntity<List<Appointment>> getAllAppointmentsOfDoctor(@PathVariable("doctorid") String doctorId,@PathVariable("date") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date){
		return ResponseEntity.ok(appointmentSerivce.getAllAppointmentsOfDoctor(doctorId, date));
	}

	@GetMapping("/allFutureAppointments/{doctorid}/")
	public ResponseEntity<List<Appointment>> getAllFutureAppointmentsOfDoctor(@PathVariable("doctorid") String doctorId){
		return ResponseEntity.ok(appointmentSerivce.getAllFutureAppointmentsOfDoctor(doctorId));
	}
	
	@PutMapping("/reScheduleAppointment{appointmentId}/")
	public ResponseEntity<AppointmentResponseDTO> reScheduleAppointment(@PathVariable(name="appointmentId") String appointmentId, @RequestBody RescheduleAppointmentDTO rescheduleAppointmentDTO){
		
		AppointmentResponseDTO ResponseDto = appointmentSerivce.reScheduleAppointment(appointmentId,rescheduleAppointmentDTO);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDto);
	}	
	
}
