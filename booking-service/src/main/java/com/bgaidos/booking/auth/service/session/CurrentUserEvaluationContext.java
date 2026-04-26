package com.bgaidos.booking.auth.service.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.spel.spi.EvaluationContextExtension;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CurrentUserEvaluationContext implements EvaluationContextExtension {

    private final CurrentUser currentUser;

    @Override
    public String getExtensionId() {
        return "currentUser-ext";
    }

    @Override
    public Map<String, Object> getProperties() {
        return Map.of("currentUser", currentUser);
    }
}
