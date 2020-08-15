package com.mei.entity;

import java.io.Serializable;

/**
 * TP差异表
 */
public class TpDifference extends Amount implements Serializable {

    /**
     * TP参考号
     */
    public String tpReference;

    /**
     * TP卡类型
     */
    public String tpType;

    /**
     * 平台卡类型
     */
    public String plantformCardType;

}
