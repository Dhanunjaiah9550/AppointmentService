package com.flmhospitals.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
	
}
