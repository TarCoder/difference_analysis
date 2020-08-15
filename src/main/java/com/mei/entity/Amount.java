package com.mei.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Amount implements Comparable<Amount> {

    /**
     * 门店编号
     */
    public String storeCode;

    /**
     * 交易金额
     */
    public BigDecimal transactionAmount;

    /**
     * 交易日期
     */
    public Date date;

    /**
     * 交易号
     */
    public String cardNum;

    /**
     * 数据来源表
     */
    public String dataTable;

    @Override
    public int compareTo(Amount o) {
        return this.transactionAmount.compareTo(o.transactionAmount);
    }

}
