package com.company.birthday.service.impl;

import com.company.birthday.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String defaultSubject;
    private final String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${birthday.notification.email.subject:Chuc mung sinh nhat}") String defaultSubject,
                            @Value("${spring.mail.username:}") String fromAddress) {
        this.mailSender = mailSender;
        this.defaultSubject = defaultSubject;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendBirthdayMessage(String to, String body) {
        if (to == null || to.isBlank() || body == null || body.isBlank()) {
            return;
        }


        SimpleMailMessage mail = new SimpleMailMessage();
        if (fromAddress != null && !fromAddress.isBlank()) {
            mail.setFrom(fromAddress);
        }
        mail.setTo(to);
        mail.setSubject(defaultSubject);
        mail.setText(body);

        mailSender.send(mail);
    }
}

