package com.ethereum.token.transfer.listener;

import java.util.Date;

/**
 * Created by jackliu on 2018/4/9.
 */
@FunctionalInterface
interface ICallBack {
    void callBack(String from,String to,double value,long timestamp);
}
