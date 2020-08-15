package com.mei.util;

import com.mei.entity.Amount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MathUtil {

    public static BigDecimal count(Amount... amounts) {
        if (amounts == null || amounts.length == 0) {
            return new BigDecimal(0);
        }
        BigDecimal result = new BigDecimal(0);
        for (Object obj : amounts) {
            if (!(obj instanceof Amount)) {
                continue;
            }
            Amount amount = (Amount)obj;
            result = result.add(amount.transactionAmount);
        }
        return result;
    }

    public static List<Amount> getGroup(int[] indexArr, List<? extends Amount> amountList, BigDecimal target, int maxLength) {
        List<Amount> result = new ArrayList<>();
        if (ObjectUtil.isAnyEmpty(amountList, target) || maxLength > indexArr.length) {
            throw new RuntimeException("参数不对");
        }
        reSetIndexArr(indexArr);
        while (indexArr[0] < amountList.size() - maxLength + 1) {
            result.clear();
            for (int i = 0; i < maxLength; i++) {
                result.add(amountList.get(indexArr[i]));
            }
            BigDecimal count = count(result.toArray(new Amount[]{}));
            if (count.compareTo(target) == 0) {
                return result;
            }
            if (indexArr[0] == amountList.size() - maxLength) {
                break;
            }
            addOne(indexArr, amountList.size(), maxLength, 1);
        }
        return new ArrayList<>();
    }

    private static void addOne(int[] indexArr, int size, int maxLength, int index) {
        if (maxLength < index) {
            return;
        }
        if (indexArr[maxLength - index] < size - index) {
            indexArr[maxLength - index] += 1;
        } else {
            if (maxLength == index) {
                return;
            }
            addOne(indexArr, size, maxLength, index + 1);
            indexArr[maxLength - index] = indexArr[maxLength - index - 1] + 1;
        }
    }

    private static void reSetIndexArr(int[] indexArr) {
        for (int i = 0; i < indexArr.length; i++) {
            indexArr[i] = i;
        }
    }
}
