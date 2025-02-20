package org.example.Service;

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
    private final StatusServiceImpl statusService;
    private final String statusUser = "User";
    private final String statusAdmin = "Admin";
    private final ModelMapper modelMapper;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, StatusServiceImpl statusService) {
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
    public void save(UserDto userDto) throws Exception {
        User userEntity = modelMapper.map(userDto, User.class);
        Status status = getStatus(statusUser);
        if (status!=null) {
            userEntity.setStatus(status);
            userRepository.save(userEntity);
        }
    }
    @Override
    public UserDto get(Long id) {
        UserDto userDto = null;
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) userDto = new UserDto(user.get().getId(), user.get().getName(), user.get().getSurname(),
                user.get().getUsername(), user.get().getPhoneNumber(), user.get().getStatus().getIdStatus(),
                user.get().getEmail(), user.get().getPassword());
        return userDto;
    }
    public User getEntity(Long id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) return user.get();
        else throw new Exception("Пользователен с данным id не найден");
    }

    /**
     * Проверка, существует ли в БД пользователь с данным id чата
     * @param id id чата
     * @return true - пользователь существует, иначе false
     */
    public boolean isUserExist(Long id){
        Optional<User> user = userRepository.findById(id);
        return user.isPresent();
    }
    /**
     * Проверка, есть ли в БД зарегистрированные администраторы
     * @return true - администратор есть, иначе - false
     */
    public boolean isAdministratorRegistered() throws Exception {
        Status status = getStatus(statusAdmin);
        List<User> admins = userRepository.findByStatus(status);
        return !admins.isEmpty();
    }

    /**
     * Получение статуса по названию
     * @param status название статуса
     * @return сущность статуса
     * @throws Exception не найдена сущность статуса
     */
    public Status getStatus(String status) throws Exception {
        Status statusEntity = statusService.findByName(status);
        if (statusEntity==null) throw new Exception("Не удалось получить статус "+status);
        return statusEntity;
    }

    /**
     * Изменение сущности обычного пользователя на администратора
     * @param userDto дто пользователя
     * @param password пароль для доступа к режиму администратора
     * @throws Exception не найдена сущность статуса
     */
    public void updateUserToAdmin(UserDto userDto, String password) throws Exception {
        Status status = getStatus(statusAdmin);
        Optional<User> currentUserOptional = userRepository.findById(userDto.getId());
        if (currentUserOptional.isPresent() && status!=null) {
            User currentUser = currentUserOptional.get();
            currentUser.setEmail(userDto.getEmail());
            currentUser.setPassword(PasswordUtil.hashPassword(password));
            currentUser.setStatus(status);
            userRepository.save(currentUser);
        }
    }
}
