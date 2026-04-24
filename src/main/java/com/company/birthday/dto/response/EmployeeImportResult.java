package com.company.birthday.dto.response;

import java.util.List;

public record EmployeeImportResult(int successCount, List<String> errors) {
}

