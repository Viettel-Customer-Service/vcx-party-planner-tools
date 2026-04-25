package com.company.birthday.service.impl;

import com.company.birthday.dto.request.EmployeeFormRequest;
import com.company.birthday.dto.response.EmployeeListResponse;
import com.company.birthday.dto.response.UpcomingBirthdayResponse;
import com.company.birthday.entity.Department;
import com.company.birthday.entity.Employee;
import com.company.birthday.repository.DepartmentRepository;
import com.company.birthday.repository.EmployeeRepository;
import com.company.birthday.service.EmployeeService;
import com.company.birthday.service.exception.DuplicateFieldException;
import com.company.birthday.service.mapper.EmployeeMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


@Slf4j
@Service
public class EmployeeServiceImpl implements EmployeeService {

	private static final Pattern EMPLOYEE_CODE_PATTERN = Pattern.compile("\\d+");
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final EmployeeMapper employeeMapper;

	public EmployeeServiceImpl(
			EmployeeRepository employeeRepository,
			DepartmentRepository departmentRepository,
			EmployeeMapper employeeMapper
	) {
		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
		this.employeeMapper = employeeMapper;
	}

	@Override
	public Page<EmployeeListResponse> getActiveEmployees(Pageable pageable) {
		Page<Employee> employeePage = employeeRepository.findByIsActiveTrue(pageable);
		AtomicInteger rowNumber = new AtomicInteger((int) pageable.getOffset() + 1);
		return employeePage.map(employee -> employeeMapper.toListResponse(employee, rowNumber.getAndIncrement()));
	}

	@Override
	public List<Department> getAllDepartments() {
		return departmentRepository.findAllDepartment();
	}

	@Override
	public List<UpcomingBirthdayResponse> getUpcomingBirthdays() {
		LocalDate today = LocalDate.now();
		LocalDate endDate = today.plusDays(7);
		boolean sameMonth = today.getYear() == endDate.getYear() && today.getMonthValue() == endDate.getMonthValue();
		boolean wrapped = endDate.getYear() > today.getYear();

		List<Employee> employees = employeeRepository.findUpcomingBirthdaysRaw(
				today.getMonthValue(),
				today.getDayOfMonth(),
				endDate.getMonthValue(),
				endDate.getDayOfMonth(),
				sameMonth,
				wrapped
		);

		List<UpcomingBirthdayItem> items = employees.stream()
				.map(employee -> toUpcomingBirthdayItem(employee, today))
				.filter(item -> item.daysUntil >= 0 && item.daysUntil <= 7)
				.sorted(Comparator
						.comparingLong((UpcomingBirthdayItem item) -> item.daysUntil)
						.thenComparing(item -> item.response.fullName(), String.CASE_INSENSITIVE_ORDER))
				.toList();

		List<UpcomingBirthdayResponse> responses = new ArrayList<>(items.size());
		for (int i = 0; i < items.size(); i++) {
			UpcomingBirthdayResponse response = items.get(i).response;
			responses.add(new UpcomingBirthdayResponse(
					i + 1,
					response.title(),
					response.fullName(),
					response.dateOfBirth(),
					response.status()
			));
		}
		log.info("phần tử: ", responses.size());
		return responses;
	}

	@Override
	@Transactional
	public EmployeeListResponse createEmployee(EmployeeFormRequest request) {
		validateUniqueFields(request, null);
		Employee employee = new Employee();
		updateEntity(employee, request);
		Employee savedEmployee = employeeRepository.save(employee);
		return employeeMapper.toListResponse(savedEmployee, 0);
	}

	@Override
	@Transactional
	public EmployeeListResponse updateEmployee(Integer employeeId, EmployeeFormRequest request) {
		validateUniqueFields(request, employeeId);
		Employee employee = employeeRepository.findByEmployeeIdAndIsActiveTrue(employeeId)
				.orElseThrow(() -> new EntityNotFoundException("Nhan vien khong ton tai hoac da bi khoa."));
		updateEntity(employee, request);
		Employee savedEmployee = employeeRepository.save(employee);
		return employeeMapper.toListResponse(savedEmployee, 0);
	}

	@Override
	public void deleteEmployee(Integer employeeId) {
		Employee employee = employeeRepository.findById(employeeId)
				.orElseThrow(() -> new EntityNotFoundException("Nhan vien khong ton tai."));
		employeeRepository.delete(employee);
	}

	@Override
	@Transactional
	public Map<String, Object> importEmployee(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new RuntimeException("File import không được rỗng.");
		}

		List<String> errors = new ArrayList<>();
		List<Employee> employeesToInsert = new ArrayList<>();
		Set<String> seenEmployeeCodes = new HashSet<>();
		Set<String> seenEmails = new HashSet<>();
		DataFormatter formatter = new DataFormatter();

		java.util.function.Function<String, String> normalizeText = value -> {
			if (value == null) {
				return null;
			}
			String normalized = value.trim();
			return normalized.isBlank() ? null : normalized;
		};

		java.util.function.Function<String, Integer> parseInteger = value -> {
			if (value == null || value.isBlank()) {
				return null;
			}
			try {
				return new BigDecimal(value.trim()).intValueExact();
			} catch (Exception ex) {
				return null;
			}
		};

		try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
			if (workbook.getNumberOfSheets() == 0) {
				throw new RuntimeException("File import không có dữ liệu.");
			}

			Sheet sheet = workbook.getSheetAt(0);
			if (sheet == null) {
				throw new RuntimeException("File import không có dữ liệu.");
			}

			boolean hasDataRow = false;
			for (int rowIndex = Math.max(sheet.getFirstRowNum(), 3); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);

				boolean blankRow = true;
				// B-J la du lieu import, cot A(STT) bo qua
				for (int cellIndex = 1; cellIndex <= 9; cellIndex++) {
					String value = row == null
							? null
							: formatter.formatCellValue(row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
					if (value != null && !value.trim().isBlank()) {
						blankRow = false;
						break;
					}
				}
				if (blankRow) {
					continue;
				}

				hasDataRow = true;
				List<String> rowErrors = new ArrayList<>();
				int excelRowNumber = rowIndex + 1;

				java.util.function.Function<Integer, String> cell = index -> {
					String raw = formatter.formatCellValue(row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
					return normalizeText.apply(raw);
				};

				String departmentCode = cell.apply(1);
				String jobTitle = cell.apply(2);
				String employeeCode = normalizeEmployeeCode(cell.apply(3));
				String fullName = cell.apply(4);
				String phoneNumber = cell.apply(5);
				String email = normalizeEmail(cell.apply(6));
				Integer birthDay = parseInteger.apply(cell.apply(7));
				Integer birthMonth = parseInteger.apply(cell.apply(8));
				Integer birthYear = parseInteger.apply(cell.apply(9));

				Department department = null;
				if (departmentCode == null) {
					rowErrors.add("Sai mã Phòng");
				} else {
					department = departmentRepository.findByDepartmentCodeIgnoreCase(departmentCode).orElse(null);
					if (department == null) {
						rowErrors.add("Sai mã Phòng");
					}
				}

				if (jobTitle == null) {
					rowErrors.add("Vui lòng nhập chức danh");
				}

				if (fullName == null) {
					rowErrors.add("Vui lòng nhập họ và tên");
				}

				if (phoneNumber == null) {
					rowErrors.add("Vui lòng nhập số điện thoại");
				}

				LocalDate dateOfBirth = null;
				if (birthDay == null || birthMonth == null || birthYear == null) {
					rowErrors.add("Sai ngày, tháng, năm sinh");
				} else {
					try {
						dateOfBirth = LocalDate.of(birthYear, birthMonth, birthDay);
					} catch (DateTimeException ex) {
						rowErrors.add("Sai ngày, tháng, năm sinh");
					}
				}

				if (employeeCode != null) {
					String codeKey = employeeCode.toLowerCase(Locale.ROOT);
					if (!seenEmployeeCodes.add(codeKey)) {
						rowErrors.add("Mã nhân viên trùng trong file");
					}
					if (employeeRepository.existsByEmployeeCodeIgnoreCase(employeeCode)) {
						rowErrors.add("Mã nhân viên đã tồn tại");
					}
				}

				if (email != null) {
					if (!EMAIL_PATTERN.matcher(email).matches()) {
						rowErrors.add("Email không đúng định dạng");
					} else {
						String emailKey = email.toLowerCase(Locale.ROOT);
						if (!seenEmails.add(emailKey)) {
							rowErrors.add("Email trùng trong file");
						}
						if (employeeRepository.existsByEmailIgnoreCase(email)) {
							rowErrors.add("Email đã tồn tại");
						}
					}
				}

				if (!rowErrors.isEmpty()) {
					errors.add("Dòng " + excelRowNumber + ": " + String.join(", ", rowErrors));
					continue;
				}

				Employee employee = new Employee();
				employee.setDepartment(department);
				employee.setJobTitle(jobTitle);
				employee.setEmployeeCode(employeeCode);
				employee.setFullName(fullName);
				employee.setPhoneNumber(phoneNumber);
				employee.setEmail(email);
				employee.setDateOfBirth(dateOfBirth);
				employee.setIsActive(true);
				employeesToInsert.add(employee);
			}

			if (!hasDataRow) {
				throw new RuntimeException("File import không có dữ liệu.");
			}

			// Nếu có lỗi ở bất kỳ dòng nào -> không add, chỉ báo lỗi
			if (!errors.isEmpty()) {
				Map<String, Object> result = new LinkedHashMap<>();
				result.put("success", 0);
				result.put("errors", errors);
				return result;
			}

			// Chỉ add khi toàn bộ file không có lỗi
			if (!employeesToInsert.isEmpty()) {
				employeeRepository.saveAll(employeesToInsert);
			}

			Map<String, Object> result = new LinkedHashMap<>();
			result.put("success", employeesToInsert.size());
			result.put("errors", errors);
			return result;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			String message = ex.getMessage() == null || ex.getMessage().isBlank()
					? "Có lỗi xảy ra khi import nhân viên."
					: "Có lỗi xảy ra khi import nhân viên: " + ex.getMessage();
			throw new RuntimeException(message, ex);
		}
	}

	@Override
	public byte[] downloadEmployeeTemplate() {
		ClassPathResource template = new ClassPathResource("templates/employeeList/ThemDanhSachNhanVien.xlsx");
		try (InputStream inputStream = template.getInputStream()) {
			return inputStream.readAllBytes();
		} catch (Exception ex) {
			throw new RuntimeException("Có lỗi xảy ra khi tải file mẫu.", ex);
		}
	}

	private void updateEntity(Employee employee, EmployeeFormRequest request) {
		Department department = departmentRepository.findById(request.getDepartmentId())
				.orElseThrow(() -> new EntityNotFoundException("Phong ban khong ton tai."));

		employee.setDepartment(department);
		employee.setJobTitle(request.getJobTitle().trim());
		employee.setEmployeeCode(normalizeEmployeeCode(request.getEmployeeCode()));
		employee.setFullName(request.getFullName().trim());
		employee.setDateOfBirth(request.getDateOfBirth());
		employee.setPhoneNumber(request.getPhoneNumber().trim());
		employee.setEmail(normalizeEmail(request.getEmail()));
		employee.setIsActive(true);
	}

	private void validateUniqueFields(EmployeeFormRequest request, Integer excludedEmployeeId) {
		String employeeCode = normalizeEmployeeCode(request.getEmployeeCode());
		String email = normalizeEmail(request.getEmail());

		validateEmailFormat(email);
		validateEmployeeCode(employeeCode, excludedEmployeeId);
		validateEmail(email, excludedEmployeeId);
	}

	private String normalizeEmployeeCode(String employeeCode) {
		if (employeeCode == null) {
			return null;
		}

		String normalized = employeeCode.trim();
		if (normalized.isBlank()) {
			return null;
		}

		return EMPLOYEE_CODE_PATTERN.matcher(normalized).matches() ? normalized : null;
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}

		String normalized = email.trim();
		return normalized.isBlank() ? null : normalized;
	}

	private void validateEmailFormat(String email) {
		if (email == null) {
			return;
		}

		if (!EMAIL_PATTERN.matcher(email).matches()) {
			throw new DuplicateFieldException("email", "Email khong dung dinh dang.");
		}
	}

	private void validateEmployeeCode(String employeeCode, Integer excludedEmployeeId) {
		if (employeeCode == null || employeeCode.isBlank()) {
			return;
		}

		boolean duplicateEmployeeCode = excludedEmployeeId == null
				? employeeRepository.existsByEmployeeCodeIgnoreCase(employeeCode)
				: employeeRepository.existsByEmployeeCodeIgnoreCaseAndEmployeeIdNot(employeeCode, excludedEmployeeId);
		if (duplicateEmployeeCode) {
			throw new DuplicateFieldException("employeeCode", "Ma nhan vien da ton tai.");
		}
	}

	private void validateEmail(String email, Integer excludedEmployeeId) {
		if (email == null || email.isBlank()) {
			return;
		}

		boolean duplicateEmail = excludedEmployeeId == null
				? employeeRepository.existsByEmailIgnoreCase(email)
				: employeeRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(email, excludedEmployeeId);
		if (duplicateEmail) {
			throw new DuplicateFieldException("email", "Email da ton tai.");
		}
	}

	private UpcomingBirthdayItem toUpcomingBirthdayItem(Employee employee, LocalDate today) {
		LocalDate nextBirthday = resolveBirthdayInYear(employee.getBirthMonth(), employee.getBirthDay(), today.getYear());
		if (nextBirthday.isBefore(today)) {
			nextBirthday = resolveBirthdayInYear(employee.getBirthMonth(), employee.getBirthDay(), today.getYear() + 1);
		}

		long daysUntil = ChronoUnit.DAYS.between(today, nextBirthday);
		String status = daysUntil == 0 ? "Hôm nay" : daysUntil + " ngày tới";

		UpcomingBirthdayResponse response = new UpcomingBirthdayResponse(
				0,
				valueOrEmpty(employee.getJobTitle()),
				valueOrEmpty(employee.getFullName()),
				employee.getDateOfBirth(),
				status
		);

		return new UpcomingBirthdayItem(response, daysUntil);
	}

	private LocalDate resolveBirthdayInYear(Integer month, Integer day, int year) {
		int safeMonth = month == null ? 1 : month;
		int safeDay = day == null ? 1 : day;
		int maxDay = YearMonth.of(year, safeMonth).lengthOfMonth();
		return LocalDate.of(year, safeMonth, Math.min(safeDay, maxDay));
	}

	private String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private record UpcomingBirthdayItem(UpcomingBirthdayResponse response, long daysUntil) {
	}

}
