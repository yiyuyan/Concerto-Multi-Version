package top.gregtao.concerto.screen.skija;

import io.github.humbleui.skija.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.gregtao.concerto.ConcertoClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ConfigScreen extends OptionsSubScreen {

    private final Field[] fields;
    
    public Map<Field, ArrayList<LayoutElement>> edits = new HashMap<>();

    public ConfigScreen(Screen pLastScreen, Field... fields) {
        super(pLastScreen, Minecraft.getInstance().options, Component.literal(ConfigScreen.class.getSimpleName()));
        this.fields = fields;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void addOptions() {}

    @Override
    protected void addFooter() {
        LinearLayout layout1 = LinearLayout.horizontal();
        for (Field field : fields) {
            try {
                ArrayList<LayoutElement> boxes = new ArrayList<>();
                if(field.getName().toLowerCase().endsWith("color") && field.getType().equals(int.class)){
                    for (int i = 0; i < 4; i++) {
                        EditBox editBox = getEditBox(field);

                        Component message = Component.literal(getColorTypeName(i));
                        int value = (int) field.get(null);

                        editBox.setResponder((s)->{});

                        editBox.setMessage(message);
                        editBox.setHint(message);
                        editBox.setValue(getColorTypeValue(value,i));

                        editBox.setResponder((s)->{
                            try {
                                ArrayList<LayoutElement> boxArrayList = edits.get(field);

                                EditBox ab = ((EditBox)boxArrayList.get(0));
                                EditBox rb = ((EditBox)boxArrayList.get(1));
                                EditBox gb = ((EditBox)boxArrayList.get(2));
                                EditBox bb = ((EditBox)boxArrayList.get(3));

                                int a = Integer.parseInt(ab.getValue());
                                int r = Integer.parseInt(rb.getValue());
                                int g = Integer.parseInt(gb.getValue());
                                int b = Integer.parseInt(bb.getValue());

                                a = Math.max(0, Math.min(255, a));
                                r = Math.max(0, Math.min(255, r));
                                g = Math.max(0, Math.min(255, g));
                                b = Math.max(0, Math.min(255, b));

                                field.set(null,Color.makeARGB(a,r,g,b));

                                setEditBoxValueDirect(ab,a);
                                setEditBoxValueDirect(rb,r);
                                setEditBoxValueDirect(gb,g);
                                setEditBoxValueDirect(bb,b);

                            } catch (Throwable ignored) {}
                        });
                        boxes.add(editBox);
                    }

                }
                else if(field.getType().equals(int.class)){
                    boxes.add(getEditBoxWithName(field));
                }
                else if(field.getType().equals(float.class)){
                    EditBox editBox = getFloatBox(field);
                    boxes.add(getEditBoxWithName(field,field.getName(),editBox));
                }
                edits.put(field,boxes);
            } catch (Throwable e) {
                ConcertoClient.LOGGER.error("Failed to start the skija config screen.",e);
                onClose();
            }
        }
        edits.values().forEach((w)->{
            for (LayoutElement editBox : w) {
                layout1.addChild(editBox);
            }
        });

        this.layout.addToFooter(layout1);
    }

    private @NotNull EditBox getFloatBox(Field field) throws IllegalAccessException {
        EditBox editBox = new EditBox(font,50,20,Component.literal(field.getName()));
        editBox.setCanLoseFocus(true);
        editBox.setValue(String.valueOf(field.get(null)));
        editBox.setFilter((s)->{
            try {
                return Float.parseFloat(s) > 0F;
            } catch (Throwable e) {
                return false;
            }
        });
        editBox.setResponder((s)->{
            try {
                field.set(null,Float.parseFloat(s));
            } catch (IllegalAccessException ignored) {}
        });
        return editBox;
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

    private @NotNull LinearLayout getEditBoxWithName(Field field) throws IllegalAccessException {
        return getEditBoxWithName(field,field.getName(),null);
    }

    private @NotNull LinearLayout getEditBoxWithName(Field field, String name,@Nullable EditBox editBox) throws IllegalAccessException {
        String str = name + ": ";
        LinearLayout linearLayout = LinearLayout.horizontal();

        StringWidget label = new StringWidget(Component.literal(str), font);
        label.setHeight(20);
        label.setY((20 - font.lineHeight) / 2);

        linearLayout.addChild(label);
        if(editBox==null){
            linearLayout.addChild(getEditBox(field));
        }
        else{
            linearLayout.addChild(editBox);
        }
        return linearLayout;
    }

    private @NotNull EditBox getEditBox(Field field) throws IllegalAccessException {
        EditBox editBox = new EditBox(font,50,20,Component.literal(field.getName()));
        editBox.setCanLoseFocus(true);
        editBox.setValue(String.valueOf(field.get(null)));
        editBox.setHint(Component.literal(field.getName()));
        editBox.setFilter((s)->{
            try {
                return Integer.parseInt(s)>0;
            } catch (Throwable e) {
                return false;
            }
        });
        editBox.setResponder((s)->{
            try {
                field.set(null,Integer.parseInt(s));
            } catch (Throwable ignored) {}
        });
        return editBox;
    }

    public void setEditBoxValueDirect(EditBox editBox,Object o) throws IllegalAccessException {
        Field field = null;
        for (Field declaredField : editBox.getClass().getDeclaredFields()) {
            if(declaredField.getType().equals(Consumer.class)){
                field = declaredField;
                break;
            }
        }
        if(field==null) throw new RuntimeException("Cannot find EditBox:responder");
        field.setAccessible(true);
        final Consumer<String> originalResponder = (Consumer<String>) field.get(editBox);
        editBox.setResponder((s)->{});
        editBox.setValue(String.valueOf(o));
        editBox.setResponder(originalResponder);
    }
}
