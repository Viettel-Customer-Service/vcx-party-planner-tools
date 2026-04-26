package com.company.birthday.service.impl;

import com.company.birthday.entity.MessageTemplate;
import com.company.birthday.repository.MessageTemplateRepository;
import com.company.birthday.service.GeminiClientService;
import com.company.birthday.service.MessageComposerService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class MessageComposerServiceImpl implements MessageComposerService {

    private static final Integer DEFAULT_FALLBACK_MESSAGE_ID = 1;

    private static final Logger log = LoggerFactory.getLogger(MessageComposerServiceImpl.class);

    private final MessageTemplateRepository messageTemplateRepository;
    private final GeminiClientService geminiClientService;

    public MessageComposerServiceImpl(MessageTemplateRepository messageTemplateRepository,
                                      GeminiClientService geminiClientService) {
        this.messageTemplateRepository = messageTemplateRepository;
        this.geminiClientService = geminiClientService;
    }

    @Override
    public String composeBirthdayMessage(Integer messageId, String fullName, LocalDate dateOfBirth, String jobTitle) {
        MessageTemplate messageTemplate = messageTemplateRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + messageId));

        String fallbackMessage = fillTemplate(messageTemplate.getContent(), fullName, jobTitle);

        try {
            String aiMessage = geminiClientService.generateBirthdayMessage(fullName, dateOfBirth, jobTitle, fallbackMessage);
            if (aiMessage == null || aiMessage.isBlank()) {
                return getDefaultFallbackMessage(fullName, jobTitle);
            }
            
            aiMessage  = "Chúc mừng sinh nhật " + fullName + "!\n\n" + aiMessage + "\n\n" + "From TTCN&KT With Love <3";
            return normalize(aiMessage);
        } catch (RuntimeException ex) {
            log.warn("Gemini unavailable, fallback to DB template. messageId={}, fallbackMessageId={}", messageId, DEFAULT_FALLBACK_MESSAGE_ID, ex);
            return getDefaultFallbackMessage(fullName, jobTitle);
        }
    }

    private String getDefaultFallbackMessage(String fullName, String jobTitle) {
        MessageTemplate fallbackTemplate = messageTemplateRepository.findById(DEFAULT_FALLBACK_MESSAGE_ID)
                .orElseThrow(() -> new EntityNotFoundException("Message template not found with id: " + DEFAULT_FALLBACK_MESSAGE_ID));
        return fillTemplate(fallbackTemplate.getContent(), fullName, jobTitle);
    }

    private String fillTemplate(String template, String fullName, String jobTitle) {
        String safeFullName = fullName == null ? "" : fullName.trim();
        String safeJobTitle = jobTitle == null ? "" : jobTitle.trim();

        return normalize(template
                .replace("{fullName}", safeFullName)
                .replace("{jobTitle}", safeJobTitle)
        );
    }

    private String normalize(String content) {
        return content.replace("\r\n", "\n")
                .replace("\r", "\n");
    }
}

