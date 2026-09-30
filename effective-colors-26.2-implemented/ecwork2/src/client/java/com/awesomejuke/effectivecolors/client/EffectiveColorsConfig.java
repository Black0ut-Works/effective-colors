package com.awesomejuke.effectivecolors.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class EffectiveColorsConfig {
    public enum Aura { OFF, RAINBOW, FIRE, LIGHTNING, CRYSTAL, WAVE, STAR, SPIRAL }
    public enum NameMode { OFF, RGB, WAVE }

    public static final EffectiveColorsConfig INSTANCE = new EffectiveColorsConfig();

    public NameMode nameMode = NameMode.RGB;
    public Aura aura = Aura.RAINBOW;
    public int color = 0xFFFF00;
    public int particles = 8;
    public double speed = 1.0;
    public boolean skinRgb = false;

    private Path path;

    public void load(Path configDir) {
        path = configDir.resolve("effectivecolors.properties");
        Properties p = new Properties();
        if (Files.exists(path)) {
            try (var in = Files.newInputStream(path)) { p.load(in); } catch (IOException ignored) { }
        }
        try { nameMode = NameMode.valueOf(p.getProperty("nameMode", nameMode.name())); } catch (Exception ignored) { }
        try { aura = Aura.valueOf(p.getProperty("aura", aura.name())); } catch (Exception ignored) { }
        try { color = Integer.parseInt(p.getProperty("color", Integer.toString(color)), 16) & 0xFFFFFF; } catch (Exception ignored) { }
        try { particles = Math.max(1, Math.min(40, Integer.parseInt(p.getProperty("particles", "8")))); } catch (Exception ignored) { }
        try { speed = Math.max(0.1, Math.min(5.0, Double.parseDouble(p.getProperty("speed", "1.0")))); } catch (Exception ignored) { }
        skinRgb = Boolean.parseBoolean(p.getProperty("skinRgb", Boolean.toString(skinRgb)));
    }

    public void save() {
        if (path == null) return;
        Properties p = new Properties();
        p.setProperty("nameMode", nameMode.name());
        p.setProperty("aura", aura.name());
        p.setProperty("color", String.format("%06X", color));
        p.setProperty("particles", Integer.toString(particles));
        p.setProperty("speed", Double.toString(speed));
        p.setProperty("skinRgb", Boolean.toString(skinRgb));
        try {
            Files.createDirectories(path.getParent());
            try (var out = Files.newOutputStream(path)) { p.store(out, "Effective Colors settings"); }
        } catch (IOException ignored) { }
    }
}
