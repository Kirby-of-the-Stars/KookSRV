package com.xiaoace.kooksrv.database.dao;

import com.xiaoace.kooksrv.database.dao.pojo.User;

import java.util.List;

public interface UserDao {

    Integer createTable();

    // 用不到
    Integer dropTable();

    // 用不到
    List<User> selectAllUser();

    User selectUserByKookID(String kookID);

    User selectUserByUUID(String UUID);

    Integer deleteUserByKookID(String kookID);

    Integer deleteUserByUUID(String UUID);

    Integer insert(User user);
}
