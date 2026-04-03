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

    // ==================== 原 render 方法 ====================
    public static void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // ... 保持原有代码不变 ...
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

        if(mc.screen instanceof PauseScreen || mc.screen instanceof FontSettingsScreen) return;
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

            // 获取自定义的 HUD 尺寸
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

                // 根据实际尺寸计算布局参数
                LayoutParams layoutParams = calculateLayoutParams(innerWidth, innerHeight, options);

                // 创建自适应字体
                Font adaptedFont = new Font(baseFont.getTypeface(), layoutParams.fontSize);

                // 渲染封面（左侧）
                if (layoutParams.showCover && HEAD_PICTURE.getUrl() != null) {
                    int coverX = innerX + layoutParams.padding;
                    int coverY = innerY + (innerHeight - layoutParams.coverSize) / 2;
                    coverY = Math.max(innerY + layoutParams.padding,
                            Math.min(coverY, innerY + innerHeight - layoutParams.coverSize - layoutParams.padding));

                    renderCoverImage(canvas, adaptedFont, coverX, coverY, layoutParams.coverSize, options);
                }

                // 渲染文本内容
                int textStartX = innerX + layoutParams.textStartX;
                int textMaxWidth = layoutParams.textMaxWidth;
                int currentY = innerY + layoutParams.textStartY;
                int lineHeight = (int) (layoutParams.fontSize + 2);

                // 歌词
                if (options.displayLyrics && texts[0] != null && !texts[0].isEmpty()) {
                    if (currentY + lineHeight <= innerY + innerHeight - layoutParams.padding) {
                        drawTextSkijaWithinBounds(canvas, adaptedFont, Component.literal(texts[0]),
                                options.lyricsAlignment, textStartX, currentY, textMaxWidth,
                                (int) config.lyricsColor.getNumber(), options.textShadow);
                        currentY += lineHeight;
                    }
                }

                // 子歌词
                if (options.displaySubLyrics && texts[1] != null && !texts[1].isEmpty()) {
                    if (currentY + lineHeight <= innerY + innerHeight - layoutParams.padding) {
                        drawTextSkijaWithinBounds(canvas, adaptedFont, Component.literal(texts[1]),
                                options.subLyricsAlignment, textStartX, currentY, textMaxWidth,
                                (int) config.subLyricsColor.getNumber(), options.textShadow);
                        currentY += lineHeight;
                    }
                }

                // 音乐详情（滚动）
                if (options.displayMusicDetails && layoutParams.showMusicDetails) {
                    if (currentY + lineHeight <= innerY + innerHeight - layoutParams.padding) {
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
                        currentY += lineHeight;
                    }
                }

                // 时间和进度条
                if (options.displayTimeProgress && layoutParams.showProgress) {
                    if (currentY + lineHeight + 4 <= innerY + innerHeight - layoutParams.padding) {
                        Component timeText = Component.literal(texts[3]);
                        // 时间文本可能太长，截断处理
                        String timeStr = timeText.getString();
                        if (getTextWidth(adaptedFont, timeText) > textMaxWidth) {
                            timeStr = truncateText(adaptedFont, timeStr, textMaxWidth - 10);
                            timeText = Component.literal(timeStr);
                        }
                        drawTextSkijaWithinBounds(canvas, adaptedFont, timeText,
                                options.timeProgressAlignment, textStartX, currentY, textMaxWidth,
                                (int) config.timeProgressTextColor.getNumber(), options.textShadow);
                        currentY += lineHeight;

                        // 进度条
                        if (MusicPlayerHandler.INSTANCE.currentMeta != null &&
                                MusicPlayerHandler.INSTANCE.currentMeta.getDuration() != null &&
                                currentY + 2 <= innerY + innerHeight - layoutParams.padding) {

                            int barWidth = layoutParams.progressBarWidth;
                            int barX = textStartX + (textMaxWidth - barWidth) / 2;
                            barX = Math.max(innerX + layoutParams.padding,
                                    Math.min(barX, innerX + innerWidth - barWidth - layoutParams.padding));
                            barWidth = Math.min(barWidth, innerX + innerWidth - layoutParams.padding - barX);

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
            }

            canvas.restore();
        }
    }

    // 布局参数计算
    private LayoutParams calculateLayoutParams(int width, int height, ClientConfig.ClientConfigOptions options) {
        LayoutParams params = new LayoutParams();

        // 内边距
        params.padding = Math.max(2, Math.min(4, width / 40));

        // 字体大小 - 根据高度自适应
        int availableLines = 4; // 最多显示4行
        float idealFontSize = (float) (height - params.padding * 2) / (availableLines + 1.5f);
        params.fontSize = Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, idealFontSize));

        // 封面大小
        params.coverSize = 0;
        params.showCover = options.displayCoverImg && width >= 60 && height >= 30;
        if (params.showCover) {
            params.coverSize = Math.min(options.coverImgSize, height - params.padding * 2);
            params.coverSize = Math.max(12, Math.min(params.coverSize, width / 3));
            // 如果封面太小，就不显示
            if (params.coverSize < 12) {
                params.showCover = false;
                params.coverSize = 0;
            }
        }

        // 文本区域
        if (params.showCover) {
            params.textStartX = params.padding + params.coverSize + params.padding;
            params.textMaxWidth = width - params.padding * 3 - params.coverSize;
        } else {
            params.textStartX = params.padding;
            params.textMaxWidth = width - params.padding * 2;
        }
        params.textMaxWidth = Math.max(30, params.textMaxWidth);

        // 文本起始Y
        params.textStartY = params.padding;

        // 是否显示音乐详情（滚动文本需要更多空间）
        int lineHeight = (int) (params.fontSize + 2);
        int totalLines = 0;
        if (options.displayLyrics) totalLines++;
        if (options.displaySubLyrics) totalLines++;
        if (options.displayMusicDetails) totalLines++;
        if (options.displayTimeProgress) totalLines++;

        int neededHeight = totalLines * lineHeight + params.padding * 2;
        if (options.displayTimeProgress) neededHeight += 4; // 进度条空间

        params.showMusicDetails = options.displayMusicDetails && neededHeight <= height;
        params.showProgress = options.displayTimeProgress && neededHeight + 2 <= height;

        // 进度条宽度
        params.progressBarWidth = Math.min(params.textMaxWidth - 20, 80);
        params.progressBarWidth = Math.max(30, params.progressBarWidth);

        return params;
    }

    // 渲染封面图片
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

    // 截断文本
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

    // 在边界内绘制文本
    private void drawTextSkijaWithinBounds(Canvas canvas, Font font, Component text, TextAlignment alignment,
                                           int startX, int y, int maxWidth, int color, boolean shadow) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return;

        // 如果文本太长，截断
        if (getTextWidth(font, text) > maxWidth) {
            textStr = truncateText(font, textStr, maxWidth);
            text = Component.literal(textStr);
        }

        int textWidth = getTextWidth(font, text);
        int renderX;

        switch (alignment) {
            case LEFT -> renderX = startX;
            case CENTER -> renderX = startX + (maxWidth - textWidth) / 2;
            case RIGHT -> renderX = startX + maxWidth - textWidth;
            default -> renderX = startX;
        }

        renderX = Math.max(startX, renderX);
        drawTextSkija(canvas, font, text, TextAlignment.LEFT, renderX, y, color, shadow);
    }

    // 基础文本绘制
    private void drawTextSkija(Canvas canvas, Font font, Component text, TextAlignment alignment,
                               int x, int y, int color, boolean shadow) {
        String textStr = text.getString();
        if (textStr == null || textStr.isEmpty()) return;

        int renderY = y + (int) font.getSize() - 2;

        if (shadow && (int) font.getSize() > 9) {
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

    // 布局参数内部类
    private static class LayoutParams {
        int padding = 2;
        float fontSize = 11f;
        int coverSize = 0;
        boolean showCover = false;
        int textStartX = 0;
        int textStartY = 0;
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