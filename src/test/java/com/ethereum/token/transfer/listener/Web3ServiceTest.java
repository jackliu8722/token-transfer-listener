package com.ethereum.token.transfer.listener;

import org.junit.Test;

/**
 * Created by jackliu on 2018/4/9.
 */
public class Web3ServiceTest {

    @Test
    public void test(){
        Web3jService service = new Web3jService();
        service.init(1,"http://127.0.0.1:8546","0x3959413ceae81db48d8161e939501c8f994acdaf",10);
        service.start((from,to,value,time,fromValue,toValue,txHash) -> {
            System.out.println("from:" + from + ", to:" + to + ", value:" + value.doubleValue() + ", time:" + time
            + ", fromValue:" + fromValue.doubleValue() + ", toValue:" + toValue.doubleValue() + ", txHash:" + txHash);
        });
    }
}
