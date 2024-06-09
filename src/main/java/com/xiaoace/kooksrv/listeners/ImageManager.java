package com.xiaoace.kooksrv.listeners;

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
import java.util.ArrayList;
import java.util.Objects;

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

            File imageFile = new File(imageFolder, view.getId() + ".png");
            try {
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
                managedMapIds.add(Integer.parseInt(fileName.replace(".png", "")));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("ImageMapRenderer: Could not find images folder");
        }
    }
}
