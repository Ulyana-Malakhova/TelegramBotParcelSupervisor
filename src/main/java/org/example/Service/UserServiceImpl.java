package org.example.Service;

import jakarta.persistence.EntityNotFoundException;
import org.example.Entity.User;
import org.example.Dto.UserDto;
import org.example.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            userDtos.add(new UserDto(user.getId(),user.getName(),user.getSurname(),user.getUsername(),user.getPhoneNumber(),user.getIdStatus(),user.getEmail(),user.getPassword()));
        }
        return userDtos;
    }
    @Override
    public void save(UserDto userDto) {
        User userEntity = modelMapper.map(userDto, User.class);
        userRepository.save(userEntity);
    }

    @Override
    public UserDto get(Long id){
        UserDto userDto = null;
        try {
            User user = userRepository.getReferenceById(id);
            userDto = new UserDto(user.getId(), user.getName(),user.getSurname(),user.getUsername(),user.getPhoneNumber(),user.getIdStatus(),user.getEmail(),user.getPassword());
        } catch (EntityNotFoundException e){
            System.out.println(e.getMessage());
        }
        return userDto;
    }
    public boolean isAdministratorRegistered(){
        Long idAdmin = statusService.findByNameStatus("Admin");
        List<User> admins = userRepository.findAdmins(idAdmin);
        return !admins.isEmpty();
    }
}
