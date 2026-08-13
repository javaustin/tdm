package com.carrotguy69.tdm.utils;

import static com.carrotguy69.tdm.TDM.plugin;

public class Logger {
    public static void info(String s) {
        plugin.getLogger().info(s);
    }

    public static void log(String s) {
        plugin.getLogger().info(s);
    }

    public static void severe(String s) {
        plugin.getLogger().severe(s);
    }

    public static void warning(String s) {
        plugin.getLogger().warning(s);
    }
}
