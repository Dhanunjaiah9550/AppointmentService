package com.flmhospitals.service;

import java.time.LocalDate;
import java.util.List;
public interface AppointmentService {

	List<String> getPatientsByDoctor(String staffId,LocalDate startDate, LocalDate endDate);

}
