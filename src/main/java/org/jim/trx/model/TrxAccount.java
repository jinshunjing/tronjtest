package org.jim.trx.model;

import lombok.Data;

@Data
public class TrxAccount {

    private String accountId;
    private String address;

    private long balance;
    private long frozenBalance;

    private long freeNetLimit;
    private long freeNetUsed;
    private long netLimit;
    private long netUsed;

}
