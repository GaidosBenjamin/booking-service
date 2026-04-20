package com.bgaidos.booking.config;

import com.bgaidos.booking.auth.mail.AuthMailer;
import com.bgaidos.booking.auth.mail.BrevoAuthMailer;
import com.bgaidos.booking.auth.mail.LoggingAuthMailer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.mail", name = "host")
    public AuthMailer brevoAuthMailer(
        JavaMailSender mailSender,
        @Value("${app.mail.from}") String from,
        @Value("${app.mail.brand-name}") String brandName
    ) {
        return new BrevoAuthMailer(mailSender, from, brandName);
    }

    @Bean
    @ConditionalOnMissingBean(AuthMailer.class)
    public AuthMailer loggingAuthMailer() {
        return new LoggingAuthMailer();
    }
}
