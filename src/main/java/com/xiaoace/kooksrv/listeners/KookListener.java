package com.xiaoace.kooksrv.listeners;

import com.xiaoace.kooksrv.KookSRV;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import snw.jkook.entity.User;
import snw.jkook.entity.channel.TextChannel;
import snw.jkook.event.EventHandler;
import snw.jkook.event.Listener;
import snw.jkook.event.channel.ChannelMessageEvent;
import snw.jkook.message.component.TextComponent;

import static com.xiaoace.kooksrv.utils.MinecraftTextConverter.convertToMinecraftFormat;

public class KookListener implements Listener {

    private final KookSRV plugin;
    private String targetChannelID;

    private boolean kookToMinecraft;
    private String needFormatMessage;

    public KookListener(KookSRV plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        kookToMinecraft = plugin.getConfig().getBoolean("feature.kook-to-minecraft.enable",true);
        targetChannelID = plugin.getConfig().getString("feature.targetChannelID");

        needFormatMessage = plugin.getConfig().getString("feature.kook-to-minecraft.message-format","<{nickName}> {message}");
    }

    @EventHandler
    public void onKookTextMessage(ChannelMessageEvent event) {

        System.out.println(event.getMessage().getComponent().toString());
        System.out.println(targetChannelID);

        // enable?
        if (!kookToMinecraft) return;

        // in right channel?
        if (!(event.getChannel().getId().equals(targetChannelID))) return;

        // isBot?
        if (event.getMessage().getSender().isBot()) return;

        // really a text message?
        if (!(event.getMessage().getComponent() instanceof TextComponent textComponent)) return;

        User sender = event.getMessage().getSender();
        String senderId = sender.getId();
        TextChannel channel = event.getChannel();
        String message = textComponent.toString();
        String senderNickname = sender.getNickName(channel.getGuild());

        String formattedMessage = needFormatMessage.replaceAll("\\{nickName}",senderNickname)
                .replaceAll("\\{message}",convertToMinecraftFormat(message));

        if (!formattedMessage.trim().isEmpty()){

            String clickEventValue = String.format("(met)%s(met)",senderId);

            String hoverText = "点击以快速回复Kook的消息,注意: 会直接覆盖聊天栏！！！";

            net.md_5.bungee.api.chat.TextComponent ct = new net.md_5.bungee.api.chat.TextComponent(formattedMessage);

            ct.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,clickEventValue));

            Text text = new Text(new ComponentBuilder(hoverText).create());

            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,text);

            ct.setHoverEvent(hoverEvent);

            Bukkit.spigot().broadcast(ct);

        }

    }

}
