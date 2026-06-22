package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import java.util.ArrayList;
import java.util.List;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 */

public class NangCapSaoPhaLe {

    private static final int REQUIRED_SKH = 10;
    private static final int CAPSULE_KICH_HOAT = 1655;
    private static final int RATIO_NANG_CAP = 10;

    public static void showInfoCombine(Player player) {
        if (!isValidCombineItems(player)) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Cần đúng " + REQUIRED_SKH + " Đồ Kích hoạt HSD.\n"
                    + "Chỉ nhận Đồ Kích hoạt HSD.\n"
                    + "Đồ Kích hoạt không có HSD không dùng được.",
                    "Đóng");
            return;
        }

        player.combineNew.ratioCombine = RATIO_NANG_CAP;
        String npcSay = "|2|Tái chế " + REQUIRED_SKH + " Đồ Kích hoạt HSD\n"
                + "|2|Tỉ lệ nhận Capsule kích hoạt: " + RATIO_NANG_CAP + "%\n"
                + "|2|Thành công nhận x1 Capsule kích hoạt\n"
                + "|7|Thất bại mất toàn bộ nguyên liệu\n";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay, "Nâng cấp", "Từ chối");
    }

    public static void nangCapSaoPhaLe(Player player) {
        if (!isValidCombineItems(player)) {
            Service.gI().sendThongBao(player, "Cần đúng " + REQUIRED_SKH + " Đồ Kích hoạt HSD.");
            return;
        }

        removeCombineItems(player);

        if (Util.isTrue(RATIO_NANG_CAP, 100)) {
            Item capsule = ItemService.gI().createNewItem((short) CAPSULE_KICH_HOAT, 1);
            InventoryService.gI().addItemBag(player, capsule);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Nâng cấp thành công, nhận x1 " + capsule.template.name + ".");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Nâng cấp thất bại.");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static boolean isValidCombineItems(Player player) {
        if (player.combineNew.itemsCombine.size() != REQUIRED_SKH) {
            return false;
        }
        for (Item item : player.combineNew.itemsCombine) {
            if (!isSethKichHoatMapThuong(item)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSethKichHoatMapThuong(Item item) {
        if (item == null || !item.isNotNullItem() || item.itemOptions == null) {
            return false;
        }
        return hasAnyOption(item, 127, 128, 129, 130, 131, 132, 133, 134, 135)
                && hasAnyOption(item, 93);
    }

    private static boolean hasAnyOption(Item item, int... optionIds) {
        for (Item.ItemOption option : item.itemOptions) {
            if (option == null || option.optionTemplate == null) {
                continue;
            }
            for (int optionId : optionIds) {
                if (option.optionTemplate.id == optionId) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeCombineItems(Player player) {
        List<Item> items = new ArrayList<>(player.combineNew.itemsCombine);
        for (Item item : items) {
            if (item != null && item.isNotNullItem()) {
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
            }
        }
    }
}
