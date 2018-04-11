package com.ethereum.token.transfer.listener;

import org.junit.Test;

/**
 * Created by jackliu on 2018/4/9.
 */
public class Web3ServiceTest {

    @Test
    public void test(){
        Web3jService service = new Web3jService();
        service.init(4851647,"http://35.194.247.139:8545","0x5A9bF6bADCd24Fe0d58E1087290c2FE2c728736a",10);
//        service.start((from,to,value,time,fromValue,toValue,txHash) -> {
//            System.out.println("from:" + from + ", to:" + to + ", value:" + value.doubleValue() + ", time:" + time
//            + ", fromValue:" + fromValue.doubleValue() + ", toValue:" + toValue.doubleValue() + ", txHash:" + txHash);
//        });

        String addr = "0x9e0ad2e988d64e83af7fe251dfe2ab9ecdd4ca06";
        System.out.println(service.balanceOf(addr));
    }
}
