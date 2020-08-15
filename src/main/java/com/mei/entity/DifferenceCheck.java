package com.mei.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 差异核对表
 */
public class DifferenceCheck implements Comparable<DifferenceCheck>, Serializable {

    /**
     * BU号
     */
    public String bu;

    /**
     * 门店编码
     */
    public String storeCode;

    /**
     * 交易日期
     */
    public Date transactionDate;

    /**
     * 差异描述
     */
    public String differenceDiscription;

    /**
     * 交易金额
     */
    public BigDecimal amount;

    /**
     * 描述
     */
    public String note;

    /**
     * 交易号
     */
    public String orderNum;

    public DifferenceCheck() {
    }

    public DifferenceCheck(String storeCode, String bu) {
        this.storeCode = storeCode;
        this.bu = bu;
    }

    @Override
    public int compareTo(DifferenceCheck o) {
        return this.storeCode.compareTo(o.storeCode);
    }
}
