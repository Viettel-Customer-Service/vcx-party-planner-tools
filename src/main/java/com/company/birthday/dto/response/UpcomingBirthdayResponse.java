package com.company.birthday.dto.response;

import java.time.LocalDate;

public record UpcomingBirthdayResponse(
        int index,
        String title,
        String fullName,
        LocalDate dateOfBirth,
        String status
) {
}
