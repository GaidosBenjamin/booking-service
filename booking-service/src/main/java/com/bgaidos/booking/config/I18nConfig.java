package com.bgaidos.booking.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class I18nConfig {

    @Bean
    public MessageSource messageSource() {
        var source = new ResourceBundleMessageSource();
        source.setBasename("i18n/email-messages");
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.of("ro"));
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
