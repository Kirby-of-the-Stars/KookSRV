package com.xiaoace.kooksrv;


import com.xiaoace.kooksrv.command.MinecraftCommandManager;
import com.xiaoace.kooksrv.database.SqliteHelper;
import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.database.dao.impl.UserDaoImpl;
import com.xiaoace.kooksrv.kook.Bot;
import com.xiaoace.kooksrv.listeners.ImageManager;
import com.xiaoace.kooksrv.listeners.MinecraftListener;
import com.xiaoace.kooksrv.utils.CacheTools;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;

public class KookSRV extends JavaPlugin {

    static {
        //重新设置类加载器以至于可以扫描到 webp support 类
        ImageIO.scanForPlugins();
        Thread.currentThread().setContextClassLoader(KookSRV.class.getClassLoader());
        ImageIO.scanForPlugins();
    }
    @Getter
    private Bot bot;
    @Getter
    private SqliteHelper sqliteHelper;
    @Getter
    private CacheTools cacheTools;
    private UserDao userDao;

    private BukkitAudiences adventure;

    public BukkitAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        }
        return this.adventure;
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void onEnable() {

        try {
            initDatabase();
            initCacheTools();
            initBot();
            initListener();
            getCommand("kooksrv").setExecutor(new MinecraftCommandManager(cacheTools, userDao));

            this.adventure = BukkitAudiences.create(this);

            // 创建图片存储文件夹
            File cacheFolder = new File(getDataFolder(), "images");
            if (!cacheFolder.exists()) {
                cacheFolder.mkdir();
            }
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        // KOOK机器人关闭
        this.bot.getKbcClient().shutdown();
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void initBot() {
        String bot_token = getConfig().getString("kook.bot-token", "No token provided");
        if (bot_token.equals("No token provided")) {
            getLogger().log(Level.SEVERE, "你没有提供bot-token或者bot-token不正确");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.bot = new Bot(this, bot_token, cacheTools, userDao);
    }

    private void initListener() {
        Bukkit.getPluginManager().registerEvents(new MinecraftListener(this), this);
        ImageManager manager = ImageManager.getInstance();
        manager.init();
    }

    private void initDatabase() {
        try {
            this.sqliteHelper = new SqliteHelper(this);
            this.userDao = new UserDaoImpl(this);
        } catch (ClassNotFoundException e) {
            getLogger().log(Level.SEVERE, "找不到sqlite相关包 " + e);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Sqlite数据库连接时异常 " + e);
        }

        // 检查数据库是否已经存在
        File userDB = new File(getDataFolder(), "user.db");
        if (userDB.exists()) {
            getLogger().log(Level.SEVERE, "数据库已存在!");
        } else {
            getLogger().log(Level.SEVERE, "数据库未存在,正在初始化数据库!");
            userDao.createTable();
        }

    }

    private void initCacheTools() {
        this.cacheTools = new CacheTools(this);
    }

}
