package com.company.birthday.scheduler;

import com.company.birthday.dto.response.BirthdayConfigResponse;
import com.company.birthday.service.BirthdayConfigService;
import com.company.birthday.service.BirthdayService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BirthdaySchedulerTest {

    @Test
    void doesNotRunWhenDisabled() {
        BirthdayService birthdayService = mock(BirthdayService.class);
        BirthdayConfigService configService = mock(BirthdayConfigService.class);
        when(configService.getBirthdayConfig()).thenReturn(new BirthdayConfigResponse(false, 8, 30));

        BirthdayScheduler scheduler = new BirthdayScheduler(birthdayService, configService, 1);

        scheduler.sendBirthdayMessageDaily();

        verify(birthdayService, never()).handleTodayBirthday(1);
    }
}

