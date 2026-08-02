package com.automation.hybrid.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralized configuration reader. Precedence: -D system property > config.properties > default.
 */
public final class ConfigManager {

    private static final Properties PROPERTIES = new Properties();
    private static volatile boolean loaded = false;

    private ConfigManager() {
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        synchronized (ConfigManager.class) {
            if (loaded) {
                return;
            }
            try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in != null) {
                    PROPERTIES.load(in);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load config.properties", e);
            }
            loaded = true;
        }
    }

    public static String get(String key, String defaultValue) {
        ensureLoaded();
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, null);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, null);
        return value == null ? defaultValue : Integer.parseInt(value);
    }
}
