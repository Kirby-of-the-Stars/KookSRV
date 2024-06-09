package com.xiaoace.kooksrv.listeners;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.xiaoace.kooksrv.KookSRV;
import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.utils.ImageMapRender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitScheduler;
import snw.jkook.entity.User;
import snw.jkook.entity.channel.TextChannel;
import snw.jkook.event.EventHandler;
import snw.jkook.event.Listener;
import snw.jkook.event.channel.ChannelMessageEvent;
import snw.jkook.message.component.TextComponent;
import snw.jkook.message.component.card.CardComponent;
import snw.jkook.message.component.card.element.ImageElement;
import snw.jkook.message.component.card.module.BaseModule;
import snw.jkook.message.component.card.module.ContainerModule;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static com.xiaoace.kooksrv.utils.MinecraftTextConverter.convertToMinecraftFormat;
import static org.bukkit.Bukkit.getServer;

public class KookListener implements Listener {

    private final KookSRV plugin;
    private String targetChannelID;

    private boolean kookToMinecraft;
    private String needFormatMessage;
    UserDao userDao;

    public KookListener(KookSRV plugin, UserDao userDao) {
        this.plugin = plugin;
        this.userDao = userDao;
        init();
    }

    private void init() {
        kookToMinecraft = plugin.getConfig().getBoolean("feature.kook-to-minecraft.enable", true);
        targetChannelID = plugin.getConfig().getString("feature.targetChannelID");

        needFormatMessage = plugin.getConfig().getString("feature.kook-to-minecraft.message-format", "<{nickName}> {message}");
    }

    @EventHandler
    public void onKookTextMessage(ChannelMessageEvent event) {

        // enable?
        if (!kookToMinecraft) return;

        // in right channel?
        if (!(event.getChannel().getId().equals(targetChannelID))) return;

        // isBot?
        if (event.getMessage().getSender().isBot()) return;

        User sender = event.getMessage().getSender();
        String senderId = sender.getId();
        TextChannel channel = event.getChannel();
        String senderNickname = sender.getNickName(channel.getGuild());

        // really a text message?
        if ((event.getMessage().getComponent() instanceof TextComponent textComponent)) {

            String message = textComponent.toString();

            String formattedMessage = needFormatMessage.replaceAll("\\{nickName}", senderNickname)
                    .replaceAll("\\{message}", convertToMinecraftFormat(message));

            if (!formattedMessage.trim().isEmpty()) {

                String clickEventValue = String.format("(met)%s(met)", senderId);

                String hoverText = "点击以快速回复Kook的消息,注意: 会直接覆盖聊天栏！！！";

                net.md_5.bungee.api.chat.TextComponent ct = new net.md_5.bungee.api.chat.TextComponent(formattedMessage);

                ct.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, clickEventValue));

                Text text = new Text(new ComponentBuilder(hoverText).create());

                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);

                ct.setHoverEvent(hoverEvent);

                Bukkit.spigot().broadcast(ct);

            }

        } else if ((event.getMessage().getComponent() instanceof CardComponent cardComponent)) {

            List<BaseModule> baseModuleList = cardComponent.getModules();

            // 仅通过单张图片
            if (baseModuleList.size() == 1) {
                if (baseModuleList.get(0) instanceof ContainerModule containerModule) {
                    if (containerModule.getImages().size() == 1) {
                        ImageElement imageElement = containerModule.getImages().get(0);
                        String imageUrl = imageElement.getSource();

                        BukkitScheduler scheduler = getServer().getScheduler();
                        scheduler.runTask(plugin, () -> makeMap(imageUrl, senderNickname));
                        return;
                    }
                }
            }

        } else {
            return;
        }
    }

    public void makeMap(String url, String senderNickName) {

        String lowercaseUrl = url.toLowerCase();
        if (lowercaseUrl.endsWith(".webp")) {
            net.md_5.bungee.api.chat.TextComponent ct = new net.md_5.bungee.api.chat.TextComponent("<" + senderNickName + ">" + " [" + url + "]");
            ct.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            Bukkit.spigot().broadcast(ct);
        } else {

            File cacheFolder = new File(plugin.getDataFolder(), "images");

            // 从KOOK下载图片
            String randomName = RandomUtil.randomString(16);
            File imageFile1 = new File(cacheFolder, randomName + ".png");
            HttpUtil.downloadFileFromUrl(url, imageFile1);

            ItemStack map = new ItemStack(Material.FILLED_MAP, 1);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();

            try {
                // 创建地图
                MapView view = Bukkit.createMap(plugin.getServer().getWorlds().get(0));

                // 缩放并存储图片
                File imageFile2 = new File(cacheFolder, view.getId() + ".png");
                ImgUtil.scale(imageFile1, imageFile2, 128, 128, null);

                // 清空渲染器
                view.getRenderers().clear();

                // 添加自己的渲染器
                BufferedImage image = ImageIO.read(imageFile2);
                view.addRenderer(new ImageMapRender(image));

                mapMeta.setMapView(view);
                mapMeta.setDisplayName(String.valueOf(view.getId()));
                map.setItemMeta(mapMeta);

                net.md_5.bungee.api.chat.TextComponent ct = new net.md_5.bungee.api.chat.TextComponent("<" + senderNickName + ">" + " [" + url + "]");
                ct.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                Bukkit.spigot().broadcast(ct);

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.getInventory().addItem(map);
                }

            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Error downloading and caching image: " + e.getMessage());
            } finally {
                imageFile1.delete();
            }

        }

    }

}
