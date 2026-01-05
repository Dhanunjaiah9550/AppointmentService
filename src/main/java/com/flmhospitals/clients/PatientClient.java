package com.flmhospitals.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("PatientManagement")
public interface PatientClient {

	@GetMapping("/patients/getPatientName/{patientId}")
	String getPatientName(@PathVariable(name="patientId") String patientId);

}
