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
import java.util.Objects;
import java.util.Optional;

@Service
public class PackageServiceImpl implements ServiceInterface<PackageDto> {
    private final PackageRepository packageRepository;
    private final RoleServiceImpl roleService;
    private final TrackingStatusServiceImpl trackingStatusService;
    private final UserServiceImpl userService;
    private final ModelMapper modelMapper;
    private final String tracked = "Отслеживается";
    @Autowired
    public PackageServiceImpl(PackageRepository packageRepository, RoleServiceImpl roleService,
                          TrackingStatusServiceImpl trackingStatusService, UserServiceImpl userService) {
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
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(userId, name.toLowerCase());
        if (packageOptional.isEmpty()) throw new Exception("Отправление с данным именем не найдено");
        Package packageEntity = packageOptional.get();
        TrackingStatus trackingStatus = trackingStatusService.findByName(tracked);
        if (!Objects.equals(packageEntity.getTrackingStatusEntity().getIdTrackingStatus(),
                trackingStatus.getIdTrackingStatus()))
            packageRepository.deleteByIdAndName(userId, name.toLowerCase());
        else {
            packageEntity.setNamePackage(null);
            packageRepository.save(packageEntity);
        }
    }
    public void addName(PackageDto packageDto) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(packageDto.getIdUser(),
                packageDto.getNamePackage());
        if (packageOptional.isPresent()) throw new Exception("Уже существует отправление с данным именем");
        Package packageEntity = modelMapper.map(packageDto, Package.class);
        Role role = roleService.findByName(packageDto.getNameRole());
        TrackingStatus trackingStatus = trackingStatusService.findByName(packageDto.getNameTrackingStatus());
        User user = userService.getEntity(packageDto.getIdUser());
        packageEntity.setRoleEntity(role);
        packageEntity.setUserEntity(user);
        packageEntity.setTrackingStatusEntity(trackingStatus);
        packageRepository.save(packageEntity);
    }
    public String findByName(Long userId, String name){
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(userId,
                name.toLowerCase());
        return packageOptional.map(Package::getTrackNumber).orElse(null);
    }

    @Override
    public void save(PackageDto Dto) throws Exception {
        //посмотреть, будет ли использоваться
    }

    @Override
    public PackageDto get(Long id) throws Exception {
        //посмотреть, будет ли использоваться
        return null;
    }
}
