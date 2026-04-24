package com.company.birthday.service.impl;

import com.company.birthday.dto.request.EmployeeFormRequest;
import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.entity.Department;
import com.company.birthday.entity.Employee;
import com.company.birthday.repository.DepartmentRepository;
import com.company.birthday.repository.EmployeeRepository;
import com.company.birthday.service.mapper.EmployeeMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department createDepartment() {
        Department department = new Department();
        department.setDepartmentId(1);
        department.setDepartmentCode("IT");
        department.setDepartmentName("Information Technology");
        return department;
    }

    private EmployeeFormRequest createValidRequest() {
        EmployeeFormRequest request = new EmployeeFormRequest();
        request.setDepartmentId(1);
        request.setJobTitle("Developer");
        request.setFullName("Nguyen Van A");
        request.setDateOfBirth(LocalDate.of(1995, 5, 20));
        request.setPhoneNumber("0901234567");
        return request;
    }

    @Test
    void createEmployee_normalizesInvalidEmployeeCodeAndBlankEmailToNull() {
        Department department = createDepartment();
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toListResponse(any(Employee.class), eq(0)))
                .thenReturn(new EmployeeListResponse(10, 0, 1, "Information Technology", "Developer", "", LocalDate.of(1995, 5, 20), "Nguyen Van A", "0901234567", ""));

        EmployeeFormRequest request = createValidRequest();
        request.setEmployeeCode("abc123");
        request.setEmail("   ");

        employeeService.createEmployee(request);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        Employee savedEmployee = employeeCaptor.getValue();

        assertEquals(department, savedEmployee.getDepartment());
        assertEquals("Developer", savedEmployee.getJobTitle());
        assertEquals("Nguyen Van A", savedEmployee.getFullName());
        assertEquals("0901234567", savedEmployee.getPhoneNumber());
        assertEquals(LocalDate.of(1995, 5, 20), savedEmployee.getDateOfBirth());
        assertNull(savedEmployee.getEmployeeCode());
        assertNull(savedEmployee.getEmail());

        verify(employeeRepository, never()).existsByEmployeeCodeIgnoreCase(any());
        verify(employeeRepository, never()).existsByEmailIgnoreCase(any());
    }

    @Test
    void createEmployee_keepsNumericEmployeeCodeAndTrimsEmail() {
        Department department = createDepartment();
        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toListResponse(any(Employee.class), eq(0)))
                .thenReturn(new EmployeeListResponse(10, 0, 1, "Information Technology", "Developer", "12345", LocalDate.of(1995, 5, 20), "Nguyen Van A", "0901234567", "john@example.com"));

        EmployeeFormRequest request = createValidRequest();
        request.setEmployeeCode("12345");
        request.setEmail("  john@example.com  ");

        employeeService.createEmployee(request);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        Employee savedEmployee = employeeCaptor.getValue();

        assertEquals("12345", savedEmployee.getEmployeeCode());
        assertEquals("john@example.com", savedEmployee.getEmail());
    }

    @Test
    void updateEmployee_normalizesInvalidEmployeeCodeAndBlankEmailToNull() {
        Department department = createDepartment();
        Employee existingEmployee = new Employee();
        existingEmployee.setEmployeeId(10);
        existingEmployee.setIsActive(true);

        when(departmentRepository.findById(1)).thenReturn(Optional.of(department));
        when(employeeRepository.findByEmployeeIdAndIsActiveTrue(10)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toListResponse(any(Employee.class), eq(0)))
                .thenReturn(new EmployeeListResponse(10, 0, 1, "Information Technology", "Developer", "", LocalDate.of(1995, 5, 20), "Nguyen Van A", "0901234567", ""));

        EmployeeFormRequest request = createValidRequest();
        request.setEmployeeCode("12a34");
        request.setEmail("\t\n");

        employeeService.updateEmployee(10, request);

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(employeeCaptor.capture());
        Employee savedEmployee = employeeCaptor.getValue();

        assertEquals(department, savedEmployee.getDepartment());
        assertNull(savedEmployee.getEmployeeCode());
        assertNull(savedEmployee.getEmail());
    }

    @Test
    void importEmployee_normalizesInvalidEmployeeCodeAndBlankEmailToNull() throws Exception {
        Department department = createDepartment();
        when(departmentRepository.findByDepartmentCodeIgnoreCase("IT")).thenReturn(Optional.of(department));
        when(employeeRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = createImportFile();

        Map<String, Object> result = employeeService.importEmployee(file);

        assertEquals(1, ((Number) result.get("success")).intValue());
        assertTrue(((List<?>) result.get("errors")).isEmpty());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Employee>> employeesCaptor = ArgumentCaptor.forClass(List.class);
        verify(employeeRepository).saveAll(employeesCaptor.capture());
        List<Employee> savedEmployees = employeesCaptor.getValue();
        assertEquals(1, savedEmployees.size());
        Employee savedEmployee = savedEmployees.get(0);
        assertEquals(department, savedEmployee.getDepartment());
        assertNull(savedEmployee.getEmployeeCode());
        assertNull(savedEmployee.getEmail());
        assertEquals("Developer", savedEmployee.getJobTitle());
        assertEquals("Nguyen Van A", savedEmployee.getFullName());
        assertEquals("0901234567", savedEmployee.getPhoneNumber());
    }

    private MockMultipartFile createImportFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employees");
            for (int i = 0; i < 3; i++) {
                sheet.createRow(i);
            }

            Row row = sheet.createRow(3);
            row.createCell(1).setCellValue("IT");
            row.createCell(2).setCellValue("Developer");
            row.createCell(3).setCellValue("abc123");
            row.createCell(4).setCellValue("Nguyen Van A");
            row.createCell(5).setCellValue("0901234567");
            row.createCell(6).setCellValue("   ");
            row.createCell(7).setCellValue(20);
            row.createCell(8).setCellValue(5);
            row.createCell(9).setCellValue(1995);

            workbook.write(outputStream);
            return new MockMultipartFile(
                    "file",
                    "employees.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    outputStream.toByteArray()
            );
        }
    }
}




