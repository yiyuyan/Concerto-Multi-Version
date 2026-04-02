package top.gregtao.concerto.screen.skija;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import top.gregtao.concerto.skija.SkijaHUDConfig;

public class SettingsScreen extends ConfigScreen {

    public SettingsScreen(Screen pLastScreen) {
        super(pLastScreen);
    }

    @Override
    protected void addFooter() {
        LinearLayout layout1 = LinearLayout.horizontal();

        layout1.addChild(
                new CycleButton.Builder<SkijaHUDConfig.HUDStatus>((t)-> Component.literal(t.name())).withValues(SkijaHUDConfig.HUDStatus.ALWAYS,SkijaHUDConfig.HUDStatus.PLAYING, SkijaHUDConfig.HUDStatus.NEVER)
                        .withInitialValue(SkijaHUDConfig.status)
                        .create(0,0,100,20,Component.literal("Status"),
                                ((pCycleButton, pValue) -> SkijaHUDConfig.status = pValue))
        );

        layout1.addChild(Button.builder(Component.literal("Size"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.widthF,SkijaHUDConfig.heightF))).size(80,20).build());

        layout1.addChild(Button.builder(Component.literal("BgColor"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.backgroundColorF))).size(80,20).build());

        layout1.addChild(Button.builder(Component.literal("OutlineColor"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.outlineColorF))).size(80,20).build());

        layout1.addChild(Button.builder(Component.literal("Misc"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.roundRectF,SkijaHUDConfig.outlineBoldF))).size(80,20).build());

        this.layout.addToFooter(layout1);
    }
}
