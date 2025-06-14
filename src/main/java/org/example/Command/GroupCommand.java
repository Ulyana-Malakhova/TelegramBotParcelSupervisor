package org.example.Command;

import org.apache.commons.lang3.RandomUtils;
import org.example.Dto.GroupDto;
import org.example.Dto.GroupPackageDto;
import org.example.Entity.Group;
import org.example.Entity.GroupPackage;
import org.example.Entity.Package;
import org.example.Service.GroupService;
import org.example.Service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class GroupCommand {
    private final GroupService groupService;
    private final PackageService packageService;

    @Autowired
    public GroupCommand(GroupService groupService, PackageService packageService) {
        this.groupService = groupService;
        this.packageService = packageService;
    }

    public String[] saveGroup(String[] parts, long userId){
        String groupName = parts[parts.length - 1];
        GroupDto groupDto = new GroupDto(RandomUtils.nextLong(0L, 9999L), groupName, userId);
        groupService.saveGroup(groupDto);
        String[] trackNumbers = Arrays.copyOfRange(parts, 1, parts.length - 1);
        String[] trackNumbersPackage = new String[parts.length];
        Group group = groupService.findByName(groupName, userId);

        int countPackage = 0;
        for (String track : trackNumbers){
            Package packageEntity = packageService.findByTrackAndId(userId,track);
            if (packageEntity != null){
                GroupPackageDto groupPackageDto = new GroupPackageDto(group.getId(),packageEntity.getIdPackage());
                groupService.saveGroupPackage(groupPackageDto);
            }
            else {
                trackNumbersPackage[countPackage]=track;
                countPackage++;
            }
        }
        return trackNumbersPackage;
    }

    public String[] trackingCommand(String userMessage, long userId){
        String[] parts = userMessage.split("\\s+");
        Group group = groupService.findByName(parts[1],userId);
        List<GroupPackage> groupPackages = groupService.findGroupPackageByGroup(group.getId());
        int lengthIdPackages = 0;
        String[] trackNumbers = new String[groupPackages.size()];
        for (GroupPackage groupPackage : groupPackages){
            Package packageEntity = packageService.findById(groupPackage.getPackageEntity().getIdPackage());
            int countPackage = 0;
            for (String track : trackNumbers){
                if (Objects.equals(track, packageEntity.getTrackNumber())){
                    countPackage++;
                }
            }
            if (countPackage == 0){
                trackNumbers[lengthIdPackages] = packageEntity.getTrackNumber();
                lengthIdPackages++;
            }
        }
        return trackNumbers;
    }
}
