package com.company.birthday.repository;

import com.company.birthday.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

	@EntityGraph(attributePaths = "department")
	Page<Employee> findByIsActiveTrue(Pageable pageable);

	boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

	boolean existsByEmployeeCodeIgnoreCaseAndEmployeeIdNot(String employeeCode, Integer employeeId);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndEmployeeIdNot(String email, Integer employeeId);

	Optional<Employee> findByEmployeeIdAndIsActiveTrue(Integer employeeId);

	List<Employee> findByBirthMonthAndBirthDayAndIsActiveTrue(Integer birthMonth, Integer birthDay);

	@EntityGraph(attributePaths = "department")
	@Query("""
			SELECT e
			FROM Employee e
			WHERE e.isActive = true
			  AND (
				(:sameMonth = true AND e.birthMonth = :startMonth AND e.birthDay BETWEEN :startDay AND :endDay)
				OR
				(:wrapped = false AND :sameMonth = false AND (
					(e.birthMonth = :startMonth AND e.birthDay >= :startDay)
					OR (e.birthMonth = :endMonth AND e.birthDay <= :endDay)
					OR (e.birthMonth > :startMonth AND e.birthMonth < :endMonth)
				))
				OR
				(:wrapped = true AND (
					(e.birthMonth = :startMonth AND e.birthDay >= :startDay)
					OR (e.birthMonth = :endMonth AND e.birthDay <= :endDay)
					OR e.birthMonth > :startMonth
					OR e.birthMonth < :endMonth
				))
			  )
			""")
	List<Employee> findUpcomingBirthdaysRaw(
			@Param("startMonth") int startMonth,
			@Param("startDay") int startDay,
			@Param("endMonth") int endMonth,
			@Param("endDay") int endDay,
			@Param("sameMonth") boolean sameMonth,
			@Param("wrapped") boolean wrapped
	);

	default List<Employee> findByTodayBirthday() {
		// TODO: Adjust business rules (timezone/leap day/filters) for "birthday today".
		LocalDate today = LocalDate.now();
		return findByBirthMonthAndBirthDayAndIsActiveTrue(today.getMonthValue(), today.getDayOfMonth());
	}
}
