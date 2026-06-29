package nro.models.clan;

public class ClanIntrinsicTemplate {

    public static final byte EFFECT_HP = 1;
    public static final byte EFFECT_MP = 2;
    public static final byte EFFECT_DAME = 3;
    public static final byte EFFECT_DEF = 4;
    public static final byte EFFECT_CRIT = 5;

    public final byte id;
    public final String name;
    public final String description;
    public final short icon;
    public final byte maxLevel;
    public final byte effectType;
    public final short valuePerLevel;
    public final int upgradeCostBase;

    public ClanIntrinsicTemplate(byte id, String name, String description, short icon, byte maxLevel,
            byte effectType, short valuePerLevel, int upgradeCostBase) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.effectType = effectType;
        this.valuePerLevel = valuePerLevel;
        this.upgradeCostBase = upgradeCostBase;
    }

    public int getValue(byte level) {
        return Math.max(0, level) * valuePerLevel;
    }

    public int getNextValue(byte level) {
        return getValue((byte) Math.min(maxLevel, level + 1));
    }

    public int getUpgradeCost(byte level) {
        if (level >= maxLevel) {
            return 0;
        }
        return (level + 1) * upgradeCostBase;
    }
}
