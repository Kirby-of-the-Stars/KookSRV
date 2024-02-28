package com.xiaoace.kooksrv.utils;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.RandomUtil;
import com.xiaoace.kooksrv.KookSRV;

import java.util.UUID;
import java.util.logging.Level;

public class CacheTools {

    private final KookSRV plugin;

    public TimedCache<String, String> uuidCache;
    public TimedCache<String, String> codeCache;

    public CacheTools(KookSRV plugin) {
        this.plugin = plugin;
        uuidCache = CacheUtil.newTimedCache(5 * 60000);
        codeCache = CacheUtil.newTimedCache(5 * 60000);
        uuidCache.schedulePrune(60000);
        codeCache.schedulePrune(60000);
    }

    public String createNewCache(String uuid) {

        if (uuidCache.get(uuid, false) != null) {
            return uuidCache.get(uuid, false);
        } else {
            String randomNumber = RandomUtil.randomNumbers(6);
            uuidCache.put(uuid, randomNumber);
            codeCache.put(randomNumber, uuid);
            plugin.getLogger().log(Level.SEVERE, "为玩家: " + plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)) + "创建绑定验证码: " + randomNumber);
            return randomNumber;
        }
    }

    public void removeCache(String code) {
        String key = codeCache.get(code, false);
        uuidCache.remove(key);
        codeCache.remove(code);
    }

}
