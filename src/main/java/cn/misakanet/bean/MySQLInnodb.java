package cn.misakanet.bean;

public class MySQLInnodb {
    long data_writtenP = 0;
    long data_readP = 0;
    long data_writtenN;
    long data_readN;

//    public String getDataWritten(long written) {
//        data_writtenP = data_writtenN;
//        data_writtenN = written;
//        return SizeFormat.getPrintSize(data_writtenN - data_writtenP);
//    }

    public long getDataWritten(long written) {
        data_writtenP = data_writtenN;
        data_writtenN = written;
        return data_writtenN - data_writtenP;
    }

//    public String getDataRead(long read) {
//        data_readP = data_readN;
//        data_readN = read;
//        return SizeFormat.getPrintSize(data_readN - data_readP);
//    }

    public long getDataRead(long read) {
        data_readP = data_readN;
        data_readN = read;
        return data_readN - data_readP;
    }
}
