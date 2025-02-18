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
public class StatusServiceImpl implements NameServiceInterface<Status>{
    /**
     * Репозиторий для статуса пользователя
     */
    private final StatusRepository statusRepository;
    @Autowired
    public StatusServiceImpl(StatusRepository statusRepository) {
        this.statusRepository=statusRepository;
    }

    /**
     * Поиск статуса по названию
     * @param name название статуса
     * @return сущность статуса
     */
    @Override
    public Status findByName(String name) {
        Optional<Status> statusOptional = statusRepository.findByStatusName(name);
        return statusOptional.orElse(null);
    }
}
