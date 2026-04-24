package com.company.birthday.service;

import com.company.birthday.dto.request.BirthdayConfigRequest;
import com.company.birthday.dto.response.BirthdayConfigResponse;

public interface BirthdayConfigService {

    BirthdayConfigResponse getBirthdayConfig();

    BirthdayConfigResponse saveBirthdayConfig(BirthdayConfigRequest request);
}

