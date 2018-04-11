package com.ethereum.token.transfer.listener;

import org.junit.Test;

/**
 * Created by jackliu on 2018/4/9.
 */
public class Web3ServiceTest {

    @Test
    public void test(){
        Web3jService service = new Web3jService();
        service.init(5418583,"https://mainnet.infura.io/AtQQwJGCuEE2BRkSZgi5","0x5A9bF6bADCd24Fe0d58E1087290c2FE2c728736a",10,100);
        service.start((from,to,value,time,fromValue,toValue,txHash) -> {
            System.out.println("from:" + from + ", to:" + to + ", value:" + value.doubleValue() + ", time:" + time
            + ", fromValue:" + fromValue.doubleValue() + ", toValue:" + toValue.doubleValue() + ", txHash:" + txHash);
        });

    }
}
