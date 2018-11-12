package org.jim.trx.rpc;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jim.trx.model.TrxAccount;
import org.jim.trx.model.TrxBlock;
import org.jim.trx.model.TrxTransferContract;
import org.jim.trx.model.TrxTx;
import org.jim.trx.utils.ByteArray;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TrxGrpcService {

    private String webUrl;

    private ManagedChannel channel;
    private WalletGrpc.WalletBlockingStub blockingStub;

    public void nodeConfig(String url) {
        this.webUrl = url;

        channel = ManagedChannelBuilder.forTarget(this.webUrl)
                .usePlaintext(true)
                .build();

        blockingStub = WalletGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public TrxAccount getAccount(String addrHex) {
        TrxAccount trxAccount = this.getAccountBalance(addrHex);
        this.getAccountResource(addrHex, trxAccount);
        return trxAccount;
    }

    public TrxAccount getAccountBalance(String addrHex) {
        byte[] address = ByteArray.fromHexString(addrHex);
        ByteString addressBS = ByteString.copyFrom(address);
        Protocol.Account request = Protocol.Account.newBuilder().setAddress(addressBS).build();
        Protocol.Account account = blockingStub.getAccount(request);

        return buildAccount(account);
    }

    public void getAccountResource(String addrHex, TrxAccount trxAccount) {
        byte[] address = ByteArray.fromHexString(addrHex);
        ByteString addressBS = ByteString.copyFrom(address);
        Protocol.Account request = Protocol.Account.newBuilder().setAddress(addressBS).build();
        GrpcAPI.AccountResourceMessage resource =  blockingStub.getAccountResource(request);

        trxAccount.setFreeNetLimit(resource.getFreeNetLimit());
        trxAccount.setFreeNetUsed(resource.getFreeNetUsed());
        trxAccount.setNetLimit(resource.getNetLimit());
        trxAccount.setNetUsed(resource.getNetUsed());
    }

    public TrxBlock getBlock(long blockNum) {
        GrpcAPI.BlockExtention block = null;
        if (blockNum < 0) {
            block = blockingStub.getNowBlock2(GrpcAPI.EmptyMessage.newBuilder().build());
        } else {
            GrpcAPI.NumberMessage.Builder builder = GrpcAPI.NumberMessage.newBuilder();
            builder.setNum(blockNum);
            block = blockingStub.getBlockByNum2(builder.build());
        }

        TrxBlock trxBlock = new TrxBlock();
        trxBlock.setHash(ByteArray.toHexString(block.getBlockid().toByteArray()));

        Protocol.BlockHeader.raw raw = block.getBlockHeader().getRawData();
        trxBlock.setHeight(raw.getNumber());
        trxBlock.setTime(raw.getTimestamp());

        List<TrxTx> txList = new ArrayList<>();
        for (GrpcAPI.TransactionExtention tx : block.getTransactionsList()) {
            TrxTx trxTx = buildTx(tx.getTransaction());
            if (null != trxTx) {
                trxTx.setTxid(ByteArray.toHexString(tx.getTxid().toByteArray()));
                trxTx.setTimestamp(raw.getTimestamp());
                txList.add(trxTx);
            }
        }
        trxBlock.setTxList(txList);

        return trxBlock;
    }

    public TrxTx getTransaction(String txid) {
        ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txid));
        GrpcAPI.BytesMessage request = GrpcAPI.BytesMessage.newBuilder().setValue(bsTxid).build();
        Protocol.Transaction transaction = blockingStub.getTransactionById(request);
        if (null == transaction) {
            return null;
        } else {
            return buildTx(transaction);
        }
    }

    public void getTransactionReceipt(String txid, TrxTx trxTx) {
        ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txid));
        GrpcAPI.BytesMessage request = GrpcAPI.BytesMessage.newBuilder().setValue(bsTxid).build();
        Protocol.TransactionInfo transactionInfo = blockingStub.getTransactionInfoById(request);
        if (null != transactionInfo) {
            Protocol.ResourceReceipt receipt = transactionInfo.getReceipt();
            trxTx.setResult(receipt.getResultValue());
            trxTx.setFee(receipt.getNetFee());
            trxTx.setNetUsage(receipt.getNetUsage());
        }
    }

    private TrxAccount buildAccount(Protocol.Account account) {
        TrxAccount trxAccount = new TrxAccount();

        trxAccount.setBalance(account.getBalance());

        long frozenBalance = 0L;
        for (Protocol.Account.Frozen frozen : account.getFrozenList()) {
            frozenBalance = frozenBalance + frozen.getFrozenBalance();
        }
        trxAccount.setFrozenBalance(frozenBalance);
        return trxAccount;
    }

    private TrxTx buildTx(Protocol.Transaction tx) {
        TrxTx trxTx = null;

        try {
            Protocol.Transaction.raw txRaw = tx.getRawData();
            Protocol.Transaction.Contract contract = txRaw.getContract(0);

            // transfer TRX
            if (Protocol.Transaction.Contract.ContractType.TransferContract.getNumber() == contract.getType().getNumber()) {
                trxTx = new TrxTx();
                TrxTransferContract trxContract = new TrxTransferContract();

                Contract.TransferContract ac = contract.getParameter().unpack(Contract.TransferContract.class);
                trxContract.setOwnerAddress(ByteArray.toHexString(ac.getOwnerAddress().toByteArray()));
                trxContract.setToAddress(ByteArray.toHexString(ac.getToAddress().toByteArray()));
                trxContract.setAmount(ac.getAmount());

                trxTx.setContract(trxContract);
            }
        } catch (Exception e) {
            trxTx = null;
        }
        return trxTx;
    }

    public boolean broadcastTransaction(String rawHex) {
        Protocol.Transaction signaturedTransaction = null;
        try {
            signaturedTransaction = Protocol.Transaction.parseFrom(
                    ByteArray.fromHexString(rawHex));
        } catch (Exception e) {
            return false;
        }

        int i = 1;
        GrpcAPI.Return response = blockingStub.broadcastTransaction(signaturedTransaction);
        while (response.getResult() == false && response.getCode() == GrpcAPI.Return.response_code.SERVER_BUSY
                && i > 0) {
            i--;
            response = blockingStub.broadcastTransaction(signaturedTransaction);

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        if (response.getResult() == false) {
            //logger.info("Code = " + response.getCode());
            //logger.info("Message = " + response.getMessage().toStringUtf8());
        }
        return response.getResult();
    }

}
