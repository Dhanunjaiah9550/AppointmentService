package com.flmhospitals.service;

import java.time.LocalDate;
import java.util.List;
import com.flmhospitals.dto.AppointmentRequestDTO;
import com.flmhospitals.dto.AppointmentResponseDTO;

public interface AppointmentService {

	List<String> getPatientsByDoctor(String staffId,LocalDate startDate, LocalDate endDate);

	AppointmentResponseDTO bookAppointment(AppointmentRequestDTO appointmentRequestDto);

}
