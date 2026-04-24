package com.company.birthday.scheduler;

import com.company.birthday.dto.response.BirthdayConfigResponse;
import com.company.birthday.service.BirthdayService;
import com.company.birthday.service.BirthdayConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
@EnableScheduling
public class BirthdayScheduler {

    private final BirthdayService birthdayService;
    private final BirthdayConfigService birthdayConfigService;
    private final Integer messageTemplateId;
    private static final ZoneId SCHEDULER_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public BirthdayScheduler(BirthdayService birthdayService,
                             BirthdayConfigService birthdayConfigService,
                             @Value("${birthday.scheduler.message-template-id:1}") Integer messageTemplateId) {
        this.birthdayService = birthdayService;
        this.birthdayConfigService = birthdayConfigService;
        this.messageTemplateId = messageTemplateId;
    }

    @Scheduled(cron = "0 * * * * ?", zone = "Asia/Ho_Chi_Minh")
    public void sendBirthdayMessageDaily() {
        BirthdayConfigResponse config = birthdayConfigService.getBirthdayConfig();
        if (!config.isEnabled()) {
            return;
        }

        LocalTime now = LocalTime.now(SCHEDULER_ZONE);
        if (now.getHour() != config.getHour() || now.getMinute() != config.getMinute()) {
            return;
        }

        birthdayService.handleTodayBirthday(messageTemplateId);
    }
}
