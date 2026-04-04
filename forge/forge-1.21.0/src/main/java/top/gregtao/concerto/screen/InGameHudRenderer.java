package top.gregtao.concerto.screen;

import com.mojang.blaze3d.platform.Window;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.enums.TextAlignment;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.core.player.MusicPlayerHandler;
import top.gregtao.concerto.screen.skija.ConfigScreen;
import top.gregtao.concerto.screen.skija.HUDConfigScreen;
import top.gregtao.concerto.screen.skija.font.FontSettingsScreen;
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.skija.SkijaHUDConfig;
import top.gregtao.concerto.skija.SkijaRenderEvent;
import top.gregtao.concerto.util.RenderUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class InGameHudRenderer {

    public static ScrollingText MUSIC_DETAIL_SCROLL = new ScrollingText();

    public static URLImageWidget HEAD_PICTURE = new URLImageWidget(20, 20, 0, 0, null, false);

    // 缓存 Skija Image 对象
    private static Image cachedSkijaImage = null;
    private static String cachedImageUrl = null;

    // 字体大小范围
    private static final float MIN_FONT_SIZE = 7f;
    private static final float MAX_FONT_SIZE = 14f;
    private static final float DEFAULT_FONT_SIZE = 11f;

    public static void init() {
        MusicPlayerHandler.headPictureSetter = (url) -> {
            HEAD_PICTURE.setUrl(url);
            HEAD_PICTURE.loadImage(true, ClientConfig.INSTANCE.options.coverImgInCircle);
            if (cachedSkijaImage != null && !cachedSkijaImage.isClosed()) {
                cachedSkijaImage.close();
            }
            cachedSkijaImage = null;
            cachedImageUrl = null;
        };
        MinecraftForge.EVENT_BUS.register(new InGameHudRenderer());
    }

    public static void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if(!ClientConfig.INSTANCE.options.enableDefaultLyricsHUD) return;
        Minecraft client = Minecraft.getInstance();
        if (MusicPlayer.INSTANCE.isPlaying()) {

            ClientConfig config = ClientConfig.INSTANCE;
            ClientConfig.ClientConfigOptions options = config.options;

            if (!(options.hideWhenChat && client.screen instanceof ChatScreen)) {
                int scaledWidth = client.getWindow().getGuiScaledWidth(), scaledHeight = client.getWindow().getGuiScaledHeight();
                String[] texts = MusicPlayerHandler.INSTANCE.getDisplayTexts();

                context = new GuiGraphics(Minecraft.getInstance(), context.bufferSource());

                if (options.displayLyrics) {
                    Vector2i pos = getPos(config.lyricsPosSupplier, scaledWidth, scaledHeight);
                    RenderUtil.renderText(Component.literal(texts[0]), options.lyricsAlignment,
                            pos.x, pos.y, context, client.font, (int) config.lyricsColor.getNumber());
                }
                if (options.displaySubLyrics) {
                    Vector2i pos = getPos(config.subLyricsPosSupplier, scaledWidth, scaledHeight);
                    RenderUtil.renderText(Component.literal(texts[1]), options.subLyricsAlignment,
                            pos.x, pos.y, context, client.font, (int) config.subLyricsColor.getNumber());
                }

                Component text3 = Component.literal(texts[3]);
                int text3Width = client.font.width(text3);

                if (options.displayMusicDetails) {
                    Vector2i pos = getPos(config.musicDetailsPosSupplier, scaledWidth, scaledHeight);

                    Component text2 = getComponent(texts);
                    MUSIC_DETAIL_SCROLL.setMaxWidth(text3Width);
                    MUSIC_DETAIL_SCROLL.setWidth(client.font.width(text2));
                    MUSIC_DETAIL_SCROLL.tick(options.scrollingTextSpeed);

                    int startX = RenderUtil.getTextRenderX(text3, options.musicDetailsAlignment, client.font, pos.x);
                    context.enableScissor(startX, pos.y, startX + text3Width, pos.y + client.font.lineHeight);
                    context.drawString(
                            client.font, text2, startX + MUSIC_DETAIL_SCROLL.getDx(),
                            pos.y, (int) config.musicDetailsColor.getNumber(),
                            options.textShadow
                    );
                    context.disableScissor();
                }
                if (options.displayTimeProgress) {
                    Vector2i pos = getPos(config.timeProgressPosSupplier, scaledWidth, scaledHeight);
                    RenderUtil.renderText(text3, options.timeProgressAlignment,
                            pos.x, pos.y, context, client.font, (int) config.timeProgressTextColor.getNumber());
                    int blankWidth = client.font.width("                              "); // 兼容不同字体
                    int timeWidth = (text3Width - blankWidth) / 2;
                    if (MusicPlayerHandler.INSTANCE.currentMeta != null && MusicPlayerHandler.INSTANCE.currentMeta.getDuration() != null) {
                        int x;
                        switch (options.timeProgressAlignment) {
                            case LEFT -> x = pos.x + timeWidth + 9;
                            case CENTER -> x = pos.x - blankWidth / 2 + 9;
                            default -> x = pos.x - blankWidth - timeWidth + 9;
                        }
                        context.fill(x, pos.y + 3, x + blankWidth - 20, pos.y + 5,
                                (int) config.timeProgressBgColor.getNumber());
                        context.fill(x, pos.y + 3, (int) (x + (blankWidth - 20) * MusicPlayerHandler.INSTANCE.progressPercentage),
                                pos.y + 5, (int) config.timeProgressColor.getNumber());
                    }
                }

                if (options.displayCoverImg) {
                    Vector2i pos = getPos(config.coverImgPosSupplier, scaledWidth, scaledHeight);
                    int size = config.options.coverImgSize;
                    HEAD_PICTURE.setX(pos.x);
                    HEAD_PICTURE.setY(pos.y);
                    HEAD_PICTURE.setSize(size, size);

                    if (options.coverImgRotate) {
                        float cx = pos.x + size / 2f;
                        float cy = pos.y + size / 2f;
                        float angleRad = delta * (float) Math.PI / 180f;

                        var matrices = context.pose();
                        matrices.pushPose();
                        matrices.translate(cx, cy, 0);
                        matrices.mulPose(new Quaternionf().rotateZ(angleRad));
                        matrices.translate(-cx, -cy, 0);
                    }

                    HEAD_PICTURE.render(context, mouseX, mouseY, delta);

                    if (options.coverImgRotate) {
                        context.pose().popPose();
                    }
                }
            }
        }
    }

    // ==================== Skija 渲染方法 ====================
    @SubscribeEvent
    public void renderSkija(SkijaRenderEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();

        if(mc.isPaused() || mc.screen instanceof FontSettingsScreen) return;
        if(mc.screen instanceof ChatScreen && ClientConfig.INSTANCE.options.hideWhenChat) return;

        boolean hudConfiguring = mc.screen instanceof HUDConfigScreen;
        boolean configuring = mc.screen instanceof ConfigScreen;
        boolean enable = (
                SkijaHUDConfig.status == SkijaHUDConfig.HUDStatus.PLAYING
                        ?
                        MusicPlayer.INSTANCE.isPlaying()
                        :
                        (SkijaHUDConfig.status == SkijaHUDConfig.HUDStatus.ALWAYS && (mc.cameraEntity != null))
        );

        Canvas canvas = event.canvas;
        Font baseFont = event.font;

        if (enable || configuring || hudConfiguring) {

            float screenWidth = window.getGuiScaledWidth();
            float screenHeight = window.getGuiScaledHeight();

            if (hudConfiguring) {
                try (Paint linePaint = new Paint().setColor(0x30FFFFFF).setAntiAlias(false)) {
                    for (int x = 0; x < screenWidth; x += 50) {
                        canvas.drawLine(x, 0, x, screenHeight, linePaint);
                    }
                    for (int y = 0; y < screenHeight; y += 50) {
                        canvas.drawLine(0, y, screenWidth, y, linePaint);
                    }
                }
            }

            float hudX = SkijaHUDConfig.X;
            float hudY = SkijaHUDConfig.Y;
            float hudWidth = SkijaHUDConfig.width;
            float hudHeight = SkijaHUDConfig.height;

            if (configuring) {
                hudX = (screenWidth - hudWidth) / 2.0F;
                hudY = (screenHeight - hudHeight) / 2.0F;
            }

            // 绘制背景和边框
            try (Paint backgroundPaint = new Paint().setColor(SkijaHUDConfig.bgColor).setAntiAlias(true)) {
                Paint outlinePaint = new Paint().setColor(SkijaHUDConfig.outlineColor).setAntiAlias(true)
                        .setMode(PaintMode.STROKE).setStrokeWidth(SkijaHUDConfig.outlineBold);
                RRect rRect = RRect.makeXYWH(hudX, hudY, hudWidth, hudHeight, SkijaHUDConfig.roundRect);
                canvas.drawRRect(rRect, backgroundPaint);
                canvas.drawRRect(rRect, outlinePaint);
            }

            // 裁剪区域
            canvas.save();
            canvas.clipRect(Rect.makeXYWH(hudX, hudY, hudWidth, hudHeight));

            if (MusicPlayer.INSTANCE.isPlaying()) {
                ClientConfig config = ClientConfig.INSTANCE;
                ClientConfig.ClientConfigOptions options = config.options;
                String[] texts = MusicPlayerHandler.INSTANCE.getDisplayTexts();

                int innerX = (int) hudX;
                int innerY = (int) hudY;
                int innerWidth = (int) hudWidth;
                int innerHeight = (int) hudHeight;

                LayoutParams layoutParams = calculateLayoutParams(innerWidth, innerHeight, options);

                Font adaptedFont = new Font(baseFont.getTypeface(), layoutParams.fontSize);

                // 左侧封面
                if (layoutParams.showCover && HEAD_PICTURE.getUrl() != null) {
                    int coverX = innerX + layoutParams.padding;
                    int coverY = innerY + (innerHeight - layoutParams.coverSize) / 2;
                    coverY = Math.max(innerY + layoutParams.padding,
                            Math.min(coverY, innerY + innerHeight - layoutParams.coverSize - layoutParams.padding));

                    renderCoverImage(canvas, adaptedFont, coverX, coverY, layoutParams.coverSize, options);
                }

                int textStartX = innerX + layoutParams.textStartX;
                int textMaxWidth = layoutParams.textMaxWidth;
                int lineHeight = (int) (layoutParams.fontSize + 2);

                // 收集所有需要显示的文本行
                List<TextLine> linesToRender = new ArrayList<>();

                if (options.displayLyrics && texts[0] != null && !texts[0].isEmpty()) {
                    linesToRender.add(new TextLine(Component.literal(texts[0]), options.lyricsAlignment,
                            (int) config.lyricsColor.getNumber()));
                }
                if (options.displaySubLyrics && texts[1] != null && !texts[1].isEmpty()) {
                    linesToRender.add(new TextLine(Component.literal(texts[1]), options.subLyricsAlignment,
                            (int) config.subLyricsColor.getNumber()));
                }
                if (options.displayMusicDetails && layoutParams.showMusicDetails) {
                    linesToRender.add(new TextLine(getComponent(texts), options.musicDetailsAlignment,
                            (int) config.musicDetailsColor.getNumber(), true));
                }
                if (options.displayTimeProgress && layoutParams.showProgress) {
                    linesToRender.add(new TextLine(Component.literal(texts[3]), options.timeProgressAlignment,
                            (int) config.timeProgressTextColor.getNumber()));
                }

                // 计算总高度并居中显示
                int totalTextHeight = linesToRender.size() * lineHeight;
                int startY = innerY + (innerHeight - totalTextHeight) / 2;
                startY = Math.max(innerY + layoutParams.padding,
                        Math.min(startY, innerY + innerHeight - totalTextHeight - layoutParams.padding));

                int currentY = startY;

                // 渲染所有文本行
                for (int i = 0; i < linesToRender.size(); i++) {
                    TextLine line = linesToRender.get(i);

                    if (currentY + lineHeight <= innerY + innerHeight - layoutParams.padding) {

                        if (line.isScrolling) {
                            // 滚动文本处理
                            Component staticText = Component.literal(texts[3]);
                            int staticWidth = getTextWidth(adaptedFont, staticText);

                            MUSIC_DETAIL_SCROLL.setMaxWidth(staticWidth);
                            MUSIC_DETAIL_SCROLL.setWidth(getTextWidth(adaptedFont, line.text));
                            MUSIC_DETAIL_SCROLL.tick(options.scrollingTextSpeed);

                            // 为滚动文本添加 Scissor 效果
                            canvas.save();
                            int clipWidth = Math.min(textMaxWidth, staticWidth);
                            if (clipWidth > 0) {
                                canvas.clipRect(Rect.makeXYWH(textStartX, currentY, clipWidth, lineHeight));
                            }

                            int renderX = textStartX + MUSIC_DETAIL_SCROLL.getDx();
                            drawTextWithScissor(canvas, adaptedFont, line.text, TextAlignment.LEFT,
                                    renderX, currentY, textStartX, textMaxWidth, line.color, options.textShadow);

                            canvas.restore();
                        } else {
                            // 普通文本，带 Scissor 效果
                            drawTextWithScissor(canvas, adaptedFont, line.text, line.alignment,
                                    textStartX, currentY, textStartX, textMaxWidth, line.color, options.textShadow);
                        }

                        currentY += lineHeight;
                    }
                }

                // 进度条（在最下方）
                if (options.displayTimeProgress && layoutParams.showProgress && linesToRender.size() > 0) {
                    int lastTextEndY = startY + linesToRender.size() * lineHeight;
                    int barY = lastTextEndY + 2;

                    if (barY + 2 <= innerY + innerHeight - layoutParams.padding) {
                        int barWidth = layoutParams.progressBarWidth;
                        int barX = textStartX + (textMaxWidth - barWidth) / 2;
                        barX = Math.max(innerX + layoutParams.padding,
                                Math.min(barX, innerX + innerWidth - barWidth - layoutParams.padding));
                        barWidth = Math.min(barWidth, innerX + innerWidth - layoutParams.padding - barX);

                        if (barWidth > 5 && MusicPlayerHandler.INSTANCE.currentMeta != null &&
                                MusicPlayerHandler.INSTANCE.currentMeta.getDuration() != null) {
                            int barHeight = Math.max(1, Math.min(2, innerHeight / 30));

                            try (Paint bgPaint = new Paint().setColor((int) config.timeProgressBgColor.getNumber())) {
                                canvas.drawRect(Rect.makeXYWH(barX, barY, barWidth, barHeight), bgPaint);
                            }

                            try (Paint progressPaint = new Paint().setColor((int) config.timeProgressColor.getNumber())) {
                                float progressWidth = barWidth * MusicPlayerHandler.INSTANCE.progressPercentage;
                                if (progressWidth > 0) {
                                    canvas.drawRect(Rect.makeXYWH(barX, barY, progressWidth, barHeight), progressPaint);
                                }
                            }
                        }
                    }
                }
            }

            canvas.restore();
        }
    }

    // 带 Scissor 效果的文本绘制
    private void drawTextWithScissor(Canvas canvas, Font font, Component text, TextAlignment alignment,
                                     int textX, int y, int clipStartX, int clipWidth, int color, boolean shadow) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return;

        int textWidth = getTextWidth(font, text);
        int renderX;

        switch (alignment) {
            case LEFT -> renderX = textX;
            case CENTER -> renderX = textX + (clipWidth - textWidth) / 2;
            case RIGHT -> renderX = textX + clipWidth - textWidth;
            default -> renderX = textX;
        }

        // 确保文本不超出左边界
        renderX = Math.max(clipStartX, renderX);

        // 保存画布状态并设置裁剪区域
        canvas.save();
        canvas.clipRect(Rect.makeXYWH(clipStartX, y, clipWidth, (int) font.getSize() + 2));

        // 绘制文本
        int renderY = y + (int) font.getSize() - 2;

        if (shadow && (int) font.getSize() > 9) {
            try (Paint shadowPaint = new Paint().setColor(0x55000000).setAntiAlias(true)) {
                canvas.drawString(textStr, renderX + 1, renderY + 1, font, shadowPaint);
            }
        }

        try (Paint textPaint = new Paint().setColor(color).setAntiAlias(true)) {
            canvas.drawString(textStr, renderX, renderY, font, textPaint);
        }

        canvas.restore();
    }

    private LayoutParams calculateLayoutParams(int width, int height, ClientConfig.ClientConfigOptions options) {
        LayoutParams params = new LayoutParams();

        params.padding = Math.max(2, Math.min(4, width / 40));

        int availableLines = 4;
        float idealFontSize = (float) (height - params.padding * 2) / (availableLines + 1.5f);
        params.fontSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, idealFontSize));

        params.coverSize = 0;
        params.showCover = options.displayCoverImg && width >= 60 && height >= 30;
        if (params.showCover) {
            params.coverSize = Math.min(options.coverImgSize, height - params.padding * 2);
            params.coverSize = Math.max(12, Math.min(params.coverSize, width / 3));
            if (params.coverSize < 12) {
                params.showCover = false;
                params.coverSize = 0;
            }
        }

        if (params.showCover) {
            params.textStartX = params.padding + params.coverSize + params.padding;
            params.textMaxWidth = width - params.padding * 3 - params.coverSize;
        } else {
            params.textStartX = params.padding;
            params.textMaxWidth = width - params.padding * 2;
        }
        params.textMaxWidth = Math.max(30, params.textMaxWidth);

        int lineHeight = (int) (params.fontSize + 2);
        int totalLines = 0;
        if (options.displayLyrics) totalLines++;
        if (options.displaySubLyrics) totalLines++;
        if (options.displayMusicDetails) totalLines++;
        if (options.displayTimeProgress) totalLines++;

        int neededHeight = totalLines * lineHeight + params.padding * 2;
        if (options.displayTimeProgress) neededHeight += 4;

        params.showMusicDetails = options.displayMusicDetails && neededHeight <= height;
        params.showProgress = options.displayTimeProgress && neededHeight + 2 <= height;

        params.progressBarWidth = Math.min(params.textMaxWidth - 20, 80);
        params.progressBarWidth = Math.max(30, params.progressBarWidth);

        return params;
    }

    private void renderCoverImage(Canvas canvas, Font font, int x, int y, int size,
                                  ClientConfig.ClientConfigOptions options) {
        Image skijaImage = getSkijaImageFromWidget(HEAD_PICTURE);

        if (skijaImage != null) {
            canvas.save();

            if (options.coverImgRotate && size >= 15) {
                float cx = x + size / 2f;
                float cy = y + size / 2f;
                float angleRad = (float) Math.toRadians((System.currentTimeMillis() / 8.0) % 360);
                canvas.translate(cx, cy);
                canvas.rotate(angleRad);
                canvas.translate(-cx, -cy);
            }

            try (Paint imgPaint = new Paint().setAntiAlias(true)) {
                if (options.coverImgInCircle && size >= 8) {
                    canvas.save();
                    float radius = size / 2f;
                    canvas.clipRRect(RRect.makeXYWH(x, y, size, size, radius));
                    canvas.drawImageRect(skijaImage, Rect.makeXYWH(x, y, size, size), imgPaint);
                    canvas.restore();
                } else {
                    canvas.drawImageRect(skijaImage, Rect.makeXYWH(x, y, size, size), imgPaint);
                }
            }

            canvas.restore();
        } else {
            // 占位符
            try (Paint placeholderPaint = new Paint().setColor(0xFF888888).setAntiAlias(true)) {
                if (options.coverImgInCircle && size >= 8) {
                    float radius = size / 2f;
                    canvas.drawRRect(RRect.makeXYWH(x, y, size, size, radius), placeholderPaint);
                } else {
                    canvas.drawRect(Rect.makeXYWH(x, y, size, size), placeholderPaint);
                }
            }
        }
    }

    private String truncateText(Font font, String text, int maxWidth) {
        if (getTextWidth(font, Component.literal(text)) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int ellipsisWidth = getTextWidth(font, Component.literal(ellipsis));
        int availableWidth = maxWidth - ellipsisWidth;

        for (int i = text.length(); i > 0; i--) {
            String sub = text.substring(0, i);
            if (getTextWidth(font, Component.literal(sub)) <= availableWidth) {
                return sub + ellipsis;
            }
        }
        return ellipsis;
    }

    private int getTextWidth(Font font, Component text) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return 0;
        return (int) font.measureTextWidth(textStr);
    }

    private Image getSkijaImageFromWidget(URLImageWidget widget) {
        if (widget == null || widget.getUrl() == null) return null;

        if (cachedImageUrl != null && cachedImageUrl.equals(widget.getUrl()) &&
                cachedSkijaImage != null && !cachedSkijaImage.isClosed()) {
            return cachedSkijaImage;
        }

        try {
            java.lang.reflect.Field textureField = URLImageWidget.class.getDeclaredField("texture");
            textureField.setAccessible(true);
            DynamicTexture dynamicTexture = (DynamicTexture) textureField.get(widget);

            if (dynamicTexture != null) {
                com.mojang.blaze3d.platform.NativeImage nativeImage = dynamicTexture.getPixels();
                if (nativeImage != null && nativeImage.getWidth() > 0 && nativeImage.getHeight() > 0) {
                    int width = nativeImage.getWidth();
                    int height = nativeImage.getHeight();
                    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int argb = nativeImage.getPixelRGBA(x, y);
                            bufferedImage.setRGB(x, y, argb);
                        }
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", baos);
                    byte[] imageBytes = baos.toByteArray();

                    if (imageBytes != null && imageBytes.length > 0) {
                        if (cachedSkijaImage != null && !cachedSkijaImage.isClosed()) {
                            cachedSkijaImage.close();
                        }
                        cachedSkijaImage = Image.makeFromEncoded(imageBytes);
                        cachedImageUrl = widget.getUrl();
                        return cachedSkijaImage;
                    }
                }
            }
        } catch (Exception e) {
            ConcertoClient.LOGGER.debug("Error converting widget image: {}", e.getMessage());
        }
        return null;
    }

    private static @NotNull Component getComponent(String[] texts) {
        String state = MusicPlayer.INSTANCE.isPlayingTemp ?
                ConcertoClient.clientState == ConcertoClient.ClientState.MUSIC_AGENT ? " | " + Component.translatable("concerto.agent").getString() :
                        (ConcertoClient.clientState == ConcertoClient.ClientState.MUSIC_ROOM ? " | " + Component.translatable("concerto.room").getString() : "")
                : "";

        return Component.literal(texts[2] + state);
    }

    // 文本行数据类
    private static class TextLine {
        Component text;
        TextAlignment alignment;
        int color;
        boolean isScrolling;

        TextLine(Component text, TextAlignment alignment, int color) {
            this(text, alignment, color, false);
        }

        TextLine(Component text, TextAlignment alignment, int color, boolean isScrolling) {
            this.text = text;
            this.alignment = alignment;
            this.color = color;
            this.isScrolling = isScrolling;
        }
    }

    private static class LayoutParams {
        int padding = 2;
        float fontSize = 11f;
        int coverSize = 0;
        boolean showCover = false;
        int textStartX = 0;
        int textMaxWidth = 0;
        boolean showMusicDetails = true;
        boolean showProgress = true;
        int progressBarWidth = 60;
    }

    public static class ScrollingText {
        public static int STOP_TICKS = 180;

        private int width = 0, maxWidth = 0;
        private float dx = 0, stopTicks = 0;
        private boolean stop = false, go_back = false;

        private void reset() {
            this.dx = 0;
            this.go_back = false;
            this.stop = true;
            this.stopTicks = STOP_TICKS;
        }

        public void setWidth(int width) {
            if (width != this.width) this.reset();
            this.width = width;
        }

        public void setMaxWidth(int maxWidth) {
            if (maxWidth > this.maxWidth + 5 || maxWidth < this.maxWidth - 5) this.reset();
            this.maxWidth = maxWidth;
        }

        public void tick(float speed) {
            if (this.width <= this.maxWidth) return;

            float delta = speed * 40f / Minecraft.getInstance().getFps();
            if (this.stop) {
                this.stopTicks -= delta;
                if (this.stopTicks <= 0) {
                    this.stop = false;
                    this.go_back = !this.go_back;
                }
            } else {
                float limit = this.go_back ? 0 : (this.maxWidth - this.width);
                this.dx = this.go_back ? Math.min(limit, this.dx + delta) : Math.max(limit, this.dx - delta);
                if (this.dx == limit) {
                    this.stop = true;
                    this.stopTicks = STOP_TICKS;
                }
            }
        }

        public int getDx() {
            return this.width <= this.maxWidth ? (this.maxWidth - this.width) / 2 : (int) this.dx;
        }
    }

    public static Vector2i getPos(ClientConfig.PositionXYSupplier supplier, int scaledWidth, int scaledHeight) {
        return new Vector2i(supplier.getX(scaledWidth), supplier.getY(scaledHeight));
    }
}