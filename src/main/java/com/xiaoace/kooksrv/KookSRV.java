package com.xiaoace.kooksrv;

import com.xiaoace.kooksrv.kook.Bot;
import com.xiaoace.kooksrv.listeners.MinecraftListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class KookSRV extends JavaPlugin {

    @Getter
    private Bot bot;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void onEnable() {

        try {
            initBot();
            initListener();
        } catch (Exception e) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        // KOOK机器人关闭
        this.bot.getKbcClient().shutdown();
    }

    private void initBot() {
        String bot_token = getConfig().getString("kook.bot-token", "No token provided");
        if (bot_token.equals("No token provided")) {
            getLogger().log(Level.SEVERE,"你没有提供bot-token或者bot-token不正确");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.bot = new Bot(this, bot_token);
    }

    private void initListener() {
        Bukkit.getPluginManager().registerEvents(new MinecraftListener(this),this);
    }

}
