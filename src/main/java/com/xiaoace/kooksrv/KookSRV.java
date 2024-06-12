package com.xiaoace.kooksrv;

import com.luciad.imageio.webp.*;
import com.luciad.imageio.webp.util.OSInfo;
import com.xiaoace.kooksrv.command.MinecraftCommandManager;
import com.xiaoace.kooksrv.database.SqliteHelper;
import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.database.dao.impl.UserDaoImpl;
import com.xiaoace.kooksrv.kook.Bot;
import com.xiaoace.kooksrv.listeners.ImageManager;
import com.xiaoace.kooksrv.listeners.MinecraftListener;
import com.xiaoace.kooksrv.utils.CacheTools;
import com.xiaoace.kooksrv.utils.FieldUtil;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;

public class KookSRV extends JavaPlugin {

    static {
        //主动式加载库
        OSInfo.Init();
        if(!WebP.loadNativeLibrary()){
            System.out.println("Native library加载失败");
        }
        WebPDecoderOptions.Init();
        WebPEncoderOptions.Init();
        WebPImageReaderSpi.Init();
        WebPImageWriterSpi.Init();
        WebPReader.Init();
        WebPReadParam.Init();
        WebPWriteParam.Init();
        WebPWriteParam.Init();
        //开始反射暴力
        try {
            //反射出ImageIO的注册中心，拿到后用插件的ClassLoader加载Provider后重新赋值回ImageIO
            ImageIO.scanForPlugins();
            Field theRegistry = ImageIO.class.getDeclaredField("theRegistry");
            IIORegistry registry = (IIORegistry) FieldUtil.getFinalStatic(theRegistry);
            registerApplicationClasspathSpis(registry);
            FieldUtil.setFinalStatic(theRegistry,registry);
            ImageIO.scanForPlugins();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 在这里用插件的ClassLoader加载Provider
     * @param registry 注册中心
     */
    private static void registerApplicationClasspathSpis(IIORegistry registry){
        // FIX: load only from application classpath(hey,we need load out side!)
        Iterator<Class<?>> categories = registry.getCategories();
        while (categories.hasNext()) {
            @SuppressWarnings("unchecked")
            Class<IIOServiceProvider> c = (Class<IIOServiceProvider>)categories.next();
            Iterator<IIOServiceProvider> riter =
                    ServiceLoader.load(c, KookSRV.class.getClassLoader()).iterator();
            while (riter.hasNext()) {
                try {
                    IIOServiceProvider r = riter.next();
                    registry.registerServiceProvider(r);
                } catch (ServiceConfigurationError err) {
                    err.printStackTrace();
                }
            }
        }
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

            //init webp lib
//            WebP.loadNativeLibrary();
//            System.load(new File("D:\\DEV\\Windows\\x86_64\\webp-imageio.dll").getAbsolutePath());
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
