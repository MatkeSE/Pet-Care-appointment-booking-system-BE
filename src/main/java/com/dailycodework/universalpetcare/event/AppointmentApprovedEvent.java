package com.dailycodework.universalpetcare.event;

import com.dailycodework.universalpetcare.model.Appointment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AppointmentApprovedEvent extends ApplicationEvent {
    private final Appointment appointment;

    public AppointmentApprovedEvent(Appointment appointment) {
        super(appointment);
        this.appointment = appointment;
    }
}
