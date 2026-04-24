package com.company.birthday.repository;

import com.company.birthday.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    @Query("SELECT d FROM Department d ORDER BY d.departmentName")
    List<Department> findAllDepartment();

    Optional<Department> findByDepartmentCodeIgnoreCase(String departmentCode);
}
