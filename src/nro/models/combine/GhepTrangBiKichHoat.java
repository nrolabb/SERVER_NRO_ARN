package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.player_system.Template;
import nro.models.server.Manager;
import nro.models.utils.Util;

/**
 *
 * @author Antigravity
 */
public class GhepTrangBiKichHoat {

    private static final int GOLD_GHEP = 2_000_000_000;
    private static final int SOLUONG_MANH = 10;
    private static final int RATIO_SUCCESS = 20; // 20% thành công
    private static final int[][] TRANG_BI_KICH_HOAT = {
        {0, 6, 21, 27, 12}, // Trai Dat: ao, quan, gang, giay, rada
        {1, 7, 22, 28, 12}, // Namek
        {2, 8, 23, 29, 12}  // Xayda
    };

    public static Template.SetKichHoatTemplate getSetKichHoatTemplate(int typeManh) {
        for (Template.SetKichHoatTemplate temp : Manager.SET_KICH_HOAT_TEMPLATES) {
            if (temp.typeManh == typeManh) {
                return temp;
            }
        }
        return null;
    }

    public static boolean isTrangBiKichHoat(Item item) {
        if (item == null || item.itemOptions == null) {
            return false;
        }
        for (Item.ItemOption io : item.itemOptions) {
            int optId = io.optionTemplate.id;
            if (optId >= 127 && optId <= 135) {
                return true;
            }
            if (optId == 233 || optId == 237 || optId == 241 || optId == 245 || optId == 251 || optId == 252 || optId == 253) {
                return true;
            }
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Ta có thể giúp gì cho ngươi?\nHãy đặt 1 loại Mảnh SKH tương ứng (yêu cầu " + SOLUONG_MANH + " cái).", "Đóng");
            return;
        }

        Item itemManh = getItemManh(player);
        if (itemManh == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Vật phẩm mảnh kích hoạt không hợp lệ hoặc chưa được đăng ký trong hệ thống!", "Đóng");
            return;
        }

        Template.SetKichHoatTemplate skh = getSetKichHoatTemplate(itemManh.template.type);
        player.combineNew.goldCombine = GOLD_GHEP;
        player.combineNew.ratioCombine = RATIO_SUCCESS;

        String npcSay = "|2|Ghép Trang Bị Kích Hoạt\n";
        npcSay += "|1|Set kích hoạt mục tiêu: " + skh.name + " (" + skh.description + ")\n";
        npcSay += "|1|Trang bị nhận được: đúng loại mảnh và đúng hành tinh của set\n";
        npcSay += "|2|Yêu cầu mảnh: " + SOLUONG_MANH + " " + itemManh.template.name + " (Đang có: " + itemManh.quantity + ")\n";
        npcSay += "|2|Yêu cầu lệ phí: " + Util.numberToMoney(GOLD_GHEP) + " vàng\n";
        npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n";
        npcSay += "|7|Lưu ý: Nếu thất bại sẽ mất vàng và mảnh nguyên liệu!\n";

        if (itemManh.quantity < SOLUONG_MANH) {
            npcSay += "|7|Số lượng mảnh không đủ " + SOLUONG_MANH + " cái!\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else if (player.inventory.gold < GOLD_GHEP) {
            npcSay += "|7|Bạn không đủ vàng, còn thiếu " + Util.powerToString(GOLD_GHEP - player.inventory.gold) + " vàng!\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Ghép ngay\n(Cần " + Util.numberToMoney(GOLD_GHEP) + " vàng)", "Từ chối");
        }
    }

    public static void thucHienGhep(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Nguyên liệu không hợp lệ");
            return;
        }

        Item itemManh = getItemManh(player);
        if (itemManh == null) {
            Service.gI().sendThongBao(player, "Nguyên liệu không hợp lệ");
            return;
        }

        Template.SetKichHoatTemplate skh = getSetKichHoatTemplate(itemManh.template.type);
        if (skh == null) {
            Service.gI().sendThongBao(player, "Mảnh kích hoạt không hợp lệ");
            return;
        }

        if (itemManh.quantity < SOLUONG_MANH) {
            Service.gI().sendThongBao(player, "Số lượng mảnh kích hoạt không đủ");
            return;
        }

        if (player.inventory.gold < GOLD_GHEP) {
            Service.gI().sendThongBao(player, "Không đủ vàng");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang phải có ít nhất 1 ô trống");
            return;
        }

        player.inventory.gold -= GOLD_GHEP;
        InventoryService.gI().subQuantityItemsBag(player, itemManh, SOLUONG_MANH);

        // Kiểm tra tỉ lệ thành công
        boolean success = Util.isTrue(RATIO_SUCCESS, 100);
        if (success) {
            int itemId = getItemKichHoatByManh(skh.gender, itemManh);
            Item itemSKH = itemId != -1 ? ItemService.gI().createItemSKH(itemId, skh.id) : null;
            if (itemSKH != null) {
                InventoryService.gI().addItemBag(player, itemSKH);
                player.combineNew.itemsCombine.clear();
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã ghép thành công " + itemSKH.template.name + " (" + skh.name + ")!");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Có lỗi xảy ra, việc ghép thất bại!");
            }
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Rất tiếc, ghép trang bị kích hoạt thất bại!");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        if (!success || !itemManh.isNotNullItem() || itemManh.quantity <= 0) {
            player.combineNew.itemsCombine.removeIf(item -> item == null || !item.isNotNullItem() || item.quantity <= 0);
        }
        CombineService.gI().reOpenItemCombine(player);
    }

    private static Item getItemManh(Player player) {
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem() && getSetKichHoatTemplate(item.template.type) != null) {
                return item;
            }
        }
        return null;
    }

    private static int getItemKichHoatByManh(int gender, Item itemManh) {
        if (gender < 0 || gender >= TRANG_BI_KICH_HOAT.length) {
            return -1;
        }
        int typeTrangBi = getTypeTrangBiByManh(itemManh);
        if (typeTrangBi < 0 || typeTrangBi >= TRANG_BI_KICH_HOAT[gender].length) {
            return -1;
        }
        return TRANG_BI_KICH_HOAT[gender][typeTrangBi];
    }

    private static int getTypeTrangBiByManh(Item itemManh) {
        if (itemManh == null || itemManh.template == null || itemManh.template.name == null) {
            return -1;
        }
        String name = itemManh.template.name.toLowerCase();
        if (name.contains("áo") || name.contains("ao")) {
            return 0;
        }
        if (name.contains("quần") || name.contains("quan")) {
            return 1;
        }
        if (name.contains("găng") || name.contains("gang")) {
            return 2;
        }
        if (name.contains("giày") || name.contains("giầy") || name.contains("giay")) {
            return 3;
        }
        if (name.contains("rada") || name.contains("rađa")) {
            return 4;
        }
        return -1;
    }
}
