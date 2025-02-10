package org.example.Service;

import org.example.Entity.Status;
import org.example.Repository.StatusRepository;
import org.example.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StatusService {
    private final StatusRepository statusRepository;
    @Autowired
    public StatusService(StatusRepository statusRepository) {
        this.statusRepository=statusRepository;
    }
    public Long findByNameStatus(String status){
        Optional<Status> statusOptional = statusRepository.findByStatusName(status);
        return statusOptional.map(Status::getIdStatus).orElse(null);
    }
}
