package top.gregtao.concerto.screen.skija;

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

    public class HUDWidget extends AbstractWidget{

        public HUDWidget(Component pMessage) {
            super(SkijaHUDConfig.X,SkijaHUDConfig.Y,SkijaHUDConfig.width,SkijaHUDConfig.height, pMessage);
        }

        @Override
        public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            pGuiGraphics.renderOutline(this.getX(),this.getY(),this.getWidth(),this.getHeight(),0x30FFFFFF);
            setDragging(
                    GLFW.glfwGetMouseButton(
                            Minecraft.getInstance().getWindow().getWindow(),
                            GLFW.GLFW_MOUSE_BUTTON_1)
                            == GLFW.GLFW_PRESS
            );
        }

        @Override
        public void mouseMoved(double pMouseX, double pMouseY) {
            if(isMouseOver(pMouseX, pMouseY) && isDragging()){
                SkijaHUDConfig.X = (int) pMouseX;
                SkijaHUDConfig.Y = (int) pMouseY;
            }
        }

        @Override
        public int getX() {
            this.setX(SkijaHUDConfig.X);
            return super.getX();
        }

        @Override
        public int getY() {
            this.setY(SkijaHUDConfig.Y);
            return super.getY();
        }

        @Override
        public int getWidth() {
            this.setWidth(SkijaHUDConfig.width);
            return super.getWidth();
        }

        @Override
        public int getHeight() {
            this.setHeight(SkijaHUDConfig.height);
            return super.getHeight();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {}
    }
}
