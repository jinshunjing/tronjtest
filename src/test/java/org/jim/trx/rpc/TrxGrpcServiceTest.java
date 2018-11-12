package org.jim.trx.rpc;

import org.jim.trx.model.TrxAccount;
import org.jim.trx.model.TrxBlock;
import org.jim.trx.model.TrxTx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TrxGrpcServiceTest {

    private TrxGrpcService service;

    @Before
    public void setUp() throws Exception {
        String fullNodeUrl = "grpc.shasta.trongrid.io:50051";
        service = new TrxGrpcService();
        service.nodeConfig(fullNodeUrl);
        Thread.sleep(1000L);
    }

    @After
    public void tearDown() throws Exception {
        service.shutdown();
        Thread.sleep(1000L);
    }

    @Test
    public void testGetAccount() {
        String addr = "4198a84a6399f1eefa7c805bba4130be263e556c1b";
        TrxAccount account = service.getAccount(addr);
        System.out.println(account);
    }

    @Test
    public void testGetBlock() {
        long blockNum = 402045;
        TrxBlock block = service.getBlock(blockNum);
        System.out.println(block);
    }

    @Test
    public void testGetTx() {
        String txid = "3160191c1735e7b3528d7d924e1bc3989a81c666e79f24c7819c8cc8fc9070e9";
        TrxTx tx = service.getTransaction(txid);
        System.out.println(tx);
    }

    @Test
    public void testGetTxReceipt() {
        String txid = "3160191c1735e7b3528d7d924e1bc3989a81c666e79f24c7819c8cc8fc9070e9";
        TrxTx tx = new TrxTx();
        service.getTransactionReceipt(txid, tx);
        System.out.println(tx);
    }

}
