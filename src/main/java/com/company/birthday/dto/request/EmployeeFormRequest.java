package com.company.birthday.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class EmployeeFormRequest {

    @NotNull(message = "Vui long chon phong.")
    private Integer departmentId;

    @NotBlank(message = "Vui long nhap chuc danh.")
    private String jobTitle;

    @NotBlank(message = "Vui long nhap ma nhan vien.")
    private String employeeCode;

    @NotBlank(message = "Vui long nhap ho va ten.")
    private String fullName;

    @NotNull(message = "Vui long chon ngay sinh.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Vui long nhap so dien thoai.")
    private String phoneNumber;

    @Email(message = "Email khong dung dinh dang.")
    private String email;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        if (employeeCode == null) {
            this.employeeCode = null;
            return;
        }

        String normalized = employeeCode.trim();
        this.employeeCode = normalized.isBlank() ? null : normalized;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null) {
            this.email = null;
            return;
        }

        String normalized = email.trim();
        this.email = normalized.isBlank() ? null : normalized;
    }
}

