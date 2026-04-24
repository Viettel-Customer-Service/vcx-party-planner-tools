package com.company.birthday.service;

public interface BirthdayService {
    void handleTodayBirthday(Integer messageTemplateId);

    void handleBirthdayForEmployee(Integer employeeId, Integer messageTemplateId);
}
