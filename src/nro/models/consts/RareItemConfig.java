package nro.models.consts;

import nro.models.utils.Logger;
import java.io.FileInputStream;
import java.util.Properties;

public class RareItemConfig {
    private static RareItemConfig instance;

    public boolean enabled;
    public int dropRateDefault;
    public int dropRateSpecial;
    public int[] normalMaps;
    public int[] specialMaps;

    public int rateRed;
    public int rateYellow;
    public int rateBlue;

    public int minPercent;
    public int maxPercent;
    public int minAbsolute;
    public int maxAbsolute;

    public static RareItemConfig gI() {
        if (instance == null) {
            instance = new RareItemConfig();
            instance.loadConfig();
        }
        return instance;
    }

    public void loadConfig() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream("Config.properties"));

            this.enabled = Boolean.parseBoolean(properties.getProperty("rareitem.enabled", "false"));
            this.dropRateDefault = Integer.parseInt(properties.getProperty("rareitem.drop_rate_default", "50").trim());
            this.dropRateSpecial = Integer.parseInt(properties.getProperty("rareitem.drop_rate_special", "150").trim());

            String[] normalMapsStr = properties.getProperty("rareitem.normal_maps", "141,142,143,144").split(",");
            this.normalMaps = new int[normalMapsStr.length];
            for (int i = 0; i < normalMapsStr.length; i++) {
                if (!normalMapsStr[i].trim().isEmpty()) {
                    this.normalMaps[i] = Integer.parseInt(normalMapsStr[i].trim());
                }
            }

            String[] maps = properties.getProperty("rareitem.special_maps", "").split(",");
            this.specialMaps = new int[maps.length];
            for (int i = 0; i < maps.length; i++) {
                if (!maps[i].trim().isEmpty()) {
                    this.specialMaps[i] = Integer.parseInt(maps[i].trim());
                }
            }

            this.rateRed = Integer.parseInt(properties.getProperty("rareitem.rate_red", "70").trim());
            this.rateYellow = Integer.parseInt(properties.getProperty("rareitem.rate_yellow", "25").trim());
            this.rateBlue = Integer.parseInt(properties.getProperty("rareitem.rate_blue", "5").trim());

            this.minPercent = Integer.parseInt(properties.getProperty("rareitem.min_percent", "5").trim());
            this.maxPercent = Integer.parseInt(properties.getProperty("rareitem.max_percent", "20").trim());
            this.minAbsolute = Integer.parseInt(properties.getProperty("rareitem.min_absolute", "500").trim());
            this.maxAbsolute = Integer.parseInt(properties.getProperty("rareitem.max_absolute", "3000").trim());

            Logger.success("Loaded RareItemConfig successfully!\n");
        } catch (Exception e) {
            Logger.logException(RareItemConfig.class, e, "Error loading RareItemConfig");
        }
    }

    public boolean isMapValid(int mapId) {
        if (specialMaps != null) {
            for (int id : specialMaps) {
                if (id == mapId) return true;
            }
        }
        if (normalMaps != null) {
            for (int id : normalMaps) {
                if (id == mapId) return true;
            }
        }
        return false;
    }
}
