package nro.models.shop;

/** IDs mirrored by the shop_sell_type database table. */
public final class ShopSellType {

    public static final byte GOLD = 0;
    public static final byte GEM = 1;
    public static final byte RUBY = 2;
    public static final byte CLAN_POINT = 4;
    public static final byte ACTIVE_POINT = 5;
    public static final byte SPECIFIC_ITEM = 6;

    private ShopSellType() {
    }
}
