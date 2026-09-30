package com.awesomejuke.effectivecolors.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EffectiveColorsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreen::new;
    }

    public static class ConfigScreen extends Screen {
        private final Screen parent;
        private EditBox color;
        private EditBox particles;
        private EditBox speed;
        private Button aura;
        private Button name;
        private Button skin;

        public ConfigScreen(Screen parent) {
            super(Component.literal("Effective Colors"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            color = new EditBox(this.font, cx - 100, 45, 200, 20, Component.literal("Hex color"));
            color.setValue(String.format("#%06X", EffectiveColorsClient.CONFIG.color));
            addRenderableWidget(color);
            particles = new EditBox(this.font, cx - 100, 90, 200, 20, Component.literal("Particles"));
            particles.setValue(Integer.toString(EffectiveColorsClient.CONFIG.particles));
            addRenderableWidget(particles);
            speed = new EditBox(this.font, cx - 100, 135, 200, 20, Component.literal("Speed"));
            speed.setValue(Double.toString(EffectiveColorsClient.CONFIG.speed));
            addRenderableWidget(speed);
            aura = addRenderableWidget(Button.builder(auraText(), b -> cycleAura()).bounds(cx - 100, 175, 200, 20).build());
            name = addRenderableWidget(Button.builder(nameText(), b -> cycleName()).bounds(cx - 100, 205, 200, 20).build());
            skin = addRenderableWidget(Button.builder(skinText(), b -> { EffectiveColorsClient.CONFIG.skinRgb = !EffectiveColorsClient.CONFIG.skinRgb; b.setMessage(skinText()); save(); }).bounds(cx - 100, 235, 200, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveAndClose()).bounds(cx - 100, 275, 95, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> minecraft.setScreen(parent)).bounds(cx + 5, 275, 95, 20).build());
        }
        private Component auraText(){ return Component.literal("Aura: "+EffectiveColorsClient.CONFIG.aura); }
        private Component nameText(){ return Component.literal("Name: "+EffectiveColorsClient.CONFIG.nameMode); }
        private Component skinText(){ return Component.literal("Skin RGB: "+(EffectiveColorsClient.CONFIG.skinRgb?"ON":"OFF")); }
        private void cycleAura(){ var v=EffectiveColorsConfig.Aura.values(); int n=(EffectiveColorsClient.CONFIG.aura.ordinal()+1)%v.length; EffectiveColorsClient.CONFIG.aura=v[n]; aura.setMessage(auraText()); }
        private void cycleName(){ var v=EffectiveColorsConfig.NameMode.values(); int n=(EffectiveColorsClient.CONFIG.nameMode.ordinal()+1)%v.length; EffectiveColorsClient.CONFIG.nameMode=v[n]; name.setMessage(nameText()); }
        private void save(){ try{EffectiveColorsClient.CONFIG.color=Integer.parseInt(color.getValue().replace("#",""),16)&0xFFFFFF;}catch(Exception ignored){} try{EffectiveColorsClient.CONFIG.particles=Math.max(1,Math.min(40,Integer.parseInt(particles.getValue())));}catch(Exception ignored){} try{EffectiveColorsClient.CONFIG.speed=Math.max(0.1,Math.min(5.0,Double.parseDouble(speed.getValue())));}catch(Exception ignored){} EffectiveColorsClient.CONFIG.save(); }
        private void saveAndClose(){ save(); minecraft.setScreen(parent); }
        @Override public void render(net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY, float delta){ renderBackground(g, mouseX, mouseY, delta); g.drawCenteredString(font, title, width/2, 15, 0xFFFFFF); g.drawString(font, "Hex color", width/2-100, 34, 0xAAAAAA); g.drawString(font, "Particles", width/2-100, 79, 0xAAAAAA); g.drawString(font, "Speed", width/2-100, 124, 0xAAAAAA); super.render(g,mouseX,mouseY,delta); }
        @Override public void onClose(){ minecraft.setScreen(parent); }
    }
}
