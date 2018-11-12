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
public class TrxTransferContract implements TrxContract, Serializable {

    public String ownerAddress;

    public String toAddress;

    public long amount;

}
