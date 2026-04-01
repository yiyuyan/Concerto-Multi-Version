package top.gregtao.concerto.screen.skija;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.gregtao.concerto.skija.SkijaHUDConfig;

public class HUDConfigScreen extends Screen {
    public HUDConfigScreen() {
        super(Component.literal(HUDConfigScreen.class.getSimpleName()));
    }

    @Override
    protected void init() {
        addRenderableWidget(new HUDWidget(Component.literal("hud")));
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
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
    }
}
