package com.xiaoace.kooksrv.listeners;

import cn.hutool.core.net.url.UrlBuilder;
import com.xiaoace.kooksrv.KookSRV;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.advancement.AdvancementDisplayType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import snw.jkook.entity.abilities.Accessory;
import snw.jkook.entity.channel.Channel;
import snw.jkook.entity.channel.TextChannel;
import snw.jkook.message.component.card.CardBuilder;
import snw.jkook.message.component.card.MultipleCardComponent;
import snw.jkook.message.component.card.Size;
import snw.jkook.message.component.card.Theme;
import snw.jkook.message.component.card.element.ImageElement;
import snw.jkook.message.component.card.element.MarkdownElement;
import snw.jkook.message.component.card.module.SectionModule;
import snw.jkook.message.component.card.structure.Paragraph;

import java.util.logging.Level;

public class MinecraftListener implements Listener {

    private final KookSRV plugin;

    private boolean minecraftToKook;
    private String playerMessage;
    private boolean onJoin;
    private String joinMessage;
    private boolean onQuit;
    private String quitMessage;
    private boolean onDeath;
    private String deathMessage;
    private boolean onAdvancement;

    private TextChannel textChannel;

    public MinecraftListener(KookSRV plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        minecraftToKook = plugin.getConfig().getBoolean("feature.minecraft-to-kook.enable", true);
        playerMessage = plugin.getConfig().getString("feature.minecraft-to-kook.message-format", "<{playerName}> {message}");

        onJoin = plugin.getConfig().getBoolean("feature.minecraft-to-kook.extra.join", true);
        joinMessage = plugin.getConfig().getString("feature.minecraft-to-kook.extra.join-message-format", "{playerName}偷偷的溜进了服务器");

        onQuit = plugin.getConfig().getBoolean("feature.minecraft-to-kook.extra.quit", true);
        quitMessage = plugin.getConfig().getString("feature.minecraft-to-kook.extra.quit-message-format", "肝帝{playerName}歇逼了");

        onDeath = plugin.getConfig().getBoolean("feature.minecraft-to-kook.extra.death", true);
        deathMessage = plugin.getConfig().getString("feature.minecraft-to-kook.extra.death-message-format", "{playerName}去势了喵");

        onAdvancement = plugin.getConfig().getBoolean("feature.minecraft-to-kook.extra.advancement", true);


        Channel channel = plugin.getBot().getKbcClient().getCore().getHttpAPI().getChannel(plugin.getConfig().getString("feature.targetChannelID"));

        if (channel instanceof TextChannel) {
            textChannel = (TextChannel) plugin.getBot().getKbcClient().getCore().getHttpAPI().getChannel(plugin.getConfig().getString("feature.targetChannelID"));
        } else {
            plugin.getLogger().log(Level.SEVERE, "你没有提供channel ID或channel ID不正确");
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

    }

    //游戏内消息
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMinecraftPlayerMessage(AsyncPlayerChatEvent event) {

        if (!minecraftToKook) return;
        if (event.isCancelled()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                String finalMessage = playerMessage
                        .replaceAll("\\{playerName}", event.getPlayer().getName())
                        .replaceAll("\\{message}", event.getMessage());
                textChannel.sendComponent(finalMessage);
            }
        }.runTaskAsynchronously(plugin);
    }

    //玩家上线
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {

        if (!onJoin) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                textChannel.sendComponent(buildOlineCard(event, true));
            }
        }.runTaskAsynchronously(plugin);

    }

    //玩家下线
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {

        if (!onQuit) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                textChannel.sendComponent(buildOlineCard(event, false));
            }
        }.runTaskAsynchronously(plugin);

    }

    //玩家死亡
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {

        if (!onDeath) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                String finalMessage = deathMessage.replaceAll("\\{playerName}", event.getEntity().getName());
                CardBuilder cardBuilder = new CardBuilder();
                cardBuilder.setSize(Size.LG);
                cardBuilder.setTheme(Theme.DANGER);
                cardBuilder.addModule(
                        new SectionModule(
                                new MarkdownElement(finalMessage),
                                new ImageElement(getPlayerIconUrl(event.getEntity().getUniqueId().toString()), null, Size.SM, false),
                                Accessory.Mode.LEFT
                        )
                );
                textChannel.sendComponent(cardBuilder.build());
            }
        }.runTaskAsynchronously(plugin);

    }

    //玩家获得新成就
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {

        if (!onAdvancement) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                AdvancementDisplay display = event.getAdvancement().getDisplay();
                if(display == null) return;
                AdvancementDisplayType type = display.getType();

                String title = event.getPlayer().getName()+ " 取得了进度 " + "["+ "(font)" + display.getTitle() + "(font)" + "[success]" +"]";
                switch (type) {
                    case TASK, GOAL -> title = event.getPlayer().getName() + " 取得了进度 " + "["+ "(font)" + display.getTitle() + "(font)" + "[success]" +"]";
                    case CHALLENGE -> title = event.getPlayer().getName()+ " 完成了挑战 " + "["+ "(font)" + display.getTitle() + "(font)" + "[purple]" +"]";
                }

                String description = display.getDescription();

                CardBuilder cardBuilder = new CardBuilder();
                cardBuilder.setSize(Size.LG);
                cardBuilder.setTheme(Theme.NONE);
                cardBuilder.addModule(
                        new SectionModule(new Paragraph.Builder(1)
                                .addField(new MarkdownElement(title))
                                .addField(new MarkdownElement(description))
                                .build(),
                                new ImageElement(getPlayerIconUrl(event.getPlayer().getUniqueId().toString()), null, Size.SM, false),
                                Accessory.Mode.LEFT
                        )
                );
                textChannel.sendComponent(cardBuilder.build());
            }
        }.runTaskAsynchronously(plugin);

    }

    //上下线卡片 true为join false为Quit
    private MultipleCardComponent buildOlineCard(PlayerEvent event, boolean joinOrQuit) {

        String finalMessage;
        CardBuilder cardBuilder = new CardBuilder();
        cardBuilder.setSize(Size.LG);
        if (joinOrQuit) {
            cardBuilder.setTheme(Theme.SUCCESS);
            finalMessage = joinMessage.replaceAll("\\{playerName}", event.getPlayer().getName());
        } else {
            cardBuilder.setTheme(Theme.DANGER);
            finalMessage = quitMessage.replaceAll("\\{playerName}", event.getPlayer().getName());
        }
        cardBuilder.addModule(
                new SectionModule(
                        new MarkdownElement(finalMessage),
                        new ImageElement(getPlayerIconUrl(event.getPlayer().getUniqueId().toString()), null, Size.SM, false),
                        Accessory.Mode.LEFT
                )
        );
        return cardBuilder.build();
    }


    private static String getPlayerIconUrl(String playerUUID) {
        return UrlBuilder.of("https://crafatar.com").addPath("avatars").addPath(playerUUID).addQuery("overlay", "true").build();
    }

}
