package com.mei.service;

import com.mei.config.Config;
import com.mei.entity.Amount;
import com.mei.entity.Data;
import com.mei.entity.DifferenceCheck;
import com.mei.entity.TpDifference;
import com.mei.util.ExcelUtil;
import com.mei.util.MathUtil;
import com.mei.util.ObjectUtil;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TpDifferenceService {

    public void parseExcelToTpDifference() {
        if (Config.ACTIVE_TASK != 0 && Config.ACTIVE_TASK != 1) {
            return;
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始解析TP差异表...");
        File dir = new File(Config.TP_DIFFERENCE_EXCEL_ADDRESS);
        if (!dir.isDirectory()) {
            throw new RuntimeException("TP差异表文件夹地址错误");
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File file : files) {
            List<TpDifference> tpDifferenceList = ExcelUtil.getCsvDataFromTpDifference(file.getPath());
            Data.tpDifferenceList.addAll(tpDifferenceList);
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 解析TP差异表完成");
    }

    public List<Amount> getStoreTpDifferenceGroup(DifferenceCheck differenceCheck, List<Amount> storeTpDifferenceList) {
        if (ObjectUtil.isAnyEmpty(differenceCheck, storeTpDifferenceList) || storeTpDifferenceList.size() > 100) {
            return new ArrayList<>();
        }
        if (differenceCheck.amount.compareTo(MathUtil.count(storeTpDifferenceList.toArray(new Amount[]{}))) == 0) {
            return storeTpDifferenceList;
        }
        int[] indexArr = new int[Config.MAX_LENGTH];
        for (int i =1; i <= Config.MAX_LENGTH; i++) {
            if (i > storeTpDifferenceList.size()) {
                return new ArrayList<>();
            }
            List<Amount> group = MathUtil.getGroup(indexArr, storeTpDifferenceList, differenceCheck.amount, i);
            if (!ObjectUtil.isAnyEmpty(group)) {
                return group;
            }
        }
        return new ArrayList<>();
    }
}
