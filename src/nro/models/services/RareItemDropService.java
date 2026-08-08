package nro.models.services;

import nro.models.consts.RareItemConfig;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.utils.Logger;
import nro.models.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RareItemDropService {
    private static RareItemDropService instance;

    // Các bộ đồ
    private static final int[] DO_THAN_LINH = {555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 567};
    private static final int[] DO_HUY_DIET = {650, 651, 652, 653, 654, 655, 656, 657, 658, 659, 660, 661, 662};
    private static final int[] DO_THIEN_SU = {1048, 1049, 1050, 1051, 1052, 1053, 1054, 1055, 1056, 1057, 1058, 1059, 1060, 1061, 1062};

    // Thuộc tính tuyệt đối (số lượng)
    private static final int[] ABSOLUTE_OPTIONS = {14}; // Chỉ dùng Chí Mạng để test
    // Thuộc tính phần trăm
    private static final int[] PERCENT_OPTIONS = {14}; // Chỉ dùng Chí Mạng để test

    public static RareItemDropService gI() {
        if (instance == null) {
            instance = new RareItemDropService();
        }
        return instance;
    }

    public ItemMap tryDropRareItem(Zone zone, Player player, int x, int y) {
        RareItemConfig config = RareItemConfig.gI();
        if (!config.enabled) return null;

        // Tính toán tỉ lệ rơi dựa trên map
        int dropRate = config.dropRateDefault;
        if (config.specialMaps != null) {
            for (int mapId : config.specialMaps) {
                if (zone.map.mapId == mapId) {
                    dropRate = config.dropRateSpecial;
                    break;
                }
            }
        }

        // Tỉ lệ dropRate / 10000
        if (!Util.isTrue(dropRate, 10000)) {
            return null;
        }

        // Random loại độ hiếm: 1 = Đỏ, 2 = Vàng, 3 = Xanh, 0 = Thường
        int rarity = rollRarity(config);
        int numLines = rarity; // 1, 2, 3 dòng bonus

        // Nếu rarity = 0 (đồ thường không có dòng bonus), không tạo rare item
        if (rarity == 0) {
            return null;
        }

        // Random Item
        int itemId = rollItemId();

        // Tạo ItemMap - lưu ý constructor sẽ gọi zone.addItem(this)
        ItemMap itemMap = new ItemMap(zone, itemId, 1, x, y, player.id);

        // Tạo chỉ số gốc (base stats) dựa trên loại trang bị
        initBaseStats(itemMap, itemId);

        // Random options thêm (1-3 dòng bonus)
        List<Item.ItemOption> bonusOptions = rollOptions(numLines, config);
        itemMap.options.addAll(bonusOptions);

        // Thêm Option 73, 254, 255 để Client mod hiển thị Khung viền màu theo chuẩn byte < 255
        // Option 225 để Client đổi màu và tên (nhờ bản mod C# vừa làm)
        int colorParam = 0;
        int textOptionId = 73; // Mặc định Đỏ (text)
        if (rarity == 1) { colorParam = 3; textOptionId = 73; } // Đỏ
        else if (rarity == 2) { colorParam = 7; textOptionId = 254; } // Vàng
        else if (rarity == 3) { colorParam = 5; textOptionId = 255; } // Xanh
        itemMap.options.add(new Item.ItemOption(225, colorParam));
        itemMap.options.add(new Item.ItemOption(textOptionId, 1));

        // Lưu rarity vào ItemMap để nhận diện khi nhặt
        itemMap.rarity = rarity;

        Logger.log("[RareItemDrop] Player=" + player.name
                + " Map=" + zone.map.mapId
                + " ItemId=" + itemId
                + " Rarity=" + rarity
                + " BonusLines=" + numLines
                + " TotalOptions=" + itemMap.options.size() + "\n");

        StringBuilder sb = new StringBuilder("Item rơi ra có các option: ");
        for (Item.ItemOption opt : itemMap.options) {
            sb.append(opt.optionTemplate.id).append(" (").append(opt.param).append("), ");
        }
        nro.models.services.Service.gI().sendThongBao(player, sb.toString());

        return itemMap;
    }

    /**
     * Roll rarity: tỉ lệ mỗi loại được tính dựa trên tổng tỉ lệ (trọng số) của cả 3 loại.
     * Ưu tiên: Xanh (hiếm nhất) > Vàng > Đỏ
     */
    private int rollRarity(RareItemConfig config) {
        int totalWeight = config.rateBlue + config.rateYellow + config.rateRed;
        if (totalWeight <= 0) return 0;

        int r = Util.nextInt(totalWeight);

        // Kiểm tra Xanh
        if (r < config.rateBlue) return 3;

        // Kiểm tra Vàng
        if (r < config.rateBlue + config.rateYellow) return 2;

        // Trả về Đỏ (trường hợp còn lại)
        return 1;
    }

    private int rollItemId() {
        int r = Util.nextInt(100);
        if (r < 60) return DO_THAN_LINH[Util.nextInt(DO_THAN_LINH.length)];
        if (r < 90) return DO_HUY_DIET[Util.nextInt(DO_HUY_DIET.length)];
        return DO_THIEN_SU[Util.nextInt(DO_THIEN_SU.length)];
    }

    private void initBaseStats(ItemMap itemMap, int itemId) {
        nro.models.player_system.Template.ItemTemplate template = ItemService.gI().getTemplate(itemId);
        if (template == null) return;

        int tier = 0; // 1 = Thần, 2 = Hủy Diệt, 3 = Thiên Sứ
        if (itemId >= 555 && itemId <= 567) tier = 1;
        else if (itemId >= 650 && itemId <= 662) tier = 2;
        else if (itemId >= 1048 && itemId <= 1062) tier = 3;

        if (tier == 0) return;

        int type = template.type;
        int optionId = -1;
        int value = 0;

        switch (type) {
            case 0: // Áo -> Giáp
                optionId = 47;
                value = (tier == 1) ? 700 : (tier == 2) ? 1100 : 3400;
                break;
            case 1: // Quần -> HP
                optionId = 22; // HP tính bằng K (1K = 1000)
                value = (tier == 1) ? 50 : (tier == 2) ? 80 : 130;
                break;
            case 2: // Găng -> Sức đánh
                optionId = 0;
                value = (tier == 1) ? 3700 : (tier == 2) ? 5000 : 10000;
                break;
            case 3: // Giày -> KI
                optionId = 23; // KI tính bằng K
                value = (tier == 1) ? 50 : (tier == 2) ? 80 : 110;
                break;
            case 4: // Nhẫn -> Chí mạng %
                optionId = 14;
                value = (tier == 1) ? 15 : (tier == 2) ? 17 : 20;
                break;
        }

        if (optionId != -1) {
            // Random dao động +/- 10%
            int randomPercent = Util.nextInt(90, 110);
            int finalValue = (int) (value * (randomPercent / 100.0f));
            itemMap.options.add(new Item.ItemOption(optionId, finalValue));

            // Yêu cầu sức mạnh (option 21)
            itemMap.options.add(new Item.ItemOption(21, (tier == 1) ? 15 : (tier == 2) ? 17 : 20));
        }
    }

    private List<Item.ItemOption> rollOptions(int numLines, RareItemConfig config) {
        List<Item.ItemOption> result = new ArrayList<>();
        List<Integer> pool = new ArrayList<>();
        for (int opt : ABSOLUTE_OPTIONS) pool.add(opt);
        for (int opt : PERCENT_OPTIONS) pool.add(opt);

        Collections.shuffle(pool);

        for (int i = 0; i < numLines; i++) {
            int optionId = pool.get(i);
            int param = 0;

            if (isAbsoluteOption(optionId)) {
                param = Util.nextInt(config.minAbsolute, config.maxAbsolute);
            } else {
                param = Util.nextInt(config.minPercent, config.maxPercent);
            }

            result.add(new Item.ItemOption(optionId, param));
        }

        return result;
    }

    private boolean isAbsoluteOption(int optionId) {
        for (int opt : ABSOLUTE_OPTIONS) {
            if (opt == optionId) return true;
        }
        return false;
    }

    /**
     * Trả về prefix text màu cho item info dựa trên rarity.
     * 1 = Đỏ, 2 = Vàng, 3 = Xanh
     */
    public static String getRarityPrefix(int rarity) {
        switch (rarity) {
            case 1: return "[Đỏ] ";
            case 2: return "[Vàng] ";
            case 3: return "[Xanh] ";
            default: return "";
        }
    }
}
