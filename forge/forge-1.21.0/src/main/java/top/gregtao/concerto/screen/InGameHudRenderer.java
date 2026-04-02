package top.gregtao.concerto.screen;

import com.mojang.blaze3d.platform.Window;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
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
import top.gregtao.concerto.screen.widget.URLImageWidget;
import top.gregtao.concerto.skija.SkijaHUDConfig;
import top.gregtao.concerto.skija.SkijaRenderEvent;
import top.gregtao.concerto.util.RenderUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class InGameHudRenderer {

    public static ScrollingText MUSIC_DETAIL_SCROLL = new ScrollingText();

    public static URLImageWidget HEAD_PICTURE = new URLImageWidget(20, 20, 0, 0, null, false);

    // 缓存 Skija Image 对象
    private static Image cachedSkijaImage = null;
    private static String cachedImageUrl = null;

    // 自适应字体大小
    private static float currentFontSize = 13f;
    private static float minFontSize = 8f;
    private static float maxFontSize = 16f;

    public static void init() {
        MusicPlayerHandler.headPictureSetter = (url) -> {
            HEAD_PICTURE.setUrl(url);
            HEAD_PICTURE.loadImage(true, ClientConfig.INSTANCE.options.coverImgInCircle);
            // 清除图片缓存
            if (cachedSkijaImage != null && !cachedSkijaImage.isClosed()) {
                cachedSkijaImage.close();
            }
            cachedSkijaImage = null;
            cachedImageUrl = null;
        };
        MinecraftForge.EVENT_BUS.register(new InGameHudRenderer());
    }

    // ==================== 原 render 方法 ====================
    public static void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
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

            // 绘制辅助网格（仅在配置模式）
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

            // 计算 HUD 矩形位置
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

            // 保存画布状态，设置裁剪区域
            canvas.save();
            canvas.clipRect(Rect.makeXYWH(hudX, hudY, hudWidth, hudHeight));

            // 如果音乐正在播放，在矩形内部渲染音乐信息
            if (MusicPlayer.INSTANCE.isPlaying()) {
                ClientConfig config = ClientConfig.INSTANCE;
                ClientConfig.ClientConfigOptions options = config.options;

                String[] texts = MusicPlayerHandler.INSTANCE.getDisplayTexts();

                // 计算矩形内部的可用区域
                int innerX = (int) hudX;
                int innerY = (int) hudY;
                int innerWidth = (int) hudWidth;
                int innerHeight = (int) hudHeight;

                // 自适应字体大小
                float adaptedFontSize = calculateAdaptiveFontSize(innerWidth, innerHeight, options.displayCoverImg);
                Font adaptedFont = new Font(baseFont.getTypeface(), adaptedFontSize);

                // 内边距
                int padding = Math.max(2, Math.min(5, innerWidth / 30));

                // 封面图片大小
                int coverSize = 0;
                if (options.displayCoverImg) {
                    coverSize = Math.min(options.coverImgSize, innerHeight - padding * 2);
                    coverSize = Math.max(8, Math.min(coverSize, Math.min(innerWidth / 3, 40)));
                    if (innerWidth < 80 || innerHeight < 40) {
                        coverSize = 0;
                    }
                }

                int textStartX = innerX + padding;
                int textMaxWidth = innerWidth - padding * 2;

                if (coverSize > 0) {
                    textStartX = innerX + padding + coverSize + padding;
                    textMaxWidth = innerWidth - padding * 3 - coverSize;
                    textMaxWidth = Math.max(30, textMaxWidth);
                }

                int lineHeight = getFontHeight(adaptedFont);

                // 渲染封面图片
                if (coverSize > 0) {
                    int coverX = innerX + padding;
                    int coverY = innerY + (innerHeight - coverSize) / 2;
                    coverY = Math.max(innerY + padding, Math.min(coverY, innerY + innerHeight - coverSize - padding));

                    Image skijaImage = getSkijaImageFromWidget(HEAD_PICTURE);

                    if (skijaImage != null) {
                        canvas.save();

                        if (options.coverImgRotate && coverSize >= 15) {
                            float cx = coverX + coverSize / 2f;
                            float cy = coverY + coverSize / 2f;
                            float angleRad = (float) Math.toRadians((System.currentTimeMillis() / 10.0) % 360);
                            canvas.translate(cx, cy);
                            canvas.rotate(angleRad);
                            canvas.translate(-cx, -cy);
                        }

                        try (Paint imgPaint = new Paint().setAntiAlias(true)) {
                            if (options.coverImgInCircle && coverSize >= 10) {
                                canvas.save();
                                float radius = coverSize / 2f;
                                canvas.clipRRect(RRect.makeXYWH(coverX, coverY, coverSize, coverSize, radius));
                                canvas.drawImageRect(skijaImage, Rect.makeXYWH(coverX, coverY, coverSize, coverSize), imgPaint);
                                canvas.restore();
                            } else {
                                canvas.drawImageRect(skijaImage, Rect.makeXYWH(coverX, coverY, coverSize, coverSize), imgPaint);
                            }
                        }

                        canvas.restore();
                    }
                }

                // 渲染文本 - 从顶部开始
                int currentY = innerY + padding;

                // 歌词
                if (options.displayLyrics && texts[0] != null && !texts[0].isEmpty()) {
                    if (currentY + lineHeight <= innerY + innerHeight - padding) {
                        Component lyricText = Component.literal(texts[0]);
                        drawTextSkijaWithinBounds(canvas, adaptedFont, lyricText, options.lyricsAlignment,
                                textStartX, currentY, textMaxWidth, (int) config.lyricsColor.getNumber(), options.textShadow);
                        currentY += lineHeight + 2;
                    }
                }

                // 子歌词
                if (options.displaySubLyrics && texts[1] != null && !texts[1].isEmpty()) {
                    if (currentY + lineHeight <= innerY + innerHeight - padding) {
                        Component subLyricText = Component.literal(texts[1]);
                        drawTextSkijaWithinBounds(canvas, adaptedFont, subLyricText, options.subLyricsAlignment,
                                textStartX, currentY, textMaxWidth, (int) config.subLyricsColor.getNumber(), options.textShadow);
                        currentY += lineHeight + 2;
                    }
                }

                // 音乐详情（滚动文本）
                if (options.displayMusicDetails) {
                    if (currentY + lineHeight <= innerY + innerHeight - padding) {
                        Component detailText = getComponent(texts);
                        Component staticText = Component.literal(texts[3]);
                        int staticWidth = getTextWidth(adaptedFont, staticText);

                        MUSIC_DETAIL_SCROLL.setMaxWidth(staticWidth);
                        MUSIC_DETAIL_SCROLL.setWidth(getTextWidth(adaptedFont, detailText));
                        MUSIC_DETAIL_SCROLL.tick(options.scrollingTextSpeed);

                        canvas.save();
                        int clipWidth = Math.min(textMaxWidth, staticWidth);
                        if (clipWidth > 0) {
                            canvas.clipRect(Rect.makeXYWH(textStartX, currentY, clipWidth, lineHeight));
                        }

                        int renderX = textStartX + MUSIC_DETAIL_SCROLL.getDx();
                        drawTextSkija(canvas, adaptedFont, detailText, TextAlignment.LEFT,
                                renderX, currentY, (int) config.musicDetailsColor.getNumber(), options.textShadow);

                        canvas.restore();
                        currentY += lineHeight + 2;
                    }
                }

                // 时间文本和进度条
                if (options.displayTimeProgress && currentY + lineHeight + 4 <= innerY + innerHeight - padding) {
                    Component timeText = Component.literal(texts[3]);
                    drawTextSkijaWithinBounds(canvas, adaptedFont, timeText, options.timeProgressAlignment,
                            textStartX, currentY, textMaxWidth, (int) config.timeProgressTextColor.getNumber(), options.textShadow);
                    currentY += lineHeight + 2;

                    // 进度条
                    if (MusicPlayerHandler.INSTANCE.currentMeta != null &&
                            MusicPlayerHandler.INSTANCE.currentMeta.getDuration() != null &&
                            currentY + 2 <= innerY + innerHeight - padding) {

                        int barWidth = Math.min(textMaxWidth - 10, 100);
                        int barX = textStartX + (textMaxWidth - barWidth) / 2;
                        barX = Math.max(innerX + padding, Math.min(barX, innerX + innerWidth - barWidth - padding));
                        barWidth = Math.min(barWidth, innerX + innerWidth - padding - barX);

                        if (barWidth > 5) {
                            int barHeight = Math.max(1, Math.min(2, innerHeight / 30));

                            try (Paint bgPaint = new Paint().setColor((int) config.timeProgressBgColor.getNumber())) {
                                canvas.drawRect(Rect.makeXYWH(barX, currentY, barWidth, barHeight), bgPaint);
                            }

                            try (Paint progressPaint = new Paint().setColor((int) config.timeProgressColor.getNumber())) {
                                float progressWidth = barWidth * MusicPlayerHandler.INSTANCE.progressPercentage;
                                if (progressWidth > 0) {
                                    canvas.drawRect(Rect.makeXYWH(barX, currentY, progressWidth, barHeight), progressPaint);
                                }
                            }
                        }
                    }
                }
            }

            canvas.restore();
        }
    }

    // 计算自适应字体大小
    private float calculateAdaptiveFontSize(int width, int height, boolean hasCover) {
        float baseSize = 13f;

        if (height < 50) {
            baseSize = Math.max(minFontSize, height * 0.25f);
        } else if (height < 70) {
            baseSize = Math.max(minFontSize, height * 0.2f);
        } else {
            baseSize = Math.min(maxFontSize, height * 0.18f);
        }

        if (width < 120) {
            baseSize = Math.max(minFontSize, baseSize * 0.8f);
        } else if (width < 180) {
            baseSize = Math.max(minFontSize, baseSize * 0.9f);
        }

        if (hasCover && width < 150) {
            baseSize = Math.max(minFontSize, baseSize * 0.85f);
        }

        return Math.max(minFontSize, Math.min(maxFontSize, baseSize));
    }

    // 在边界内绘制文本
    private void drawTextSkijaWithinBounds(Canvas canvas, Font font, Component text, TextAlignment alignment,
                                           int startX, int y, int maxWidth, int color, boolean shadow) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return;

        int textWidth = getTextWidth(font, text);
        int renderX;

        switch (alignment) {
            case LEFT -> renderX = startX;
            case CENTER -> renderX = startX + (maxWidth - textWidth) / 2;
            case RIGHT -> renderX = startX + maxWidth - textWidth;
            default -> renderX = startX;
        }

        renderX = Math.max(startX, renderX);

        int textEndX = renderX + textWidth;
        int clipEndX = startX + maxWidth;

        if (textEndX > clipEndX) {
            canvas.save();
            canvas.clipRect(Rect.makeXYWH(startX, y, maxWidth, getFontHeight(font)));
            drawTextSkija(canvas, font, text, TextAlignment.LEFT, renderX, y, color, shadow);
            canvas.restore();
        } else {
            drawTextSkija(canvas, font, text, TextAlignment.LEFT, renderX, y, color, shadow);
        }
    }

    // 基础文本绘制
    private void drawTextSkija(Canvas canvas, Font font, Component text, TextAlignment alignment,
                               int x, int y, int color, boolean shadow) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return;

        int renderY = y + getFontHeight(font) - 3;

        if (shadow && getFontHeight(font) > 10) {
            try (Paint shadowPaint = new Paint().setColor(0x55000000).setAntiAlias(true)) {
                canvas.drawString(textStr, x + 1, renderY + 1, font, shadowPaint);
            }
        }

        try (Paint textPaint = new Paint().setColor(color).setAntiAlias(true)) {
            canvas.drawString(textStr, x, renderY, font, textPaint);
        }
    }

    // 获取文本宽度
    private int getTextWidth(Font font, Component text) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return 0;
        return (int) font.measureTextWidth(textStr);
    }

    // 获取字体高度
    private int getFontHeight(Font font) {
        return (int) Math.ceil(font.getSize());
    }

    // 转换图片
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