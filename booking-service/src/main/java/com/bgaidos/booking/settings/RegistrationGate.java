package com.bgaidos.booking.settings;

import com.bgaidos.booking.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class RegistrationGate {

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicBoolean memberOnly = new AtomicBoolean(false);

    public boolean isEnabled() {
        return enabled.get();
    }

    public boolean isMemberOnly() {
        return memberOnly.get();
    }

    public void setEnabled(boolean value) {
        enabled.set(value);
        log.info("registrations {}", value ? "enabled" : "disabled");
    }

    public void setMemberOnly(boolean value) {
        memberOnly.set(value);
        log.info("registrations member-only {}", value ? "enabled" : "disabled");
    }

    public void assertEnabled() {
        if (!enabled.get()) {
            throw new ServiceUnavailableException("Registrations are currently closed");
        }
    }

    public void assertMemberIfRequired(boolean isMember) {
        if (memberOnly.get() && !isMember) {
            throw new ServiceUnavailableException("Registrations are currently open to members only");
        }
    }
}
