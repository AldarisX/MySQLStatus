package cn.misakanet.bean;

public class MySQLCom {
    long sqlInsertP = 0;
    long sqlSelectP = 0;
    long sqlUpdateP = 0;
    long sqlDeleteP = 0;
    long sqlInsertN;
    long sqlSelectN;
    long sqlUpdateN;
    long sqlDeleteN;

    public long getInsert(long insert) {
        sqlInsertP = sqlInsertN;
        sqlInsertN = insert;
        return sqlInsertN - sqlInsertP;
    }

    public long getSelect(long select) {
        sqlSelectP = sqlSelectN;
        sqlSelectN = select;
        return sqlSelectN - sqlSelectP;
    }

    public long getUpdate(long update) {
        sqlUpdateP = sqlUpdateN;
        sqlUpdateN = update;
        return sqlUpdateN - sqlUpdateP;
    }

    public long getDelete(long delete) {
        sqlDeleteP = sqlDeleteN;
        sqlDeleteN = delete;
        return sqlDeleteN - sqlDeleteP;
    }
}
