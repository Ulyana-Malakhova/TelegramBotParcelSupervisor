package org.example.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Dto.MessageDto;
import org.example.Dto.MessageTemplateDto;
import org.example.Entity.Message;
import org.example.Entity.MessageTemplate;
import org.example.Entity.User;
import org.example.Repository.MessageTemplateRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для шаблонов сообщений
 */
@Service
public class MessageTemplateServiceImpl implements ServiceInterface<MessageTemplateDto> {
    /**
     * Репозиторий для шаблонов сообщений
     */
    private final MessageTemplateRepository messageTemplateRepository;
    private final ModelMapper modelMapper;
    private final UserServiceImpl userService;

    public MessageTemplateServiceImpl(MessageTemplateRepository messageTemplateRepository, UserServiceImpl userService) {
        this.messageTemplateRepository = messageTemplateRepository;
        this.modelMapper = new ModelMapper();
        this.userService = userService;
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /**
     * Составление excel-документа со списком всех шаблонов
     * @return массив байтов с данными таблицы
     * @throws Exception проблемы с записью и закрытием потока
     */
    public ByteArrayOutputStream exportToExcel() throws Exception {
        List<MessageTemplate> messageTemplates = messageTemplateRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("MessageTemplate");
        CellStyle cellStyle = workbook.createCellStyle();
        // Устанавливаем формат числа, чтобы избежать научной нотации
        cellStyle.setDataFormat(workbook.createDataFormat().getFormat("0"));
        CellStyle dateCellStyle = workbook.createCellStyle();
        // Устанавливаем формат даты
        dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/MM/yyyy"));
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("id");
        headerRow.createCell(1).setCellValue("event");
        headerRow.createCell(2).setCellValue("text");
        headerRow.createCell(3).setCellValue("edit date");
        sheet.setColumnWidth(3, 256*11);
        headerRow.createCell(4).setCellValue("author id");
        sheet.setColumnWidth(4, 256*15);
        headerRow.createCell(5).setCellValue("author name");
        int rowNum = 1;
        for (MessageTemplate messageTemplate : messageTemplates) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(messageTemplate.getId());
            row.createCell(1).setCellValue(messageTemplate.getEvent());
            row.createCell(2).setCellValue(messageTemplate.getText());
            Cell dateCell = row.createCell(3);
            dateCell.setCellValue(messageTemplate.getEditDate());
            dateCell.setCellStyle(dateCellStyle);
            if (messageTemplate.getAuthorUser()!=null) {
                Cell idCell = row.createCell(4);
                idCell.setCellValue(messageTemplate.getAuthorUser().getId());
                idCell.setCellStyle(cellStyle);
                row.createCell(5).setCellValue(messageTemplate.getAuthorUser().getName());
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream;
    }

    /**
     * Сохранение шаблона сообщения
     * @param Dto dto-объект шаблона сообщения
     * @throws Exception не найден пользователь
     */
    @Override
    public void save(MessageTemplateDto Dto) throws Exception {
        MessageTemplate messageTemplate = modelMapper.map(Dto,MessageTemplate.class);
        if (Dto.getIdAuthorUser()!=null) {
            User user = userService.findById(Dto.getIdAuthorUser());
            if (user != null) {
                messageTemplate.setAuthorUser(user);
                messageTemplateRepository.save(messageTemplate);
            }
            else throw new Exception("Пользователь не найден.");
        }
    }

    /**
     * Получение шаблона сообщения по id
     * @param id id шаблона
     * @return dto-объект шаблона сообщения
     */
    @Override
    public MessageTemplateDto get(Long id){
        MessageTemplateDto messageTemplateDto = null;
        Optional<MessageTemplate> messageTemplateOptional = messageTemplateRepository.findById(id);
        if(messageTemplateOptional.isPresent()){
            MessageTemplate messageTemplate = messageTemplateOptional.get();
            messageTemplateDto = MessageTemplateDto.builder().id(messageTemplate.getId())
                    .text(messageTemplate.getText()).editDate(messageTemplate.getEditDate())
                    .event(messageTemplate.getEvent()).build();
            if (messageTemplate.getAuthorUser()!=null) messageTemplateDto.setIdAuthorUser(messageTemplate.getAuthorUser().getId());
        }
        return messageTemplateDto;
    }

    /**
     * Получение шаблона сообщения по событию
     * @param event строка-событие
     * @return dto-объект шаблона сообщения
     */
    public MessageTemplateDto findByEvent(String event){
        MessageTemplateDto messageTemplateDto = null;
        Optional<MessageTemplate> messageTemplateOptional = messageTemplateRepository.findByEvent(event);
        if(messageTemplateOptional.isPresent()){
            MessageTemplate messageTemplate = messageTemplateOptional.get();
            messageTemplateDto = MessageTemplateDto.builder().id(messageTemplate.getId())
                    .text(messageTemplate.getText()).editDate(messageTemplate.getEditDate())
                    .event(messageTemplate.getEvent()).build();
            if (messageTemplate.getAuthorUser()!=null) messageTemplateDto.setIdAuthorUser(messageTemplate.getAuthorUser().getId());
        }
        return messageTemplateDto;
    }
}
