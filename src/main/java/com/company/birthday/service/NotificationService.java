package com.company.birthday.service;

import com.company.birthday.entity.Employee;

public interface NotificationService {
    void sendBirthdayMessage(Employee employee, Integer messageId);
}

