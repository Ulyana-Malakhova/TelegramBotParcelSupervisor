package org.example.Service;

import org.example.Dto.MessageDto;
import org.example.Entity.Message;
import org.example.Entity.User;
import org.example.Repository.MessageRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageServiceImpl implements ServiceInterface<MessageDto> {
    private final ModelMapper modelMapper;
    private final MessageRepository messageRepository;
    private final UserServiceImpl userService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository,UserServiceImpl userService) {
        this.messageRepository = messageRepository;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        this.userService=userService;
    }

    @Override
    public void save(MessageDto messageDto) {
        Message message = modelMapper.map(messageDto,Message.class);
        User user = userService.findById(messageDto.getIdUser());
        if (user!=null) {
            message.setUser(user);
            messageRepository.save(message);
        }
    }

    @Override
    public MessageDto get(Long id) {
        MessageDto messageDto = null;
        Optional<Message> message = messageRepository.findById(id);
        if(message.isPresent()){
            messageDto = new MessageDto(message.get().getId(), message.get().getText(), message.get().getDate(), message.get().getUser().getId());
        }
        return messageDto;
    }
    public MessageDto getLatest(Long userId){
        Optional<Message> messageOptional = messageRepository.findLatestMessageByUserId(userId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            MessageDto messageDto = modelMapper.map(message, MessageDto.class);
            messageDto.setIdUser(message.getUser().getId());
            return messageDto;
        }
        else
            return null;

    }
}
