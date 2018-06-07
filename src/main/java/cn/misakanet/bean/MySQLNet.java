package cn.misakanet.bean;

public class MySQLNet {
    long rxBytesP = 0;
    long txBytesP = 0;
    long rxBytesN;
    long txBytesN;

//    public String getRx(long rx) {
//        rxBytesP = rxBytesN;
//        rxBytesN = rx;
//        return SizeFormat.getPrintSize(rxBytesN - rxBytesP);
//    }

    public long getRx(long rx) {
        rxBytesP = rxBytesN;
        rxBytesN = rx;
        return rxBytesN - rxBytesP;
    }

//    public String getTx(long tx) {
//        txBytesP = txBytesN;
//        txBytesN = tx;
//        return SizeFormat.getPrintSize(txBytesN - txBytesP);
//    }

    public long getTx(long tx) {
        txBytesP = txBytesN;
        txBytesN = tx;
        return txBytesN - txBytesP;
    }
}
