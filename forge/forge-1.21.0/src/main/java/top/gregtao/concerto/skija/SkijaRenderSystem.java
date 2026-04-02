package top.gregtao.concerto.skija;

import com.mojang.blaze3d.platform.Window;

import io.github.humbleui.skija.*;

import net.minecraft.client.Minecraft;

public class SkijaRenderSystem {
    public static Surface surface;
    public static Canvas canvas;
    public static DirectContext context;
    public static BackendRenderTarget renderTarget;
    public static Font font;

    public static int currentWidth;
    public static int currentHeight;

    public static void init(){

        // Create Skia OpenGL context
        context = DirectContext.makeGL();

        // Initial setup
        recreateSurface();
        initFont();
    }

    public static void recreateSurface() {
        if (renderTarget != null) {
            renderTarget.close();
        }
        if (surface != null) {
            surface.close();
        }

        Window window = Minecraft.getInstance().getWindow();

        currentWidth = window.getWidth();
        currentHeight = window.getHeight();

        renderTarget = BackendRenderTarget.makeGL(
                currentWidth,
                currentHeight,
                0,  // samples
                8,  // stencil bits
                0,
                FramebufferFormat.GR_GL_RGBA8);

        surface = Surface.wrapBackendRenderTarget(
                context,
                renderTarget,
                SurfaceOrigin.BOTTOM_LEFT,
                ColorType.RGBA_8888,
                ColorSpace.getSRGB());

        canvas = surface.getCanvas();

        canvas.scale((float) window.getGuiScale(), (float) window.getGuiScale());
    }

    public static void initFont(){
        Typeface typeface = FontMgr.getDefault().matchFamilyStyle("Menlo", FontStyle.NORMAL);
        font = new Font(typeface,13);
    }
}
