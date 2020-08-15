package com.mei.service;

import com.mei.config.Config;
import com.mei.entity.Data;
import com.mei.entity.ClearingStatement;
import com.mei.entity.DifferenceCheck;
import com.mei.util.ExcelUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class ClearingStatementService {

    public void parseExcelToClearingStatement() {
        if (Config.ACTIVE_TASK != 0 && Config.ACTIVE_TASK != 2 && Config.ACTIVE_TASK != 4) {
            return;
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始解析清算对账单...");
        File dir = new File(Config.CLEARING_STATEMENT_EXCEL_ADDRESS);
        if (!dir.isDirectory()) {
            throw new RuntimeException("清算对账单文件夹地址错误");
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            List<ClearingStatement> clearingStatementList = ExcelUtil.getCsvDataFromClearingStatement(file.getPath());
            Data.clearingStatementList.addAll(clearingStatementList);
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 解析清算对账单完成");
    }

    public void exportDataToExcel() throws IOException {
        if (Config.ACTIVE_TASK != 2) {
            return;
        }
        if (Data.clearingStatementList.isEmpty()) {
            System.err.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 导出数据为空");
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始导出数据到\"" + Config.CLEARING_STATEMENT_FILE_NAME + "\"...");
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(Config.MODEL_CLEARING_STATEMENT_FILE_NAME);
        if (resourceAsStream == null) {
            return;
        }
        Workbook wb = new XSSFWorkbook(resourceAsStream);
        Sheet sheet = wb.getSheet("清算对账单汇总表");
        CellStyle cellStyle = wb.createCellStyle();
        DataFormat dataFormat = wb.createDataFormat();
        cellStyle.setDataFormat(dataFormat.getFormat("yyyy/MM/dd"));
        for (int i = 0; i < Data.clearingStatementList.size(); i++) {
            ClearingStatement clearingStatement = Data.clearingStatementList.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(clearingStatement.storeCode);
            Cell cell = row.createCell(1);
            cell.setCellValue(clearingStatement.date);
            cell.setCellStyle(cellStyle);
            row.createCell(2).setCellValue(clearingStatement.transactionAmount.negate().doubleValue());
            row.createCell(3).setCellValue(clearingStatement.cardNum);
            row.createCell(4).setCellValue(clearingStatement.dataResource);
        }
        try {
            File file = new File(Config.TARGET_EXCEL_ADDRESS + "/" + Config.CLEARING_STATEMENT_FILE_NAME);
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
