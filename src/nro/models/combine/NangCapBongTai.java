package nro.models.combine;

import nro.models.consts.ConstItem;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Nâng cấp bông tai Porata — class tổng quát xử lý tất cả loại nâng cấp:
 *   - C1 → C2 (BT thường)
 *   - C2 → C3 (BT thường)
 *   - BT đặc biệt (1965) cấp 0→1→2
 *
 * @author By Minh Du
 */
public class NangCapBongTai {

    // ── Shared constants ───────────────────────────────────────
    private static final int ITEM_PARAM_INDEX = 31;
    private static final int ITEM_OPTION_ID_CAP = 72;

    // ── Upgrade configs ────────────────────────────────────────

    /** Config cho nâng cấp C1 → C2 */
    private static final UpgradeConfig CONFIG_C1_TO_C2 = new UpgradeConfig(
            454,            // bongTaiId
            921,            // targetId
            933,            // manhVoId
            999,            // requiredManhVo
            99,             // failManhVo
            100_000_000,    // gold
            100,            // gem
            0,              // thoiVang
            0,              // failThoiVang
            50,             // ratio %
            2,              // capValue
            true,           // changeTemplate
            "Bông tai Porata [+2]"
    );

    /** Config cho nâng cấp C2 → C3 */
    private static final UpgradeConfig CONFIG_C2_TO_C3 = new UpgradeConfig(
            921,            // bongTaiId
            1819,           // targetId
            1820,           // manhVoId
            999,            // requiredManhVo
            99,             // failManhVo
            200_000_000,    // gold
            200,            // gem
            0,              // thoiVang
            0,              // failThoiVang
            50,             // ratio %
            3,              // capValue
            true,           // changeTemplate
            "Bông tai Porata [+3]"
    );

    /** Config cho nâng cấp BT đặc biệt 0→1 */
    private static final UpgradeConfig CONFIG_DB_0_TO_1 = new UpgradeConfig(
            1965,           // bongTaiId
            1965,           // targetId (không đổi template)
            2100,           // manhVoId (mảnh BT đặc biệt)
            999,            // requiredManhVo
            99,             // failManhVo
            0,              // gold
            0,              // gem
            50,             // thoiVang
            50,             // failThoiVang
            20,             // ratio %
            1,              // capValue
            false,          // changeTemplate
            "Bông tai Porata Đặc biệt [+1]"
    );

    /** Config cho nâng cấp BT đặc biệt 1→2 */
    private static final UpgradeConfig CONFIG_DB_1_TO_2 = new UpgradeConfig(
            1965,           // bongTaiId
            1965,           // targetId (không đổi template)
            2100,           // manhVoId (mảnh BT đặc biệt)
            999,            // requiredManhVo
            99,             // failManhVo
            0,              // gold
            0,              // gem
            50,             // thoiVang
            50,             // failThoiVang
            10,             // ratio %
            2,              // capValue
            false,          // changeTemplate
            "Bông tai Porata Đặc biệt [+2]"
    );

    // ── Record ─────────────────────────────────────────────────

    private record UpgradeConfig(
            int bongTaiId,
            int targetId,
            int manhVoId,
            int requiredManhVo,
            int failManhVo,
            int gold,
            int gem,
            int thoiVang,
            int failThoiVang,
            int ratio,
            int capValue,
            boolean changeTemplate,
            String name
    ) {}

    // ══════════════════════════════════════════════════════════
    //   Public entry-points (backward-compatible method names)
    // ══════════════════════════════════════════════════════════

    // ── BT thường C1→C2 ────────────────────────────────────
    public static void showInfoCombine(Player player) {
        showInfoCombineInternal(player, CONFIG_C1_TO_C2);
    }

    public static void nangCapBongTai(Player player) {
        nangCapInternal(player, CONFIG_C1_TO_C2);
    }

    // ── BT thường C2→C3 ────────────────────────────────────
    public static void showInfoCombineC3(Player player) {
        showInfoCombineInternal(player, CONFIG_C2_TO_C3);
    }

    public static void nangCapBongTaiC3(Player player) {
        nangCapInternal(player, CONFIG_C2_TO_C3);
    }

    // ── BT đặc biệt ────────────────────────────────────────
    public static void showInfoCombineDB(Player player) {
        // Xác định config dựa trên cấp hiện tại
        UpgradeConfig cfg = getConfigDB(player);
        if (cfg == null) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    "Bông tai Porata đặc biệt đã đạt cấp tối đa!", "Đóng");
            return;
        }
        showInfoCombineInternal(player, cfg);
    }

    public static void nangCapBongTaiDB(Player player) {
        UpgradeConfig cfg = getConfigDB(player);
        if (cfg == null) {
            Service.gI().sendThongBao(player, "Bông tai Porata đặc biệt đã đạt cấp tối đa!");
            return;
        }
        nangCapInternal(player, cfg);
    }

    // ══════════════════════════════════════════════════════════
    //   Internal: xác định config BT đặc biệt dựa trên cấp
    // ══════════════════════════════════════════════════════════

    private static UpgradeConfig getConfigDB(Player player) {
        Item bongTaiDB = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem() && item.template != null && item.template.id == 1965) {
                bongTaiDB = item;
                break;
            }
        }
        if (bongTaiDB == null) {
            // Chưa cho BT vào ô combine → mặc định config cấp 0→1
            return CONFIG_DB_0_TO_1;
        }
        int currentCap = getCurrentCap(bongTaiDB);
        if (currentCap <= 0) {
            return CONFIG_DB_0_TO_1;
        } else if (currentCap == 1) {
            return CONFIG_DB_1_TO_2;
        }
        return null; // Đã max
    }

    private static int getCurrentCap(Item bongTai) {
        if (bongTai == null || bongTai.itemOptions == null) return 0;
        for (ItemOption op : bongTai.itemOptions) {
            if (op.optionTemplate.id == ITEM_OPTION_ID_CAP) {
                return op.param;
            }
        }
        return 0;
    }

    // ══════════════════════════════════════════════════════════
    //   Internal: showInfo & nangCap dùng config
    // ══════════════════════════════════════════════════════════

    private static void showInfoCombineInternal(Player player, UpgradeConfig cfg) {
        if (player.combineNew.itemsCombine.size() != 2) {
            showRequirement(player, cfg);
            return;
        }

        Item bongTai = null;
        Item manhVo = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem() && item.template != null) {
                if (item.template.id == cfg.bongTaiId) bongTai = item;
                else if (item.template.id == cfg.manhVoId) manhVo = item;
            }
        }

        if (bongTai == null || manhVo == null) {
            showRequirement(player, cfg);
            return;
        }

        player.combineNew.goldCombine = cfg.gold;
        player.combineNew.gemCombine = cfg.gem;
        player.combineNew.ratioCombine = cfg.ratio;

        int currentMvp = getQuantity(manhVo);
        String npcSay = "|2|" + cfg.name + "\n\n";
        npcSay += "|2|Tỉ lệ thành công: " + cfg.ratio + "%\n";

        // Kiểm tra vật phẩm
        boolean missingManhVo = currentMvp < cfg.requiredManhVo;
        boolean missingGold = cfg.gold > 0 && player.inventory.gold < cfg.gold;
        boolean missingGem = cfg.gem > 0 && player.inventory.gem < cfg.gem;
        boolean missingThoiVang = false;
        Item thoiVangItem = null;
        if (cfg.thoiVang > 0) {
            thoiVangItem = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
            missingThoiVang = thoiVangItem == null || thoiVangItem.quantity < cfg.thoiVang;
        }

        // Hiển thị yêu cầu mảnh vỡ
        npcSay += (missingManhVo ? "|7|" : "|2|") + "Cần " + cfg.requiredManhVo + " " + manhVo.template.name + "\n";

        // Hiển thị yêu cầu vàng
        if (cfg.gold > 0) {
            npcSay += (missingGold ? "|7|" : "|2|") + "Cần: " + Util.numberToMoney(cfg.gold) + " vàng\n";
        }

        // Hiển thị yêu cầu ngọc
        if (cfg.gem > 0) {
            npcSay += (missingGem ? "|7|" : "|2|") + "Cần: " + cfg.gem + " ngọc\n";
        }

        // Hiển thị yêu cầu thỏi vàng
        if (cfg.thoiVang > 0) {
            npcSay += (missingThoiVang ? "|7|" : "|2|") + "Cần: " + cfg.thoiVang + " thỏi vàng\n";
        }

        // Hiển thị thất bại
        npcSay += "|7|Thất bại -" + cfg.failManhVo + " " + manhVo.template.name;
        if (cfg.failThoiVang > 0) {
            npcSay += " và -" + cfg.failThoiVang + " thỏi vàng";
        }
        npcSay += "\n";

        if (missingManhVo || missingGold || missingGem || missingThoiVang) {
            // Thiếu vật phẩm → hiển thị chi tiết thiếu
            if (missingManhVo) {
                npcSay += "Còn thiếu " + (cfg.requiredManhVo - currentMvp) + " " + manhVo.template.name;
            } else if (missingGold) {
                npcSay += "Còn thiếu " + Util.numberToMoney(cfg.gold - player.inventory.gold) + " vàng";
            } else if (missingGem) {
                npcSay += "Còn thiếu " + (cfg.gem - player.inventory.gem) + " ngọc";
            } else {
                int currentTV = thoiVangItem != null ? thoiVangItem.quantity : 0;
                npcSay += "Còn thiếu " + (cfg.thoiVang - currentTV) + " thỏi vàng";
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            // Đủ điều kiện → cho phép nâng cấp
            String btnText = "Nâng cấp\n";
            if (cfg.gold > 0) btnText += Util.numberToMoney(cfg.gold) + " vàng\n";
            if (cfg.gem > 0) btnText += cfg.gem + " ngọc\n";
            if (cfg.thoiVang > 0) btnText += cfg.thoiVang + " thỏi vàng\n";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    btnText, "Từ chối");
        }
    }

    private static void nangCapInternal(Player player, UpgradeConfig cfg) {
        if (player.combineNew.itemsCombine.size() != 2) return;

        // Validate resources
        if (cfg.gold > 0 && player.inventory.gold < cfg.gold) {
            Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
            return;
        }
        if (cfg.gem > 0 && player.inventory.gem < cfg.gem) {
            Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
            return;
        }

        Item thoiVangItem = null;
        if (cfg.thoiVang > 0) {
            thoiVangItem = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
            if (thoiVangItem == null || thoiVangItem.quantity < cfg.thoiVang) {
                Service.gI().sendThongBao(player, "Không đủ thỏi vàng để thực hiện");
                return;
            }
        }

        Item bongTai = null;
        Item manhVo = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem() && item.template != null) {
                if (item.template.id == cfg.bongTaiId) bongTai = item;
                else if (item.template.id == cfg.manhVoId) manhVo = item;
            }
        }

        if (bongTai == null || manhVo == null) return;

        // Kiểm tra đã có bông tai đích chưa (chỉ cho BT thường - thay đổi template)
        if (cfg.changeTemplate && cfg.targetId != cfg.bongTaiId) {
            Item existing = InventoryService.gI().findItemBag(player, cfg.targetId);
            if (existing != null) {
                Service.gI().sendThongBao(player,
                        "Ngươi đã có " + existing.template.name + " trong hành trang rồi, không thể nâng cấp nữa.");
                return;
            }
        }

        // Trừ tiền/ngọc
        if (cfg.gold > 0) player.inventory.gold -= cfg.gold;
        if (cfg.gem > 0) player.inventory.gem -= cfg.gem;

        if (Util.isTrue(cfg.ratio, 100)) {
            // ── Thành công ──
            if (cfg.changeTemplate) {
                // BT thường: đổi template
                bongTai.template = ItemService.gI().getTemplate(cfg.targetId);
                bongTai.itemOptions.clear();
                bongTai.itemOptions.add(new ItemOption(ITEM_OPTION_ID_CAP, cfg.capValue));
            } else {
                // BT đặc biệt: giữ template, cập nhật option cấp
                updateCapOption(bongTai, cfg.capValue);
            }
            // Trừ toàn bộ mảnh vỡ yêu cầu
            subQuantity(player, manhVo, cfg.requiredManhVo);
            // Trừ thỏi vàng
            if (cfg.thoiVang > 0 && thoiVangItem != null) {
                InventoryService.gI().subQuantityItemsBag(player, thoiVangItem, cfg.thoiVang);
            }
            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            // ── Thất bại ──
            // Trừ mảnh vỡ theo mức fail
            subQuantity(player, manhVo, cfg.failManhVo);
            // Trừ thỏi vàng (BT đặc biệt: thất bại vẫn mất thỏi vàng)
            if (cfg.failThoiVang > 0 && thoiVangItem != null) {
                InventoryService.gI().subQuantityItemsBag(player, thoiVangItem, cfg.failThoiVang);
            }
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().sendItemBags(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    // ══════════════════════════════════════════════════════════
    //   Utility methods
    // ══════════════════════════════════════════════════════════

    private static void updateCapOption(Item bongTai, int newCap) {
        if (bongTai.itemOptions != null) {
            for (ItemOption op : bongTai.itemOptions) {
                if (op.optionTemplate.id == ITEM_OPTION_ID_CAP) {
                    op.param = newCap;
                    return;
                }
            }
        }
        // Chưa có option cấp → thêm mới
        bongTai.itemOptions.add(new ItemOption(ITEM_OPTION_ID_CAP, newCap));
    }

    private static void showRequirement(Player player, UpgradeConfig cfg) {
        String msg = "Cần 1 Bông tai Porata";
        if (cfg.bongTaiId == 1965) {
            msg = "Cần 1 Bông tai Porata đặc biệt";
        } else if (cfg.bongTaiId == 921) {
            msg += " cấp 2";
        } else if (cfg.bongTaiId == 454) {
            msg += " cấp 1";
        }
        msg += " và Mảnh bông tai tương ứng";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, msg, "Đóng");
    }

    private static int getQuantity(Item item) {
        if (item == null) return 0;
        int q = 0;
        if (item.itemOptions != null) {
            for (ItemOption op : item.itemOptions) {
                if (op.optionTemplate.id == ITEM_PARAM_INDEX) {
                    q = op.param;
                    break;
                }
            }
        }
        if (q == 0) {
            q = item.quantity;
        }
        return q;
    }

    private static void subQuantity(Player player, Item item, int amount) {
        boolean hasOption = false;
        if (item.itemOptions != null) {
            for (ItemOption op : item.itemOptions) {
                if (op.optionTemplate.id == ITEM_PARAM_INDEX) {
                    hasOption = true;
                    break;
                }
            }
        }
        if (hasOption) {
            InventoryService.gI().subParamItemsBag(player, item.template.id, ITEM_PARAM_INDEX, amount);
        } else {
            InventoryService.gI().subQuantityItemsBag(player, item, amount);
        }
    }
}