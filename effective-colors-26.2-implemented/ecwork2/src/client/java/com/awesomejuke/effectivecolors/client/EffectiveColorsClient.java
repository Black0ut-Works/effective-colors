package com.awesomejuke.effectivecolors.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;

public class EffectiveColorsClient implements ClientModInitializer {
    public static final EffectiveColorsConfig CONFIG = EffectiveColorsConfig.INSTANCE;
    private static long ticks;

    @Override
    public void onInitializeClient() {
        CONFIG.load(Path.of("config"));
        registerCommands();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            ticks++;
            updateName(client.player);
            spawnAura(client, client.player);
        });
    }

    private static void updateName(LocalPlayer player) {
        if (CONFIG.nameMode == EffectiveColorsConfig.NameMode.OFF) {
            player.setCustomName(null);
            player.setCustomNameVisible(false);
            return;
        }
        String name = player.getGameProfile().name();
        Component result = Component.empty();
        double t = ticks * 0.035 * CONFIG.speed;
        for (int i = 0; i < name.length(); i++) {
            float h = (float)((t + i * 0.075) % 1.0);
            if (CONFIG.nameMode == EffectiveColorsConfig.NameMode.WAVE) {
                h = (float)((Math.sin(t * 2.0 + i * 0.7) + 1.0) * 0.5);
            }
            int rgb = hsv(h, 1.0f, 1.0f);
            result = result.copy().append(Component.literal(String.valueOf(name.charAt(i))).setStyle(Style.EMPTY.withColor(rgb)));
        }
        player.setCustomName(result);
        player.setCustomNameVisible(true);
    }

    private static void spawnAura(Minecraft client, LocalPlayer p) {
        var aura = CONFIG.aura;
        if (aura == EffectiveColorsConfig.Aura.OFF) return;
        int count = Math.max(1, CONFIG.particles);
        double time = ticks * 0.08 * CONFIG.speed;
        for (int i = 0; i < count; i++) {
            double u = (double)i / count;
            double a = time + u * Math.PI * 2.0;
            double x, y, z, vx = 0, vy = 0, vz = 0;
            switch (aura) {
                case RAINBOW -> {
                    double r = 0.9 + 0.25 * Math.sin(time * 2.0);
                    x = p.getX() + Math.cos(a) * r;
                    y = p.getY() + 0.8 + 0.8 * Math.sin(a * 2.0);
                    z = p.getZ() + Math.sin(a) * r;
                    client.level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.01, 0);
                }
                case FIRE -> {
                    x = p.getX() + Math.cos(a) * 0.65;
                    y = p.getY() + 0.1 + ((u + time * 0.18) % 1.0) * 1.9;
                    z = p.getZ() + Math.sin(a) * 0.65;
                    client.level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.025, 0);
                }
                case LIGHTNING -> {
                    double pulse = 0.45 + 0.35 * Math.sin(time * 7.0);
                    x = p.getX() + Math.cos(a) * pulse;
                    y = p.getY() + 0.4 + u * 1.5;
                    z = p.getZ() + Math.sin(a) * pulse;
                    client.level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0.02, 0);
                }
                case CRYSTAL -> {
                    double r = 0.9;
                    x = p.getX() + Math.cos(a) * r;
                    y = p.getY() + 1.0 + 0.35 * Math.sin(time * 2.0 + u * 6.0);
                    z = p.getZ() + Math.sin(a) * r;
                    client.level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.0, 0);
                }
                case WAVE -> {
                    double r = ((time * 0.55 + u) % 1.0) * 2.0;
                    x = p.getX() + Math.cos(a) * r;
                    y = p.getY() + 0.1 + 0.12 * Math.sin(time * 5.0 + u * 20.0);
                    z = p.getZ() + Math.sin(a) * r;
                    client.level.addParticle(ParticleTypes.SCRAPE, x, y, z, 0, 0.01, 0);
                }
                case STAR -> {
                    double r = (i % 2 == 0) ? 1.0 : 0.45;
                    x = p.getX() + Math.cos(a * 2.5) * r;
                    y = p.getY() + 1.0 + 0.5 * Math.sin(time * 3.0 + i);
                    z = p.getZ() + Math.sin(a * 2.5) * r;
                    client.level.addParticle(ParticleTypes.FIREWORK, x, y, z, 0, 0, 0);
                }
                case SPIRAL -> {
                    double r = 0.25 + u * 0.9;
                    x = p.getX() + Math.cos(a) * r;
                    y = p.getY() + u * 1.9;
                    z = p.getZ() + Math.sin(a) * r;
                    client.level.addParticle(ParticleTypes.WITCH, x, y, z, 0, 0.01, 0);
                }
                default -> { return; }
            }
        }
    }

    private static int hsv(float h, float s, float v) {
        int i = (int)(h * 6.0f);
        float f = h * 6.0f - i;
        float p = v * (1.0f - s);
        float q = v * (1.0f - f * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r=v; g=tob(f,s,v); b=p; }
            case 1 -> { r=q; g=v; b=p; }
            case 2 -> { r=p; g=v; b=tob(f,s,v); }
            case 3 -> { r=p; g=q; b=v; }
            case 4 -> { r=tob(f,s,v); g=p; b=v; }
            default -> { r=v; g=p; b=q; }
        }
        return ((int)(r*255)<<16)|((int)(g*255)<<8)|(int)(b*255);
    }
    private static float tob(float f,float s,float v){return v*(1-(1-f)*s);}

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("ec")
                .then(ClientCommandManager.literal("help").executes(ctx -> { msg("/ec aura <off|rainbow|fire|lightning|crystal|wave|star|spiral> | /ec name <off|rgb|wave> | /ec color <hex> | /ec particles <1-40> | /ec speed <0.1-5>"); return 1; }))
                .then(ClientCommandManager.literal("aura").then(ClientCommandManager.argument("type", StringArgumentType.word()).executes(ctx -> { setAura(StringArgumentType.getString(ctx,"type")); return 1; })))
                .then(ClientCommandManager.literal("name").then(ClientCommandManager.argument("mode", StringArgumentType.word()).executes(ctx -> { setName(StringArgumentType.getString(ctx,"mode")); return 1; })))
                .then(ClientCommandManager.literal("color").then(ClientCommandManager.argument("hex", StringArgumentType.word()).executes(ctx -> { setColor(StringArgumentType.getString(ctx,"hex")); return 1; })))
                .then(ClientCommandManager.literal("particles").then(ClientCommandManager.argument("count", IntegerArgumentType.integer(1,40)).executes(ctx -> { CONFIG.particles=IntegerArgumentType.getInteger(ctx,"count"); CONFIG.save(); msg("Particles: "+CONFIG.particles); return 1; })))
                .then(ClientCommandManager.literal("speed").then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.1,5.0)).executes(ctx -> { CONFIG.speed=DoubleArgumentType.getDouble(ctx,"value"); CONFIG.save(); msg("Speed: "+CONFIG.speed); return 1; })))
        ));
    }
    private static void setAura(String s){ try{CONFIG.aura=EffectiveColorsConfig.Aura.valueOf(s.toUpperCase()); CONFIG.save(); msg("Aura: "+CONFIG.aura);}catch(Exception e){msg("Unknown aura. Use /ec help");} }
    private static void setName(String s){ try{CONFIG.nameMode=EffectiveColorsConfig.NameMode.valueOf(s.toUpperCase()); CONFIG.save(); msg("Name: "+CONFIG.nameMode);}catch(Exception e){msg("Unknown name mode. Use /ec help");} }
    private static void setColor(String s){try{String h=s.replace("#",""); CONFIG.color=Integer.parseInt(h,16)&0xFFFFFF; CONFIG.save(); msg("Color: #"+String.format("%06X",CONFIG.color));}catch(Exception e){msg("Use /ec color FFFF00");}}
    private static void msg(String s){ if(Minecraft.getInstance().player!=null) Minecraft.getInstance().player.displayClientMessage(Component.literal("[Effective Colors] "+s), false); }
}
