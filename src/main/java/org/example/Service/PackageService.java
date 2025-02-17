package org.example.Service;

import org.example.Dto.PackageDto;
import org.example.Entity.Package;
import org.example.Entity.Role;
import org.example.Entity.TrackingStatus;
import org.example.Entity.User;
import org.example.Repository.PackageRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PackageService {
    private final PackageRepository packageRepository;
    private final RoleService roleService;
    private final TrackingStatusService trackingStatusService;
    private final UserServiceImpl userService;
    private final ModelMapper modelMapper;
    @Autowired
    public PackageService(PackageRepository packageRepository, RoleService roleService, TrackingStatusService trackingStatusService, UserServiceImpl userService) {
        this.packageRepository = packageRepository;
        this.roleService = roleService;
        this.trackingStatusService = trackingStatusService;
        this.userService = userService;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }
    public List<PackageDto> findByUser(Long userId){
        List<PackageDto> packageDtos = new ArrayList<>();
        List<Package> packages = packageRepository.findByUserIdEntity(userId);
        for (Package p: packages){
            PackageDto packageDto = modelMapper.map(p, PackageDto.class);
            packageDto.setNameRole(p.getRoleEntity().getNameRole());
            packageDto.setNameTrackingStatus(p.getTrackingStatusEntity().getNameTrackingStatus());
            packageDto.setIdUser(p.getUserEntity().getId());
            packageDtos.add(packageDto);
        }
        return packageDtos;
    }
    public void delete(Long userId, String name) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(userId, name);
        if (packageOptional.isEmpty()) throw new Exception("Отправление с данным именем не найдено");
        packageRepository.deleteByIdAndName(userId, name);
    }
    public void addName(PackageDto packageDto) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(packageDto.getIdUser(),
                packageDto.getNamePackage());
        if (packageOptional.isPresent()) throw new Exception("Уже существует отправление с данным именем");
        Package packageEntity = modelMapper.map(packageDto, Package.class);
        Role role = roleService.findByNameRole(packageDto.getNameRole());
        TrackingStatus trackingStatus = trackingStatusService.findByNameTrackingStatus(packageDto.getNameTrackingStatus());
        User user = userService.get(packageDto.getIdUser());
        packageEntity.setRoleEntity(role);
        packageEntity.setUserEntity(user);
        packageEntity.setTrackingStatusEntity(trackingStatus);
        packageRepository.save(packageEntity);
    }
}
