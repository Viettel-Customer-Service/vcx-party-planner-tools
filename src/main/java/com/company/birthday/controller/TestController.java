package com.company.birthday.controller;

import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.dto.response.PaginationView;
import com.company.birthday.service.BirthdayService;
import com.company.birthday.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TestController {

    private final EmployeeService employeeService;
    private final BirthdayService birthdayService;
    private final Integer messageTemplateId;

    public TestController(EmployeeService employeeService,
                          BirthdayService birthdayService,
                          @org.springframework.beans.factory.annotation.Value("${birthday.scheduler.message-template-id:1}") Integer messageTemplateId) {
        this.employeeService = employeeService;
        this.birthdayService = birthdayService;
        this.messageTemplateId = messageTemplateId;
    }

    @GetMapping("/test")
    public String test(
            @PageableDefault(sort = "employeeCode", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        Page<EmployeeListResponse> employeePage = employeeService.getActiveEmployees(keyword, pageable);
        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("pagination", PaginationView.from(employeePage));
        model.addAttribute("baseUrl", "/test");
        return "test/test";
    }

    @PostMapping("/test/send/{employeeId}")
    public String sendBirthdayMessage(
            @PathVariable Integer employeeId,
            Pageable pageable,
            RedirectAttributes redirectAttributes
    ) {
        try {
            birthdayService.handleBirthdayForEmployee(employeeId, messageTemplateId);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi lời chúc thành công.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        redirectAttributes.addAttribute("page", Math.max(pageable.getPageNumber(), 0));
        redirectAttributes.addAttribute("size", Math.max(pageable.getPageSize(), 1));
        return "redirect:/test";
    }
}
