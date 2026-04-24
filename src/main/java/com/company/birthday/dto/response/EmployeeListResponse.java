package com.company.birthday.dto.response;

import java.time.LocalDate;

public record EmployeeListResponse(
        Integer employeeId,
        int index,
        Integer departmentId,
        String department,
        String title,
        String employeeCode,
        LocalDate dateOfBirth,
        String fullName,
        String phone,
        String email
) {
}



