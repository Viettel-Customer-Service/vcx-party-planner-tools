package com.company.birthday.controller;

import com.company.birthday.dto.request.BirthdayConfigRequest;
import com.company.birthday.dto.response.BirthdayConfigResponse;
import com.company.birthday.service.BirthdayConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config/birthday")
public class BirthdayConfigController {

    private final BirthdayConfigService birthdayConfigService;

    public BirthdayConfigController(BirthdayConfigService birthdayConfigService) {
        this.birthdayConfigService = birthdayConfigService;
    }

    @GetMapping
    public BirthdayConfigResponse getBirthdayConfig() {
        return birthdayConfigService.getBirthdayConfig();
    }

    @PostMapping
    public BirthdayConfigResponse saveBirthdayConfig(@Valid @RequestBody BirthdayConfigRequest request) {
        return birthdayConfigService.saveBirthdayConfig(request);
    }
}

