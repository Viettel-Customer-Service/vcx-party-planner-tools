package com.company.birthday.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.annotations.Check;

@Entity
@Table(
        name = "employee",
        indexes = {
                @Index(name = "idx_employee_birthday", columnList = "birthmonth, birthday")
        }
)
@Check(constraints = "birthday BETWEEN 1 AND 31")
@Check(constraints = "birthmonth BETWEEN 1 AND 12")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeid")
    private Integer employeeId;

    @Column(name = "employeecode", unique = true, length = 50)
    private String employeeCode;

    @Column(name = "fullname", nullable = false, length = 255)
    private String fullName;

    @Column(name = "jobtitle", length = 255)
    private String jobTitle;

    @Column(name = "phonenumber", length = 20)
    private String phoneNumber;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "dateofbirth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "birthday", nullable = false)
    private Integer birthDay;

    @Column(name = "birthmonth", nullable = false)
    private Integer birthMonth;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departmentid", nullable = false)
    private Department department;

    @Column(name = "isactive", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    @PreUpdate
    private void syncBirthFields() {
        if (dateOfBirth != null) {
            this.birthDay = dateOfBirth.getDayOfMonth();
            this.birthMonth = dateOfBirth.getMonthValue();
        }
        if (isActive == null) {
            this.isActive = true;
        }
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(Integer birthDay) {
        this.birthDay = birthDay;
    }

    public Integer getBirthMonth() {
        return birthMonth;
    }

    public void setBirthMonth(Integer birthMonth) {
        this.birthMonth = birthMonth;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
