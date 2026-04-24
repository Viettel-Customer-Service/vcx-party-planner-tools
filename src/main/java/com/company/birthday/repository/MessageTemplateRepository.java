package com.company.birthday.repository;

import com.company.birthday.entity.MessageTemplate;
import com.company.birthday.entity.MessageTemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Integer> {

    Optional<MessageTemplate> findById(Integer id);

    List<MessageTemplate> findByIsActiveTrue();

    List<MessageTemplate> findByType(MessageTemplateType type);

    Optional<MessageTemplate> findByTypeAndIsDefaultTrue(MessageTemplateType type);

    Optional<MessageTemplate> findByName(String name);


    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);
}

