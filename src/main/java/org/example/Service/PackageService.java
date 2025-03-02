package org.example.Service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.AppConstants;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Сервис посылок
 */
@Service
public class PackageService {
    /**
     * Репозиторий посылок
     */
    private final PackageRepository packageRepository;
    /**
     * Сервис ролей
     */
    private final RoleServiceImpl roleService;
    /**
     * Сервис статусов отслеживания
     */
    private final TrackingStatusServiceImpl trackingStatusService;
    /**
     * Сервис пользователей
     */
    private final UserServiceImpl userService;
    /**
     * Объект преобразования для DTO и сущностей
     */
    private final ModelMapper modelMapper;

    @Autowired
    public PackageService(PackageRepository packageRepository, RoleServiceImpl roleService,
                          TrackingStatusServiceImpl trackingStatusService, UserServiceImpl userService) {
        this.packageRepository = packageRepository;
        this.roleService = roleService;
        this.trackingStatusService = trackingStatusService;
        this.userService = userService;
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /**
     * Поиск посылок конкретного пользователя
     *
     * @param userId id пользователя
     * @return список dto-объектов посылок
     */
    public List<PackageDto> findByUser(Long userId) {
        List<PackageDto> packageDtos = new ArrayList<>();
        List<Package> packages = packageRepository.findByUserIdEntity(userId);
        for (Package p : packages) {
            PackageDto packageDto = modelMapper.map(p, PackageDto.class);
            packageDto.setNameRole(p.getRoleEntity().getNameRole());
            packageDto.setNameTrackingStatus(p.getTrackingStatusEntity().getNameTrackingStatus());
            packageDto.setIdUser(p.getUserEntity().getId());
            packageDtos.add(packageDto);
        }
        return packageDtos;
    }

    /**
     * Удаление имени посылки
     *
     * @param userId id пользователя
     * @param name   имя посылки
     * @throws Exception если посылка с данным именем не найдена
     */
    public void delete(Long userId, String name) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(userId, name);
        if (packageOptional.isEmpty()) throw new Exception("Отправление с данным именем не найдено");
        Package packageEntity = packageOptional.get();
        TrackingStatus trackingStatus = trackingStatusService.findByName(AppConstants.TRACKED);
        if (!Objects.equals(packageEntity.getTrackingStatusEntity().getIdTrackingStatus(),
                trackingStatus.getIdTrackingStatus()))
            packageRepository.deleteByIdAndName(userId, name);  //если посылка не отслеживается - запись о посылке полностью удаляется
        else {  //иначе - только обнуляется поле имени
            packageEntity.setNamePackage(null);
            packageRepository.save(packageEntity);
        }
    }

    /**
     * Добавление имени посылке
     *
     * @param packageDto dto-объект со всеми данными посылки
     * @throws Exception если посылка с таким именем уже существует у пользователя
     */
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

    /**
     * Обновление статуса отслеживания
     *
     * @param packageDto dto-объект посылки
     * @throws Exception не найдена запись посылки или статуса
     */
    public void updateTrackingStatus(PackageDto packageDto) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByTrackNumberAndUserId(packageDto.getIdUser(),
                packageDto.getTrackNumber());
        TrackingStatus trackingStatus = trackingStatusService.findByName(packageDto.getNameTrackingStatus());
        if (trackingStatus == null) throw new Exception("Статус не найден");
        if (packageOptional.isEmpty()) throw new Exception("Данные о посылке не найдены");
        else {
            Package packageEntity = packageOptional.get();
            packageEntity.setTrackingStatusEntity(trackingStatus);
            packageRepository.save(packageEntity);
        }
    }

    /**
     * Получение объекта посылки по ее имени
     *
     * @param userId id пользователя
     * @param name   имя посылки
     * @return dto-объект посылки
     */
    public PackageDto findByName(Long userId, String name) {
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(userId,
                name);
        if (packageOptional.isEmpty()) return null;
        else {
            Package packageEntity = packageOptional.get();
            PackageDto packageDto = modelMapper.map(packageEntity, PackageDto.class);
            packageDto.setNameRole(packageEntity.getRoleEntity().getNameRole());
            packageDto.setNameTrackingStatus(packageEntity.getTrackingStatusEntity().getNameTrackingStatus());
            packageDto.setIdUser(packageEntity.getUserEntity().getId());
            return packageDto;
        }
    }

    /**
     * Поиск посылки по имени
     *
     * @param userId id пользователя
     * @param track  трек-номер посылки
     * @return dto-объект посылки
     */
    public PackageDto findByTrack(Long userId, String track) {
        Optional<Package> packageOptional = packageRepository.findByTrackNumberAndUserId(userId,
                track);
        if (packageOptional.isEmpty()) return null;
        else {
            Package packageEntity = packageOptional.get();
            PackageDto packageDto = modelMapper.map(packageEntity, PackageDto.class);
            packageDto.setNameRole(packageEntity.getRoleEntity().getNameRole());
            packageDto.setNameTrackingStatus(packageEntity.getTrackingStatusEntity().getNameTrackingStatus());
            packageDto.setIdUser(packageEntity.getUserEntity().getId());
            return packageDto;
        }
    }

    /**
     * Создание и заполнение эксель файла
     *
     * @param period пероид интересующих отправленных/полученных посылок
     * @return поток с эксель файлом
     * @throws IOException при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream exportPackageToExcel(String period) throws IOException {
        List<Package> packages = getPackagesByPeriod(period);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ParcelReport");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Track_Number");
        headerRow.createCell(1).setCellValue("Name_Package");
        headerRow.createCell(2).setCellValue("Departure_Date");
        headerRow.createCell(3).setCellValue("Receipt_Date");
        headerRow.createCell(4).setCellValue("Id_Role");
        headerRow.createCell(5).setCellValue("Id_Tracking_Status");

        int rowNum = 1;
        for (Package packageEntity : packages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(packageEntity.getTrackNumber());
            row.createCell(1).setCellValue(packageEntity.getNamePackage());
            row.createCell(2).setCellValue(packageEntity.getDepartureDate());
            row.createCell(3).setCellValue(packageEntity.getReceiptDate());
            row.createCell(4).setCellValue(packageEntity.getRoleEntity().getNameRole());
            row.createCell(5).setCellValue(packageEntity.getTrackingStatusEntity().getNameTrackingStatus());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream;
    }

    /**
     * Получение сущности посылок за заданный пероид
     *
     * @param period пероид интересующих отправленных/полученных посылок
     * @return список сущностей посылки
     */
    public List<Package> getPackagesByPeriod(String period) {
        Calendar calendar = Calendar.getInstance();

        switch (period) {
            case "1 день":
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case "1 неделя":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "1 месяц":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "3 месяца":
                calendar.add(Calendar.MONTH, -3);
                break;
            case "6 месяцев":
                calendar.add(Calendar.MONTH, -6);
                break;
        }
        Date startDate1 = calendar.getTime();
        LocalDate startDate = startDate1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return packageRepository.findByPeriod(startDate);
    }
}
