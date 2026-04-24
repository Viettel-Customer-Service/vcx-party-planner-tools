package com.company.birthday.controller;

import com.company.birthday.dto.request.EmployeeFormRequest;
import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.dto.response.PaginationView;
import com.company.birthday.dto.response.UpcomingBirthdayResponse;
import com.company.birthday.service.EmployeeService;
import com.company.birthday.service.exception.DuplicateFieldException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/employees";
    }

    @GetMapping("/employees")
    public String employeeList(
            @PageableDefault(sort = "employeeCode", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        loadEmployeePage(model, pageable);
        if (!model.containsAttribute("openEmployeeModal")) {
            model.addAttribute("openEmployeeModal", false);
        }
        if (!model.containsAttribute("openImportModal")) {
            model.addAttribute("openImportModal", false);
        }
        model.addAttribute("modalMode", ModalMode.CREATE);
        model.addAttribute("employeeFormAction", "/employees");
        model.addAttribute("employeeForm", new EmployeeFormRequest());
        return "employeeList/employee-list";
    }

    @GetMapping("/employees/upcoming-birthdays")
    @ResponseBody
    public List<UpcomingBirthdayResponse> getUpcomingBirthdays() {
        return employeeService.getUpcomingBirthdays();
    }

    @PostMapping("/employees")
    public String createEmployee(
            @PageableDefault(sort = "employeeCode", direction = Sort.Direction.ASC) Pageable pageable,
            @Valid @ModelAttribute("employeeForm") EmployeeFormRequest employeeForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return handleFormError(model, pageable, ModalMode.CREATE, "/employees");
        }

        try {
            employeeService.createEmployee(employeeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công.");
            return "redirect:/employees";
        } catch (DuplicateFieldException ex) {
            bindingResult.rejectValue(ex.getFieldName(), "", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            bindingResult.rejectValue("departmentId", "", ex.getMessage());
        }

        return handleFormError(model, pageable, ModalMode.CREATE, "/employees");
    }

    @PostMapping("/employees/{employeeId}")
    public String updateEmployee(
            @PathVariable Integer employeeId,
            @PageableDefault(sort = "employeeCode", direction = Sort.Direction.ASC) Pageable pageable,
            @Valid @ModelAttribute("employeeForm") EmployeeFormRequest employeeForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return handleFormError(model, pageable, ModalMode.UPDATE, "/employees/" + employeeId);
        }

        try {
            employeeService.updateEmployee(employeeId, employeeForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công.");
            return "redirect:/employees";
        } catch (DuplicateFieldException ex) {
            bindingResult.rejectValue(ex.getFieldName(), "", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            bindingResult.reject("employee.notFound", ex.getMessage());
        }

        return handleFormError(model, pageable, ModalMode.UPDATE, "/employees/" + employeeId);
    }

    @PostMapping("/employees/{employeeId}/delete")
    public String deleteEmployee(
            @PathVariable Integer employeeId,
            Pageable pageable,
            RedirectAttributes redirectAttributes
    ) {
        try {
            employeeService.deleteEmployee(employeeId);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa nhân viên thành công.");
        } catch (EntityNotFoundException ignored) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên không tồn tại hoặc đã bị xóa.");
        }

        redirectAttributes.addAttribute("page", Math.max(pageable.getPageNumber(), 0));
        redirectAttributes.addAttribute("size", Math.max(pageable.getPageSize(), 1));
        return "redirect:/employees";
    }

    @PostMapping("/employees/import")
    public String importEmployees(
            @PageableDefault(sort = "employeeCode", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Map<String, Object> result = employeeService.importEmployee(file);
            Integer successCount = ((Number) result.getOrDefault("success", 0)).intValue();
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) result.getOrDefault("errors", List.of());

            if (successCount > 0) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã nhập thành công " + successCount + " nhân viên.");
            }

            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("importSummary",
                        "Đã nhập " + successCount + " nhân viên, có " + errors.size() + " dòng lỗi.");
                redirectAttributes.addFlashAttribute("importErrors", errors);
                redirectAttributes.addFlashAttribute("openImportModal", true);
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("openImportModal", true);
            redirectAttributes.addFlashAttribute("importErrorMessage", ex.getMessage());
        }

        redirectAttributes.addAttribute("page", Math.max(pageable.getPageNumber(), 0));
        redirectAttributes.addAttribute("size", Math.max(pageable.getPageSize(), 20));
        return "redirect:/employees";
    }

    @GetMapping("/employees/import/template")
    public ResponseEntity<byte[]> downloadEmployeeTemplate() {
        byte[] template = employeeService.downloadEmployeeTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("ThemDanhSachNhanVien.xlsx", StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(template.length);

        return new ResponseEntity<>(template, headers, HttpStatus.OK);
    }

    private void loadEmployeePage(Model model, Pageable pageable) {
        Page<EmployeeListResponse> employeePage = employeeService.getActiveEmployees(pageable);

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("departments", employeeService.getAllDepartments());
        model.addAttribute("pagination", PaginationView.from(employeePage));
        model.addAttribute("baseUrl", "/employees");
    }

    private String handleFormError(Model model, Pageable pageable, ModalMode mode, String formAction) {
        loadEmployeePage(model, pageable);
        model.addAttribute("openEmployeeModal", true);
        model.addAttribute("openImportModal", false);
        model.addAttribute("modalMode", mode);
        model.addAttribute("employeeFormAction", formAction);
        return "employeeList/employee-list";
    }

}
