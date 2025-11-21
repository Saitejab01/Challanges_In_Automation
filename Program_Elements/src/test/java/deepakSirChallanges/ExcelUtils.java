package deepakSirChallanges;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ExcelUtils {

    private String filePath;
    private String sheetName;
    private Workbook workbook;
    private Sheet sheet;
    private Map<String, Integer> headerMap = new HashMap<>();

    public ExcelUtils(String filePath, String sheetName) {
        this.filePath = filePath;
        this.sheetName = sheetName;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheet(sheetName);
            if (sheet == null)
                throw new RuntimeException("❌ Sheet not found: " + sheetName);

            loadHeaders();
        } catch (IOException e) {
            throw new RuntimeException("❌ Unable to load Excel file: " + filePath, e);
        }
    }

    /** Load header names from the first row into a map */
    private void loadHeaders() {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null)
            throw new RuntimeException("❌ No header row found in sheet: " + sheetName);

        for (Cell cell : headerRow) {
            headerMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }
    }

    /** Get cell data by header name */
    public String getCellDataByHeader(int rowNum, String headerName) {
        if (!headerMap.containsKey(headerName))
            throw new RuntimeException("❌ Header not found: " + headerName);

        int colNum = headerMap.get(headerName);
        Row row = sheet.getRow(rowNum);
        if (row == null) return "";

        Cell cell = row.getCell(colNum);
        if (cell == null) return "";

        return new DataFormatter().formatCellValue(cell);
    }

    /** Set cell data by header name */
    public void setCellDataByHeader(int rowNum, String headerName, String value) {
        if (!headerMap.containsKey(headerName))
            throw new RuntimeException("❌ Header not found: " + headerName);

        int colNum = headerMap.get(headerName);
        Row row = sheet.getRow(rowNum);
        if (row == null) row = sheet.createRow(rowNum);

        Cell cell = row.getCell(colNum);
        if (cell == null) cell = row.createCell(colNum);

        cell.setCellValue(value);
        saveFile();
    }

    /** Save the workbook */
    private void saveFile() {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to save Excel file: " + filePath, e);
        }
    }

    /** Close the workbook */
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
