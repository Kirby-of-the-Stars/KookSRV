package com.xiaoace.kooksrv.command.kook;

import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.utils.CacheTools;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import snw.jkook.command.CommandSender;
import snw.jkook.entity.User;
import snw.jkook.message.Message;
import snw.jkook.plugin.Plugin;
import snw.kookbc.impl.KBCClient;
import snw.kookbc.impl.command.litecommands.annotations.prefix.Prefix;

import java.util.Objects;

@Command(name = "link")
@Prefix("/")
@Description("将KOOK账号与MC账号绑定")
public class LinkCommand {

    KBCClient kbcClient;
    Plugin plugin;
    org.bukkit.plugin.Plugin mcPlugin;
    CacheTools cacheTools;
    UserDao userDao;

    public LinkCommand(KBCClient kbcClient, Plugin plugin, org.bukkit.plugin.Plugin mcPlugin, CacheTools cacheTools, UserDao userDao) {
        this.kbcClient = kbcClient;
        this.plugin = plugin;
        this.mcPlugin = mcPlugin;
        this.cacheTools = cacheTools;
        this.userDao = userDao;
    }

    @Execute
    void execute(@Context CommandSender sender, @Context Message message, @Arg String code) {

        if (sender instanceof User) {
            mcPlugin.getLogger().info(((User) sender).getName() + "使用了命令: " + message.getComponent().toString());

            String kookUserID = ((User) sender).getId();
            String uuid = cacheTools.codeCache.get(code, false);

            if (uuid != null) {

            } else {
                message.reply("绑定码不存在");
                return;
            }

            // 检查是否绑定过
            if (Objects.equals(userDao.selectUserByKookID(kookUserID).getKookID(), kookUserID)) {
                message.reply("该kook账号已经绑定过mc账号了，请取消原有绑定再绑定新的");
                return;
            }
            if (Objects.equals(userDao.selectUserByUUID(uuid).getUuid(), uuid)) {
                message.reply("该mc账号已经绑定过kook账号了，请取消原有绑定再绑定新的kook账号");
                return;
            }

            userDao.insert(new com.xiaoace.kooksrv.database.dao.pojo.User(kookUserID, uuid));
            // remove cache
            cacheTools.codeCache.remove(code);
            cacheTools.uuidCache.remove(uuid);
            message.reply("绑定成功");

        }


    }

}
