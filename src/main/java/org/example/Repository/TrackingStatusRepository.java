package org.example.Repository;

import org.example.Entity.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для статуса отслеживания
 */
@Repository
public interface TrackingStatusRepository extends JpaRepository<TrackingStatus, Long> {
    /**
     * Получение сущности статуса отслеживания по названию
     * @param trackingStatus название статуса отслеживания
     * @return сущность статуса
     */
    Optional<TrackingStatus> findByNameTrackingStatus(String trackingStatus);
}
