package com.xiaoace.kooksrv.kook;

import com.xiaoace.kooksrv.KookSRV;
import com.xiaoace.kooksrv.command.kook.LinkCommand;
import com.xiaoace.kooksrv.database.SqliteHelper;
import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.listeners.KookListener;
import com.xiaoace.kooksrv.utils.CacheTools;
import lombok.Getter;
import snw.jkook.JKook;
import snw.jkook.config.file.YamlConfiguration;
import snw.kookbc.impl.CoreImpl;
import snw.kookbc.impl.KBCClient;
import snw.kookbc.impl.command.litecommands.LiteKookFactory;

public class Bot {

    private final String token;
    private final KookSRV plugin;
    @Getter
    private KBCClient kbcClient;
    private final CacheTools cacheTools;
    private final UserDao userDao;

    public Bot(KookSRV minecraftPlugin, String kookBotToken, CacheTools cacheTools, UserDao userDao) {
        this.plugin = minecraftPlugin;
        this.token = kookBotToken;
        this.cacheTools = cacheTools;
        this.userDao = userDao;
        CoreImpl core = new CoreImpl();
        JKook.setCore(core);
        startBot(core);
    }

    private void startBot(CoreImpl core) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("mode", "websocket");
        kbcClient = new KBCClient(core, config, null, token);
        kbcClient.start();
        plugin.getLogger().info("kook侧机器人启动完毕");
        // 注册监听器
        kbcClient.getCore().getEventManager().registerHandlers(kbcClient.getInternalPlugin(), new KookListener(plugin, userDao));
        // 注册命令
        LiteKookFactory.builder(kbcClient.getInternalPlugin())
                        .commands(new LinkCommand(kbcClient, kbcClient.getInternalPlugin(), plugin, cacheTools, userDao)).build();
        plugin.getLogger().info("kook侧监听器注册完毕");
    }

}
