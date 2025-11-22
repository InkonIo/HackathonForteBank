package com.fortebank.fraud.batch.service;

import com.fortebank.fraud.customer.entity.CustomerBehaviorPattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // <-- ДОБАВЛЕН ЭТОТ ИМПОРТ
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class BehaviorPatternParserService {
    
    // ИСПРАВЛЕН ФОРМАТ ДАТЫ
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Парсит Excel файл с поведенческими паттернами
     */
    public List<CustomerBehaviorPattern> parseExcelFile(MultipartFile file) throws IOException {
        List<CustomerBehaviorPattern> patterns = new ArrayList<>();
        
        log.info("Начинаем парсинг Excel файла: {}", file.getOriginalFilename());
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Пропускаем заголовок
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                log.info("Заголовок: первая ячейка = {}", 
                        getCellValueAsString(headerRow.getCell(0)));
            }
            
            int rowNumber = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                try {
                    CustomerBehaviorPattern pattern = parseRow(row, rowNumber);
                    if (pattern != null) {
                        patterns.add(pattern);
                        
                        if (rowNumber <= 3) {
                            log.info("Строка {}: customerId={}, date={}", 
                                    rowNumber, pattern.getCustomerId(), pattern.getTransDate());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Ошибка парсинга строки {}: {}", rowNumber, e.getMessage());
                }
                
                rowNumber++;
            }
        }
        
        log.info("Успешно распарсено {} поведенческих паттернов из файла {}", 
                 patterns.size(), file.getOriginalFilename());
        
        return patterns;
    }
    
    /**
     * Парсит одну строку Excel
     */
    private CustomerBehaviorPattern parseRow(Row row, int rowNumber) {
        try {
            // Проверяем что строка не пустая
            if (row.getCell(0) == null && row.getCell(1) == null) {
                return null;
            }
            
            // Дата (колонка 0)
            String dateStr = getCellValueAsString(row.getCell(0));
            LocalDate transDate = parseDate(dateStr);
            
            // ID клиента (колонка 1)
            String customerId = getCellValueAsString(row.getCell(1));
            if (customerId == null || customerId.trim().isEmpty()) {
                return null;
            }
            
            return CustomerBehaviorPattern.builder()
                    .transDate(transDate)
                    .customerId(customerId)
                    .uniqueOsVersions30d(getCellValueAsInteger(row.getCell(2)))
                    .uniquePhoneModels30d(getCellValueAsInteger(row.getCell(3)))
                    .latestPhoneModel(getCellValueAsString(row.getCell(4)))
                    .latestOsVersion(getCellValueAsString(row.getCell(5)))
                    .loginsLast7Days(getCellValueAsInteger(row.getCell(6)))
                    .loginsLast30Days(getCellValueAsInteger(row.getCell(7)))
                    .avgLoginsPerDay7d(getCellValueAsBigDecimal(row.getCell(8)))
                    .avgLoginsPerDay30d(getCellValueAsBigDecimal(row.getCell(9)))
                    .loginFreqChangeRatio(getCellValueAsBigDecimal(row.getCell(10)))
                    .loginRatio7d30d(getCellValueAsBigDecimal(row.getCell(11)))
                    .avgSessionIntervalSec(getCellValueAsBigDecimal(row.getCell(12)))
                    .sessionIntervalStd(getCellValueAsBigDecimal(row.getCell(13)))
                    .sessionIntervalVariance(getCellValueAsBigDecimal(row.getCell(14)))
                    .expWeightedAvgInterval(getCellValueAsBigDecimal(row.getCell(15)))
                    .burstinessScore(getCellValueAsBigDecimal(row.getCell(16)))
                    .fanoFactor(getCellValueAsBigDecimal(row.getCell(17)))
                    .intervalZscore(row.getLastCellNum() > 18 ? 
                            getCellValueAsBigDecimal(row.getCell(18)) : BigDecimal.ZERO)
                    .build();
                    
        } catch (Exception e) {
            log.error("Ошибка парсинга строки {}: {}", rowNumber, e.getMessage());
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
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                // Используем DataFormatter для корректного преобразования числовых значений в строки
                DataFormatter formatter = new DataFormatter();
                return formatter.formatCellValue(cell);
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
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty() || value.equals("-1.0")) {
                    return BigDecimal.ZERO;
                }
                return new BigDecimal(value);
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
            return 0;
        }
        
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        return 0;
    }
    
    /**
     * Парсит дату
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDate.now();
        }
        
        try {
            // УДАЛЯЕМ КАВЫЧКИ И ПАРСИМ КАК LocalDateTime, ЗАТЕМ КОНВЕРТИРУЕМ В LocalDate
            String cleanedDateStr = dateStr.trim().replace("'", "");
            return LocalDateTime.parse(cleanedDateStr, DATE_FORMATTER).toLocalDate();
        } catch (Exception e) {
            log.warn("Не удалось распарсить дату: {}. Используем текущую дату.", dateStr);
            return LocalDate.now();
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
