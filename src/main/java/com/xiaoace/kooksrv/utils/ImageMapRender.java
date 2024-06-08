package com.xiaoace.kooksrv.utils;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class ImageMapRender extends MapRenderer {

    private BufferedImage image;

    public ImageMapRender(BufferedImage image) {

        this.image = image;

    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {

        map.setScale(MapView.Scale.FARTHEST);
        map.setLocked(true);
        canvas.drawImage(0, 0, image);

    }
}
