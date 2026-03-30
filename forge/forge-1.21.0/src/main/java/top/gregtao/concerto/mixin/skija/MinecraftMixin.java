package top.gregtao.concerto.mixin.skija;

import top.gregtao.concerto.skija.SkijaRenderSystem;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void initSkija(CallbackInfo ci){
        SkijaRenderSystem.init();
    }
}
