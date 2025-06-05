package org.example.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
     * Добавление посылкb
     *
     * @param packageDto dto-объект со всеми данными посылки
     * @throws Exception если посылка с таким именем уже существует у пользователя или сам пользователь не найден в бд
     */
    public void addTrackNumber(PackageDto packageDto) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByNamePackageAndUserId(packageDto.getIdUser(),
                packageDto.getNamePackage());
        if (packageOptional.isPresent()) throw new Exception("Уже существует отправление с данным именем");
        Package packageEntity = modelMapper.map(packageDto, Package.class);
        Role role = roleService.findByName(packageDto.getNameRole());
        TrackingStatus trackingStatus = trackingStatusService.findByName(packageDto.getNameTrackingStatus());
        User user = userService.findById(packageDto.getIdUser());
        if (user != null) packageEntity.setUserEntity(user);
        else throw new Exception("Пользователь не найден");
        packageEntity.setRoleEntity(role);
        packageEntity.setTrackingStatusEntity(trackingStatus);
        packageRepository.save(packageEntity);
    }

    /**
     * Обновление статусов посылки
     *
     * @param packageDto dto-объект посылки
     * @throws Exception не найдена запись посылки или статуса
     */
    public void updateStatus(PackageDto packageDto) throws Exception {
        Optional<Package> packageOptional = packageRepository.findByTrackNumberAndUserId(packageDto.getIdUser(),
                packageDto.getTrackNumber());
        if (packageOptional.isEmpty()) throw new Exception("Данные о посылке не найдены");
        Package packageEntity = packageOptional.get();
        if (!packageDto.getNameTrackingStatus().equals(packageEntity.getTrackingStatusEntity().getNameTrackingStatus())) {
            TrackingStatus trackingStatus = trackingStatusService.findByName(packageDto.getNameTrackingStatus());
            if (trackingStatus == null) throw new Exception("Статус не найден");
            packageEntity.setTrackingStatusEntity(trackingStatus);
        }
        if (packageDto.getLatestStatus() != null) packageEntity.setLatestStatus(packageDto.getLatestStatus());
        if (packageDto.getReceiptDate() != null) packageEntity.setReceiptDate(packageDto.getReceiptDate());
        packageRepository.save(packageEntity);
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
     * @param userId идентификатор пользователя
     * @return поток с эксель файлом
     * @throws IOException при работе с workbook, если произойдет ошибка при его создании, при записи в лист, при записи данных в поток или при закрытии workbook
     */
    public ByteArrayOutputStream exportPackageToExcel(String period, long userId) throws IOException {
        List<Package> packages = getPackagesByPeriod(period, userId);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ParcelReport");

        Row headerRow = sheet.createRow(0);
        CellStyle dateCellStyle = workbook.createCellStyle();
        // Устанавливаем формат даты
        dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));
        headerRow.createCell(0).setCellValue("Track_Number");
        sheet.setColumnWidth(0, 256 * 15);
        headerRow.createCell(1).setCellValue("Name_Package");
        sheet.setColumnWidth(1, 256 * 15);
        headerRow.createCell(2).setCellValue("Departure_Date");
        sheet.setColumnWidth(2, 256 * 15);
        headerRow.createCell(3).setCellValue("Receipt_Date");
        sheet.setColumnWidth(3, 256 * 15);
        headerRow.createCell(4).setCellValue("Role_Name");
        sheet.setColumnWidth(4, 256 * 15);
        headerRow.createCell(5).setCellValue("Tracking_Status");
        sheet.setColumnWidth(5, 256 * 15);

        int rowNum = 1;
        for (Package packageEntity : packages) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(packageEntity.getTrackNumber());
            row.createCell(1).setCellValue(packageEntity.getNamePackage());
            Cell departureDateCell = row.createCell(2);
            departureDateCell.setCellValue(packageEntity.getDepartureDate());
            departureDateCell.setCellStyle(dateCellStyle);

            Cell receiptDateCell = row.createCell(3);
            receiptDateCell.setCellValue(packageEntity.getReceiptDate());
            receiptDateCell.setCellStyle(dateCellStyle);

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
     * @param userId идентификатор пользователя
     * @return список сущностей посылки
     */
    public List<Package> getPackagesByPeriod(String period, long userId) {
        Calendar calendar = Calendar.getInstance();

        switch (period) {
            case "1day":
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case "1week":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "1month":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "3months":
                calendar.add(Calendar.MONTH, -3);
                break;
            case "6months":
                calendar.add(Calendar.MONTH, -6);
                break;
        }
        Date startDate = calendar.getTime();
        return packageRepository.findByPeriodAndById(startDate, userId);
    }

    /**
     * Получение списка посылок по статусу отслеживания
     *
     * @param status значение статуса
     * @return список dto-объектов посылок
     * @throws Exception не найден статус отслеживания
     */
    public List<PackageDto> getByTrackingStatus(String status) throws Exception {
        List<PackageDto> packageDtos = new ArrayList<>();
        TrackingStatus trackingStatus = trackingStatusService.findByName(status);
        if (trackingStatus == null) throw new Exception("Статус не найден");
        List<Package> packages = packageRepository.findByLatestStatus(trackingStatus);
        for (Package p : packages) {
            PackageDto packageDto = modelMapper.map(p, PackageDto.class);
            packageDto.setNameRole(p.getRoleEntity().getNameRole());
            packageDto.setNameTrackingStatus(p.getTrackingStatusEntity().getNameTrackingStatus());
            packageDto.setIdUser(p.getUserEntity().getId());
            packageDtos.add(packageDto);
        }
        return packageDtos;
    }

    public Package findById(Long packageId) {
        Optional<Package> packageOptional = packageRepository.findById(packageId);
        return packageOptional.orElse(null);
    }
    public Package findByTrackAndId(Long userId, String track) {
        Optional<Package> packageOptional = packageRepository.findByTrackNumberAndUserId(userId,track);
        return packageOptional.orElse(null);
    }
}
