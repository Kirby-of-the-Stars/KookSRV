package com.xiaoace.kooksrv.listeners;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.xiaoace.kooksrv.KookSRV;
import com.xiaoace.kooksrv.database.dao.UserDao;
import com.xiaoace.kooksrv.utils.ImageMapRender;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

                net.kyori.adventure.text.TextComponent textcomponent = Component.text()
                        .append(Component.text(formattedMessage).hoverEvent(Component.text("点击@该KOOK用户")).clickEvent(ClickEvent.suggestCommand(clickEventValue)))
                        .build();

                Audience audience = plugin.adventure().all();
                audience.sendMessage(textcomponent);

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
                    }
                }
            }

        }
    }

    public void makeMap(String url, String senderNickName) {
        File resultFile = null;
        try {
            File cacheFolder = new File(plugin.getDataFolder(), "images");
            // 从KOOK下载图片
            resultFile = HttpUtil.downloadFileFromUrl(url, cacheFolder);
            String MIMETYPE = FileUtil.getMimeType(resultFile.getName());
            String suffix = "." + FileUtil.getSuffix(resultFile);
            //如果ImageIO没有其对应的处理器，则仅发送链接
            if (!ImageIO.getImageReadersByMIMEType(MIMETYPE).hasNext()) {
                sendExternalLink(url, senderNickName);
                return;
            }
            ItemStack map = new ItemStack(Material.FILLED_MAP, 1);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();

            // 创建地图
            MapView view = Bukkit.createMap(plugin.getServer().getWorlds().get(0));

            // 缩放并存储图片
            File imageFile2 = new File(cacheFolder, view.getId() + suffix);
            ImgUtil.scale(resultFile, imageFile2, 128, 128, null);
            // 清空渲染器
            view.getRenderers().clear();

            // 添加自己的渲染器
            BufferedImage image = ImageIO.read(imageFile2);
            view.addRenderer(new ImageMapRender(image));

            mapMeta.setMapView(view);
            mapMeta.setDisplayName(String.valueOf(view.getId()));
            map.setItemMeta(mapMeta);

            net.kyori.adventure.text.TextComponent textcomponent = Component.text()
                    .append(Component.text("<" + senderNickName + ">" + " [图片] ")
                            .hoverEvent(Component.text("点击打开链接"))
                            .clickEvent(ClickEvent.openUrl(url)))
                    .append(Component.text("[点击获取地图]").clickEvent(ClickEvent.runCommand("/kooksrv getMap " + view.getId()))).build();

            Audience audience = plugin.adventure().all();
            audience.sendMessage(textcomponent);

        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error downloading and caching image: " + e.getMessage());
        } finally {
            if (resultFile != null && resultFile.exists()) {
                resultFile.delete();
            }
        }

    }

    /**
     * 发送图片链接
     *
     * @param url            源url
     * @param senderNickName 发送者名称
     * @author DAY
     */
    private void sendExternalLink(String url, String senderNickName) {
        net.kyori.adventure.text.TextComponent textcomponent = Component.text()
                .append(Component.text("<" + senderNickName + ">" + " [图片] ")
                        .hoverEvent(Component.text("点击打开链接"))
                        .clickEvent(ClickEvent.openUrl(url))).build();
        Audience audience = plugin.adventure().all();
        audience.sendMessage(textcomponent);
    }

}
