package com.mei.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    /**
     * 激活的功能：
     * 0：表示进行差异分析
     * 1：表示合并TP差异表
     * 2：表示合并清算对账单
     * 3: 表示合并数据（通用）
     * 4：表示合并清算对账单和退货TP表
     */
    public static int ACTIVE_TASK = 3;

    /**
     * 生成文件的地址
     */
    public static String TARGET_EXCEL_ADDRESS = "../";

    /**
     * TP差异表的文件地址
     */
    public static String TP_DIFFERENCE_EXCEL_ADDRESS =  TARGET_EXCEL_ADDRESS + "TP差异表";

    /**
     * 清算对账单文件地址
     */
    public static String CLEARING_STATEMENT_EXCEL_ADDRESS = TARGET_EXCEL_ADDRESS + "清算对账单";

    /**
     * 退货TP表地址
     */
    public static String RETURENS_TP_EXCEL_ADDRESS = TARGET_EXCEL_ADDRESS + "退货TP表";

    /**
     * 支付类型
     */
    public static List<String> TP_TYPE = Arrays.asList("易付宝", "易付宝支付优惠");

    /**
     * 最多组合的元素个数
     */
    public static int MAX_LENGTH = 5;

    /**
     * 差异核对表名称
     */
    public static String RESOURCE_FILE_NAME = "差异核对表.xlsx";

    /**
     * 生成的文件名称
     */
    public static String TARGET_FILE_NAME = "差异核对分析表.xlsx";

    /**
     * 清算对账单汇总表文件名称
     */
    public static String CLEARING_STATEMENT_FILE_NAME = "清算对账单汇总表.xlsx";

    /**
     * 汇总表文件名称
     */
    public static String SUMMARY_TABLE_FILE_NAME = "汇总表.xlsx";

    /**
     * 汇总表2文件名称
     */
    public static String SUMMARY_TABLE_FILE_NAME_2 = "退货差异汇总表.xlsx";

    /**
     * 差异分析模板文件名称
     */
    public static String MODEL_DIFFERENCE_CHECK_FILE_NAME = "model_difference_check.xlsx";

    /**
     * 清算对账单模板文件名称
     */
    public static String MODEL_CLEARING_STATEMENT_FILE_NAME = "model_clearing_statement.xlsx";

    /**
     * 汇总表模板文件名称
     */
    public static String MODEL_SUMMARY_TABLE_FILE_NAME = "model_summary_table.xlsx";

    /**
     * 汇总表2模板文件名称
     */
    public static String MODEL_SUMMARY_TABLE_FILE_NAME_2 = "model_summary_table2.xlsx";

    /**
     * 日志日期格式
     */
    public static String LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 自定义参数
     */
    public static Map<String, String> PARAMS_MAP = new HashMap<>();

    /**
     * 合并数据的源文件夹
     */
    public static String TARGET_DIR = "需要合并的数据";
}
