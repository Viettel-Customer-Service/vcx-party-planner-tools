package com.company.birthday.service;

import com.company.birthday.dto.response.BirthdayLogListResponse;
import com.company.birthday.entity.Channel;
import com.company.birthday.entity.Employee;
import com.company.birthday.entity.SendStatus;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BirthdayLogService {

    Page<BirthdayLogListResponse> getLogs(Pageable pageable);

    void saveLog(Employee employee,
                 Channel channel,
                 SendStatus status,
                 OffsetDateTime sendTime,
                 String message,
                 String errorMessage);
}

