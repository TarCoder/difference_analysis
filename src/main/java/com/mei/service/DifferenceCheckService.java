package com.mei.service;

import com.mei.config.Config;
import com.mei.entity.Data;
import com.mei.entity.DifferenceCheck;
import com.mei.util.ExcelUtil;
import com.mei.util.ObjectUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DifferenceCheckService {

    private int buNum = -1;
    private int storeCodeNum = -1;
    private int transactionDateNum = -1;
    private int differenceDiscriptionNum = -1;
    private int amountNum = -1;
    private int noteNum = -1;
    private int orderNum = -1;

    public void parseExcelToDifferenceCheck() {
        if (Config.ACTIVE_TASK != 0) {
            return;
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始解析差异核对表...");
        Workbook wb = ExcelUtil.readExcel(Config.TARGET_EXCEL_ADDRESS + "/" + Config.RESOURCE_FILE_NAME);
        Sheet sheet = wb.getSheet("差异核对表");
        if (sheet == null || sheet.getLastRowNum() < 2) {
            throw new RuntimeException("差异核对表中无数据");
        }
        int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 1) {
            return;
        }
        parseNum(sheet);
        for (int i = 2; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row.getCell(storeCodeNum) == null || ObjectUtil.isAnyEmpty(row.getCell(storeCodeNum).getStringCellValue())) {
                continue;
            }
            DifferenceCheck differenceCheck = new DifferenceCheck();
            differenceCheck.bu = ExcelUtil.getCellValue(row, buNum);
            differenceCheck.storeCode = ExcelUtil.getCellValue(row, storeCodeNum);
            differenceCheck.transactionDate = row.getCell(transactionDateNum) == null ? null : row.getCell(transactionDateNum).getDateCellValue();
            differenceCheck.differenceDiscription = ExcelUtil.getCellValue(row, differenceDiscriptionNum);
            differenceCheck.amount = new BigDecimal(ExcelUtil.getCellValue(row, amountNum));
            differenceCheck.note = ExcelUtil.getCellValue(row, noteNum);
            differenceCheck.orderNum = ExcelUtil.getCellValue(row, orderNum);
            if (ObjectUtil.isAnyEmpty(differenceCheck.orderNum) && ObjectUtil.isAnyEmpty(differenceCheck.differenceDiscription) && ObjectUtil.isAnyEmpty(differenceCheck.note)) {
                Data.differenceCheckList.add(differenceCheck);
            }
        }
        Collections.sort(Data.differenceCheckList);
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 差异核对表解析完成");
    }

    private void parseNum(Sheet sheet) {
        Row sheetRow = sheet.getRow(1);
        short lastCellNum = sheetRow.getLastCellNum();
        for (short s = 0; s < lastCellNum; s++) {
            Cell cell = sheetRow.getCell(s);
            if (!cell.getCellTypeEnum().equals(CellType.STRING)) {
                continue;
            }
            String stringCellValue = cell.getStringCellValue();
            switch (stringCellValue) {
                case "BU":
                    buNum = s;
                    break;
                case "店代码":
                    storeCodeNum = s;
                    break;
                case "原交易日期":
                    transactionDateNum = s;
                    break;
                case "差异解释":
                    differenceDiscriptionNum = s;
                    break;
                case "金额":
                    amountNum = s;
                    break;
                case "备注":
                    noteNum = s;
                    break;
                case "交易号（2019开头28位）":
                    orderNum = s;
                    break;
                default:
            }
        }
    }

    public void exportDataToExcel(List<DifferenceCheck> exportData) throws IOException {
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始导出数据到\"" + Config.TARGET_FILE_NAME + "\"...");
        if (ObjectUtil.isAnyEmpty(exportData)) {
            throw new RuntimeException("导出数据为空");
        }
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(Config.MODEL_DIFFERENCE_CHECK_FILE_NAME);
        if (resourceAsStream == null) {
            return;
        }
        Workbook wb = new XSSFWorkbook(resourceAsStream);
        Sheet sheet = wb.getSheet("差异核对表");
        CellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("yyyy/MM/dd"));
        for (int i = 0; i < exportData.size(); i++) {
            DifferenceCheck differenceCheck = exportData.get(i);
            Row row = sheet.createRow(i + 2);
            row.createCell(buNum).setCellValue(differenceCheck.bu);
            row.createCell(storeCodeNum).setCellValue(differenceCheck.storeCode);
            Cell cell = row.createCell(transactionDateNum);
            cell.setCellValue(differenceCheck.transactionDate);
            cell.setCellStyle(cellStyle);
            row.createCell(differenceDiscriptionNum).setCellValue(differenceCheck.differenceDiscription);
            row.createCell(amountNum).setCellValue(differenceCheck.amount.doubleValue());
            row.createCell(noteNum).setCellValue(differenceCheck.note);
            row.createCell(orderNum).setCellValue(differenceCheck.orderNum);
        }
        try {
            File file = new File(Config.TARGET_EXCEL_ADDRESS + "/" + Config.TARGET_FILE_NAME);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = new FileOutputStream(Config.TARGET_EXCEL_ADDRESS + "/" + Config.TARGET_FILE_NAME);
            wb.write(out);
            System.err.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 已导出数据到文件：" + file.getCanonicalPath());
        } catch (IOException e) {
            System.out.println("导出文件失败");
        }
    }
}
