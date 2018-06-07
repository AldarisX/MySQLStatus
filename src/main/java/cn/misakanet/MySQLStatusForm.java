package cn.misakanet;

import cn.misakanet.bean.MySQLCom;
import cn.misakanet.bean.MySQLInnodb;
import cn.misakanet.bean.MySQLNet;
import cn.misakanet.tool.SizeFormat;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;
import net.sf.json.JSONObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MySQLStatusForm {
    private static JFrame thisFrame;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String url;
    private String port;
    private String user;
    private String passwd;
    private int maxCount = 10;
    private DecimalFormat numFormat = (DecimalFormat) NumberFormat.getInstance();
    private long bytes_received;
    private long bytes_sent;
    private long thread_connect;
    private long sql_insert;
    private long sql_delete;
    private long sql_update;
    private long sql_select;
    private float table_open_cache_hits;
    private float table_open_cache_misses;
    private float table_open_cache;
    private float innodb_buffer_pool_total;
    private float innodb_buffer_pool_pages_free;
    private float innodb_buffer_pool;
    private long innodb_data_written;
    private long innodb_data_read;
    private JPanel panel_root;
    private JPanel panel_option;
    private JPanel panel_network;
    private JPanel panel_mysql;
    private JPanel panel_innodb;
    private JPanel panel_rx;
    private JPanel panel_tx;
    private JPanel panel_conn;
    private JPanel panel_open_cache;
    private JPanel panel_oper;
    private JPanel panel_buffer_pool;
    private JPanel panel_disk_write;
    private JPanel panel_disk_read;
    private ChartPanel chartPanel_tx;
    private ChartPanel chartPanel_conn;
    private ChartPanel chartPanel_rx;
    private ChartPanel chartPanel_open_cache;
    private ChartPanel chartPanel_oper;
    private ChartPanel chartPanel_buffer_pool;
    private ChartPanel chartPanel_disk_write;
    private ChartPanel chartPanel_disk_read;
    private JFreeChart chart_rx;
    private JFreeChart chart_tx;
    private JFreeChart chart_conn;
    private JFreeChart chart_open_cache;
    private JFreeChart chart_oper;
    private JFreeChart chart_buffer_pool;
    private JFreeChart chart_disk_write;
    private JFreeChart chart_disk_read;
    private DefaultCategoryDataset ds_rx;
    private DefaultCategoryDataset ds_tx;
    private DefaultCategoryDataset ds_conn;
    private DefaultCategoryDataset ds_open_cache;
    private DefaultCategoryDataset ds_oper;
    private DefaultCategoryDataset ds_buffer_pool;
    private DefaultCategoryDataset ds_disk_write;
    private DefaultCategoryDataset ds_disk_read;
    private CategoryPlot plot_tx;
    private CategoryPlot plot_conn;
    private CategoryPlot plot_rx;
    private CategoryPlot plot_open_cache;
    private CategoryPlot plot_oper;
    private CategoryPlot plot_buffer_pool;
    private CategoryPlot plot_disk_write;
    private CategoryPlot plot_disk_read;
    private Connection conn;
    private boolean isExit = false;
    private boolean firstRun = true;

    public MySQLStatusForm() {
        $$$setupUI$$$();
    }

    public void start(String url, String port, String user, String passwd) {
        this.url = url;
        this.port = port;
        this.user = user;
        this.passwd = passwd;

        thisFrame = new JFrame("MySQL状态" + url + ":" + port);
        thisFrame.setContentPane(panel_root);
//        thisFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        thisFrame.pack();
        thisFrame.setSize(1280, 720);
        thisFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //穿体关闭时
        thisFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                isExit = true;

                try {
                    conn.close();

                    ds_rx.clear();
                    ds_tx.clear();
                    ds_conn.clear();
                    ds_open_cache.clear();
                    ds_oper.clear();
                    ds_buffer_pool.clear();
                    ds_disk_write.clear();
                    ds_disk_read.clear();

                    LoginForm.thisFrame.setVisible(true);
                    thisFrame.dispose();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });


        int windowWidth = thisFrame.getWidth();                    //获得窗口宽
        int windowHeight = thisFrame.getHeight();                  //获得窗口高
        Toolkit kit = Toolkit.getDefaultToolkit();             //定义工具包
        Dimension screenSize = kit.getScreenSize();            //获取屏幕的尺寸
        int screenWidth = screenSize.width;                    //获取屏幕的宽
        int screenHeight = screenSize.height;                  //获取屏幕的高
        thisFrame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);//设置窗口居中显示


        thisFrame.setVisible(true);

        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + url + ":" + port, user, passwd);
            isExit = false;
            runStatus();

            //保存配置
            try {
                saveConfig();
            } catch (IOException e1) {
                showErrorOption("设置保存失败\n" + e1.getMessage());
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException | SQLException | InterruptedException e1) {
            if (e1.getMessage().startsWith("Access denied for user")) {
                showErrorOption("MySQL用户名或密码错误");
            } else {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 保存配置
     *
     * @throws IOException
     */
    private void saveConfig() throws IOException {
        JSONObject config = new JSONObject();
        config.accumulate("dbUrl", url);
        config.accumulate("dbPort", port);
        config.accumulate("dbUser", user);
        config.accumulate("dbPasswd", passwd);

        File configFile = new File("config.json");
        if (!configFile.exists()) ;
        configFile.createNewFile();
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(config.toString().getBytes("UTF-8"));
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void runStatus() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MySQLNet sqlNet = new MySQLNet();
                    MySQLCom sqlCom = new MySQLCom();
                    MySQLInnodb sqlInnodb = new MySQLInnodb();

                    String sql = "SHOW GLOBAL STATUS;";
                    Statement st = conn.createStatement();

                    Thread.sleep(1000);

                    while (!isExit) {
                        //获取开始时间
                        long statTime = System.currentTimeMillis();

                        //获取当前系统时间
                        Date now = new Date();
                        ResultSet rs = st.executeQuery(sql);
                        HashMap<String, String> result = new HashMap<>();
                        while (rs.next()) {
                            result.put(rs.getString(1), rs.getString(2));
                        }

                        //网络接受
                        bytes_received = Long.parseLong(result.get("Bytes_received"));
                        //网络发送
                        bytes_sent = Long.parseLong(result.get("Bytes_sent"));
                        if (!firstRun) {
                            //网络接受
                            bytes_received = sqlNet.getRx(bytes_received);
                            ds_rx.addValue(bytes_received / 1024, "接收速度", dateFormat.format(now));
                            chart_rx.setTitle("网络接收(" + SizeFormat.getPrintSize(bytes_received) + "/s)");
                            //网络发送
                            bytes_sent = sqlNet.getTx(bytes_sent);
                            ds_tx.addValue(bytes_sent / 1024, "发送速度", dateFormat.format(now));
                            chart_tx.setTitle("网络发送(" + SizeFormat.getPrintSize(bytes_sent) + "/s)");
                        } else {
                            sqlNet.getRx(bytes_received);
                            sqlNet.getTx(bytes_sent);
                        }

                        //连接数
                        thread_connect = Long.parseLong(result.get("Threads_connected"));
                        ds_conn.addValue(thread_connect, "", dateFormat.format(now));
                        chart_conn.setTitle("连接数(" + thread_connect + ")");

                        //Table open cache
                        try {
                            table_open_cache_hits = Float.parseFloat(result.get("Table_open_cache_hits"));
                            table_open_cache_misses = Float.parseFloat(result.get("Table_open_cache_misses"));

                            table_open_cache = Float.parseFloat(numFormat.format((1 - (table_open_cache_misses / (table_open_cache_misses + table_open_cache_hits))) * 100));
                            ds_open_cache.addValue(table_open_cache, "效率", dateFormat.format(now));
                            chart_open_cache.setTitle("Table Open Cache(" + table_open_cache + "%)");
                        } catch (NullPointerException e) {
                            chartPanel_open_cache.setEnabled(false);
                        }

                        //SQL 执行数
                        sql_insert = Long.parseLong(result.get("Com_insert"));
                        sql_select = Long.parseLong(result.get("Com_select"));
                        sql_delete = Long.parseLong(result.get("Com_delete"));
                        sql_update = Long.parseLong(result.get("Com_update"));
                        if (!firstRun) {
                            sql_insert = sqlCom.getInsert(sql_insert);
                            sql_select = sqlCom.getSelect(sql_select);
                            sql_delete = sqlCom.getDelete(sql_delete);
                            sql_update = sqlCom.getUpdate(sql_update);
                            ds_oper.addValue(sql_insert, "insert", dateFormat.format(now));
                            ds_oper.addValue(sql_select, "select", dateFormat.format(now));
                            ds_oper.addValue(sql_delete, "delete", dateFormat.format(now));
                            ds_oper.addValue(sql_update, "update", dateFormat.format(now));

                            chart_oper.setTitle("SQL 执行数\ninsert:" + sql_insert + " select:" + sql_select + "\ndelete:" + sql_delete + " update:" + sql_update);
                        } else {
                            sqlCom.getInsert(sql_insert);
                            sqlCom.getDelete(sql_delete);
                            sqlCom.getUpdate(sql_update);
                            sqlCom.getSelect(sql_select);
                        }

                        //innodb buffer pool
                        innodb_buffer_pool_pages_free = Float.parseFloat(result.get("Innodb_buffer_pool_pages_free"));
                        innodb_buffer_pool_total = Float.parseFloat(result.get("Innodb_buffer_pool_pages_total"));
                        float innodb_buffer_pool = Float.parseFloat(numFormat.format((1 - (innodb_buffer_pool_pages_free / innodb_buffer_pool_total)) * 100));
                        ds_buffer_pool.addValue(innodb_buffer_pool, "效率", dateFormat.format(now));
                        chart_buffer_pool.setTitle("Innodb缓存(" + innodb_buffer_pool + "%)");

                        //innodb io
                        innodb_data_written = Long.parseLong(result.get("Innodb_data_written"));
                        innodb_data_read = Long.parseLong(result.get("Innodb_data_read"));
                        if (!firstRun) {
                            innodb_data_written = sqlInnodb.getDataWritten(innodb_data_written) / 1024;
                            ds_disk_write.addValue(innodb_data_written, "写入速度", dateFormat.format(now));
                            chart_disk_write.setTitle("Innodb磁盘写入(" + SizeFormat.getPrintSize(innodb_data_written) + "/s)");
                            innodb_data_read = sqlInnodb.getDataRead(innodb_data_read) / 1024;
                            ds_disk_read.addValue(innodb_data_read, "读取速度", dateFormat.format(now));
                            chart_disk_read.setTitle("Innodb磁盘读取(" + SizeFormat.getPrintSize(innodb_data_read) + "/s)");
                        } else {
                            sqlInnodb.getDataWritten(innodb_data_written);
                            sqlInnodb.getDataRead(innodb_data_read);
                        }

                        firstRun = false;

                        upOver(ds_rx);
                        upOver(ds_tx);
                        upOver(ds_conn);
                        upOver(ds_open_cache);
                        upOver(ds_oper);
                        upOver(ds_buffer_pool);
                        upOver(ds_disk_write);
                        upOver(ds_disk_read);

                        thisFrame.repaint();

                        long endTime = System.currentTimeMillis();
                        Thread.sleep(1000 - (endTime - statTime));
                        System.out.println();
                    }
                } catch (MySQLNonTransientConnectionException e) {
                    showErrorOption("MySQL连接中断");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void upOver(DefaultCategoryDataset ds) {
        if (ds.getColumnCount() == maxCount) {
            ds.removeColumn(0);
        }
    }

    private void showErrorOption(String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, msg, "错误", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * IDEA的布局初始化
     */
    private void createUIComponents() {
        numFormat = new DecimalFormat("0.00");

        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        //设置标题字体
        standardChartTheme.setExtraLargeFont(new Font("微软雅黑", Font.BOLD, 15));
        //设置图例的字体
        standardChartTheme.setRegularFont(new Font("微软雅黑", Font.PLAIN, 10));
        //设置轴向的字体
        standardChartTheme.setLargeFont(new Font("微软雅黑", Font.PLAIN, 10));
        //应用主题样式
        ChartFactory.setChartTheme(standardChartTheme);

        ds_rx = new DefaultCategoryDataset();
        chart_rx = ChartFactory.createLineChart("网络接收", "时间", "KB/s", ds_rx, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_rx = new ChartPanel(chart_rx);
        plot_rx = chart_rx.getCategoryPlot();

        ds_tx = new DefaultCategoryDataset();
        chart_tx = ChartFactory.createLineChart("网络发送", "时间", "KB/s", ds_tx, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_tx = new ChartPanel(chart_tx);
        plot_tx = chart_tx.getCategoryPlot();

        ds_conn = new DefaultCategoryDataset();
        chart_conn = ChartFactory.createLineChart("连接数", "时间", "连接数", ds_conn, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_conn = new ChartPanel(chart_conn);
        plot_conn = chart_conn.getCategoryPlot();

        ds_open_cache = new DefaultCategoryDataset();
        chart_open_cache = ChartFactory.createLineChart("Table Open Cache", "时间", "效率", ds_open_cache, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_open_cache = new ChartPanel(chart_open_cache);
        plot_open_cache = chart_open_cache.getCategoryPlot();

        ds_oper = new DefaultCategoryDataset();
        chart_oper = ChartFactory.createLineChart("SQL执行数", "时间", "数量", ds_oper, PlotOrientation.VERTICAL, true, false, false);
        chartPanel_oper = new ChartPanel(chart_oper);
        plot_oper = chart_oper.getCategoryPlot();

        ds_buffer_pool = new DefaultCategoryDataset();
        chart_buffer_pool = ChartFactory.createLineChart("Innodb缓存", "时间", "百分比", ds_buffer_pool, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_buffer_pool = new ChartPanel(chart_buffer_pool);
        plot_buffer_pool = chart_buffer_pool.getCategoryPlot();

        ds_disk_write = new DefaultCategoryDataset();
        chart_disk_write = ChartFactory.createLineChart("Innodb磁盘写入", "时间", "KB/s", ds_disk_write, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_disk_write = new ChartPanel(chart_disk_write);
        plot_disk_write = chart_disk_write.getCategoryPlot();

        ds_disk_read = new DefaultCategoryDataset();
        chart_disk_read = ChartFactory.createLineChart("Innodb磁盘读取", "时间", "KB/s", ds_disk_read, PlotOrientation.VERTICAL, false, false, false);
        chartPanel_disk_read = new ChartPanel(chart_disk_read);
        plot_disk_read = chart_disk_read.getCategoryPlot();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        panel_root = new JPanel();
        panel_root.setLayout(new GridLayoutManager(1, 10, new Insets(0, 0, 0, 0), -1, -1));
        panel_network = new JPanel();
        panel_network.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_root.add(panel_network, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_rx = new JPanel();
        panel_rx.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_network.add(panel_rx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_rx.add(chartPanel_rx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_tx = new JPanel();
        panel_tx.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_network.add(panel_tx, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_tx.add(chartPanel_tx, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_conn = new JPanel();
        panel_conn.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_network.add(panel_conn, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_conn.add(chartPanel_conn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_mysql = new JPanel();
        panel_mysql.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_root.add(panel_mysql, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_open_cache = new JPanel();
        panel_open_cache.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_mysql.add(panel_open_cache, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_open_cache.add(chartPanel_open_cache, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_oper = new JPanel();
        panel_oper.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_mysql.add(panel_oper, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_oper.add(chartPanel_oper, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_mysql.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_innodb = new JPanel();
        panel_innodb.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_root.add(panel_innodb, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_buffer_pool = new JPanel();
        panel_buffer_pool.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_innodb.add(panel_buffer_pool, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_buffer_pool.add(chartPanel_buffer_pool, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_disk_write = new JPanel();
        panel_disk_write.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_innodb.add(panel_disk_write, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_disk_write.add(chartPanel_disk_write, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_disk_read = new JPanel();
        panel_disk_read.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel_innodb.add(panel_disk_read, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel_disk_read.add(chartPanel_disk_read, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel_root;
    }
}
