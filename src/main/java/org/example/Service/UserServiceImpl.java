package org.example.Service;

import jakarta.persistence.EntityNotFoundException;
import org.example.Entity.Status;
import org.example.Entity.User;
import org.example.Dto.UserDto;
import org.example.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements ServiceInterface<UserDto>{
    private final UserRepository userRepository;
    private final StatusService statusService;
    private final ModelMapper modelMapper;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, StatusService statusService) {
        this.userRepository=userRepository;
        this.statusService=statusService;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private List<UserDto> toDto(){
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for (User user: users){
            userDtos.add(new UserDto(user.getId(),user.getName(),user.getSurname(),user.getUsername(),user.getPhoneNumber(),user.getStatus().getIdStatus(),user.getEmail(),user.getPassword()));
        }
        return userDtos;
    }
    @Override
    public void save(UserDto userDto) {
        User userEntity = modelMapper.map(userDto, User.class);
        Status idAdmin = statusService.findById(userDto.getIdStatus());
        userEntity.setStatus(idAdmin);
        userRepository.save(userEntity);
    }

    @Override
    public UserDto get(Long id){
        UserDto userDto = null;
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) userDto = new UserDto(user.get().getId(),
                    user.get().getName(),user.get().getSurname(),user.get().getUsername(),
                    user.get().getPhoneNumber(),user.get().getStatus().getIdStatus(),
                    user.get().getEmail(),user.get().getPassword());
        } catch (EntityNotFoundException e){
            System.out.println(e.getMessage());
        }
        return userDto;
    }
    public boolean isAdministratorRegistered(){
        Status idAdmin = statusService.findByNameStatus("Admin");
        List<User> admins = userRepository.findByStatus(idAdmin);
        return !admins.isEmpty();
    }
}
