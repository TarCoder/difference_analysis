package com.mei.util;

import com.csvreader.CsvReader;
import com.mei.config.Config;
import com.mei.entity.ClearingStatement;
import com.mei.entity.TpDifference;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {

    public static Workbook readExcel(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new RuntimeException("文件地址为空");
        }
        return ExcelUtil.getWorkbook(filePath);
    }

    private static Workbook getWorkbook(String filePath) {
        String extString = filePath.substring(filePath.lastIndexOf("."));
        InputStream is;
        try {
            is = new FileInputStream(filePath);
            if(".xls".equals(extString)){
                return new HSSFWorkbook(is);
            }else if(".xlsx".equals(extString)){
                return new XSSFWorkbook(is);
            }else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<TpDifference> getCsvDataFromTpDifference(String filePath) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        List<TpDifference> result = new ArrayList<>();
        try {
            CsvReader r = new CsvReader(filePath, ',', Charset.forName("gbk"));
            //读取表头
            r.readHeaders();
            //逐条读取记录，直至读完
            while (r.readRecord()) {
                if ((!Config.TP_TYPE.contains(r.get("TP卡类型")) && !Config.TP_TYPE.contains(r.get("平台卡类型")))
                        || ObjectUtil.isAnyEmpty(r.get("应收数据差异"))) {
                    continue;
                }
                TpDifference tpDifference = new TpDifference();
                tpDifference.storeCode = r.get("Location");
                tpDifference.cardNum = r.get("平台交易卡号");
                tpDifference.tpReference = r.get("TP参考号");
                tpDifference.transactionAmount = new BigDecimal(r.get("应收数据差异"));
                tpDifference.tpType = r.get("TP卡类型");
                tpDifference.plantformCardType = r.get("平台卡类型");
                tpDifference.date = simpleDateFormat.parse(r.get("收银日期").replaceAll("-", "/"));
                result.add(tpDifference);
            }
            r.close();
        } catch (IOException | ParseException e) {
            throw new RuntimeException("解析CSV文件失败");
        }
        return result;
    }

    public static List<ClearingStatement> getCsvDataFromClearingStatement(String filePath) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        List<ClearingStatement> result = new ArrayList<>();
        try {
            CsvReader r = new CsvReader(filePath, ',', Charset.forName("gbk"));
            //读取表头
            r.readHeaders();
            //逐条读取记录，直至读完
            while (r.readRecord()) {
                if ((!Config.TP_TYPE.contains(r.get("数据来源")))) {
                    continue;
                }
                ClearingStatement clearingStatement = new ClearingStatement();
                clearingStatement.storeCode = r.get("门店编码");
                clearingStatement.transactionAmount = new BigDecimal(r.get("交易金额"));
                clearingStatement.date = simpleDateFormat.parse(r.get("交易日期").replaceAll("-", "/"));
                clearingStatement.cardNum = r.get("卡号");
                clearingStatement.dataResource = r.get("数据来源");
                clearingStatement.dataTable = "清算对账单";
                if (clearingStatement.transactionAmount.compareTo(BigDecimal.ZERO) < 0) {
                    clearingStatement.transactionAmount = clearingStatement.transactionAmount.negate();
                    result.add(clearingStatement);
                }
            }
            r.close();
        } catch (IOException | ParseException e) {
            throw new RuntimeException("解析CSV文件失败");
        }
        return result;
    }

    public static String getCellValue(Row row, int num) {
        String value = "";
        if (row == null || num < 0 || num > row.getLastCellNum()) {
            return value;
        }
        Cell cell = row.getCell(num);
        switch (cell.getCellTypeEnum()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                value = String.valueOf(cell.getNumericCellValue());
                break;
            default:
        }
        return value;
    }

}
