package com.dailycodework.universalpetcare.data;

import com.dailycodework.universalpetcare.model.Patient;
import com.dailycodework.universalpetcare.model.Veterinarian;
import com.dailycodework.universalpetcare.repository.PatientRepository;
import com.dailycodework.universalpetcare.repository.UserRepository;
import com.dailycodework.universalpetcare.repository.VeterinarianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultDataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final UserRepository userRepository;
    private final VeterinarianRepository veterinarianRepository;
   private final PatientRepository patientRepository;



    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createDefaultVetIfNotExits();
        createDefaultPatientIfNotExits();

    }

    private void createDefaultVetIfNotExits(){
        for (int i = 1; i<=10; i++){
            String defaultEmail = "vet"+i+"@gmail.com";
            if (userRepository.existsByEmail(defaultEmail)){
                continue;
            }
            Veterinarian vet = new Veterinarian();
            vet.setFirstName("Vet");
            vet.setLastName("Number" + i);
            vet.setGender("Not Specified");
            vet.setPhoneNumber("1234567890");
            vet.setEmail(defaultEmail);
            vet.setPassword("password" + i);
            vet.setUserType("VET");
            vet.setSpecialization("Dermatologist");
            Veterinarian theVet = veterinarianRepository.save(vet);
            theVet.setEnabled(true);
            System.out.println("Default vet user " + i + " created successfully.");
        }
    }



    private void createDefaultPatientIfNotExits(){
        for (int i = 1; i<=10; i++){
            String defaultEmail = "pat"+i+"@gmail.com";
            if (userRepository.existsByEmail(defaultEmail)){
                continue;
            }
            Patient pat = new Patient();
            pat.setFirstName("Pat");
            pat.setLastName("Patient" + i);
            pat.setGender("Not Specified");
            pat.setPhoneNumber("1234567890");
            pat.setEmail(defaultEmail);
            pat.setPassword("password" + i);
            pat.setUserType("PATIENT");
            Patient thePatient = patientRepository.save(pat);
            thePatient.setEnabled(true);
            System.out.println("Default vet user " + i + " created successfully.");
        }
    }
}
