package com.mei;

import com.mei.config.Config;
import com.mei.entity.*;
import com.mei.service.*;
import com.mei.util.MathUtil;
import com.mei.util.ObjectUtil;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class App {

    private static DifferenceCheckService differenceCheckService = new DifferenceCheckService();

    private static TpDifferenceService tpDifferenceService = new TpDifferenceService();

    private static ClearingStatementService clearingStatementService = new ClearingStatementService();

    private static ReturnsTpService returnsTpService = new ReturnsTpService();

    private static DataHandleService dataHandleService = new DataHandleService();

    public static void main(String[] args) throws ParseException, IOException {
        parse(args);
        handleTask();
    }

    private static void handleTask() throws ParseException, IOException {
        if (Config.ACTIVE_TASK >= 0 && Config.ACTIVE_TASK <= 2) {
            initData();
            handleData();
            exportDataToExcel();
        }  else if (Config.ACTIVE_TASK == 3) {
            dataHandleService.mergeData();
            dataHandleService.exportData();
        }  else if (Config.ACTIVE_TASK == 4) {
            clearingStatementService.parseExcelToClearingStatement();
            returnsTpService.parseExcelToReturnsTp();
            dataHandleService.exportDataForClearingStatementAndReturensTp();
        }

    }

    private static void parse(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        for (String arg : args) {
            String[] split = arg.split("=");
            if (split.length != 2) {
                continue;
            }
            if (Objects.equals(split[0], "activeTask") && !ObjectUtil.isAnyEmpty(split[1])) {
                Config.ACTIVE_TASK = Integer.parseInt(split[1]);
                continue;
            }
            Config.PARAMS_MAP.put(split[0].trim(), split[1].trim());
        }
    }

    private static void exportDataToExcel() throws IOException {
        if (Config.ACTIVE_TASK == 0) {
            exportDifferenceCheckData();
        } else if (Config.ACTIVE_TASK == 2) {
            exportClearingStatementData();
        }
    }

    private static void exportClearingStatementData() throws IOException {
        clearingStatementService.exportDataToExcel();
    }

    private static void exportDifferenceCheckData() throws IOException {
        List<DifferenceCheck> exportData = new ArrayList<>();
        Map<String, DifferenceCheck> differenceCheckMap = Data.differenceCheckList.stream().collect(Collectors.toMap(differenceCheck -> differenceCheck.storeCode + differenceCheck.transactionDate, it -> it));
        for (Map.Entry<String, List<? extends Amount>> amountEntry : Data.targetDifferenceCheckData.entrySet()) {
            if (!ObjectUtil.isAnyEmpty(amountEntry.getValue())) {
                for (Amount amount : amountEntry.getValue()) {
                    exportData.add(formatDataToDifferenceCheck(amount, differenceCheckMap.get(amount.storeCode + amount.date)));
                }
            } else {
                exportData.add(differenceCheckMap.get(amountEntry.getKey()));
            }
        }
        differenceCheckService.exportDataToExcel(exportData);
    }

    private static DifferenceCheck formatDataToDifferenceCheck(Amount amount, DifferenceCheck differenceCheck) {
        DifferenceCheck newDifferenceCheck = new DifferenceCheck(differenceCheck.storeCode, differenceCheck.bu);
        if (amount instanceof TpDifference) {
            TpDifference tpDifference = (TpDifference)amount;
            newDifferenceCheck.orderNum = getOrderNum(tpDifference);
            newDifferenceCheck.differenceDiscription = amount.transactionAmount.compareTo(BigDecimal.ZERO) > 0 ? "待门店核实" : "TP数据缺失";
        } else if (amount instanceof ClearingStatement) {
            ClearingStatement clearingStatement = (ClearingStatement) amount;
            newDifferenceCheck.orderNum = clearingStatement.cardNum;
            newDifferenceCheck.differenceDiscription= "TP退货缺失";
        } else if (amount instanceof ReturnsTp) {
            ReturnsTp returnsTp = (ReturnsTp) amount;
            newDifferenceCheck.orderNum = returnsTp.cardNum;
            newDifferenceCheck.differenceDiscription= "退货";
        }
        newDifferenceCheck.transactionDate = amount.date;
        newDifferenceCheck.amount = amount.transactionAmount;
        newDifferenceCheck.note = "";
        return newDifferenceCheck;
    }

    private static String getOrderNum(TpDifference tpDifference) {
        return Objects.equals(tpDifference.cardNum.trim(), "'") ? tpDifference.tpReference : tpDifference.cardNum;
    }

    private static void handleData() {
        if (Config.ACTIVE_TASK != 0) {
            return;
        }
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 开始匹配数据...");
        System.out.println("----------------------------------------------------------------------------------------------------");
        List<Thread> taskList = new ArrayList<>();
        for (DifferenceCheck differenceCheck : Data.differenceCheckList) {
            Thread task = new Thread(() -> {
                List<TpDifference> storeTpDifferenceList = Data.tpDifferenceList.stream()
                        .filter(it -> Objects.equals(it.storeCode + it.date, differenceCheck.storeCode + differenceCheck.transactionDate))
                        .collect(Collectors.toList());
                List<ClearingStatement> clearingStatementList = Data.clearingStatementList.stream()
                        .filter(it -> Objects.equals(it.storeCode + it.date, differenceCheck.storeCode + differenceCheck.transactionDate))
                        .collect(Collectors.toList());
                List<ReturnsTp> returnsTpList = Data.returnsTpList.stream()
                        .filter(it -> Objects.equals(it.storeCode + it.date, differenceCheck.storeCode + differenceCheck.transactionDate))
                        .collect(Collectors.toList());
                removeRedundantDataFromStoreTpDifference(storeTpDifferenceList, clearingStatementList, returnsTpList);
                List<Amount> tatolData = new ArrayList<>();
                tatolData.addAll(storeTpDifferenceList);
                tatolData.addAll(clearingStatementList);
                tatolData.addAll(returnsTpList);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
                String message = String.format("门店编号：[%s]，日期[%s]，TP差异表[%s]条，清算对账单[%s]条，退货TP单[%s]条", differenceCheck.storeCode, simpleDateFormat.format(differenceCheck.transactionDate), storeTpDifferenceList.size(), clearingStatementList.size(), returnsTpList.size());
                System.out.println(message);
                List<Amount> group = tpDifferenceService.getStoreTpDifferenceGroup(differenceCheck, tatolData);
                Data.targetDifferenceCheckData.put(differenceCheck.storeCode + differenceCheck.transactionDate, group);
            });
            taskList.add(task);
        }
        taskList.forEach(Thread::start);
        taskList.forEach(it -> {
            try {
                it.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("----------------------------------------------------------------------------------------------------");
        System.out.println(DateFormatUtils.format(new Date(), Config.LOG_DATE_FORMAT) + " >>> 匹配数据完成");
    }

    private static void removeRedundantDataFromStoreTpDifference(List<TpDifference> storeTpDifferenceList, List<ClearingStatement> clearingStatementList, List<ReturnsTp> returnsTpList) {
        List<TpDifference> tpDifferenceWithPlantformCardList = storeTpDifferenceList.stream().filter(it -> !ObjectUtil.isAnyEmpty(it.plantformCardType)).collect(Collectors.toList());
        List<TpDifference> tpDifferenceWithTpTypeList = storeTpDifferenceList.stream().filter(it -> !ObjectUtil.isAnyEmpty(it.tpType)).collect(Collectors.toList());
        if (!ObjectUtil.isAnyEmpty(tpDifferenceWithPlantformCardList, tpDifferenceWithTpTypeList)) {
            for (TpDifference tpDifferenceWithPlantformCard : tpDifferenceWithPlantformCardList) {
                for (TpDifference tpDifferenceWithTpType : tpDifferenceWithTpTypeList) {
                    if (MathUtil.count(tpDifferenceWithPlantformCard, tpDifferenceWithTpType).compareTo(BigDecimal.ZERO) == 0
                            && tpDifferenceWithPlantformCard.plantformCardType.contains(tpDifferenceWithTpType.tpType.substring(1))) {
                        storeTpDifferenceList.remove(tpDifferenceWithPlantformCard);
                        storeTpDifferenceList.remove(tpDifferenceWithTpType);
                        break;
                    }
                }
            }
        }
        if (!ObjectUtil.isAnyEmpty(clearingStatementList, returnsTpList)) {
            List<Amount> listBak1 = new ArrayList<>(clearingStatementList);
            List<Amount> listBak2 = new ArrayList<>(returnsTpList);
            Set<String> cardNumSet = listBak2.stream().map(it -> it.cardNum).collect(Collectors.toSet());
            for (String cardNum : cardNumSet) {
                List<Amount> list1 = listBak1.stream().filter(it -> it.cardNum.contains(cardNum)).collect(Collectors.toList());
                List<Amount> list2 = listBak2.stream().filter(it -> it.cardNum.contains(cardNum)).collect(Collectors.toList());
                if (ObjectUtil.isAnyEmpty(list1, list2)) {
                    continue;
                }
                list1.addAll(list2);
                if (MathUtil.count(list1.toArray(new Amount[]{})).compareTo(BigDecimal.ZERO) == 0) {
                    clearingStatementList.removeAll(list1);
                    returnsTpList.removeAll(list2);
                }
            }
        }
    }

    private static void initData() throws ParseException {
        differenceCheckService.parseExcelToDifferenceCheck();
        tpDifferenceService.parseExcelToTpDifference();
        clearingStatementService.parseExcelToClearingStatement();
        returnsTpService.parseExcelToReturnsTp();
    }
}
