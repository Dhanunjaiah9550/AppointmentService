package com.flmhospitals.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.flmhospitals.model.Appointment;


public interface AppointmentRepository extends JpaRepository<Appointment, String> {
	@Query(value = "SELECT appointment_id FROM appointments ORDER BY appointment_id DESC LIMIT 1", nativeQuery = true)
    String findLastAppointmentId();

	@Query("SELECT DISTINCT patientId FROM Appointment WHERE doctorId = :staffId AND appointmentDate BETWEEN LEAST(:startDate, :endDate) AND GREATEST(:startDate, :endDate)")
	List<String> findPatientsByStaffId(@Param("staffId") String staffId,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
}
