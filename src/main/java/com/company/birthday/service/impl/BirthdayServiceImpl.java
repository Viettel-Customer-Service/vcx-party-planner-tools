package com.company.birthday.service.impl;

import com.company.birthday.entity.Employee;
import com.company.birthday.repository.EmployeeRepository;
import com.company.birthday.service.BirthdayService;
import com.company.birthday.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BirthdayServiceImpl implements BirthdayService {

    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public BirthdayServiceImpl(EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void handleTodayBirthday(Integer messageTemplateId) {
        List<Employee> list = employeeRepository.findByTodayBirthday();

        for (Employee e : list) {
            notificationService.sendBirthdayMessage(e, messageTemplateId);
        }
    }

    @Override
    public void handleBirthdayForEmployee(Integer employeeId, Integer messageTemplateId) {
        Employee employee = employeeRepository.findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Nhan vien khong ton tai hoac da bi khoa."));
        notificationService.sendBirthdayMessage(employee, messageTemplateId);
    }
}
