package org.example.Repository;

import org.example.Entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для статуса пользователя
 */
@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    /**
     * Получение сущности статуса по названию
     * @param status название статуса
     * @return сущность статуса
     */
    Optional<Status> findByStatusName(String status);
}
