package com.xiaoace.kooksrv.command;

import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.utils.CacheTools;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MinecraftCommandManager implements TabExecutor {

    CacheTools cacheTools;
    UserDao userDao;

    public MinecraftCommandManager(CacheTools cacheTools, UserDao userDao) {
        this.cacheTools = cacheTools;
        this.userDao = userDao;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("kooksrv")) {

            if (args.length == 0) {
                sender.sendMessage("使用方法: /kooksrv link|unlink  把MC账号绑定到Kook或是取消绑定.");
                return true;
            }

            String subCommand = args[0];

            // 处理子命令: link
            if (subCommand.equalsIgnoreCase("link")) {
                if (sender instanceof Player) {
                    String uuid = String.valueOf(((Player) sender).getUniqueId());

                    if (Objects.equals(userDao.selectUserByUUID(uuid).getUuid(), uuid)) {
                        sender.sendMessage("您已经绑定过KOOK账号了噢");
                        return true;
                    } else {
                        String code = cacheTools.createNewCache(uuid);
                        sender.sendMessage("您的绑定码为: " + code);
                    }
                }
                return true;
            }

            // 处理子命令: unlink
            if (subCommand.equalsIgnoreCase("unlink")) {
                if (sender instanceof Player) {
                    String uuid = String.valueOf(((Player) sender).getUniqueId());
                    userDao.deleteUserByUUID(uuid);
                    sender.sendMessage("已成功解绑!");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return null;
        }

        if (command.getName().equalsIgnoreCase("kooksrv")) {
            if (args.length == 1) {
                List<String> subCommands = new ArrayList<>();
                subCommands.add("link");
                subCommands.add("unlink");
                return subCommands;
            }
        }

        return null;
    }
}
