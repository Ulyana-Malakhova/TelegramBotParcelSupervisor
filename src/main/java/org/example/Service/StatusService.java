package org.example.Service;

import org.example.Entity.Status;
import org.example.Repository.StatusRepository;
import org.example.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для статуса пользователя
 */
@Service
public class StatusService {
    /**
     * Репозиторий для статуса пользователя
     */
    private final StatusRepository statusRepository;
    @Autowired
    public StatusService(StatusRepository statusRepository) {
        this.statusRepository=statusRepository;
    }

    /**
     * Поиск статуса по названию
     * @param status название статуса
     * @return сущность статуса
     */
    public Status findByNameStatus(String status){
        Optional<Status> statusOptional = statusRepository.findByStatusName(status);
        return statusOptional.orElse(null);
    }
}
