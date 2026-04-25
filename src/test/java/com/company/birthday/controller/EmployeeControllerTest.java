package com.company.birthday.controller;

import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void createEmployeeAllowsWhitespaceOnlyEmailAfterBindingNormalization() throws Exception {
        when(employeeService.createEmployee(any())).thenReturn(
                new EmployeeListResponse(
                        1,
                        1,
                        1,
                        "Information Technology",
                        "Developer",
                        null,
                        LocalDate.of(1995, 5, 20),
                        "Nguyen Van A",
                        "0901234567",
                        null
                )
        );
        when(employeeService.getActiveEmployees(any(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));
        when(employeeService.getAllDepartments()).thenReturn(List.of());

        mockMvc.perform(post("/employees")
                        .param("departmentId", "1")
                        .param("jobTitle", "Developer")
                        .param("fullName", "Nguyen Van A")
                        .param("dateOfBirth", "1995-05-20")
                        .param("phoneNumber", "0901234567")
                        .param("employeeCode", "abc123")
                        .param("email", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).createEmployee(any());
    }
}


