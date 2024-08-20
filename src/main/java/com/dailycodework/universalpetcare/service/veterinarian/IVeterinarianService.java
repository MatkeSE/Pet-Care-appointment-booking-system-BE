package com.dailycodework.universalpetcare.service.veterinarian;

import com.dailycodework.universalpetcare.dto.UserDto;
import com.dailycodework.universalpetcare.model.Veterinarian;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IVeterinarianService {

    List<UserDto> getAllVeterinariansWithDetails();

//    List<UserDto> findAvailableVetsForAppointment(String specialization, LocalDate date, LocalTime time);
//
//    List<Veterinarian> getVeterinariansBySpecialization(String specialization);
}
