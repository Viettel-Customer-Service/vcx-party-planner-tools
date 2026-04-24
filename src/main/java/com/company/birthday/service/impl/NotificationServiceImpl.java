package com.company.birthday.service.impl;

import com.company.birthday.entity.Channel;
import com.company.birthday.entity.Employee;
import com.company.birthday.entity.SendStatus;
import com.company.birthday.service.BirthdayLogService;
import com.company.birthday.service.EmailService;
import com.company.birthday.service.MessageComposerService;
import com.company.birthday.service.NotificationService;
import com.company.birthday.service.WhatsAppGreenApiService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final WhatsAppGreenApiService whatsAppGreenApiService;
    private final EmailService emailService;
    private final BirthdayLogService birthdayLogService;
    private final MessageComposerService messageComposerService;
    private final boolean whatsappEnabled;
    private final boolean emailEnabled;
    private final String whatsappGroupId;

    public NotificationServiceImpl(WhatsAppGreenApiService whatsAppGreenApiService,
                                   EmailService emailService,
                                   BirthdayLogService birthdayLogService,
                                   MessageComposerService messageComposerService,
                                   @Value("${birthday.notification.whatsapp.enabled:true}") boolean whatsappEnabled,
                                   @Value("${birthday.notification.email.enabled:true}") boolean emailEnabled,
                                   @Value("${birthday.notification.whatsapp.group-id:}") String whatsappGroupId) {
        this.whatsAppGreenApiService = whatsAppGreenApiService;
        this.emailService = emailService;
        this.birthdayLogService = birthdayLogService;
        this.messageComposerService = messageComposerService;
        this.whatsappEnabled = whatsappEnabled;
        this.emailEnabled = emailEnabled;
        this.whatsappGroupId = whatsappGroupId;
    }

    @Override
    @Async("birthdayTaskExecutor")
    public void sendBirthdayMessage(Employee employee, Integer messageId) {
        String email = employee.getEmail();
        String fullName = employee.getFullName();
        LocalDate dateOfBirth = employee.getDateOfBirth();
        String jobTitle = employee.getJobTitle();

        String messageContent = null;
        RuntimeException composeException = null;
        if ((whatsappEnabled && whatsappGroupId != null && !whatsappGroupId.isBlank())
                || (emailEnabled && email != null && !email.isBlank())) {
            try {
                messageContent = messageComposerService.composeBirthdayMessage(messageId, fullName, dateOfBirth, jobTitle);
            } catch (RuntimeException ex) {
                composeException = ex;
            }
        }

        if (whatsappEnabled && whatsappGroupId != null && !whatsappGroupId.isBlank()) {
            OffsetDateTime sendTime = OffsetDateTime.now();
            if (composeException != null) {
                birthdayLogService.saveLog(employee, Channel.WHATSAPP, SendStatus.FAILED, sendTime, null, composeException.getMessage());
                log.warn("Cannot compose message for WhatsApp groupId={}", whatsappGroupId, composeException);
            } else {
            try {
                whatsAppGreenApiService.sendGroupMessage(whatsappGroupId, messageContent);
                    birthdayLogService.saveLog(employee, Channel.WHATSAPP, SendStatus.SUCCESS, sendTime, messageContent, null);
            } catch (RuntimeException ex) {
                    birthdayLogService.saveLog(employee, Channel.WHATSAPP, SendStatus.FAILED, sendTime, messageContent, ex.getMessage());
                log.warn("WhatsApp birthday notification failed for groupId={}", whatsappGroupId, ex);
            }
            }
        }

        if (emailEnabled && email != null && !email.isBlank()) {
            OffsetDateTime sendTime = OffsetDateTime.now();
            if (composeException != null) {
                birthdayLogService.saveLog(employee, Channel.EMAIL, SendStatus.FAILED, sendTime, null, composeException.getMessage());
                log.warn("Cannot compose message for email={}", email, composeException);
            } else {
            try {
                emailService.sendBirthdayMessage(email, messageContent);
                    birthdayLogService.saveLog(employee, Channel.EMAIL, SendStatus.SUCCESS, sendTime, messageContent, null);
            } catch (RuntimeException ex) {
                    birthdayLogService.saveLog(employee, Channel.EMAIL, SendStatus.FAILED, sendTime, messageContent, ex.getMessage());
                log.warn("Email birthday notification failed for email={}", email, ex);
            }
            }
        }
    }
}

