package top.gregtao.concerto.skija;

import io.github.humbleui.skija.*;
import net.minecraftforge.eventbus.api.Event;

public class SkijaRenderEvent extends Event {
    public final Surface surface;
    public final Canvas canvas;
    public final DirectContext directContext;
    public final BackendRenderTarget backendRenderTarget;
    public final Font font;

    public final int currentW,currentH;

    public SkijaRenderEvent(Surface surface, Canvas canvas, DirectContext directContext, BackendRenderTarget backendRenderTarget, Font font, int currentW, int currentH) {
        this.surface = surface;
        this.canvas = canvas;
        this.directContext = directContext;
        this.backendRenderTarget = backendRenderTarget;
        this.font = font;
        this.currentW = currentW;
        this.currentH = currentH;
    }
}
