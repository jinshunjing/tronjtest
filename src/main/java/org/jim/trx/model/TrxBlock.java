package org.jim.trx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrxBlock implements Serializable {

    public String hash;

    public long height;

    public long time;

    private List<TrxTx> txList;

}
