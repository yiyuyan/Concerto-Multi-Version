package top.gregtao.concerto.util;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Font;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import top.gregtao.concerto.ConcertoClient;
import top.gregtao.concerto.core.config.ClientConfig;
import top.gregtao.concerto.core.player.MusicPlayer;
import top.gregtao.concerto.screen.ConcertoIndexScreen;
import top.gregtao.concerto.screen.GeneralPlaylistScreen;
import top.gregtao.concerto.screen.skija.ConfigScreen;
import top.gregtao.concerto.screen.skija.HUDConfigScreen;
import top.gregtao.concerto.screen.skija.font.FontSettingsScreen;
import top.gregtao.concerto.skija.SkijaHUDConfig;

public class ConcertoHotkeys {

    public static String CATEGORY = "concerto.hotkey";

    public static KeyMapping GENERAL_PLAYLIST, INDEX_SCREEN, NEXT_MUSIC, PAUSE_RESUME;

    public static KeyMapping HUD_SCREEN,ENLARGING_HUD,REDUCE_HUD;

    @SuppressWarnings("removal")
    @Mod.EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class KeyMappingRegistry {
        @SubscribeEvent
        public static void registerMapping(RegisterKeyMappingsEvent event) {
            GENERAL_PLAYLIST = new KeyMapping(
                    "concerto.hotkey.general_music_list",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_U,
                    CATEGORY
            );
            INDEX_SCREEN = new KeyMapping(
                    "concerto.hotkey.index",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_I,
                    CATEGORY
            );
            NEXT_MUSIC = new KeyMapping(
                    "concerto.screen.next",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_N,
                    CATEGORY
            );
            PAUSE_RESUME = new KeyMapping(
                    "concerto.screen.pause_resume",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_P,
                    CATEGORY
            );

            //SKIJA START
            HUD_SCREEN = new KeyMapping("concerto.hud.open",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_O,
                    CATEGORY);

            ENLARGING_HUD = new KeyMapping("concerto.hud.enlarge",
                    KeyConflictContext.UNIVERSAL,
                    KeyModifier.CONTROL,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_PAGE_UP,
                    CATEGORY);

            REDUCE_HUD = new KeyMapping("concerto.hud.reduce",
                    KeyConflictContext.UNIVERSAL,
                    KeyModifier.CONTROL,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_PAGE_DOWN,
                    CATEGORY);

            //STIJA END

            event.register(GENERAL_PLAYLIST);
            event.register(INDEX_SCREEN);
            event.register(NEXT_MUSIC);
            event.register(PAUSE_RESUME);

            event.register(HUD_SCREEN);
            event.register(ENLARGING_HUD);
            event.register(REDUCE_HUD);
        }
    }

    @Mod.EventBusSubscriber(modid = ConcertoClient.MOD_ID, value = Dist.CLIENT)
    public static class KeyEventHandler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (GENERAL_PLAYLIST.consumeClick()) {
                client.setScreen(new GeneralPlaylistScreen(null));
            } else if (INDEX_SCREEN.consumeClick()) {
                client.setScreen(new ConcertoIndexScreen(null));
            } else if (NEXT_MUSIC.consumeClick()) {
                if (!MusicPlayer.INSTANCE.started) MusicPlayer.INSTANCE.start();
                else if (!MusicPlayer.INSTANCE.playNextLock.get()) MusicPlayer.INSTANCE.playNext(1);
            } else if (PAUSE_RESUME.consumeClick()) {
                if (MusicPlayer.INSTANCE.started) {
                    if (MusicPlayer.INSTANCE.forcePaused) MusicPlayer.INSTANCE.forceResume();
                    else MusicPlayer.INSTANCE.forcePause();
                }
            }
            else if(HUD_SCREEN.consumeClick()){
                client.setScreen(new HUDConfigScreen(null));
            }
            else if(ENLARGING_HUD.consumeClick()){
                if(checkHUDDisplaying()){
                    SkijaHUDConfig.width++;
                    SkijaHUDConfig.height++;
                    SkijaHUDConfig.save();
                }
            }
            else if(REDUCE_HUD.consumeClick()){
                if(checkHUDDisplaying()){
                    int w = Math.max(SkijaHUDConfig.width-1,1);
                    int h = Math.max(SkijaHUDConfig.height-1,1);
                    SkijaHUDConfig.width = w;
                    SkijaHUDConfig.height = h;
                    SkijaHUDConfig.save();
                }
            }
        }

        private static boolean checkHUDDisplaying(){
            Minecraft mc = Minecraft.getInstance();
            if(mc.screen instanceof PauseScreen || mc.screen instanceof FontSettingsScreen) return false;
            if(mc.screen instanceof ChatScreen && ClientConfig.INSTANCE.options.hideWhenChat) return false;

            boolean hudConfiguring = mc.screen instanceof HUDConfigScreen;
            boolean configuring = mc.screen instanceof ConfigScreen;
            boolean enable = (
                    SkijaHUDConfig.status == SkijaHUDConfig.HUDStatus.PLAYING
                            ?
                            MusicPlayer.INSTANCE.isPlaying()
                            :
                            (SkijaHUDConfig.status == SkijaHUDConfig.HUDStatus.ALWAYS && (mc.cameraEntity != null))
            );

            return enable || configuring || hudConfiguring;
        }
    }
    }