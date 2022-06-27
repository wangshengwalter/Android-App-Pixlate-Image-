package com.example.opencvproject;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBOpenHelper {
    private static String diver = "com.mysql.jdbc.Driver";
    //加入utf-8是为了后面往表中输入中文，表中不会出现乱码的情况
    private static String url = "jdbc:mysql://database-1.cx1uwwrahv9d.ca-central-1.rds.amazonaws.com:3306/sys";
    private static String user = "admin";//用户名
    private static String password = "12345678";//密码
    /*
     * 连接数据库
     * */

    //root@127.0.0.1:3306
    public static Connection getConn(){
        Connection conn = null;
        try {
            Class.forName(diver);
            conn = (Connection) DriverManager.getConnection(url,user,password);//获取连接
            Log.e("数据库连接", "成功!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            Log.e("数据库连接", "失败!");
            e.printStackTrace();
        }
        return conn;
    }
}