package com.company.birthday.service.impl;

import com.company.birthday.dto.response.BirthdayLogListResponse;
import com.company.birthday.entity.BirthdayLog;
import com.company.birthday.entity.Channel;
import com.company.birthday.entity.Employee;
import com.company.birthday.entity.SendStatus;
import com.company.birthday.repository.BirthdayLogRepository;
import com.company.birthday.service.BirthdayLogService;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BirthdayLogServiceImpl implements BirthdayLogService {

    private final BirthdayLogRepository birthdayLogRepository;

    public BirthdayLogServiceImpl(BirthdayLogRepository birthdayLogRepository) {
        this.birthdayLogRepository = birthdayLogRepository;
    }

    @Override
    public Page<BirthdayLogListResponse> getLogs(Pageable pageable) {
        Page<BirthdayLog> logPage = birthdayLogRepository.findAllByOrderBySendTimeDesc(pageable);
        AtomicInteger rowNumber = new AtomicInteger((int) pageable.getOffset() + 1);

        return logPage.map(log -> new BirthdayLogListResponse(
                log.getLogId(),
                rowNumber.getAndIncrement(),
                log.getEmployee().getFullName(),
                log.getChannel(),
                log.getStatus(),
                log.getSendTime(),
                log.getMessage(),
                log.getErrorMessage()
        ));
    }

    @Override
    public void saveLog(Employee employee,
                        Channel channel,
                        SendStatus status,
                        OffsetDateTime sendTime,
                        String message,
                        String errorMessage) {
        BirthdayLog birthdayLog = new BirthdayLog();
        birthdayLog.setEmployee(employee);
        birthdayLog.setChannel(channel);
        birthdayLog.setStatus(status);
        birthdayLog.setSendTime(sendTime != null ? sendTime : OffsetDateTime.now());
        birthdayLog.setMessage(message);
        birthdayLog.setErrorMessage(errorMessage);

        birthdayLogRepository.save(birthdayLog);
    }
}

