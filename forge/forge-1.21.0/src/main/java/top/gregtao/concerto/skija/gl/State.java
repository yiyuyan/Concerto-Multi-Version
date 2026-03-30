/*
 * This file is part of https://github.com/Lyzev/Skija.
 *
 * Copyright (c) 2025. Lyzev
 *
 * Skija is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * (at your option) any later version.
 *
 * Skija is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skija. If not, see <https://www.gnu.org/licenses/>.
 */

package top.gregtao.concerto.skija.gl;

import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL45.*;

/**
 * Represents the OpenGL state.
 */
public class State {

    private final int glVersion;

    /**
     * The properties of the OpenGL state.
     */
    private final Properties props = new Properties();

    public State(int glVersion) {
        this.glVersion = glVersion;
    }

    /**
     * Saves the current OpenGL state.
     *
     * This code was inspired by [imgui-java](https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L398-L425)
     * and modified to fit the project's codebase.
     *
     * @see #pop()
     */
    public State push() {
        Properties p = props;

        glGetIntegerv(GL_ACTIVE_TEXTURE, p.lastActiveTexture);
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(GL_CURRENT_PROGRAM, p.lastProgram);
        glGetIntegerv(GL_TEXTURE_BINDING_2D, p.lastTexture);
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            glGetIntegerv(GL_SAMPLER_BINDING, p.lastSampler);
        }
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, p.lastArrayBuffer);
        glGetIntegerv(GL_VERTEX_ARRAY_BINDING, p.lastVertexArrayObject);
        if (glVersion >= 200) {
            glGetIntegerv(GL_POLYGON_MODE, p.lastPolygonMode);
        }
        glGetIntegerv(GL_VIEWPORT, p.lastViewport);
        glGetIntegerv(GL_SCISSOR_BOX, p.lastScissorBox);
        glGetIntegerv(GL_BLEND_SRC_RGB, p.lastBlendSrcRgb);
        glGetIntegerv(GL_BLEND_DST_RGB, p.lastBlendDstRgb);
        glGetIntegerv(GL_BLEND_SRC_ALPHA, p.lastBlendSrcAlpha);
        glGetIntegerv(GL_BLEND_DST_ALPHA, p.lastBlendDstAlpha);
        glGetIntegerv(GL_BLEND_EQUATION_RGB, p.lastBlendEquationRgb);
        glGetIntegerv(GL_BLEND_EQUATION_ALPHA, p.lastBlendEquationAlpha);
        p.setLastEnableBlend(glIsEnabled(GL_BLEND));
        p.setLastEnableCullFace(glIsEnabled(GL_CULL_FACE));
        p.setLastEnableDepthTest(glIsEnabled(GL_DEPTH_TEST));
        p.setLastEnableStencilTest(glIsEnabled(GL_STENCIL_TEST));
        p.setLastEnableScissorTest(glIsEnabled(GL_SCISSOR_TEST));
        if (glVersion >= 310) {
            p.setLastEnablePrimitiveRestart(glIsEnabled(GL_PRIMITIVE_RESTART));
        }

        // This state is not saved in the original imgui-java project but is included to address bugs encountered when drawing with Skija.
        p.setLastDepthMask(glGetBoolean(GL_DEPTH_WRITEMASK));

        glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, p.lastPixelUnpackBufferBinding);
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

        glGetIntegerv(GL_PACK_SWAP_BYTES, p.lastPackSwapBytes);
        glGetIntegerv(GL_PACK_LSB_FIRST, p.lastPackLsbFirst);
        glGetIntegerv(GL_PACK_ROW_LENGTH, p.lastPackRowLength);
        glGetIntegerv(GL_PACK_SKIP_PIXELS, p.lastPackSkipPixels);
        glGetIntegerv(GL_PACK_SKIP_ROWS, p.lastPackSkipRows);
        glGetIntegerv(GL_PACK_ALIGNMENT, p.lastPackAlignment);

        glGetIntegerv(GL_UNPACK_SWAP_BYTES, p.lastUnpackSwapBytes);
        glGetIntegerv(GL_UNPACK_LSB_FIRST, p.lastUnpackLsbFirst);
        glGetIntegerv(GL_UNPACK_ALIGNMENT, p.lastUnpackAlignment);
        glGetIntegerv(GL_UNPACK_ROW_LENGTH, p.lastUnpackRowLength);
        glGetIntegerv(GL_UNPACK_SKIP_PIXELS, p.lastUnpackSkipPixels);
        glGetIntegerv(GL_UNPACK_SKIP_ROWS, p.lastUnpackSkipRows);

        if (glVersion >= 120) {
            glGetIntegerv(GL_PACK_IMAGE_HEIGHT, p.lastPackImageHeight);
            glGetIntegerv(GL_PACK_SKIP_IMAGES, p.lastPackSkipImages);
            glGetIntegerv(GL_UNPACK_IMAGE_HEIGHT, p.lastUnpackImageHeight);
            glGetIntegerv(GL_UNPACK_SKIP_IMAGES, p.lastUnpackSkipImages);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

        return this;
    }

    /**
     * Restores the state that was saved with [push].
     *
     * This code was inspired by [imgui-java](https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L500-L532)
     * and modified to fit the project's codebase.
     *
     * @see #push()
     */
    public State pop() {
        Properties p = props;

        glUseProgram(p.lastProgram[0]);
        glBindTexture(GL_TEXTURE_2D, p.lastTexture[0]);
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            glBindSampler(0, p.lastSampler[0]);
        }
        glActiveTexture(p.lastActiveTexture[0]);
        glBindVertexArray(p.lastVertexArrayObject[0]);
        glBindBuffer(GL_ARRAY_BUFFER, p.lastArrayBuffer[0]);
        glBlendEquationSeparate(p.lastBlendEquationRgb[0], p.lastBlendEquationAlpha[0]);
        glBlendFuncSeparate(
                p.lastBlendSrcRgb[0],
                p.lastBlendDstRgb[0],
                p.lastBlendSrcAlpha[0],
                p.lastBlendDstAlpha[0]
        );
        if (p.isLastEnableBlend()) glEnable(GL_BLEND);
        else glDisable(GL_BLEND);
        if (p.isLastEnableCullFace()) glEnable(GL_CULL_FACE);
        else glDisable(GL_CULL_FACE);
        if (p.isLastEnableDepthTest()) glEnable(GL_DEPTH_TEST);
        else glDisable(GL_DEPTH_TEST);
        if (p.isLastEnableStencilTest()) glEnable(GL_STENCIL_TEST);
        else glDisable(GL_STENCIL_TEST);
        if (p.isLastEnableScissorTest()) glEnable(GL_SCISSOR_TEST);
        else glDisable(GL_SCISSOR_TEST);
        if (glVersion >= 310) {
            if (p.isLastEnablePrimitiveRestart()) glEnable(GL_PRIMITIVE_RESTART);
            else glDisable(GL_PRIMITIVE_RESTART);
        }
        if (glVersion >= 200) {
            glPolygonMode(GL_FRONT_AND_BACK, p.lastPolygonMode[0]);
        }
        glViewport(p.lastViewport[0], p.lastViewport[1], p.lastViewport[2], p.lastViewport[3]);
        glScissor(
                p.lastScissorBox[0],
                p.lastScissorBox[1],
                p.lastScissorBox[2],
                p.lastScissorBox[3]
        );

        glPixelStorei(GL_PACK_SWAP_BYTES, p.lastPackSwapBytes[0]);
        glPixelStorei(GL_PACK_LSB_FIRST, p.lastPackLsbFirst[0]);
        glPixelStorei(GL_PACK_ROW_LENGTH, p.lastPackRowLength[0]);
        glPixelStorei(GL_PACK_SKIP_PIXELS, p.lastPackSkipPixels[0]);
        glPixelStorei(GL_PACK_SKIP_ROWS, p.lastPackSkipRows[0]);
        glPixelStorei(GL_PACK_ALIGNMENT, p.lastPackAlignment[0]);

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, p.lastPixelUnpackBufferBinding[0]);
        glPixelStorei(GL_UNPACK_SWAP_BYTES, p.lastUnpackSwapBytes[0]);
        glPixelStorei(GL_UNPACK_LSB_FIRST, p.lastUnpackLsbFirst[0]);
        glPixelStorei(GL_UNPACK_ALIGNMENT, p.lastUnpackAlignment[0]);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, p.lastUnpackRowLength[0]);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, p.lastUnpackSkipPixels[0]);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, p.lastUnpackSkipRows[0]);

        if (glVersion >= 120) {
            glPixelStorei(GL_PACK_IMAGE_HEIGHT, p.lastPackImageHeight[0]);
            glPixelStorei(GL_PACK_SKIP_IMAGES, p.lastPackSkipImages[0]);
            glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, p.lastUnpackImageHeight[0]);
            glPixelStorei(GL_UNPACK_SKIP_IMAGES, p.lastUnpackSkipImages[0]);
        }

        // This state is not restored in the original imgui-java project but is included to address bugs encountered when drawing with Skija.
        glDepthMask(p.isLastDepthMask()); // This is a workaround for a bug where the text renderer of Minecraft would not render text properly (flickering text). This also fixes the issue that resizing the window would cause the buttons and more to disappear.

        return this;
    }
}