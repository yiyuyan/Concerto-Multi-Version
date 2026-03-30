package top.gregtao.concerto.mixin.skija;

import net.minecraftforge.common.MinecraftForge;
import top.gregtao.concerto.skija.SkijaRenderEvent;
import top.gregtao.concerto.skija.SkijaRenderSystem;
import top.gregtao.concerto.skija.gl.States;

import com.mojang.blaze3d.platform.Window;

import static top.gregtao.concerto.skija.SkijaRenderSystem.*;

import org.lwjgl.opengl.GL11;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    @Inject(method = "onFramebufferResize",at = @At("RETURN"))
    private void resize(long p_85416_, int p_85417_, int p_85418_, CallbackInfo ci){
        SkijaRenderSystem.recreateSurface();
    }

    @Inject(at = @At("HEAD"), method = "updateDisplay")
    private void render(CallbackInfo info) {
        if (context == null || surface == null) return;
        States.push();

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0f, 0f, 0f, 0f);

        context.resetAll();

        MinecraftForge.EVENT_BUS.post(new SkijaRenderEvent(surface,canvas,
                context, renderTarget, font,
                currentWidth,currentHeight));

        // Ensure Skija submits all pending operations
        context.flush();

        States.pop();

    }
}
