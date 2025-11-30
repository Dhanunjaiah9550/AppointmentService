package com.flmhospitals.service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.flmhospitals.dao.AppointmentRepository;
import com.flmhospitals.service.AppointmentService;

@Service
public class AppointmentServiceImpl implements AppointmentService{
	
	public final AppointmentRepository appointementRepository;

	public AppointmentServiceImpl(AppointmentRepository appointementRepository) {
		
		this.appointementRepository = appointementRepository;
	}

	@Override
	public List<String> getPatientsByDoctor(String staffId,LocalDate startDate, LocalDate endDate) {
		
		 List<String> patientsByStaffId = appointementRepository.findPatientsByStaffId(staffId, startDate, endDate);
		 
		 return patientsByStaffId;
	}

}
