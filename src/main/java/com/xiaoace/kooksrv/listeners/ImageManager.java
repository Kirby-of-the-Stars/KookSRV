package com.xiaoace.kooksrv.listeners;

import cn.hutool.core.io.FileUtil;
import com.xiaoace.kooksrv.KookSRV;
import com.xiaoace.kooksrv.utils.ImageMapRender;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

public class ImageManager implements Listener {

    private final KookSRV plugin;
    private static ImageManager instance = null;
    private final File imageFolder;
    private final ArrayList<Integer> managedMapIds = new ArrayList<>();

    public ImageManager(KookSRV plugin) {
        this.plugin = plugin;
        imageFolder = new File(plugin.getDataFolder(), "images");
    }

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager(KookSRV.getPlugin(KookSRV.class));
        }
        return instance;
    }

    @EventHandler
    public void onMapInitEvent(MapInitializeEvent event) {
        if (managedMapIds.contains(event.getMap().getId())) {
            MapView view = event.getMap();
            view.getRenderers().clear();
            File imageFile = findFile(String.valueOf(view.getId()), imageFolder);
            try {
                if(imageFile==null) return;
                BufferedImage image = ImageIO.read(imageFile);
                view.addRenderer(new ImageMapRender(image));
                view.setScale(MapView.Scale.FARTHEST);
                view.setTrackingPosition(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    public void init() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadImages();
    }

    private void loadImages() {
        if (imageFolder.exists() && imageFolder.isDirectory()) {
            for (String fileName : Objects.requireNonNull(imageFolder.list())) {
                try{
                    managedMapIds.add(Integer.parseInt(FileUtil.getPrefix(fileName)));
                }catch (NumberFormatException e) {
                    plugin.getLogger().log(Level.WARNING,"Reading Not Map id Image :"+fileName);
                }

            }
        } else {
            Bukkit.getConsoleSender().sendMessage("ImageMapRenderer: Could not find images folder");
        }
    }

    private File findFile(String mainName, File folder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder.toPath(), mainName + ".*")) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    return path.toFile();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "ImageMapRenderer: Could not find images folder");
        }
        return null;
    }
}
