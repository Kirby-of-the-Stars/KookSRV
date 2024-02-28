package com.xiaoace.kooksrv.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabExecutor {
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
                sender.sendMessage("执行子命令: link");
                return true;
            }

            // 处理子命令: link
            if (subCommand.equalsIgnoreCase("unlink")) {
                sender.sendMessage("执行子命令: unlink");
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
