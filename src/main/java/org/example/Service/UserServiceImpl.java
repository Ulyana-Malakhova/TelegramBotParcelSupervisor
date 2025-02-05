package org.example.Service;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
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
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    //private final MapperFacade mapperFacade = new DefaultMapperFactory.Builder()
    //        .build().getMapperFacade();
    private final ModelMapper modelMapper;
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository=userRepository;
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
    public UserDto save(UserDto userDto) {
        User userEntity = modelMapper.map(userDto, User.class);
        userRepository.save(userEntity);
        return userDto;
    }
}
