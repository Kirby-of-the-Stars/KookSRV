package com.xiaoace.kooksrv.database.dao.impl;

import com.xiaoace.kooksrv.KookSRV;
import com.xiaoace.kooksrv.database.SqliteHelper;
import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.database.dao.pojo.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class UserDaoImpl implements UserDao {

    private final KookSRV plugin;
    private final SqliteHelper sqliteHelper;

    public UserDaoImpl(KookSRV plugin) {
        this.plugin = plugin;
        this.sqliteHelper = plugin.getSqliteHelper();
    }

    @Override
    public Integer createTable() {

        String createUserTableSql = "CREATE TABLE IF NOT EXISTS 'User'('kookID' CHAR(50) NOT NULL PRIMARY KEY,'UUID' CHAR(50) NOT NULL)";
        int result = 0;
        try {
            result = sqliteHelper.executeUpdate(createUserTableSql);
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "创建数据表时失败" + e);
        } finally {
            sqliteHelper.destroyed();
        }
        return result;
    }

    @Override
    public Integer dropTable() {
        return 0;
    }

    @Override
    public List<User> selectAllUser() {
        return null;
    }

    @Override
    public User selectUserByKookID(String kookID) {
        String selectUserByKookID = "select * from User where kookID = ?";
        User user = null;
        try {
            PreparedStatement statement = sqliteHelper.getConnection().prepareStatement(selectUserByKookID);
            statement.setString(1, kookID);
            ResultSet rs = statement.executeQuery();
            user = new User(rs.getString("kookID"), rs.getString("UUID"));
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "查询用户时失败" + e);
        } finally {
            sqliteHelper.destroyed();
        }
        return user;
    }

    @Override
    public User selectUserByUUID(String UUID) {
        String selectUserByUUID = "select * from User where kookID = ?";
        User user = null;
        try {
            PreparedStatement statement = sqliteHelper.getConnection().prepareStatement(selectUserByUUID);
            statement.setString(1, UUID);
            ResultSet rs = statement.executeQuery();
            user = new User(rs.getString("kookID"), rs.getString("UUID"));
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "查询用户时失败" + e);
        } finally {
            sqliteHelper.destroyed();
        }
        return user;
    }

    @Override
    public Integer deleteUserByKookID(String kookID) {
        String deleteUserByKookID = "delete from User where kookID = ?";
        int result = 0;
        try {
            PreparedStatement statement = sqliteHelper.getConnection().prepareStatement(deleteUserByKookID);
            statement.setString(1, kookID);
            result = statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "删除用户时失败" + e);
        } finally {
            sqliteHelper.destroyed();
        }
        return result;
    }

    @Override
    public Integer deleteUserByUUID(String UUID) {
        String deleteUserByUUID = "delete from User where UUID = ?";
        int result = 0;
        try {
            PreparedStatement statement = sqliteHelper.getConnection().prepareStatement(deleteUserByUUID);
            statement.setString(1, UUID);
            result = statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "删除用户时失败" + e);
        } finally {
            sqliteHelper.destroyed();
        }
        return result;
    }

    @Override
    public Integer insert(User user) {
        String insert = "insert into User(kookID,UUID) values (?,?)";
        int result = 0;
        try {
            PreparedStatement statement = sqliteHelper.getConnection().prepareStatement(insert);
            statement.setString(1, user.getKookID());
            statement.setString(2, user.getUuid());
            result = statement.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "插入用户时失败" + e);
        } finally {
            sqliteHelper.destroyed();
        }
        return result;
    }
}
