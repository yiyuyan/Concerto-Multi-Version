package top.gregtao.concerto.skija.gl;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;

/**
 * Stores and restores OpenGL states.
 */
public final class States {

    /**
     * The current OpenGL version.
     */
    private static final int glVersion;

    /**
     * The stack of OpenGL states.
     */
    public static final Stack<State> states = new Stack<>();

    static {
        int[] major = new int[1];
        int[] minor = new int[1];
        glGetIntegerv(GL_MAJOR_VERSION, major);
        glGetIntegerv(GL_MINOR_VERSION, minor);
        glVersion = major[0] * 100 + minor[0] * 10;
    }

    private States() {
        // Private constructor to prevent instantiation
    }

    /**
     * Pushes the current OpenGL state onto the stack.
     */
    public static void push() {
        states.push(new State(glVersion).push());
    }

    /**
     * Pops the last OpenGL state from the stack and restores it.
     */
    public static void pop() {
        if (states.isEmpty()) {
            throw new IllegalStateException("No state to restore.");
        }
        states.pop().pop();
    }
}