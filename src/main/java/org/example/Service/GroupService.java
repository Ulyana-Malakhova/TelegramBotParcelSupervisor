package org.example.Service;

import org.example.Dto.GroupDto;
import org.example.Dto.GroupPackageDto;
import org.example.Entity.*;
import org.example.Entity.Package;
import org.example.Repository.GroupPackageRepository;
import org.example.Repository.GroupRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupService {
    private final ModelMapper modelMapper;
    private final GroupRepository groupRepository;
    private final GroupPackageRepository groupPackageRepository;
    private final UserServiceImpl userService;
    private final PackageService packageService;

    @Autowired
    public GroupService(GroupRepository groupRepository, GroupPackageRepository groupPackageRepository, UserServiceImpl userService, PackageService packageService) {
        this.userService = userService;
        this.packageService = packageService;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        this.groupRepository = groupRepository;
        this.groupPackageRepository = groupPackageRepository;
    }

    public void saveGroup(GroupDto groupDto) {
        Group group = modelMapper.map(groupDto, Group.class);
        User user = userService.findById(groupDto.getIdUser());
        if (user != null) {
            group.setUser(user);
            groupRepository.save(group);
        }
    }

    public void saveGroupPackage(GroupPackageDto groupPackageDto) {
        GroupPackage groupPackage = new GroupPackage();
        GroupPackageId id = new GroupPackageId();
        id.setGroupId(groupPackageDto.getIdGroup());
        id.setPackageId(groupPackageDto.getIdPackage());
        groupPackage.setId(id);

        Package packageEntity = packageService.findById(groupPackageDto.getIdPackage());
        Group group = findById(groupPackageDto.getIdGroup());
        if (group != null && packageEntity != null){
            groupPackage.setPackageEntity(packageEntity);
            groupPackage.setGroup(group);
            groupPackageRepository.save(groupPackage);
        }
        else {
            System.out.println("error");
        }
    }

    public Group findById(Long id){
        Optional<Group> groupOptional = groupRepository.findById(id);
        return groupOptional.orElse(null);
    }

    public Group findByName(String name, Long userId){
        return groupRepository.findByIdAndName(name, userId);

    }
}

