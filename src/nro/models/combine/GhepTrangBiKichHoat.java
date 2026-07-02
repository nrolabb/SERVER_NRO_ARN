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

    private static final int THOI_VANG_ID = 457;
    private static final int SOLUONG_THOI_VANG = 10;
    private static final int SOLUONG_MANH = 10;
    private static final int RATIO_SUCCESS = 20; // 20% thành công

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
            if (optId == 233 || optId == 237 || optId == 241 || optId == 245 || optId == 250 || optId == 252 || optId == 253) {
                return true;
            }
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            Item itemGoc = null;
            Item itemManh = null;

            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (item.template.type >= 0 && item.template.type <= 4) {
                        if (!isTrangBiKichHoat(item)) {
                            itemGoc = item;
                        }
                    } else {
                        itemManh = item;
                    }
                }
            }

            if (itemGoc == null) {
                for (Item item : player.combineNew.itemsCombine) {
                    if (item.isNotNullItem() && item.template.type >= 0 && item.template.type <= 4 && isTrangBiKichHoat(item)) {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Trang bị này đã có set kích hoạt rồi!", "Đóng");
                        return;
                    }
                }
            }

            if (itemGoc != null && itemManh != null) {
                Template.SetKichHoatTemplate skh = getSetKichHoatTemplate(itemManh.template.type);
                if (skh != null) {
                    // Kiểm tra tính tương thích giới tính
                    if (itemGoc.template.gender == 3 || itemGoc.template.gender == skh.gender) {
                        player.combineNew.goldCombine = 0; // Không tốn vàng thường
                        player.combineNew.ratioCombine = RATIO_SUCCESS;

                        Item thoiVang = InventoryService.gI().findItemBag(player, THOI_VANG_ID);
                        int currentThoiVang = thoiVang != null ? thoiVang.quantity : 0;

                        String npcSay = "|2|Ghép Trang Bị Kích Hoạt\n";
                        npcSay += "|1|Trang bị gốc: " + itemGoc.template.name + "\n";
                        npcSay += "|1|Set kích hoạt mục tiêu: " + skh.name + " (" + skh.description + ")\n";
                        npcSay += "|2|Yêu cầu mảnh: " + SOLUONG_MANH + " " + itemManh.template.name + " (Đang có: " + itemManh.quantity + ")\n";
                        npcSay += "|2|Yêu cầu lệ phí: " + SOLUONG_THOI_VANG + " Thỏi vàng (Đang có: " + currentThoiVang + ")\n";
                        npcSay += "|2|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n";
                        npcSay += "|7|Lưu ý: Nếu thất bại sẽ mất thỏi vàng và mảnh nguyên liệu, trang bị gốc giữ nguyên!\n";

                        if (itemManh.quantity < SOLUONG_MANH) {
                            npcSay += "|7|Số lượng mảnh không đủ " + SOLUONG_MANH + " cái!\n";
                            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        } else if (currentThoiVang < SOLUONG_THOI_VANG) {
                            npcSay += "|7|Bạn không đủ thỏi vàng, còn thiếu " + (SOLUONG_THOI_VANG - currentThoiVang) + " thỏi vàng!\n";
                            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        } else {
                            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Ghép ngay\n(Cần " + SOLUONG_THOI_VANG + " Thỏi vàng)", "Từ chối");
                        }
                    } else {
                        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Hệ phái của trang bị gốc và set kích hoạt không tương thích!", "Đóng");
                    }
                } else {
                    CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Vật phẩm mảnh kích hoạt không hợp lệ hoặc chưa được đăng ký trong hệ thống!", "Đóng");
                }
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Vui lòng đặt đúng 1 Trang bị gốc (chưa kích hoạt) và 1 loại Mảnh SKH tương ứng!", "Đóng");
            }
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Ta có thể giúp gì cho ngươi?\nHãy đặt 1 trang bị gốc (Áo, Quần, Găng, Giày, Rada chưa kích hoạt) và Mảnh SKH tương ứng (yêu cầu " + SOLUONG_MANH + " cái).", "Đóng");
        }
    }

    public static void thucHienGhep(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Nguyên liệu không hợp lệ");
            return;
        }

        Item itemGoc = null;
        Item itemManh = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item.isNotNullItem()) {
                if (item.template.type >= 0 && item.template.type <= 4) {
                    if (!isTrangBiKichHoat(item)) {
                        itemGoc = item;
                    }
                } else {
                    itemManh = item;
                }
            }
        }

        if (itemGoc == null || itemManh == null) {
            Service.gI().sendThongBao(player, "Nguyên liệu không hợp lệ");
            return;
        }

        Template.SetKichHoatTemplate skh = getSetKichHoatTemplate(itemManh.template.type);
        if (skh == null) {
            Service.gI().sendThongBao(player, "Mảnh kích hoạt không hợp lệ");
            return;
        }

        if (itemGoc.template.gender != 3 && itemGoc.template.gender != skh.gender) {
            Service.gI().sendThongBao(player, "Hệ phái trang bị và set kích hoạt không tương thích");
            return;
        }

        if (itemManh.quantity < SOLUONG_MANH) {
            Service.gI().sendThongBao(player, "Số lượng mảnh kích hoạt không đủ");
            return;
        }

        Item thoiVang = InventoryService.gI().findItemBag(player, THOI_VANG_ID);
        if (thoiVang == null || thoiVang.quantity < SOLUONG_THOI_VANG) {
            Service.gI().sendThongBao(player, "Không đủ thỏi vàng");
            return;
        }

        if (InventoryService.gI().getCountEmptyBag(player) < 1) {
            Service.gI().sendThongBao(player, "Hành trang phải có ít nhất 1 ô trống");
            return;
        }

        // Thực hiện trừ lệ phí thỏi vàng và mảnh nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, SOLUONG_THOI_VANG);
        InventoryService.gI().subQuantityItemsBag(player, itemManh, SOLUONG_MANH);

        // Kiểm tra tỉ lệ thành công
        boolean success = Util.isTrue(RATIO_SUCCESS, 100);
        if (success) {
            // Trừ trang bị gốc cũ
            InventoryService.gI().subQuantityItemsBag(player, itemGoc, 1);
            
            // Tạo trang bị kích hoạt mới
            Item itemSKH = ItemService.gI().createItemSKH(itemGoc.template.id, skh.id);
            if (itemSKH != null) {
                InventoryService.gI().addItemBag(player, itemSKH);
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Chúc mừng! Bạn đã ghép thành công " + itemSKH.template.name + " (" + skh.name + ")!");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Có lỗi xảy ra, việc ghép thất bại!");
            }
        } else {
            // Thất bại: Mất thỏi vàng + mảnh, trang bị gốc giữ nguyên
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Rất tiếc, ghép trang bị kích hoạt thất bại!");
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        player.combineNew.itemsCombine.clear();
        CombineService.gI().reOpenItemCombine(player);
    }
}
