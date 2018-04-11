package com.ethereum.token.transfer.listener;

import com.ethereum.token.transfer.listener.contract.TOKEN;
import com.ethereum.token.transfer.listener.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

public class Web3jService {

    private final Logger log = LoggerFactory.getLogger(Web3jService.class);

    private Web3j web3;

    private String contractAddress;

    private BigInteger nextBlockNumber;

    private Map<String,Integer> decimals = new HashMap<>();

    private TOKEN token;

    private int retryTimes;

    public void init(long startBlockNumber,String url,String contractAddress,int retryTimes) {

        this.contractAddress = contractAddress.toLowerCase();
        web3 = Web3j.build(new HttpService(url));
        log.info("init web3j success! ");
        nextBlockNumber = BigInteger.valueOf(startBlockNumber);
        this.retryTimes = retryTimes;
        token = createToken(contractAddress);
    }

    private TOKEN createToken(String addr){
        TransactionManager manager = new ClientTransactionManager(web3,addr);
        TOKEN token = TOKEN.load(
                addr, web3, manager, GAS_PRICE, BigInteger.valueOf(4_700_000));
        return token;
    }

    public void start(ICallBack callBack){

        while(true){

            BigInteger last = getLastBlockNumber();
            if(last == null){
                log.error("Cant not get last BlockNumber!");
                Utils.sleep(1000);
                continue;
            }

            last = last.subtract(BigInteger.valueOf(12));
            if(nextBlockNumber.compareTo(last) >= 0){
                Utils.sleep(1000);
                continue;
            }
            log.info("----Sync block number = " + nextBlockNumber.intValue());
            Request<?,EthBlock> ethBlock = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(nextBlockNumber),true);
            try {
                EthBlock.Block block = ethBlock.send().getBlock();
                block.getTransactions().forEach(t -> {
                    Transaction tx = (Transaction)t.get();
                    handleTx(tx,block.getTimestamp().longValue(),callBack);
                });
                log.info("----Completed handle block number = " + nextBlockNumber.intValue());
                nextBlockNumber = nextBlockNumber.add(BigInteger.valueOf(1));
            } catch (IOException e) {
                log.error("",e);
                Utils.sleep(1000);
                continue;
            }
        }
    }

    private int getDecimal(String addr){
        return 18;
    }



    private BigInteger getLastBlockNumber(){
        int times = 0;
        while(times++ < this.retryTimes) {
            try {
                return web3.ethBlockNumber().send().getBlockNumber();
            } catch (Exception e) {
                log.error("", e);
            }
            try{
                Thread.sleep(1000);
            }catch (InterruptedException e){

            }
        }
        return null;
    }

    private boolean isSuccess(String txid,String contractAddr) {
        Request<?, EthGetTransactionReceipt> receiptRequest = web3.ethGetTransactionReceipt(txid);
        try {
            EthGetTransactionReceipt receipt = receiptRequest.send();
            Optional<TransactionReceipt> optional = receipt.getTransactionReceipt();
            if (optional.isPresent()) {
                List<TOKEN.TransferEventResponse> responses = token.getTransferEvents(optional.get());
                return responses.size() > 0;
            }
        } catch (IOException e) {
            log.info("IOExcetion", e);
        }
        return false;
    }

    public TransactionReceipt getTransactionReceipt(String txHash) {

        int times = 0;

        while(times++ < this.retryTimes) {
            try {
                return web3.ethGetTransactionReceipt(txHash).send().getTransactionReceipt().get();
            } catch (IOException e) {
                log.error("getTransactionReceipt", e);
                Utils.sleep(1000);
            }
        }
        return null;

    }

    private InputData deserizeInput(String data) {
        if (data.substring(0, 10).equals("0xa9059cbb")) {
            InputData inputData = new InputData();
            String to = data.substring(34, 74);
            String hexTo = Numeric.prependHexPrefix(to);
            String value = data.substring(74);
            BigInteger valueBigInteger = Numeric.toBigInt(value);
            inputData.setTo(hexTo);
            inputData.setValue(valueBigInteger);
            return inputData;
        }
        return null;
    }

    private void handleTx(Transaction tx ,long timestamp,ICallBack callBack) {
        String from = tx.getFrom();
        String to = tx.getTo();
        if(to == null){
            return;
        }
        if(isContract(to)){
            // 判断是否有这个币种
            if(to == null || !to.toLowerCase().equals(contractAddress)){
                return;
            }

            String input = tx.getInput();
            InputData inputData = deserizeInput(input);
            //不是transfer操作
            if(inputData == null){
                return ;
            }
            if(!isSuccess(tx.getHash(),to)){
                return;
            }
            inputData.setFrom(from);

            int decimal = getDecimal(to);
            BigDecimal value = Utils.valueTransfer(inputData.getValue(),decimal);
            log.info("transfer token transaction:{From: " + from +
                    ",To: " + inputData.getTo() +
                    ",Value: " + value.doubleValue() +
                    ",Hash: " + tx.getHash() +
                    ",ContractAddress: " + to + "}");
            BigDecimal fromValue = Utils.valueTransfer(balanceOf(from),decimal);
            BigDecimal toValue = Utils.valueTransfer(balanceOf(inputData.getTo()),decimal);
            callBack.callBack(from,inputData.getTo(),value,timestamp,fromValue,toValue,tx.getHash());
        }
    }

    private boolean isContract(String addr) {
        int times = 0;
        while(times++ < this.retryTimes) {
            try {
                Request<?, EthGetCode> ethGetCodeRequest = web3.ethGetCode(addr, DefaultBlockParameterName.LATEST);
                EthGetCode ethGetCode = ethGetCodeRequest.send();
                byte[] but = ethGetCode.getCode().getBytes();
                if (but.length > 2) {
                    return true;
                }
            } catch (Exception e) {
                log.error("",e);
                Utils.sleep(500);
            }
        }
        return false;
    }

    public BigInteger balanceOf(String addr){
        int times = 0;
        BigInteger value = BigInteger.valueOf(0);
        while(times++ < retryTimes) {
            try {
                Address addressA = new Address(addr);
                Function function = new Function("balanceOf", Arrays.<Type>asList(addressA), Collections.<TypeReference<?>>emptyList());
                String dataHex = FunctionEncoder.encode(function);
                org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(addr, contractAddress, dataHex);

                org.web3j.protocol.core.methods.response.EthCall ethCall = web3.ethCall(transaction, DefaultBlockParameter.valueOf(nextBlockNumber)).send();
                String val = ethCall.getValue();
                if (val == null || val.equals("0x")) {
                    val = "0x0";
                }
                return  Numeric.toBigInt(val);
            } catch (Exception e) {
                log.error("balanceOf error",e);
                Utils.sleep(100);
            }
        }
        return value;
    }
}