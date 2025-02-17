package org.example.Service;

import org.example.Entity.Role;
import org.example.Entity.TrackingStatus;
import org.example.Repository.TrackingStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrackingStatusService {
    private final TrackingStatusRepository trackingStatusRepository;
    @Autowired
    public TrackingStatusService(TrackingStatusRepository trackingStatusRepository) {
        this.trackingStatusRepository = trackingStatusRepository;
    }
    /**
     * Поиск статуса трека по названию
     * @param trackingStatus название статуса
     * @return сущность статуса
     */
    public TrackingStatus findByNameTrackingStatus(String trackingStatus){
        Optional<TrackingStatus> trackingStatusOptional =
                trackingStatusRepository.findByNameTrackingStatus(trackingStatus);
        return trackingStatusOptional.orElse(null);
    }
}
