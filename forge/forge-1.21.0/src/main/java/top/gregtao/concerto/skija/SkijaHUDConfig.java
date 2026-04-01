package top.gregtao.concerto.skija;

import io.github.humbleui.skija.*;

import java.lang.reflect.Field;

public class SkijaHUDConfig {
    public static int X,Y;
    public static int width = 300,height = 120;

    public static int roundRect = 15;

    public static int backgroundColor = Color.makeARGB(165,128,128,128);
    public static int outlineColor = Color.makeARGB(165,255,255,255);

    public static int outlineBold = 3;

    public static Field XF;
    public static Field YF;

    public static Field widthF;
    public static Field heightF;

    public static Field roundRectF;

    public static Field backgroundColorF;
    public static Field outlineColorF;

    public static Field outlineBoldF;

    static {
        try {
            XF = SkijaHUDConfig.class.getField("X");
            YF = SkijaHUDConfig.class.getField("Y");

            widthF = SkijaHUDConfig.class.getField("width");
            heightF = SkijaHUDConfig.class.getField("height");

            roundRectF = SkijaHUDConfig.class.getField("roundRect");
            backgroundColorF  = SkijaHUDConfig.class.getField("backgroundColor");
            outlineColorF = SkijaHUDConfig.class.getField("outlineColor");
            outlineBoldF = SkijaHUDConfig.class.getField("outlineBold");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


}
