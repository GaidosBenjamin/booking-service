package com.bgaidos.booking.auth.util;

import java.time.Duration;

public final class AuthMailTemplates {

    private AuthMailTemplates() {
    }

    public static MailBody verification(String code, Duration expiresIn, String brandName) {
        return build(
            brandName,
            "Verify your email",
            "Thanks for signing up! Enter the code below to confirm this email address and finish setting up your account.",
            "If you didn't create an account, you can safely ignore this email.",
            code,
            expiresIn);
    }

    public static MailBody passwordReset(String code, Duration expiresIn, String brandName) {
        return build(
            brandName,
            "Reset your password",
            "We received a request to reset the password for your account. Enter the code below to choose a new one.",
            "If you didn't request a reset, you can safely ignore this email — your current password still works.",
            code,
            expiresIn);
    }

    public static String formatDuration(Duration duration) {
        var minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes == 1 ? "1 minute" : minutes + " minutes";
        }
        var hours = duration.toHours();
        if (duration.toMinutes() % 60 == 0) {
            return hours == 1 ? "1 hour" : hours + " hours";
        }
        return minutes + " minutes";
    }

    public record MailBody(String subject, String html, String plainText) {
    }

    private static MailBody build(
        String brandName,
        String title,
        String intro,
        String footer,
        String code,
        Duration expiresIn
    ) {
        var ttl = formatDuration(expiresIn);
        var subject = title + " — " + brandName;
        var html = HTML_LAYOUT
            .replace("{{BRAND}}", escape(brandName))
            .replace("{{PREHEADER}}", escape("Your code is " + code + ", valid for " + ttl + "."))
            .replace("{{TITLE}}", escape(title))
            .replace("{{INTRO}}", escape(intro))
            .replace("{{CODE}}", escape(code))
            .replace("{{TTL}}", escape(ttl))
            .replace("{{FOOTER}}", escape(footer));
        var plain = PLAIN_LAYOUT
            .replace("{{BRAND}}", brandName)
            .replace("{{TITLE}}", title)
            .replace("{{INTRO}}", intro)
            .replace("{{CODE}}", code)
            .replace("{{TTL}}", ttl)
            .replace("{{FOOTER}}", footer);
        return new MailBody(subject, html, plain);
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
        <html lang="en">
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
                      This code expires in {{TTL}}.
                    </td>
                  </tr>
                  <tr>
                    <td style="border-top:1px solid #e5e7eb;padding:20px 40px 28px;color:#9ca3af;font-size:12px;line-height:1.5;">
                      {{FOOTER}}
                    </td>
                  </tr>
                </table>
                <p style="margin:16px 0 0;color:#9ca3af;font-size:12px;">© {{BRAND}}</p>
              </td>
            </tr>
          </table>
        </body>
        </html>
        """;

    private static final String PLAIN_LAYOUT = """
        {{TITLE}}

        {{INTRO}}

        Your code:

            {{CODE}}

        This code expires in {{TTL}}.

        {{FOOTER}}

        — {{BRAND}}
        """;
}
