package com.company.birthday.service;

import java.time.LocalDate;

public interface GeminiClientService {
    String generateBirthdayMessage(String fullName, LocalDate dateOfBirth, String jobTitle, String fallbackMessage);
}

