package top.gregtao.concerto.screen.skija;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
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
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {}

    @Override
    protected void addOptions() {}

    @Override
    protected void addFooter() {

        LinearLayout layout1 = LinearLayout.horizontal();

        layout1.addChild(Button.builder(Component.literal("BasicSettings"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.widthF,SkijaHUDConfig.heightF,
                SkijaHUDConfig.roundRectF,SkijaHUDConfig.outlineBoldF))).size(80,20).build());

        layout1.addChild(Button.builder(Component.literal("BgColorSettings"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.backgroundColorF))).size(80,20).build());

        layout1.addChild(Button.builder(Component.literal("OlColorSettings"),(b)-> Minecraft.getInstance().setScreen(new ConfigScreen(this,
                SkijaHUDConfig.outlineColorF))).size(80,20).build());

        this.layout.addToFooter(layout1);
    }

    public static class HUDWidget extends AbstractWidget{

        public boolean pressing = false;

        public HUDWidget(Component pMessage) {
            super(SkijaHUDConfig.X,SkijaHUDConfig.Y,SkijaHUDConfig.width,SkijaHUDConfig.height, pMessage);
        }

        @Override
        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            pGuiGraphics.renderOutline(this.getX(),this.getY(),this.getWidth(),this.getHeight(),0x3FFFFFFF);
            this.pressing = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), InputConstants.MOUSE_BUTTON_LEFT)==1;
        }

        @Override
        public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
            if(this.pressing){
                SkijaHUDConfig.X = (int) pMouseX;
                SkijaHUDConfig.Y = (int) pMouseY;

                setPosition(SkijaHUDConfig.X,SkijaHUDConfig.Y);
            }
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        @Override
        protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {}
    }
}
