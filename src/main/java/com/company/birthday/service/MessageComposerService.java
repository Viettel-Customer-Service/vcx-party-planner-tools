package com.company.birthday.service;

import java.time.LocalDate;

public interface MessageComposerService {
    String composeBirthdayMessage(Integer messageId, String fullName, LocalDate dateOfBirth, String jobTitle);
}

