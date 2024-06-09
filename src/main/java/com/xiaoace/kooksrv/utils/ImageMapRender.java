package com.xiaoace.kooksrv.utils;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

public class ImageMapRender extends MapRenderer {

    private BufferedImage image;
    private boolean flag;

    public ImageMapRender(BufferedImage image) {
        this.image = image;
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {

        if (flag) {
            return;
        }
        canvas.drawImage(0, 0, image);
        map.setScale(MapView.Scale.FARTHEST);
        map.setTrackingPosition(false);
        map.setLocked(true);
        flag = true;
    }
}
