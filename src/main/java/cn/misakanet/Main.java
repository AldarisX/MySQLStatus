package cn.misakanet;

import cn.misakanet.bean.MySQLCom;
import cn.misakanet.bean.MySQLInnodb;
import cn.misakanet.bean.MySQLNet;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

public class Main {
    private String url = "127.0.0.1";
    private String port = "3306";
    private String user = "root";
    private String passwd = "aldaris";

    private long bytes_received;
    private long bytes_sent;
    private long thread_connect;
    private long sql_insert;
    private long sql_delete;
    private long sql_update;
    private long sql_select;
    private float table_open_cache_hits;
    private float table_open_cache_misses;
    private float innodb_buffer_pool_total;
    private float innodb_buffer_pool_pages_free;
    private long innodb_data_written;
    private long innodb_data_read;

    private DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

    public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException {
        new Main().init();
    }

    public void init() throws ClassNotFoundException, SQLException, InterruptedException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + url + ":" + port, user, passwd);

        MySQLNet sqlNet = new MySQLNet();
        MySQLCom sqlCom = new MySQLCom();
        MySQLInnodb sqlInnodb = new MySQLInnodb();

        String sql = "SHOW GLOBAL STATUS;";
        Statement st = conn.createStatement();
        while (true) {
            long statTime = System.currentTimeMillis();
            ResultSet rs = st.executeQuery(sql);
            HashMap<String, String> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString(1), rs.getString(2));
            }

            bytes_sent = Long.parseLong(result.get("Bytes_sent"));
            System.out.println("sending:" + sqlNet.getTx(bytes_sent) + "/s");
            bytes_received = Long.parseLong(result.get("Bytes_received"));
            System.out.println("receiving:" + sqlNet.getRx(bytes_received) + "/s");

            thread_connect = Long.parseLong(result.get("Threads_connected"));
            System.out.println("Connections:" + thread_connect);

            table_open_cache_hits = Float.parseFloat(result.get("Table_open_cache_hits"));
            table_open_cache_misses = Float.parseFloat(result.get("Table_open_cache_misses"));
            System.out.println("table open cache:" + df.format((1 - (table_open_cache_misses / (table_open_cache_misses + table_open_cache_hits))) * 100) + "% Efficiency");

            sql_insert = Long.parseLong(result.get("Com_insert"));
            System.out.println("insert:" + sqlCom.getInsert(sql_insert) + " row/s");
            sql_delete = Long.parseLong(result.get("Com_delete"));
            System.out.println("delete:" + sqlCom.getDelete(sql_delete) + " row/s");
            sql_update = Long.parseLong(result.get("Com_update"));
            System.out.println("update:" + sqlCom.getUpdate(sql_update) + " row/s");
            sql_select = Long.parseLong(result.get("Com_select"));
            System.out.println("select:" + sqlCom.getSelect(sql_select) + " row/s");

            innodb_buffer_pool_pages_free = Float.parseFloat(result.get("Innodb_buffer_pool_pages_free"));
            innodb_buffer_pool_total = Float.parseFloat(result.get("Innodb_buffer_pool_pages_total"));
            System.out.println("Innodb pool usage:" + df.format((1 - (innodb_buffer_pool_pages_free / innodb_buffer_pool_total)) * 100) + "%");

            innodb_data_written = Long.parseLong(result.get("Innodb_data_written"));
            System.out.println("innodb disk write;" + sqlInnodb.getDataWritten(innodb_data_written) + "/s");
            innodb_data_read = Long.parseLong(result.get("Innodb_data_read"));
            System.out.println("innodb disk read:" + sqlInnodb.getDataRead(innodb_data_read) + "/s");

            long endTime = System.currentTimeMillis();
            Thread.sleep(1000 - (endTime - statTime));
            System.out.println();
        }
    }
}
