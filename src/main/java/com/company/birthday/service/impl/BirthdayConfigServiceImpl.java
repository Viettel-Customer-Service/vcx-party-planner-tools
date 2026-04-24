package com.company.birthday.service.impl;

import com.company.birthday.dto.request.BirthdayConfigRequest;
import com.company.birthday.dto.response.BirthdayConfigResponse;
import com.company.birthday.entity.SystemConfig;
import com.company.birthday.repository.SystemConfigRepository;
import com.company.birthday.service.BirthdayConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BirthdayConfigServiceImpl implements BirthdayConfigService {

    private static final Logger log = LoggerFactory.getLogger(BirthdayConfigServiceImpl.class);

    private static final String KEY_ENABLED = "BIRTHDAY_ENABLED";
    private static final String KEY_HOUR = "BIRTHDAY_HOUR";
    private static final String KEY_MINUTE = "BIRTHDAY_MINUTE";

    private static final String DEFAULT_ENABLED = "true";
    private static final String DEFAULT_HOUR = "8";
    private static final String DEFAULT_MINUTE = "0";
    private static final boolean DEFAULT_ENABLED_BOOL = true;
    private static final int DEFAULT_HOUR_INT = 8;
    private static final int DEFAULT_MINUTE_INT = 0;

    private final SystemConfigRepository systemConfigRepository;

    public BirthdayConfigServiceImpl(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BirthdayConfigResponse getBirthdayConfig() {
        boolean enabled = parseBoolean(getConfigValue(KEY_ENABLED, DEFAULT_ENABLED), DEFAULT_ENABLED_BOOL);
        int hour = parseInteger(KEY_HOUR, getConfigValue(KEY_HOUR, DEFAULT_HOUR), DEFAULT_HOUR_INT);
        int minute = parseInteger(KEY_MINUTE, getConfigValue(KEY_MINUTE, DEFAULT_MINUTE), DEFAULT_MINUTE_INT);
        return new BirthdayConfigResponse(enabled, hour, minute);
    }

    @Override
    @Transactional
    public BirthdayConfigResponse saveBirthdayConfig(BirthdayConfigRequest request) {
        upsertConfig(KEY_ENABLED, String.valueOf(request.getEnabled()), "Bat/tat gui sinh nhat");
        upsertConfig(KEY_HOUR, String.valueOf(request.getHour()), "Gio gui sinh nhat");
        upsertConfig(KEY_MINUTE, String.valueOf(request.getMinute()), "Phut gui sinh nhat");
        return new BirthdayConfigResponse(Boolean.TRUE.equals(request.getEnabled()), request.getHour(), request.getMinute());
    }

    private String getConfigValue(String key, String defaultValue) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    private void upsertConfig(String key, String value, String defaultDescription) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key).orElseGet(SystemConfig::new);
        config.setConfigKey(key);
        config.setConfigValue(value);
        if (config.getDescription() == null || config.getDescription().isBlank()) {
            config.setDescription(defaultDescription);
        }
        systemConfigRepository.save(config);
    }

    private boolean parseBoolean(String rawValue, boolean fallback) {
        if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue)) {
            return Boolean.parseBoolean(rawValue);
        }
        log.warn("Invalid boolean config value '{}'. Fallback to {}", rawValue, fallback);
        return fallback;
    }

    private int parseInteger(String key, String rawValue, int fallback) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException ex) {
            log.warn("Invalid integer config for key {}: '{}'. Fallback to {}", key, rawValue, fallback);
            return fallback;
        }
    }
}


