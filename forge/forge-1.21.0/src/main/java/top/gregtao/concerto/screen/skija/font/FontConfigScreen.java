package top.gregtao.concerto.screen.skija.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.screen.skija.ConfigScreen;
import top.gregtao.concerto.skija.SkijaRenderSystem;

import java.util.Objects;

public class FontConfigScreen extends ConfigScreen {
    public FontConfigScreen(Screen pLastScreen) {
        super(pLastScreen);
    }

    @Override
    protected void addFooter() {
        LinearLayout layout1 = LinearLayout.horizontal();
        LinearLayout layout2 = LinearLayout.horizontal();
        EditBox box = new EditBox(font,180,20, Component.literal("font"));

        box.setMaxLength(2560);
        box.setCanLoseFocus(true);
        box.setValue(Objects.requireNonNull(SkijaRenderSystem.font.getTypeface()).getFamilyName());
        box.setResponder(SkijaRenderSystem::setFont);

        layout2.addChild(box);
        layout2.addChild(Button.builder(Component.literal("..."),(b)-> Minecraft.getInstance().setScreen(new FontSettingsScreen(this,(fs)->{
            try {
                this.setEditBoxValueDirect(box,fs);
            } catch (IllegalAccessException e) {
                ConcertoClient.LOGGER.warn("Can't set the editbox value directly.");
            }
        }))).size(20,20).build());

        layout1.addChild(layout2);

        this.layout.addToFooter(layout1);
    }
}
