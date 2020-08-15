package com.mei.service;

import com.mei.config.Config;
import com.mei.entity.Data;
import com.mei.entity.ReturnsTp;
import com.mei.util.ExcelUtil;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReturnsTpService {
    
    public void parseExcelToReturnsTp() throws ParseException {
        if (Config.ACTIVE_TASK != 0 && Config.ACTIVE_TASK != 1 && Config.ACTIVE_TASK != 4) {
            return;
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始解析退货TP表...");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        File dir = new File(Config.RETURENS_TP_EXCEL_ADDRESS);
        if (!dir.isDirectory()) {
            throw new RuntimeException("退货TP表文件夹地址错误");
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            Workbook wb = ExcelUtil.readExcel(file.getPath());
            Sheet sheet = wb.getSheet("卡退货");
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new RuntimeException("退货表中无数据");
            }
            int lastRowNum = sheet.getLastRowNum();
            for (int i = 1; i < lastRowNum; i++) {
                ReturnsTp returnsTp = new ReturnsTp();
                Row row = sheet.getRow(i);
                returnsTp.storeCode = row.getCell(4) != null ? row.getCell(4).getStringCellValue() : "";
                returnsTp.transactionAmount = row.getCell(11) != null ? new BigDecimal(row.getCell(11).getStringCellValue()) : BigDecimal.ZERO;
                returnsTp.date = row.getCell(10) != null ? simpleDateFormat.parse(row.getCell(10).getStringCellValue()) : null;
                returnsTp.cardNum = row.getCell(7) != null ? row.getCell(7).getStringCellValue() : "";
                returnsTp.returnsType = row.getCell(6) != null ? row.getCell(6).getStringCellValue() : "";
                returnsTp.dataTable = "退货TP表";
                if (Config.TP_TYPE.contains(returnsTp.returnsType)) {
                    returnsTp.transactionAmount = returnsTp.transactionAmount.negate();
                    Data.returnsTpList.add(returnsTp);
                }
            }
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 解析退货TP表完成");
    }
    
}
