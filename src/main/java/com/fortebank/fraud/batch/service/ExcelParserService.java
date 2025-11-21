package com.fortebank.fraud.batch.service;

import com.fortebank.fraud.transaction.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class ExcelParserService {
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Парсит Excel файл и возвращает список транзакций
     */
    public List<Transaction> parseExcelFile(MultipartFile file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Пропускаем заголовок
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }
            
            int rowNumber = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                try {
                    Transaction transaction = parseRow(row);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                } catch (Exception e) {
                    log.warn("Ошибка парсинга строки {}: {}", rowNumber, e.getMessage());
                }
                
                rowNumber++;
            }
        }
        
        log.info("Успешно распарсено {} транзакций из файла {}", 
                 transactions.size(), file.getOriginalFilename());
        
        return transactions;
    }
    
    /**
     * Парсит одну строку Excel
     */
    private Transaction parseRow(Row row) {
        try {
            // cs_Clnt_Id (колонка 0)
            String customerId = getCellValueAsString(row.getCell(0));
            if (customerId == null || customerId.trim().isEmpty()) {
                return null;
            }
            
            // transdatetime (колонка 2)
            String dateTimeStr = getCellValueAsString(row.getCell(2));
            LocalDateTime transactionDateTime = parseDateTime(dateTimeStr);
            
            // amount (колонка 3)
            BigDecimal amount = getCellValueAsBigDecimal(row.getCell(3));
            
            // docno (колонка 4)
            String transactionId = getCellValueAsString(row.getCell(4));
            
            // direction (колонка 5)
            String recipientId = getCellValueAsString(row.getCell(5));
            
            // target (колонка 6)
            Integer target = getCellValueAsInteger(row.getCell(6));
            Boolean isFraud = target != null && target == 1;
            
            return Transaction.builder()
                    .customerId(customerId)
                    .transactionDateTime(transactionDateTime)
                    .amount(amount)
                    .transactionId(transactionId)
                    .recipientId(recipientId)
                    .isFraud(isFraud)
                    .build();
                    
        } catch (Exception e) {
            log.error("Ошибка парсинга строки: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Получить значение ячейки как String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * Получить значение ячейки как BigDecimal
     */
    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Получить значение ячейки как Integer
     */
    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Парсит дату и время
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            // Формат: '2025-01-05 16:32:02.000'
            dateTimeStr = dateTimeStr.replace("'", "").trim();
            return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Не удалось распарсить дату: {}. Используем текущую дату.", dateTimeStr);
            return LocalDateTime.now();
        }
    }
    
    /**
     * Валидация Excel файла
     */
    public boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        return filename != null && 
               (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }
}