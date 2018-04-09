package com.ethereum.token.transfer.listener;

import java.math.BigDecimal;

/**
 * Created by jackliu on 2018/4/9.
 */
@FunctionalInterface
interface ICallBack {

    /**
     *
     * @param from The address of from
     * @param to The address of to
     * @param value The value of the transfer
     * @param timestamp timestamp
     * @param fromValue The token balance of the address from
     * @param toValue The token balance of the address to
     * @param txHash The hash of the transaction
     */
    void callBack(String from,
                  String to,
                  BigDecimal value,
                  long timestamp,
                  BigDecimal fromValue,
                  BigDecimal toValue,
                  String txHash);
}
