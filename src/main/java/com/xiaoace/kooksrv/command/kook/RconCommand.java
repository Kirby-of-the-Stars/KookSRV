package com.xiaoace.kooksrv.command.kook;

import com.xiaoace.kooksrv.database.dao.UserDao;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import snw.jkook.command.CommandSender;
import snw.jkook.entity.User;
import snw.jkook.message.Message;
import snw.jkook.message.TextChannelMessage;
import snw.jkook.plugin.Plugin;
import snw.kookbc.impl.KBCClient;
import snw.kookbc.impl.command.litecommands.annotations.prefix.Prefix;

import java.util.UUID;

@Command(name = "rcon")
@Prefix("/")
@Description("以你的身份执行这条命令")
public class RconCommand {

    KBCClient kbcClient;
    Plugin plugin;
    org.bukkit.plugin.Plugin mcPlugin;
    UserDao userDao;

    public RconCommand(KBCClient kbcClient, Plugin plugin, org.bukkit.plugin.Plugin mcPlugin, UserDao userDao) {
        this.kbcClient = kbcClient;
        this.plugin = plugin;
        this.mcPlugin = mcPlugin;
        this.userDao = userDao;
    }

    @Execute
    void execute(@Context CommandSender sender, @Context Message message, @Join String command) {

        if (sender instanceof User) {

            String kookUserID = ((User) sender).getId();
            String uuid = userDao.selectUserByKookID(kookUserID).getUuid();

            if (uuid != null) {
                OfflinePlayer player = mcPlugin.getServer().getOfflinePlayer(UUID.fromString(uuid));
                if (player.isOp()) {

                    mcPlugin.getServer().getScheduler().scheduleSyncDelayedTask(mcPlugin, () -> {

                        try {
                            if (mcPlugin.getServer().dispatchCommand(mcPlugin.getServer().getConsoleSender(), command)) {
                                message.reply("命令执行成功！");
                                mcPlugin.getLogger().info(((User) sender).getName() + "成功使用了命令: " + message.getComponent().toString());
                            } else {
                                message.reply("命令执行失败！");
                                mcPlugin.getLogger().info(((User) sender).getName() + "使用命令失败: " + message.getComponent().toString());
                            }

                        } catch (CommandException e) {
                            message.reply("命令执行失败!");
                            mcPlugin.getLogger().info(((User) sender).getName() + "使用命令失败: " + message.getComponent().toString());
                        }
                    });

                } else {

                    if (player.isOnline()) {

                        mcPlugin.getServer().getScheduler().scheduleSyncDelayedTask(mcPlugin, () -> {

                            try {
                                if (((Player) player).performCommand(command)) {
                                    message.reply("命令执行成功！");
                                    mcPlugin.getLogger().info(((User) sender).getName() + "成功使用了命令: " + message.getComponent().toString());
                                } else {
                                    message.reply("命令执行失败");
                                    mcPlugin.getLogger().info(((User) sender).getName() + "使用命令失败: " + message.getComponent().toString());
                                }
                            } catch (NullPointerException e) {

                                if (message instanceof TextChannelMessage) {
                                    ((TextChannelMessage) message).replyTemp("你不是OP,你不能离线执行命令噢");
                                } else {
                                    message.reply("你不是OP,你不能离线执行命令噢");
                                }

                            }

                        });

                    }

                }

            }

        }

    }

}
