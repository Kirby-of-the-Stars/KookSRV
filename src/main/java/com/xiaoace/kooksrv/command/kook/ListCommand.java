package com.xiaoace.kooksrv.command.kook;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import snw.jkook.command.CommandSender;
import snw.jkook.entity.User;
import snw.jkook.message.Message;
import snw.jkook.plugin.Plugin;
import snw.kookbc.impl.KBCClient;
import snw.kookbc.impl.command.litecommands.annotations.prefix.Prefix;

import java.util.Collection;

@Command(name = "list")
@Prefix("/")
@Description("查询服务器在线玩家列表")
public class ListCommand {

    KBCClient kbcClient;
    Plugin plugin;

    public ListCommand(KBCClient kbcClient, Plugin plugin) {
        this.kbcClient = kbcClient;
        this.plugin = plugin;

    }

    @Execute
    void execute(@Context CommandSender sender, @Context Message message) {

        if (sender instanceof User) {

            StringBuilder online = new StringBuilder();
            final Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (Player player : players) {
                if (sender instanceof Player && !((Player) sender).canSee(player)) {
                    continue;
                }

                if (online.length() > 0) {
                    online.append(", ");
                }
                online.append(player.getDisplayName());

            }

            message.sendToSource("当前共有" + players.size() + "名玩家在线(最大玩家数为" + Bukkit.getMaxPlayers() + "):\n" + online.toString());

        }

    }
}
