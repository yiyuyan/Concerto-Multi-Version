package top.gregtao.concerto.skija;

import com.google.gson.*;
import io.github.humbleui.skija.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.config.ConfigFile;
import top.gregtao.concerto.screen.skija.ConfigScreen;
import top.gregtao.concerto.screen.skija.HUDConfigScreen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

@Mod.EventBusSubscriber
public class SkijaHUDConfig {

    //CONFIG FILE START
    public static final ConfigFile configFile = new ConfigFile("Concerto/client_hud_config.json");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    //CONFIG FILE END

    //CONFIGS START
    public static int X,Y;
    public static int width = 100,height = 40;

    public static int roundRect = 6;

    public static int bgColor = Color.makeARGB(165,47,79,79);
    public static int outlineColor = Color.makeARGB(165,255,255,255);

    public static int outlineBold = 1;

    public static HUDStatus status = HUDStatus.NEVER;
    //CONFIGS END

    public static void init(){
        try {
            JsonObject object = JsonParser.parseString(configFile.read()).getAsJsonObject();
            for (Field field : allConfigurableFields) {
                try {
                    if(object.has(field.getName())) field.set(null,object.get(field.getName()).getAsInt());
                } catch (IllegalAccessException e) {
                    ConcertoClient.LOGGER.error("[{}] Failed to read config: {}",SkijaHUDConfig.class.getSimpleName(),field.getName(),e);
                }
            }
        } catch (Throwable e) {
            ConcertoClient.LOGGER.error("[{}] Failed to read the config file.",SkijaHUDConfig.class.getSimpleName(),e);
        } finally {
            save();
        }
    }

    public static void save(){
        JsonObject object = new JsonObject();
        for (Field field : allConfigurableFields) {
            try {
                object.addProperty(field.getName(),(int)field.get(null));
            } catch (IllegalAccessException e) {
                ConcertoClient.LOGGER.error("[{}] Failed to save config: {}",SkijaHUDConfig.class.getSimpleName(),field.getName(),e);
            }
        }
        configFile.write(GSON.toJson(object));
    }

    @SubscribeEvent
    public static void saveEvent(ScreenEvent.Closing event){
        Screen screen = event.getScreen();
        if(screen instanceof HUDConfigScreen || screen instanceof ConfigScreen) save();
    }

    public enum HUDStatus {
        ALWAYS,
        PLAYING,
        NEVER
    }

    //CONFIG FIELDS START
    public static Field XF;
    public static Field YF;

    public static Field widthF;
    public static Field heightF;

    public static Field roundRectF;

    public static Field backgroundColorF;
    public static Field outlineColorF;

    public static Field outlineBoldF;

    public static ArrayList<Field> allConfigurableFields = new ArrayList<>();

    static {
        try {
            XF = SkijaHUDConfig.class.getField("X");
            YF = SkijaHUDConfig.class.getField("Y");

            widthF = SkijaHUDConfig.class.getField("width");
            heightF = SkijaHUDConfig.class.getField("height");

            roundRectF = SkijaHUDConfig.class.getField("roundRect");
            backgroundColorF  = SkijaHUDConfig.class.getField("bgColor");
            outlineColorF = SkijaHUDConfig.class.getField("outlineColor");
            outlineBoldF = SkijaHUDConfig.class.getField("outlineBold");

            Arrays.stream(SkijaHUDConfig.class.getFields()).filter(f->f.getType().equals(Field.class)).forEach(f->{
                try {
                    allConfigurableFields.add((Field) f.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    //CONFIG FIELDS END
}
