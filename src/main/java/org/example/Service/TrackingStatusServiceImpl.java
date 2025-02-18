package org.example.Service;

import org.example.Entity.Role;
import org.example.Entity.TrackingStatus;
import org.example.Repository.TrackingStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrackingStatusServiceImpl implements NameServiceInterface<TrackingStatus>{
    private final TrackingStatusRepository trackingStatusRepository;
    @Autowired
    public TrackingStatusServiceImpl(TrackingStatusRepository trackingStatusRepository) {
        this.trackingStatusRepository = trackingStatusRepository;
    }
    /**
     * Поиск статуса трека по названию
     * @param name название статуса
     * @return сущность статуса
     */
    @Override
    public TrackingStatus findByName(String name) {
        Optional<TrackingStatus> trackingStatusOptional =
                trackingStatusRepository.findByNameTrackingStatus(name);
        return trackingStatusOptional.orElse(null);
    }
}
