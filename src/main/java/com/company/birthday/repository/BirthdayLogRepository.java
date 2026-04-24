package com.company.birthday.repository;

import com.company.birthday.entity.BirthdayLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BirthdayLogRepository extends JpaRepository<BirthdayLog, Long> {

    @EntityGraph(attributePaths = "employee")
    Page<BirthdayLog> findAllByOrderBySendTimeDesc(Pageable pageable);
}

