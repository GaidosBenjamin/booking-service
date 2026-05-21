package com.bgaidos.booking.config;

import com.bgaidos.booking.sms.BrevoSmsSender;
import com.bgaidos.booking.sms.LoggingSmsSender;
import com.bgaidos.booking.sms.SmsSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class SmsConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.sms", name = "enabled", havingValue = "true")
    public SmsSender brevoSmsSender(
        @Value("${app.sms.api-key}") String apiKey,
        @Value("${app.sms.sender}") String sender
    ) {
        var restClient = RestClient.builder()
            .baseUrl("https://api.brevo.com")
            .defaultHeader("api-key", apiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
        return new BrevoSmsSender(restClient, sender);
    }

    @Bean
    @ConditionalOnMissingBean(SmsSender.class)
    public SmsSender loggingSmsSender() {
        return new LoggingSmsSender();
    }
}
