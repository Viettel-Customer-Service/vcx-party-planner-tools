package com.company.birthday.service.impl;

import com.company.birthday.dto.request.BirthdayConfigRequest;
import com.company.birthday.dto.response.BirthdayConfigResponse;
import com.company.birthday.entity.SystemConfig;
import com.company.birthday.repository.SystemConfigRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BirthdayConfigServiceImplTest {

    @Test
    void returnsFallbackWhenConfigMissing() {
        SystemConfigRepository repository = mock(SystemConfigRepository.class);
        when(repository.findByConfigKey("BIRTHDAY_ENABLED")).thenReturn(Optional.empty());
        when(repository.findByConfigKey("BIRTHDAY_HOUR")).thenReturn(Optional.empty());
        when(repository.findByConfigKey("BIRTHDAY_MINUTE")).thenReturn(Optional.empty());

        BirthdayConfigServiceImpl service = new BirthdayConfigServiceImpl(repository);

        BirthdayConfigResponse response = service.getBirthdayConfig();

        assertTrue(response.isEnabled());
        assertEquals(8, response.getHour());
        assertEquals(0, response.getMinute());
    }

    @Test
    void savesAllBirthdayKeys() {
        SystemConfigRepository repository = mock(SystemConfigRepository.class);
        when(repository.findByConfigKey("BIRTHDAY_ENABLED")).thenReturn(Optional.empty());
        when(repository.findByConfigKey("BIRTHDAY_HOUR")).thenReturn(Optional.empty());
        when(repository.findByConfigKey("BIRTHDAY_MINUTE")).thenReturn(Optional.empty());
        when(repository.save(any(SystemConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BirthdayConfigServiceImpl service = new BirthdayConfigServiceImpl(repository);
        BirthdayConfigRequest request = new BirthdayConfigRequest();
        request.setEnabled(false);
        request.setHour(9);
        request.setMinute(30);

        BirthdayConfigResponse response = service.saveBirthdayConfig(request);

        assertFalse(response.isEnabled());
        assertEquals(9, response.getHour());
        assertEquals(30, response.getMinute());
        verify(repository, times(3)).save(any(SystemConfig.class));
    }
}

