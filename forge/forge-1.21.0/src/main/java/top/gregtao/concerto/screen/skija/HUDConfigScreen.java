package top.gregtao.concerto.screen.skija;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.screen.skija.font.FontConfigScreen;
import top.gregtao.concerto.skija.SkijaHUDConfig;

public class HUDConfigScreen extends OptionsSubScreen {
    public HUDConfigScreen(Screen lastScreen) {
        super(lastScreen,Minecraft.getInstance().options,Component.literal(HUDConfigScreen.class.getSimpleName()));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new HUDWidget(Component.literal("hud")));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void addOptions() {}

    @Override
    protected void addFooter() {

        LinearLayout layout1 = LinearLayout.horizontal();

        layout1.addChild(
                new CycleButton.Builder<SkijaHUDConfig.HUDStatus>((t)-> Component.literal(t.name())).withValues(SkijaHUDConfig.HUDStatus.ALWAYS,SkijaHUDConfig.HUDStatus.PLAYING, SkijaHUDConfig.HUDStatus.NEVER)
                        .withInitialValue(SkijaHUDConfig.status)
                        .create(0,0,100,20,Component.translatable("concerto.screen.status"),
                                ((pCycleButton, pValue) -> {
                                    SkijaHUDConfig.status = pValue;
                                    ClientConfig.INSTANCE.options.enableDefaultLyricsHUD = pValue.equals(SkijaHUDConfig.HUDStatus.NEVER);
                                    ClientConfig.INSTANCE.writeOptions();
                                }))
        );

        layout1.addChild(Button.builder(Component.translatable("concerto.screen.settings"),(b)-> Minecraft.getInstance().setScreen(new SettingsScreen(Minecraft.getInstance().screen,
                Button.builder(Component.translatable("concerto.screen.size"),(bb)-> Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen,
                        SkijaHUDConfig.widthF,SkijaHUDConfig.heightF))).size(80,20).build(),
                Button.builder(Component.translatable("concerto.screen.colors"),(bb)-> Minecraft.getInstance().setScreen(new SettingsScreen(Minecraft.getInstance().screen,
                        Button.builder(Component.translatable("concerto.screen.bg_color"),(bbb)-> Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen,
                                SkijaHUDConfig.backgroundColorF))).size(100,20).build(),
                        Button.builder(Component.translatable("concerto.screen.ol_color"),(bbb)-> Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen,
                                SkijaHUDConfig.outlineColorF))).size(100,20).build()
                ))).size(80,20).build(),
                Button.builder(Component.translatable("concerto.screen.font"),(cb)->Minecraft.getInstance().setScreen(new FontConfigScreen(Minecraft.getInstance().screen))).size(80,20).build(),
                Button.builder(Component.translatable("concerto.screen.rect"),(bb)-> Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen,
                        SkijaHUDConfig.roundRectF,SkijaHUDConfig.outlineBoldF))).size(80,20).build()
                ))).size(80,20).build()
        );

        this.layout.addToFooter(layout1);
    }

    public static class HUDWidget extends AbstractWidget{

        public HUDWidget(Component pMessage) {
            super(SkijaHUDConfig.X,SkijaHUDConfig.Y,SkijaHUDConfig.width,SkijaHUDConfig.height, pMessage);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {}

        @Override
        public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
            SkijaHUDConfig.X = (int) pMouseX;
            SkijaHUDConfig.Y = (int) pMouseY;

            setPosition(SkijaHUDConfig.X,SkijaHUDConfig.Y);
            return true;
        }

        @Override
        public int getWidth() {
            return SkijaHUDConfig.width;
        }

        @Override
        public int getHeight() {
            return SkijaHUDConfig.height;
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {}
    }
}
