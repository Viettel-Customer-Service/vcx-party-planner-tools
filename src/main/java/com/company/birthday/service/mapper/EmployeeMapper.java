package com.company.birthday.service.mapper;

import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeListResponse toListResponse(Employee employee, int index) {
        String departmentName = employee.getDepartment() != null
                ? employee.getDepartment().getDepartmentName()
                : "";

        Integer departmentId = employee.getDepartment() != null
                ? employee.getDepartment().getDepartmentId()
                : null;

        return new EmployeeListResponse(
                employee.getEmployeeId(),
                index,
                departmentId,
                departmentName,
                valueOrEmpty(employee.getJobTitle()),
                valueOrEmpty(employee.getEmployeeCode()),
                employee.getDateOfBirth(),
                valueOrEmpty(employee.getFullName()),
                valueOrEmpty(employee.getPhoneNumber()),
                valueOrEmpty(employee.getEmail())
        );
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}

