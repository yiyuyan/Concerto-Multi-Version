package top.gregtao.concerto.screen.skija;

import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;

public class SettingsScreen extends ConfigScreen {

    private final LayoutElement[] layoutElements;

    public SettingsScreen(Screen pLastScreen, LayoutElement... elements) {
        super(pLastScreen);
        this.layoutElements = elements;
    }

    @Override
    protected void addFooter() {
        LinearLayout layout1 = LinearLayout.horizontal();

        for (LayoutElement layoutElement : this.layoutElements) {
            layout1.addChild(layoutElement);
        }

        this.layout.addToFooter(layout1);
    }
}
