package com.bgaidos.booking.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MailTemplates {

    private final MessageSource messageSource;

    public MailBody verification(String code, Duration expiresIn, String brandName, Locale locale) {
        return build(
            brandName,
            msg("verification.subject", locale),
            msg("verification.title", locale),
            msg("verification.intro", locale),
            msg("verification.footer", locale),
            code,
            expiresIn,
            locale);
    }

    public MailBody passwordReset(String code, Duration expiresIn, String brandName, Locale locale) {
        return build(
            brandName,
            msg("password_reset.subject", locale),
            msg("password_reset.title", locale),
            msg("password_reset.intro", locale),
            msg("password_reset.footer", locale),
            code,
            expiresIn,
            locale);
    }

    public MailBody bookingConfirmation(
        UUID bookingId,
        BigDecimal total,
        String currency,
        List<String> camperNames,
        String brandName,
        Locale locale
    ) {
        var subject = msg("booking.subject", locale) + " \u2014 " + brandName;
        var heading = msg("booking.heading", locale);
        var body = msg("booking.body", locale);
        var idLabel = msg("booking.id_label", locale);
        var campersLabel = msg("booking.campers_label", locale);
        var totalLabel = msg("booking.total_label", locale);
        var plainHeader = msg("booking.plain_header", locale);
        var plainBody = msg("booking.plain_body", locale);
        var camperList = camperNames.isEmpty() ? "\u2014" : String.join(", ", camperNames);
        var lang = locale.getLanguage();

        var plain = """
            %s

            %s

            %s: %s
            %s: %s
            %s: %s %s

            \u2014 %s
            """.formatted(plainHeader, plainBody,
                idLabel, bookingId,
                campersLabel, camperList,
                totalLabel, total.toPlainString(), currency,
                brandName);

        var html = """
            <!DOCTYPE html>
            <html lang="%s">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#2a2f45;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;">
                <tr><td align="center" style="padding:40px 16px;">
                  <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;background-color:#ffffff;border-radius:8px;box-shadow:0 1px 3px rgba(16,24,40,0.08);">
                    <tr><td style="padding:28px 40px 8px;">
                      <p style="margin:0;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#6b7280;font-weight:600;">%s</p>
                    </td></tr>
                    <tr><td style="padding:8px 40px 24px;">
                      <h1 style="margin:0;font-size:24px;font-weight:700;color:#111827;">%s</h1>
                    </td></tr>
                    <tr><td style="padding:0 40px 24px;color:#4b5563;font-size:15px;line-height:1.6;">
                      %s
                    </td></tr>
                    <tr><td style="padding:0 40px 24px;background-color:#f9fafb;border-radius:6px;">
                      <table width="100%%" cellpadding="6" cellspacing="0">
                        <tr><td style="color:#6b7280;font-size:13px;">%s</td><td style="font-size:13px;font-family:monospace;color:#111827;">%s</td></tr>
                        <tr><td style="color:#6b7280;font-size:13px;">%s</td><td style="font-size:13px;color:#111827;">%s</td></tr>
                        <tr><td style="color:#6b7280;font-size:13px;">%s</td><td style="font-size:15px;font-weight:700;color:#111827;">%s %s</td></tr>
                      </table>
                    </td></tr>
                    <tr><td style="border-top:1px solid #e5e7eb;padding:20px 40px 28px;color:#9ca3af;font-size:12px;">
                      \u00a9 %s
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                lang,
                escape(brandName), escape(heading), escape(body),
                escape(idLabel), escape(bookingId.toString()),
                escape(campersLabel), escape(camperList),
                escape(totalLabel), escape(total.toPlainString()), escape(currency.toUpperCase()),
                escape(brandName));

        return new MailBody(subject, html, plain);
    }

    public MailBody donationConfirmation(
        UUID donationId,
        BigDecimal amount,
        String currency,
        String donorName,
        String brandName,
        Locale locale
    ) {
        var subject = msg("donation.subject", locale) + " \u2014 " + brandName;
        var heading = msg("donation.heading", locale);
        var body = donorName != null && !donorName.isBlank()
            ? msg("donation.body_named", new Object[]{donorName}, locale)
            : msg("donation.body", locale);
        var amountLabel = msg("donation.amount_label", locale);
        var plainHeader = msg("donation.plain_header", locale);
        var lang = locale.getLanguage();

        var plain = """
            %s

            %s

            %s: %s %s

            \u2014 %s
            """.formatted(plainHeader, body, amountLabel, amount.toPlainString(), currency.toUpperCase(), brandName);

        var html = """
            <!DOCTYPE html>
            <html lang="%s">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
            <body style="margin:0;padding:0;background-color:#f4f4f7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;color:#2a2f45;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;">
                <tr><td align="center" style="padding:40px 16px;">
                  <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;background-color:#ffffff;border-radius:8px;box-shadow:0 1px 3px rgba(16,24,40,0.08);">
                    <tr><td style="padding:28px 40px 8px;">
                      <p style="margin:0;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#6b7280;font-weight:600;">%s</p>
                    </td></tr>
                    <tr><td style="padding:8px 40px 24px;">
                      <h1 style="margin:0;font-size:24px;font-weight:700;color:#111827;">%s</h1>
                    </td></tr>
                    <tr><td style="padding:0 40px 24px;color:#4b5563;font-size:15px;line-height:1.6;">
                      %s
                    </td></tr>
                    <tr><td style="padding:0 40px 24px;background-color:#f9fafb;border-radius:6px;">
                      <table width="100%%" cellpadding="6" cellspacing="0">
                        <tr><td style="color:#6b7280;font-size:13px;">%s</td>
                            <td style="font-size:15px;font-weight:700;color:#111827;">%s %s</td></tr>
                      </table>
                    </td></tr>
                    <tr><td style="border-top:1px solid #e5e7eb;padding:20px 40px 28px;color:#9ca3af;font-size:12px;">
                      \u00a9 %s
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                lang,
                escape(brandName), escape(heading), escape(body),
                escape(amountLabel), escape(amount.toPlainString()), escape(currency.toUpperCase()),
                escape(brandName));

        return new MailBody(subject, html, plain);
    }

    public String formatDuration(Duration duration, Locale locale) {
        var minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes == 1
                ? msg("duration.minute", locale)
                : msg("duration.minutes", new Object[]{minutes}, locale);
        }
        var hours = duration.toHours();
        if (duration.toMinutes() % 60 == 0) {
            return hours == 1
                ? msg("duration.hour", locale)
                : msg("duration.hours", new Object[]{hours}, locale);
        }
        return minutes == 1
            ? msg("duration.minute", locale)
            : msg("duration.minutes", new Object[]{minutes}, locale);
    }

    public record MailBody(String subject, String html, String plainText) {
    }

    private MailBody build(
        String brandName,
        String subject,
        String title,
        String intro,
        String footer,
        String code,
        Duration expiresIn,
        Locale locale
    ) {
        var ttl = formatDuration(expiresIn, locale);
        var ttlMsg = msg("code.expires_in", new Object[]{ttl}, locale);
        var codeLabel = msg("code.your_code", locale);
        var lang = locale.getLanguage();

        var fullSubject = subject + " \u2014 " + brandName;
        var html = HTML_LAYOUT
            .replace("{{LANG}}", lang)
            .replace("{{BRAND}}", escape(brandName))
            .replace("{{PREHEADER}}", escape("Your code is " + code + ", valid for " + ttl + "."))
            .replace("{{TITLE}}", escape(title))
            .replace("{{INTRO}}", escape(intro))
            .replace("{{CODE}}", escape(code))
            .replace("{{TTL_MSG}}", escape(ttlMsg))
            .replace("{{FOOTER}}", escape(footer));
        var plain = PLAIN_LAYOUT
            .replace("{{BRAND}}", brandName)
            .replace("{{TITLE}}", title)
            .replace("{{INTRO}}", intro)
            .replace("{{CODE_LABEL}}", codeLabel)
            .replace("{{CODE}}", code)
            .replace("{{TTL_MSG}}", ttlMsg)
            .replace("{{FOOTER}}", footer);
        return new MailBody(fullSubject, html, plain);
    }

    private String msg(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    private String msg(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }

    private static String escape(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
    }

    private static final String HTML_LAYOUT = """
        <!DOCTYPE html>
        <html lang="{{LANG}}">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>{{TITLE}}</title>
        </head>
        <body style="margin:0;padding:0;background-color:#f4f4f7;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#2a2f45;">
          <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">{{PREHEADER}}</div>
          <table role="presentation" width="100%" cellpadding="0" cellspacing="0" style="background-color:#f4f4f7;">
            <tr>
              <td align="center" style="padding:40px 16px;">
                <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="max-width:560px;background-color:#ffffff;border-radius:8px;box-shadow:0 1px 3px rgba(16,24,40,0.08);">
                  <tr>
                    <td style="padding:28px 40px 8px;text-align:left;">
                      <p style="margin:0;font-size:13px;letter-spacing:0.08em;text-transform:uppercase;color:#6b7280;font-weight:600;">{{BRAND}}</p>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:8px 40px 16px;">
                      <h1 style="margin:0;font-size:24px;line-height:1.3;font-weight:700;color:#111827;">{{TITLE}}</h1>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:0 40px 24px;color:#4b5563;font-size:15px;line-height:1.6;">
                      {{INTRO}}
                    </td>
                  </tr>
                  <tr>
                    <td align="center" style="padding:0 40px 24px;">
                      <div style="display:inline-block;padding:18px 32px;background-color:#f3f4f6;border:1px solid #e5e7eb;border-radius:8px;font-family:'SFMono-Regular',Consolas,'Liberation Mono',Menlo,monospace;font-size:32px;font-weight:700;letter-spacing:8px;color:#111827;">
                        {{CODE}}
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:0 40px 32px;color:#6b7280;font-size:13px;line-height:1.5;text-align:center;">
                      {{TTL_MSG}}
                    </td>
                  </tr>
                  <tr>
                    <td style="border-top:1px solid #e5e7eb;padding:20px 40px 28px;color:#9ca3af;font-size:12px;line-height:1.5;">
                      {{FOOTER}}
                    </td>
                  </tr>
                </table>
                <p style="margin:16px 0 0;color:#9ca3af;font-size:12px;">\u00a9 {{BRAND}}</p>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """;

    private static final String PLAIN_LAYOUT = """
        {{TITLE}}

        {{INTRO}}

        {{CODE_LABEL}}

            {{CODE}}

        {{TTL_MSG}}

        {{FOOTER}}

        \u2014 {{BRAND}}
        """;
}
