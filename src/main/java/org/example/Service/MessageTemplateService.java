package org.example.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Entity.MessageTemplate;
import org.example.Entity.User;
import org.example.Repository.MessageRepository;
import org.example.Repository.MessageTemplateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Сервис для шаблонов сообщений
 */
@Service
public class MessageTemplateService {
    /**
     * Репозиторий для шаблонов сообщений
     */
    private final MessageTemplateRepository messageTemplateRepository;

    public MessageTemplateService(MessageTemplateRepository messageTemplateRepository) {
        this.messageTemplateRepository = messageTemplateRepository;
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
}
