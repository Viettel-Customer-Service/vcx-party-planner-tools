package com.company.birthday.service.impl;

import com.company.birthday.entity.Channel;
import com.company.birthday.entity.Employee;
import com.company.birthday.entity.SendStatus;
import com.company.birthday.service.BirthdayLogService;
import com.company.birthday.service.EmailService;
import com.company.birthday.service.MessageComposerService;
import com.company.birthday.service.WhatsAppGreenApiService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    private Employee createEmployee() {
        Employee employee = new Employee();
        employee.setPhoneNumber("+84901122334");
        employee.setEmail("john@example.com");
        employee.setFullName("John");
        employee.setJobTitle("Dev");
        employee.setDateOfBirth(LocalDate.of(1995, 5, 20));
        return employee;
    }

    @Test
    void sendsBothChannelsWhenEnabledAndDestinationExists() {
        Employee employee = createEmployee();
        WhatsAppGreenApiService whatsAppGreenApiService = mock(WhatsAppGreenApiService.class);
        EmailService emailService = mock(EmailService.class);
        BirthdayLogService birthdayLogService = mock(BirthdayLogService.class);
        MessageComposerService messageComposerService = mock(MessageComposerService.class);
        NotificationServiceImpl notificationService = new NotificationServiceImpl(
                whatsAppGreenApiService,
                emailService,
                birthdayLogService,
                messageComposerService,
                true,
                true,
                "test-group@g.us"
        );
        when(messageComposerService.composeBirthdayMessage(1, "John", LocalDate.of(1995, 5, 20), "Dev"))
                .thenReturn("Happy birthday, John");

        notificationService.sendBirthdayMessage(employee, 1);

        verify(whatsAppGreenApiService).sendGroupMessage("test-group@g.us", "Happy birthday, John");
        verify(emailService).sendBirthdayMessage("john@example.com", "Happy birthday, John");
        verify(birthdayLogService).saveLog(eq(employee), eq(Channel.WHATSAPP), eq(SendStatus.SUCCESS), any(), eq("Happy birthday, John"), eq(null));
        verify(birthdayLogService).saveLog(eq(employee), eq(Channel.EMAIL), eq(SendStatus.SUCCESS), any(), eq("Happy birthday, John"), eq(null));
    }

    @Test
    void skipsDisabledOrMissingDestinationChannels() {
        Employee employee = new Employee();
        employee.setPhoneNumber("");
        employee.setEmail("");
        employee.setFullName("John");
        employee.setJobTitle("Dev");
        WhatsAppGreenApiService whatsAppGreenApiService = mock(WhatsAppGreenApiService.class);
        EmailService emailService = mock(EmailService.class);
        BirthdayLogService birthdayLogService = mock(BirthdayLogService.class);
        MessageComposerService messageComposerService = mock(MessageComposerService.class);
        NotificationServiceImpl notificationService = new NotificationServiceImpl(
                whatsAppGreenApiService,
                emailService,
                birthdayLogService,
                messageComposerService,
                false,
                true,
                "test-group@g.us"
        );

        notificationService.sendBirthdayMessage(employee, 1);

        verify(whatsAppGreenApiService, never()).sendGroupMessage("test-group@g.us", "Happy birthday, John");
        verify(emailService, never()).sendBirthdayMessage("", "Happy birthday, John");
        verify(birthdayLogService, never()).saveLog(any(), any(), any(), any(), any(), any());
    }

    @Test
    void continuesSendingOtherChannelsWhenOneChannelFails() {
        Employee employee = createEmployee();
        WhatsAppGreenApiService whatsAppGreenApiService = mock(WhatsAppGreenApiService.class);
        EmailService emailService = mock(EmailService.class);
        BirthdayLogService birthdayLogService = mock(BirthdayLogService.class);
        MessageComposerService messageComposerService = mock(MessageComposerService.class);
        NotificationServiceImpl notificationService = new NotificationServiceImpl(
                whatsAppGreenApiService,
                emailService,
                birthdayLogService,
                messageComposerService,
                true,
                true,
                "test-group@g.us"
        );
        when(messageComposerService.composeBirthdayMessage(1, "John", LocalDate.of(1995, 5, 20), "Dev"))
                .thenReturn("Happy birthday, John");

        doThrow(new RuntimeException("green api down"))
                .when(whatsAppGreenApiService)
                .sendGroupMessage("test-group@g.us", "Happy birthday, John");

        notificationService.sendBirthdayMessage(employee, 1);

        verify(emailService).sendBirthdayMessage("john@example.com", "Happy birthday, John");
        verify(birthdayLogService).saveLog(eq(employee), eq(Channel.WHATSAPP), eq(SendStatus.FAILED), any(), eq("Happy birthday, John"), eq("green api down"));
        verify(birthdayLogService).saveLog(eq(employee), eq(Channel.EMAIL), eq(SendStatus.SUCCESS), any(), eq("Happy birthday, John"), eq(null));
    }
}

