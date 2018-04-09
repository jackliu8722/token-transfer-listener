# token-transfer-listener
Token transfer listener

## Starting

```java
       Web3jService service = new Web3jService();
       service.init(1,"http://127.0.0.1:8546","0x3959413ceae81db48d8161e939501c8f994acdaf",10);
       service.start((from,to,value,time) -> {
           System.out.println("from:" + from + ", to:" + to + ", value:" + value + ", time:" + time);
       });

```
