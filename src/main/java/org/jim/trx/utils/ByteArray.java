package org.jim.trx.utils;

import org.spongycastle.util.encoders.Hex;

public class ByteArray {
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static String toHexString(byte[] data) {
        return data == null ? "" : Hex.toHexString(data);
    }

    public static byte[] fromHexString(String data) {
        if (data == null) {
            return EMPTY_BYTE_ARRAY;
        }
        if (data.startsWith("0x")) {
            data = data.substring(2);
        }
        if (data.length() % 2 == 1) {
            data = "0" + data;
        }
        return Hex.decode(data);
    }

}
