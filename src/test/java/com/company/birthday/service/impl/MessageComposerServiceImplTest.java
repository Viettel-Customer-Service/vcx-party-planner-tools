package com.company.birthday.service.impl;

import com.company.birthday.entity.MessageTemplate;
import com.company.birthday.entity.MessageTemplateType;
import com.company.birthday.repository.MessageTemplateRepository;
import com.company.birthday.service.GeminiClientService;
import com.company.birthday.service.exception.GeminiTimeoutException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageComposerServiceImplTest {

    @Test
    void returnsGeminiMessageWhenGeminiSucceeds() {
        MessageTemplateRepository repository = mock(MessageTemplateRepository.class);
        GeminiClientService geminiClientService = mock(GeminiClientService.class);
        MessageComposerServiceImpl service = new MessageComposerServiceImpl(repository, geminiClientService);

        MessageTemplate template = new MessageTemplate();
        template.setId(1);
        template.setType(MessageTemplateType.BIRTHDAY);
        template.setContent("Chuc mung sinh nhat {fullName} - {jobTitle}");

        when(repository.findById(1)).thenReturn(Optional.of(template));
        when(geminiClientService.generateBirthdayMessage(eq("John"), eq(LocalDate.of(1995, 5, 20)), eq("Dev"), anyString()))
                .thenReturn("Happy birthday from Gemini");

        String result = service.composeBirthdayMessage(1, "John", LocalDate.of(1995, 5, 20), "Dev");

        assertEquals("Happy birthday from Gemini", result);
    }

    @Test
    void fallsBackToTemplateWhenGeminiTimeoutsAfterRetries() {
        MessageTemplateRepository repository = mock(MessageTemplateRepository.class);
        GeminiClientService geminiClientService = mock(GeminiClientService.class);
        MessageComposerServiceImpl service = new MessageComposerServiceImpl(repository, geminiClientService);

        MessageTemplate template = new MessageTemplate();
        template.setId(1);
        template.setType(MessageTemplateType.BIRTHDAY);
        template.setContent("Chuc mung sinh nhat {fullName} - {jobTitle}");

        when(repository.findById(1)).thenReturn(Optional.of(template));
        when(geminiClientService.generateBirthdayMessage(eq("John"), eq(LocalDate.of(1995, 5, 20)), eq("Dev"), anyString()))
                .thenThrow(new GeminiTimeoutException("Gemini timeout", null));

        String result = service.composeBirthdayMessage(1, "John", LocalDate.of(1995, 5, 20), "Dev");

        assertEquals("Chuc mung sinh nhat John - Dev", result);
    }

    @Test
    void fallsBackToDefaultTemplateWhenGeminiTimeoutsAndRequestedTemplateIsDifferent() {
        MessageTemplateRepository repository = mock(MessageTemplateRepository.class);
        GeminiClientService geminiClientService = mock(GeminiClientService.class);
        MessageComposerServiceImpl service = new MessageComposerServiceImpl(repository, geminiClientService);

        MessageTemplate requestedTemplate = new MessageTemplate();
        requestedTemplate.setId(2);
        requestedTemplate.setType(MessageTemplateType.BIRTHDAY);
        requestedTemplate.setContent("Template 2 {fullName} - {jobTitle}");

        MessageTemplate defaultTemplate = new MessageTemplate();
        defaultTemplate.setId(1);
        defaultTemplate.setType(MessageTemplateType.BIRTHDAY);
        defaultTemplate.setContent("Chuc mung sinh nhat {fullName} - {jobTitle}");

        when(repository.findById(2)).thenReturn(Optional.of(requestedTemplate));
        when(repository.findById(1)).thenReturn(Optional.of(defaultTemplate));
        when(geminiClientService.generateBirthdayMessage(eq("John"), eq(LocalDate.of(1995, 5, 20)), eq("Dev"), anyString()))
                .thenThrow(new GeminiTimeoutException("Gemini timeout", null));

        String result = service.composeBirthdayMessage(2, "John", LocalDate.of(1995, 5, 20), "Dev");

        assertEquals("Chuc mung sinh nhat John - Dev", result);
    }

    @Test
    void fallsBackToDefaultTemplateWhenGeminiThrowsIllegalState() {
        MessageTemplateRepository repository = mock(MessageTemplateRepository.class);
        GeminiClientService geminiClientService = mock(GeminiClientService.class);
        MessageComposerServiceImpl service = new MessageComposerServiceImpl(repository, geminiClientService);

        MessageTemplate requestedTemplate = new MessageTemplate();
        requestedTemplate.setId(2);
        requestedTemplate.setType(MessageTemplateType.BIRTHDAY);
        requestedTemplate.setContent("Template 2 {fullName} - {jobTitle}");

        MessageTemplate defaultTemplate = new MessageTemplate();
        defaultTemplate.setId(1);
        defaultTemplate.setType(MessageTemplateType.BIRTHDAY);
        defaultTemplate.setContent("Chuc mung sinh nhat {fullName} - {jobTitle}");

        when(repository.findById(2)).thenReturn(Optional.of(requestedTemplate));
        when(repository.findById(1)).thenReturn(Optional.of(defaultTemplate));
        when(geminiClientService.generateBirthdayMessage(eq("John"), eq(LocalDate.of(1995, 5, 20)), eq("Dev"), anyString()))
                .thenThrow(new IllegalStateException("GEMINI_API_KEY is missing."));

        String result = service.composeBirthdayMessage(2, "John", LocalDate.of(1995, 5, 20), "Dev");

        assertEquals("Chuc mung sinh nhat John - Dev", result);
    }
}

