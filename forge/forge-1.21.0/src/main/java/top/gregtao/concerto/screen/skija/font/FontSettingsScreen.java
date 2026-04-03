package top.gregtao.concerto.screen.skija.font;

import io.github.humbleui.skija.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.gregtao.concerto.skija.SkijaRenderSystem;

import java.util.function.Consumer;

public class FontSettingsScreen extends OptionsSubScreen {

    private final Consumer<String> callback;

    private FontSelectionList fontSelectionList;

    public FontSettingsScreen(Screen pLastScreen,Consumer<String> callback) {
        super(pLastScreen, Minecraft.getInstance().options,Component.literal(FontSettingsScreen.class.getSimpleName()));
        this.callback = callback;

    }

    @Override
    protected void addContents() {
        this.fontSelectionList = this.layout.addToContents(new FontSelectionList(this.minecraft));
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, p_343150_ -> this.onDone()).width(200).build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        this.fontSelectionList.updateSize(this.width,this.layout);
    }

    void onDone() {
        FontSelectionList.Entry entry = this.fontSelectionList.getSelected();
        if (entry != null && entry.font.getTypeface() != null
                && !entry.font.equals(SkijaRenderSystem.font)) {
            SkijaRenderSystem.font = entry.font;
            this.callback.accept(SkijaRenderSystem.font.getTypeface().getFamilyName());
        }

        Minecraft.getInstance().setScreen(this.lastScreen);
    }

    @Override
    protected void addOptions() {}

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {}

    @OnlyIn(Dist.CLIENT)
    public class FontSelectionList extends ObjectSelectionList<FontSelectionList.Entry>{
        public FontSelectionList(final Minecraft pMinecraft) {
            super(pMinecraft, FontSettingsScreen.this.width, FontSettingsScreen.this.height - 33 - 53, 33, 18);

            FontMgr fontMgr = FontMgr.getDefault();
            for (int i = 0; i < fontMgr.getFamiliesCount(); i++) {
                Typeface typeface = fontMgr.matchFamilyStyle(fontMgr.getFamilyName(i), FontStyle.NORMAL);
                if(typeface!=null){
                    Entry entry = new Entry(typeface);
                    this.addEntry(entry);
                    if(typeface.equals(SkijaRenderSystem.font.getTypeface())) this.setSelected(entry);
                }
            }

            if (this.getSelected() != null) {
                this.centerScrollOn(this.getSelected());
            }
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<FontSelectionList.Entry> {

            private final Font font;

            private int y = Integer.MIN_VALUE;

            private long lastClickTime;

            public Entry(@NotNull Typeface typeface){
                this.font = new Font(typeface,9);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(@NotNull GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {

                if(font.getTypeface() == null) return;

                FontMetrics metrics = font.getMetrics();
                float ascent = metrics.getAscent();

                this.y = (int)(pTop + pHeight / 2F + ascent / 2F) + 9;

                try (Paint paint = new Paint().setColor(-1)){
                    String text = font.getTypeface().getFamilyName();
                    float textWidth = font.measureTextWidth(text,paint);
                    float x = pLeft + pWidth / 2F - textWidth / 2F;
                    SkijaRenderSystem.canvas.drawString(text, x, y, font, paint);
                }
            }

            @Override
            public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
                if (CommonInputs.selected(pKeyCode)) {
                    this.select();
                    FontSettingsScreen.this.onDone();
                    return true;
                } else {
                    return super.keyPressed(pKeyCode, pScanCode, pModifiers);
                }
            }

            /**
             * Called when a mouse button is clicked within the GUI element.
             * <p>
             * @return {@code true} if the event is consumed, {@code false} otherwise.
             * @param pMouseX the X coordinate of the mouse.
             * @param pMouseY the Y coordinate of the mouse.
             * @param pButton the button that was clicked.
             */
            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
                this.select();
                if (Util.getMillis() - this.lastClickTime < 250L) {
                    FontSettingsScreen.this.onDone();
                }

                this.lastClickTime = Util.getMillis();
                return super.mouseClicked(pMouseX, pMouseY, pButton);
            }

            private void select() {
                FontSelectionList.this.setSelected(this);
            }
        }
    }
}
