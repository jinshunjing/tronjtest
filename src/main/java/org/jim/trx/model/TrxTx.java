package org.jim.trx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrxTx implements Serializable {

    public String txid;

    public long timestamp;

    public TrxContract contract;

    public int result;
    public long fee;
    public long netUsage;

}
