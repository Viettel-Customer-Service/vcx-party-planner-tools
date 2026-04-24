package com.company.birthday.controller;

import com.company.birthday.dto.response.BirthdayLogListResponse;
import com.company.birthday.dto.response.PaginationView;
import com.company.birthday.service.BirthdayLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BirthdayLogController {

    private final BirthdayLogService birthdayLogService;

    public BirthdayLogController(BirthdayLogService birthdayLogService) {
        this.birthdayLogService = birthdayLogService;
    }

    @GetMapping("/logs")
    public String logList(
            @PageableDefault(sort = "sendTime", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<BirthdayLogListResponse> logPage = birthdayLogService.getLogs(pageable);

        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("pagination", PaginationView.from(logPage));
        model.addAttribute("baseUrl", "/logs");
        return "logList/log-list";
    }
}

