package com.pdffixnow.desktop;

/**
 * Plain launcher class (does not extend Application) so the shaded jar and the
 * jpackage-built installer can start JavaFX from the classpath.
 */
public final class Launcher {
    private Launcher() {}

    public static void main(String[] args) {
        App.main(args);
    }
}
