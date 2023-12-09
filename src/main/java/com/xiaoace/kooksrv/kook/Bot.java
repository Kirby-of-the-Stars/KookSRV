package com.xiaoace.kooksrv.kook;

import com.xiaoace.kooksrv.KookSRV;
import com.xiaoace.kooksrv.listeners.KookListener;
import lombok.Getter;
import snw.jkook.JKook;
import snw.jkook.config.file.YamlConfiguration;
import snw.kookbc.impl.CoreImpl;
import snw.kookbc.impl.KBCClient;

public class Bot {

    private final String token;
    private final KookSRV plugin;
    @Getter
    private KBCClient kbcClient;

    public Bot(KookSRV minecraftPlugin, String kookBotToken) {
        this.plugin = minecraftPlugin;
        this.token = kookBotToken;
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
        kbcClient.getCore().getEventManager().registerHandlers(kbcClient.getInternalPlugin(), new KookListener(plugin));
        plugin.getLogger().info("kook侧监听器注册完毕");
    }

}
