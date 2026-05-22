package com.bgaidos.booking.sms;

import com.bgaidos.booking.api.sms.BroadcastSmsResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.repo.UserProfileRepository;
import com.bgaidos.booking.util.PhoneNumbers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SmsBroadcastService {

    private final UserProfileRepository userProfileRepository;
    private final SmsSender smsSender;
    private final CurrentUser currentUser;
    private final TaskExecutor taskExecutor;

    @Value("${app.sms.default-country-code}")
    private String defaultCountryCode;

    public SmsBroadcastService(
        UserProfileRepository userProfileRepository,
        SmsSender smsSender,
        CurrentUser currentUser,
        @Qualifier("authMailExecutor") TaskExecutor taskExecutor
    ) {
        this.userProfileRepository = userProfileRepository;
        this.smsSender = smsSender;
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
}
