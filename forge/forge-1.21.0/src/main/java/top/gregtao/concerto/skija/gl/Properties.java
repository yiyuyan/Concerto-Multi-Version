package top.gregtao.concerto.skija.gl;

import java.util.*;

/**
 * Represents the OpenGL state.
 *
 * This code was inspired by [imgui-java](https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L168-L192)
 * and modified to fit the project's codebase.
 */
class Properties {

    public final int[] lastActiveTexture = new int[1];
    public final int[] lastProgram = new int[1];
    public final int[] lastTexture = new int[1];
    public final int[] lastSampler = new int[1];
    public final int[] lastArrayBuffer = new int[1];
    public final int[] lastVertexArrayObject = new int[1];
    public final int[] lastPolygonMode = new int[2];
    public final int[] lastViewport = new int[4];
    public final int[] lastScissorBox = new int[4];
    public final int[] lastBlendSrcRgb = new int[1];
    public final int[] lastBlendDstRgb = new int[1];
    public final int[] lastBlendSrcAlpha = new int[1];
    public final int[] lastBlendDstAlpha = new int[1];
    public final int[] lastBlendEquationRgb = new int[1];
    public final int[] lastBlendEquationAlpha = new int[1];

    public final int[] lastPixelUnpackBufferBinding = new int[1];
    public final int[] lastUnpackAlignment = new int[1];
    public final int[] lastUnpackRowLength = new int[1];
    public final int[] lastUnpackSkipPixels = new int[1];
    public final int[] lastUnpackSkipRows = new int[1];
    public final int[] lastPackSwapBytes = new int[1];
    public final int[] lastPackLsbFirst = new int[1];
    public final int[] lastPackRowLength = new int[1];
    public final int[] lastPackImageHeight = new int[1];
    public final int[] lastPackSkipPixels = new int[1];
    public final int[] lastPackSkipRows = new int[1];
    public final int[] lastPackSkipImages = new int[1];
    public final int[] lastPackAlignment = new int[1];
    public final int[] lastUnpackSwapBytes = new int[1];
    public final int[] lastUnpackLsbFirst = new int[1];
    public final int[] lastUnpackImageHeight = new int[1];
    public final int[] lastUnpackSkipImages = new int[1];

    private final BitSet flags = new BitSet(7);

    public boolean isLastEnableBlend() {
        return flags.get(0);
    }

    public void setLastEnableBlend(boolean value) {
        flags.set(0, value);
    }

    public boolean isLastEnableCullFace() {
        return flags.get(1);
    }

    public void setLastEnableCullFace(boolean value) {
        flags.set(1, value);
    }

    public boolean isLastEnableDepthTest() {
        return flags.get(2);
    }

    public void setLastEnableDepthTest(boolean value) {
        flags.set(2, value);
    }

    public boolean isLastEnableStencilTest() {
        return flags.get(3);
    }

    public void setLastEnableStencilTest(boolean value) {
        flags.set(3, value);
    }

    public boolean isLastEnableScissorTest() {
        return flags.get(4);
    }

    public void setLastEnableScissorTest(boolean value) {
        flags.set(4, value);
    }

    public boolean isLastEnablePrimitiveRestart() {
        return flags.get(5);
    }

    public void setLastEnablePrimitiveRestart(boolean value) {
        flags.set(5, value);
    }

    public boolean isLastDepthMask() {
        return flags.get(6);
    }

    public void setLastDepthMask(boolean value) {
        flags.set(6, value);
    }
}