package com.dailycodework.universalpetcare.service.appointment;


import com.dailycodework.universalpetcare.dto.AppointmentDto;
import com.dailycodework.universalpetcare.dto.EntityConverter;
import com.dailycodework.universalpetcare.dto.PetDto;
import com.dailycodework.universalpetcare.enums.AppointmentStatus;
import com.dailycodework.universalpetcare.exception.ResourceNotFoundException;
import com.dailycodework.universalpetcare.model.Appointment;
import com.dailycodework.universalpetcare.model.Pet;
import com.dailycodework.universalpetcare.model.User;
import com.dailycodework.universalpetcare.repository.AppointmentRepository;
import com.dailycodework.universalpetcare.repository.UserRepository;
import com.dailycodework.universalpetcare.request.AppointmentUpdateRequest;
import com.dailycodework.universalpetcare.request.BookAppointmentRequest;
import com.dailycodework.universalpetcare.service.pet.IPetService;
import com.dailycodework.universalpetcare.utils.FeedBackMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AppointmentService implements IAppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final IPetService petService;
    private final EntityConverter<Appointment, AppointmentDto> entityConverter;
    private final EntityConverter<Pet, PetDto> petEntityConverter;



    @Transactional
    @Override
    public Appointment createAppointment(BookAppointmentRequest request, Long senderId, Long recipientId) {
        Optional<User> sender = userRepository.findById(senderId);
        Optional<User> recipient = userRepository.findById(recipientId);
        if (sender.isPresent() && recipient.isPresent()) {

            Appointment appointment = request.getAppointment();
            List<Pet> pets = request.getPets();
            pets.forEach(pet -> pet.setAppointment(appointment));
            List<Pet> savedPets = petService.savePetsForAppointment(pets);
            appointment.setPets(savedPets);

            appointment.addPatient(sender.get());
            appointment.addVeterinarian(recipient.get());
            appointment.setAppointmentNo();
            appointment.setStatus(AppointmentStatus.WAITING_FOR_APPROVAL);
            return appointmentRepository.save(appointment);
        }
        throw new ResourceNotFoundException(FeedBackMessage.SENDER_RECIPIENT_NOT_FOUND);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment updateAppointment(Long id, AppointmentUpdateRequest request) {
        Appointment existingAppointment = getAppointmentById(id);
        if (!Objects.equals(existingAppointment.getStatus(), AppointmentStatus.WAITING_FOR_APPROVAL)) {
            throw new IllegalStateException(FeedBackMessage.ALREADY_APPROVED);
        }
        existingAppointment.setAppointmentDate(LocalDate.parse(request.getAppointmentDate()));
        existingAppointment.setAppointmentTime(LocalTime.parse(request.getAppointmentTime()));
        existingAppointment.setReason(request.getReason());
        return appointmentRepository.save(existingAppointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        appointmentRepository.findById(id)
                .ifPresentOrElse(appointmentRepository::delete, () -> {
                    throw new ResourceNotFoundException(FeedBackMessage.RESOURCE_NOT_FOUND);
                });

    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(FeedBackMessage.RESOURCE_NOT_FOUND));
    }

    @Override
    public Appointment getAppointmentByNo(String appointmentNo) {
        return appointmentRepository.findByAppointmentNo(appointmentNo);
    }

    @Override
    public List<AppointmentDto> getUserAppointments(Long userId) {
        List<Appointment> appointments = appointmentRepository.findAllByUserId(userId);
        return appointments.stream()
                .map(appointment -> {
                    AppointmentDto appointmentDto = entityConverter.mapEntityToDto(appointment, AppointmentDto.class);
                    List<PetDto> petDto = appointment.getPets()
                            .stream()
                            .map(pet -> petEntityConverter.mapEntityToDto(pet, PetDto.class)).toList();
                    appointmentDto.setPets(petDto);
                    return appointmentDto;
                }).toList();
    }
    @Override
    public  Appointment cancelAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.WAITING_FOR_APPROVAL))
                .map(appointment -> {appointment.setStatus(AppointmentStatus.CANCELLED);
                    return appointmentRepository.saveAndFlush(appointment);
                    }).orElseThrow(() -> new IllegalStateException(FeedBackMessage.APPOINTMENT_CANNOT_BE_CANCELLED));

    }

    @Override
    public  Appointment approveAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.WAITING_FOR_APPROVAL))
                .map(appointment -> {appointment.setStatus(AppointmentStatus.APPROVED);
                    return appointmentRepository.saveAndFlush(appointment);
                }).orElseThrow(() -> new IllegalStateException(FeedBackMessage.OPERATION_NOT_ALLOWED));

    }


    @Override
    public  Appointment declineAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.WAITING_FOR_APPROVAL))
                .map(appointment -> {appointment.setStatus(AppointmentStatus.NOT_APPROVED);
                    return appointmentRepository.saveAndFlush(appointment);
                }).orElseThrow(() -> new IllegalStateException(FeedBackMessage.OPERATION_NOT_ALLOWED));

    }

    @Override
    public long countAppointment(){
        return appointmentRepository.count();
    }

    @Override
    public List<Map<String, Object>> getAppointmentSummary() {
        return getAllAppointments()
                .stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> createStatusSummaryMap(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    

    private Map<String, Object> createStatusSummaryMap(AppointmentStatus status, Long value){
        Map<String, Object> summaryMap = new HashMap<>();
        summaryMap.put("name", formatAppointmentStatus(status));
        summaryMap.put("value", value);
        return summaryMap;
    }
    

    private String formatAppointmentStatus(AppointmentStatus appointmentStatus) {
        return appointmentStatus.toString().replace("_", "-").toLowerCase();
    }

}
