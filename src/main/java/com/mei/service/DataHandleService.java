package com.mei.service;

import com.csvreader.CsvReader;
import com.mei.config.Config;
import com.mei.entity.Amount;
import com.mei.entity.Data;
import com.mei.util.ExcelUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class DataHandleService {

    private List<Map<Integer, Object>> dataMerged = new ArrayList<>();

    private int lastCellNum = 0;

    private List<Integer> dateNum = new ArrayList<>();

    public void mergeData() throws IOException {
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始解析表格数据...");
        File dir = new File(Config.TARGET_EXCEL_ADDRESS + Config.TARGET_DIR);
        if (!dir.isDirectory()) {
            throw new RuntimeException("文件夹地址错误");
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        boolean isFirstExcel = true;
        List<String> csvHeaders = new ArrayList<>();
        for (File file : files) {
            if (file.getName().contains("xls")) {
                Workbook wb = ExcelUtil.readExcel(file.getPath());
                if (wb == null) {
                    continue;
                }
                Sheet sheet = wb.getSheetAt(0);
                if (sheet == null) {
                    continue;
                }
                int lastRowNum = sheet.getLastRowNum();
                if (lastRowNum == 0) {
                    continue;
                }
                if (isFirstExcel) {
                    Row row = sheet.getRow(0);
                    int lastCellNum = row.getLastCellNum();
                    this.lastCellNum = lastCellNum;
                    Map<Integer, Object> data = new HashMap<>();
                    for (int i = 0; i <= lastCellNum; i++) {
                        if (ExcelUtil.getCellValue(row, i).contains("日期")) {
                            dateNum.add(i);
                        }
                        data.put(i, ExcelUtil.getCellValue(row, i));
                    }
                    dataMerged.add(data);
                    isFirstExcel = false;
                }
                for (short i = 1; i < lastRowNum; i++) {
                    Row row = sheet.getRow(i);
                    short lastCellNum = row.getLastCellNum();
                    Map<Integer, Object> data = new HashMap<>();
                    for (int j = 0; j < lastCellNum; j++) {
                        if (dateNum.contains(j)) {
                            data.put(j, row.getCell(j).getDateCellValue());
                        } else {
                            data.put(j, ExcelUtil.getCellValue(row, j));
                        }
                    }
                    dataMerged.add(data);
                }
            } else if (file.getName().contains("csv")) {
                CsvReader csvReader = new CsvReader(file.getCanonicalPath(), ',', Charset.forName("gbk"));
                csvReader.readHeaders();
                int headerCount = csvReader.getHeaderCount();
                if (headerCount == 0 ) {
                    continue;
                }
                lastCellNum = headerCount - 1;
                if (isFirstExcel) {
                    for (int i = 0; i < headerCount; i++) {
                        csvHeaders.add(csvReader.getHeader(i));
                    }
                    isFirstExcel = false;
                }
                while (csvReader.readRecord()) {
                    Map<Integer, Object> data = new HashMap<>();
                    for (int i = 0; i < csvHeaders.size(); i++) {
                        data.put(i, csvReader.get(csvHeaders.get(i)));
                    }
                    dataMerged.add(data);
                }
            }
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 解析表格数据完成");
    }

    public void exportData() throws IOException {
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始导出数据到\"" + Config.SUMMARY_TABLE_FILE_NAME + "\"...");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(Config.MODEL_SUMMARY_TABLE_FILE_NAME);
        if (resourceAsStream == null) {
            return;
        }
        Workbook wb = new XSSFWorkbook(resourceAsStream);
        Sheet sheet = wb.getSheet("汇总表");
        CellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("yyyy/MM/dd"));
        for (int i = 0; i < this.dataMerged.size(); i++) {
            Map<Integer, Object> map = this.dataMerged.get(i);
            Row row = sheet.createRow(i);
            for (int j = 0; j <= lastCellNum; j++) {
                Cell cell = row.createCell(j);
                if (map.get(j) instanceof String) {
                    cell.setCellValue((String) map.get(j));
                } else if (map.get(j) instanceof Date) {
                    cell.setCellValue((Date) map.get(j));
                    cell.setCellStyle(cellStyle);
                }
            }
        }
        try {
            File file = new File(Config.TARGET_EXCEL_ADDRESS + "/" + Config.SUMMARY_TABLE_FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(file.getCanonicalPath());
            wb.write(out);
            System.err.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 已导出数据到文件：" + file.getCanonicalPath());
        } catch (IOException e) {
            System.out.println("导出文件失败");
        }
    }

    public void exportDataForClearingStatementAndReturensTp() throws IOException {
        if (Data.clearingStatementList.isEmpty() && Data.returnsTpList.isEmpty()) {
            System.err.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 导出数据为空");
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始导出数据到\"" + Config.SUMMARY_TABLE_FILE_NAME_2 + "\"...");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(Config.MODEL_SUMMARY_TABLE_FILE_NAME_2);
        if (resourceAsStream == null) {
            return;
        }
        Workbook wb = new XSSFWorkbook(resourceAsStream);
        Sheet sheet = wb.getSheet("汇总表");
        CellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("yyyy/MM/dd"));
        List<Amount> resultData = new ArrayList<>();
        resultData.addAll(Data.clearingStatementList);
        resultData.addAll(Data.returnsTpList);
        for (int i = 0; i < resultData.size(); i++) {
            Amount amount = resultData.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(amount.storeCode);
            Cell cell = row.createCell(1);
            cell.setCellValue(amount.date);
            cell.setCellStyle(cellStyle);
            row.createCell(2).setCellValue(amount.transactionAmount.doubleValue());
            row.createCell(3).setCellValue(amount.cardNum);
            row.createCell(4).setCellValue(amount.dataTable);
        }
        try {
            File file = new File(Config.TARGET_EXCEL_ADDRESS + "/" + Config.SUMMARY_TABLE_FILE_NAME_2);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(file.getCanonicalPath());
            wb.write(out);
            System.err.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 已导出数据到文件：" + file.getCanonicalPath());
        } catch (IOException e) {
            System.out.println("导出文件失败");
        }
    }
}
