package com.company.birthday.dto.response;

import com.company.birthday.entity.Channel;
import com.company.birthday.entity.SendStatus;
import java.time.OffsetDateTime;

public record BirthdayLogListResponse(
        Long logId,
        int index,
        String employeeName,
        Channel channel,
        SendStatus status,
        OffsetDateTime sendTime,
        String message,
        String errorMessage
) {
}

