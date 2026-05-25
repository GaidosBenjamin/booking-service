package com.bgaidos.booking.sms;

import com.bgaidos.booking.api.sms.BroadcastSmsResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.repo.CamperRepository;
import com.bgaidos.booking.repo.UserProfileRepository;
import com.bgaidos.booking.util.LocaleResolver;
import com.bgaidos.booking.util.PhoneNumbers;
import com.bgaidos.booking.util.SmsTemplates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SmsBroadcastService {

    private static final List<CamperStatus> UNPAID = List.of(
        CamperStatus.NEEDS_BED,
        CamperStatus.NEEDS_PAYMENT,
        CamperStatus.PAYMENT_FAILED);

    private final UserProfileRepository userProfileRepository;
    private final CamperRepository camperRepository;
    private final SmsSender smsSender;
    private final SmsTemplates smsTemplates;
    private final CurrentUser currentUser;
    private final TaskExecutor taskExecutor;

    @Value("${app.sms.default-country-code}")
    private String defaultCountryCode;

    @Value("${app.mail.brand-name}")
    private String brandName;

    public SmsBroadcastService(
        UserProfileRepository userProfileRepository,
        CamperRepository camperRepository,
        SmsSender smsSender,
        SmsTemplates smsTemplates,
        CurrentUser currentUser,
        @Qualifier("authMailExecutor") TaskExecutor taskExecutor
    ) {
        this.userProfileRepository = userProfileRepository;
        this.camperRepository = camperRepository;
        this.smsSender = smsSender;
        this.smsTemplates = smsTemplates;
        this.currentUser = currentUser;
        this.taskExecutor = taskExecutor;
    }

    @Transactional(readOnly = true)
    public BroadcastSmsResponse broadcast(String textEn, String textRo) {
        var tenantId = currentUser.tenantId();
        var profiles = userProfileRepository.findProfilesOfParentsWithCampers(tenantId, CamperStatus.PAYMENT_SUCCESS);
        var queued = 0;
        for (var profile : profiles) {
            var e164Opt = PhoneNumbers.toE164(profile.getPhone(), defaultCountryCode);
            if (e164Opt.isEmpty()) {
                log.warn("skipping broadcast SMS: malformed phone for user={}", profile.getUser().getId());
                continue;
            }
            var e164 = e164Opt.get();
            var text = "en".equals(profile.getPreferredLocale()) ? textEn : textRo;
            taskExecutor.execute(() -> smsSender.send(e164, text));
            queued++;
        }
        return new BroadcastSmsResponse(queued);
    }

    @Transactional(readOnly = true)
    public BroadcastSmsResponse broadcastPaymentReminder() {
        var tenantId = currentUser.tenantId();
        var profiles = userProfileRepository.findProfilesOfParentsWithCampersIn(tenantId, UNPAID);
        var queued = 0;
        for (var profile : profiles) {
            var e164Opt = PhoneNumbers.toE164(profile.getPhone(), defaultCountryCode);
            if (e164Opt.isEmpty()) {
                log.warn("skipping payment reminder SMS: malformed phone for user={}", profile.getUser().getId());
                continue;
            }
            var e164 = e164Opt.get();
            var campers = camperRepository.findByParentAndStatuses(profile.getUser().getId(), tenantId, UNPAID);
            if (campers.isEmpty()) continue;
            var names = campers.stream().map(c -> c.getFirstName() + " " + c.getLastName()).toList();
            var locale = LocaleResolver.resolve(profile.getPreferredLocale());
            var text = smsTemplates.paymentReminder(locale, brandName, names);
            taskExecutor.execute(() -> smsSender.send(e164, text));
            queued++;
        }
        log.info("payment reminder SMS broadcast tenant={} queued={}", tenantId, queued);
        return new BroadcastSmsResponse(queued);
    }
}
