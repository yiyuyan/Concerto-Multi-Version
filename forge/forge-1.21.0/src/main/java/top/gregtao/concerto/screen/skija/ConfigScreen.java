package top.gregtao.concerto.screen.skija;

import io.github.humbleui.skija.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import top.gregtao.concerto.ConcertoClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfigScreen extends OptionsSubScreen {

    private final Field[] fields;
    
    public Map<Field, ArrayList<EditBox>> edits = new HashMap<>();

    public ConfigScreen(Screen pLastScreen, Field... fields) {
        super(pLastScreen, Minecraft.getInstance().options, Component.literal(ConfigScreen.class.getSimpleName()));
        this.fields = fields;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {}

    @Override
    protected void addOptions() {

    }

    @Override
    protected void addFooter() {
        LinearLayout layout1 = LinearLayout.horizontal();
        for (Field field : fields) {
            try {
                ArrayList<EditBox> boxes = new ArrayList<>();
                if(field.getName().toLowerCase().endsWith("color") && field.getType().equals(int.class)){
                    for (int i = 0; i < 4; i++) {
                        EditBox editBox = getEditBox(field);

                        Component message = Component.literal(field.getName()+getColorTypeName(i));
                        int value = (int) field.get(null);

                        editBox.setMessage(message);
                        editBox.setHint(message);
                        editBox.setValue(getColorTypeValue(value,i));

                        editBox.setResponder((s)->{
                            try {
                                ArrayList<EditBox> boxArrayList = edits.get(field);
                                int a = Integer.getInteger(boxArrayList.get(0).getValue());
                                int r = Integer.getInteger(boxArrayList.get(1).getValue());
                                int g = Integer.getInteger(boxArrayList.get(2).getValue());
                                int b = Integer.getInteger(boxArrayList.get(3).getValue());

                                field.set(null,Color.makeARGB(a,r,g,b));

                            } catch (Throwable ignored) {}
                        });
                        boxes.add(editBox);
                    }

                }
                else{
                    EditBox editBox = getEditBox(field);
                    boxes.add(editBox);
                }
                edits.put(field,boxes);
            } catch (Throwable e) {
                ConcertoClient.LOGGER.error("Failed to start the skija config screen.",e);
                onClose();
            }
        }
        edits.values().forEach((w)->{
            for (EditBox editBox : w) {
                layout1.addChild(editBox);
            }
        });

        this.layout.addToFooter(layout1);
    }

    private @NotNull String getColorTypeName(int i){
        return switch (i){
            case 0 -> "Alpha";
            case 1 -> "Red";
            case 2 -> "Green";
            case 3 -> "Blue";
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    private @NotNull String getColorTypeValue(int color,int i){
        return String.valueOf(switch (i){
            case 0 -> Color.getA(color);
            case 1 -> Color.getR(color);
            case 2 -> Color.getG(color);
            case 3 -> Color.getB(color);
            default -> throw new IllegalStateException("Unexpected value: " + i);
        });
    }

    private @NotNull EditBox getEditBox(Field field) throws IllegalAccessException {
        EditBox editBox = new EditBox(font,50,20,Component.literal(field.getName()));
        editBox.setCanLoseFocus(true);
        editBox.setValue(String.valueOf(field.get(null)));
        editBox.setHint(Component.literal(field.getName()));
        editBox.setResponder((s)->{
            try {
                field.set(null,Integer.parseInt(s));
            } catch (Throwable ignored) {}
        });
        return editBox;
    }
}
