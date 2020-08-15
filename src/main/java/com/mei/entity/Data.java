package com.mei.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Data {

    public static List<DifferenceCheck> differenceCheckList = new ArrayList<>();

    public static List<TpDifference> tpDifferenceList = new ArrayList<>();

    public static List<ClearingStatement> clearingStatementList = new ArrayList<>();

    public static List<ReturnsTp> returnsTpList = new ArrayList<>();

    public static Map<String, List<? extends Amount>> targetDifferenceCheckData = new HashMap<>();
}
