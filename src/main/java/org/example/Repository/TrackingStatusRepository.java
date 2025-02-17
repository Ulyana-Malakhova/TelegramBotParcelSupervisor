package org.example.Repository;

import org.example.Entity.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingStatusRepository extends JpaRepository<TrackingStatus, Long> {
}
