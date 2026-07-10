package nro.models.event;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import nro.models.utils.Logger;

public final class EventConfig {

    public static final String LUNAR_NEW_YEAR = "lunar_new_year";
    public static final String INTERNATIONAL_WOMENS_DAY = "international_womens_day";
    public static final String CHRISTMAS = "christmas";
    public static final String HALLOWEEN = "halloween";
    public static final String HUNG_VUONG = "hung_vuong";
    public static final String TRUNG_THU = "trung_thu";
    public static final String TOP_UP = "top_up";
    public static final String SUMMER = "summer";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, EventSetting> SETTINGS = new HashMap<>();
    private static boolean loaded;

    private EventConfig() {
    }

    public static void load() {
        SETTINGS.clear();
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("Config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            Logger.error("Không thể load cấu hình event từ Config.properties: " + e.getMessage() + "\n");
        }

        register(properties, LUNAR_NEW_YEAR, false);
        register(properties, INTERNATIONAL_WOMENS_DAY, false);
        register(properties, CHRISTMAS, false);
        register(properties, HALLOWEEN, false);
        register(properties, HUNG_VUONG, true);
        register(properties, TRUNG_THU, false);
        register(properties, TOP_UP, true);
        register(properties, SUMMER, false);
        loaded = true;
    }

    public static boolean isActive(String key) {
        if (!loaded) {
            load();
        }
        EventSetting setting = SETTINGS.get(key);
        return setting != null && setting.isActive(System.currentTimeMillis());
    }

    public static boolean isEnabled(String key) {
        if (!loaded) {
            load();
        }
        EventSetting setting = SETTINGS.get(key);
        return setting != null && setting.enabled;
    }

    private static void register(Properties properties, String key, boolean defaultEnabled) {
        boolean enabled = getBoolean(properties, "event." + key + ".enabled", defaultEnabled);
        long start = getTime(properties, "event." + key + ".start", Long.MIN_VALUE);
        long end = getTime(properties, "event." + key + ".end", Long.MAX_VALUE);
        SETTINGS.put(key, new EventSetting(enabled, start, end));
    }

    private static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null || value.trim().isEmpty() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    private static long getTime(Properties properties, String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value.trim(), TIME_FORMATTER)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } catch (DateTimeParseException e) {
            Logger.error("Sai định dạng " + key + "=" + value + ". Dùng yyyy-MM-dd HH:mm:ss\n");
            return defaultValue;
        }
    }

    private static class EventSetting {

        private final boolean enabled;
        private final long start;
        private final long end;

        private EventSetting(boolean enabled, long start, long end) {
            this.enabled = enabled;
            this.start = start;
            this.end = end;
        }

        private boolean isActive(long now) {
            return enabled && now >= start && now <= end;
        }
    }
}
