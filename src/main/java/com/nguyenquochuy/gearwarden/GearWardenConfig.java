package com.nguyenquochuy.gearwarden;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class GearWardenConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("gearwarden.json");

    public String armorSide = "left";      // "left" or "right"
    public String toolSide = "right";
    public int warnThresholdPercent = 10;
    public boolean soundEnabled = true;
    public boolean showPercent = true;
    public int offsetX = 4;
    public int offsetY = 4;

    public static GearWardenConfig load() {
        if (Files.exists(PATH)) {
            try (Reader r = Files.newBufferedReader(PATH)) {
                GearWardenConfig cfg = GSON.fromJson(r, GearWardenConfig.class);
                if (cfg != null) return cfg;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GearWardenConfig def = new GearWardenConfig();
        def.save();
        return def;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
