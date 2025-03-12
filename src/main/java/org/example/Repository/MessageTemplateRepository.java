package org.example.Repository;

import org.example.Entity.Message;
import org.example.Entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для шаблонов сообщений
 */
@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
}
