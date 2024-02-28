package com.xiaoace.kooksrv.database;

import com.xiaoace.kooksrv.KookSRV;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteHelper {

    private final KookSRV plugin;

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String dbFilePath;


    public SqliteHelper(KookSRV plugin) throws ClassNotFoundException, SQLException {
        this.plugin = plugin;
        init();
    }

    private void init() throws ClassNotFoundException, SQLException {

        File userDB = new File(plugin.getDataFolder(), "user.db");
        dbFilePath = userDB.getPath();
    }

    public Connection getConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) connection = getConnection(dbFilePath);
        return connection;
    }

    private Statement getStatement() throws ClassNotFoundException, SQLException {
        if (null == statement) statement = getConnection().createStatement();
        return statement;
    }

    public void setAutoCommit(Boolean status) throws SQLException {
        connection.setAutoCommit(status);
    }

    public void destroyed() {
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            System.out.println("Sqlite数据库关闭时异常 " + e);
        }
    }

    // For select
    public <T> T executeQuery(String sql, ResultSetExtractor<T> rse) throws ClassNotFoundException, SQLException {
        try {
            resultSet = getStatement().executeQuery(sql);
            T rs = rse.extractData(resultSet);
            return rs;
        } finally {
            destroyed();
        }
    }

    public <T> List<T> executeQuery(String sql, RowMapper<T> rm) throws ClassNotFoundException, SQLException {
        List<T> rsList = new ArrayList<T>();
        try {
            resultSet = getStatement().executeQuery(sql);
            while (resultSet.next()) {
                rsList.add(rm.mapRow(resultSet, resultSet.getRow()));
            }
        } finally {
            destroyed();
        }
        return rsList;
    }

    // For update
    public int executeUpdate(String sql) throws ClassNotFoundException, SQLException {
        try {
            return getStatement().executeUpdate(sql);
        } finally {
            destroyed();
        }
    }

    public void executeUpdate(String... sqls) throws ClassNotFoundException, SQLException {
        try {
            for (String sql : sqls) {
                getStatement().executeUpdate(sql);
            }
        } finally {
            destroyed();
        }
    }

    public void executeUpdate(List<String> sqls) throws ClassNotFoundException, SQLException {
        try {
            for (String sql : sqls) {
                getStatement().executeUpdate(sql);
            }
        } finally {
            destroyed();
        }
    }

}
