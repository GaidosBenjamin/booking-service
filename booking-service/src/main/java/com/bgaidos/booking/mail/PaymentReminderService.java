package com.bgaidos.booking.mail;

import com.bgaidos.booking.api.mail.PaymentReminderBroadcastResponse;
import com.bgaidos.booking.auth.service.session.CurrentUser;
import com.bgaidos.booking.entity.CamperStatus;
import com.bgaidos.booking.repo.CamperRepository;
import com.bgaidos.booking.repo.UserProfileRepository;
import com.bgaidos.booking.util.LocaleResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PaymentReminderService {

    private static final List<CamperStatus> UNPAID = List.of(
        CamperStatus.NEEDS_BED,
        CamperStatus.NEEDS_PAYMENT,
        CamperStatus.PAYMENT_FAILED);

    private final UserProfileRepository userProfileRepository;
    private final CamperRepository camperRepository;
    private final AuthMailer authMailer;
    private final CurrentUser currentUser;
    private final TaskExecutor taskExecutor;

    public PaymentReminderService(
        UserProfileRepository userProfileRepository,
        CamperRepository camperRepository,
        AuthMailer authMailer,
        CurrentUser currentUser,
        @Qualifier("authMailExecutor") TaskExecutor taskExecutor
    ) {
        this.userProfileRepository = userProfileRepository;
        this.camperRepository = camperRepository;
        this.authMailer = authMailer;
        this.currentUser = currentUser;
        this.taskExecutor = taskExecutor;
    }

    @Transactional(readOnly = true)
    public PaymentReminderBroadcastResponse broadcast() {
        var tenantId = currentUser.tenantId();
        var profiles = userProfileRepository.findProfilesOfParentsWithCampersIn(tenantId, UNPAID);
        var queued = 0;
        for (var profile : profiles) {
            var user = profile.getUser();
            var email = user.getEmail();
            if (email == null || email.isBlank()) {
                log.warn("skipping payment reminder: no email for user={}", user.getId());
                continue;
            }
            var campers = camperRepository.findByParentAndStatuses(user.getId(), tenantId, UNPAID);
            if (campers.isEmpty()) continue;
            var names = campers.stream()
                .map(c -> c.getFirstName() + " " + c.getLastName())
                .toList();
            var locale = LocaleResolver.resolve(profile.getPreferredLocale());
            taskExecutor.execute(() -> {
                try {
                    authMailer.sendPaymentReminder(email, names, locale);
                } catch (RuntimeException ex) {
                    log.warn("failed to send payment reminder to {}", email, ex);
                }
            });
            queued++;
        }
        log.info("payment reminder broadcast tenant={} queued={}", tenantId, queued);
        return new PaymentReminderBroadcastResponse(queued);
    }
}
