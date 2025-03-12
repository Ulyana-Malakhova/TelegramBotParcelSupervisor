package org.example.Repository;

import org.example.Entity.Message;
import org.example.Entity.MessageTemplate;
import org.example.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для шаблонов сообщений
 */
@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
    Optional<MessageTemplate> findByEvent(@Param("event") String event);
}
