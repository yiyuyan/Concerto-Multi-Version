package top.gregtao.concerto.screen.skija.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
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
        LinearLayout layout3 = LinearLayout.horizontal();



        Component strMsg = Component.literal("font: ");
        layout3.addChild(new StringWidget(font.width(strMsg),20,strMsg,font));

        EditBox box = getEditBox();

        layout3.addChild(box);
        layout3.addChild(Button.builder(Component.literal("..."),(b)-> Minecraft.getInstance().setScreen(new FontSettingsScreen(this,(fs)->{
            try {
                this.setEditBoxValueDirect(box,fs);
            } catch (IllegalAccessException e) {
                ConcertoClient.LOGGER.warn("Can't set the editBox value directly.");
            }
        }))).size(20,20).build());

        layout1.addChild(layout3);

        this.layout.addToFooter(layout1);
    }

    private @NotNull EditBox getEditBox() {
        EditBox box = new EditBox(font,80,20, Component.literal("font"));

        box.setMaxLength(2560);
        box.setCanLoseFocus(true);
        box.setValue(Objects.requireNonNull(SkijaRenderSystem.font.getTypeface()).getFamilyName());
        box.setResponder((s)-> SkijaRenderSystem.setFont(s,9));
        return box;
    }
}
