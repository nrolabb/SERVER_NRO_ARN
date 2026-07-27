package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 * Mở chỉ số bông tai Porata — class tổng quát xử lý tất cả loại:
 *   - BT cấp 2 (1 dòng chỉ số)
 *   - BT cấp 3 (2 dòng chỉ số)
 *   - BT đặc biệt 1965 (2 dòng chỉ số, giống C3)
 *
 * @author By Minh Du
 */
public class NangChiSoBongTai {

    // ── Shared constants ───────────────────────────────────────
    private static final int ITEM_PARAM_INDEX = 31;
    private static final byte[] UPGRADE_OPTIONS = {77, 101, 103, 50, 94, 5};
    private static final byte PARAM_MIN = 5;
    private static final byte PARAM_MAX = 15;

    // ── Stats configs ──────────────────────────────────────────

    /** Config cho mở chỉ số BT cấp 2 */
    private static final StatsConfig CONFIG_C2 = new StatsConfig(
            921,    // bongTaiId
            934,    // honBongTaiId
            935,    // daXanhLamId
            99,     // requiredHon
            190,    // gem
            45,     // ratio %
            1,      // numStats (1 dòng)
            2,      // capValue
            "Mở chỉ số Bông tai Porata [+2]"
    );

    /** Config cho mở chỉ số BT cấp 3 */
    private static final StatsConfig CONFIG_C3 = new StatsConfig(
            1819,   // bongTaiId
            934,    // honBongTaiId
            935,    // daXanhLamId
            99,     // requiredHon
            100,    // gem
            30,     // ratio %
            2,      // numStats (2 dòng)
            3,      // capValue
            "Mở chỉ số Bông tai Porata [+3]"
    );

    /** Config cho mở chỉ số BT đặc biệt (1965) — giống C3 */
    private static final StatsConfig CONFIG_DB = new StatsConfig(
            1965,   // bongTaiId
            934,    // honBongTaiId
            935,    // daXanhLamId
            99,     // requiredHon
            100,    // gem
            30,     // ratio %
            2,      // numStats (2 dòng)
            -1,     // capValue = -1 → giữ nguyên option cấp hiện tại
            "Mở chỉ số Bông tai Porata Đặc biệt"
    );

    // ── Record ─────────────────────────────────────────────────

    private record StatsConfig(
            int bongTaiId,
            int honBongTaiId,
            int daXanhLamId,
            int requiredHon,
            int gem,
            int ratio,
            int numStats,
            int capValue,      // -1 = giữ nguyên
            String name
    ) {}

    // ══════════════════════════════════════════════════════════
    //   Public entry-points
    // ══════════════════════════════════════════════════════════

    // ── BT cấp 2 ────────────────────────────────────────────
    public static void showInfoCombine(Player player) {
        showInfoCombineInternal(player, CONFIG_C2);
    }

    public static void nangChiSoBongTai(Player player) {
        nangChiSoInternal(player, CONFIG_C2);
    }

    // ── BT cấp 3 ────────────────────────────────────────────
    public static void showInfoCombineC3(Player player) {
        showInfoCombineInternal(player, CONFIG_C3);
    }

    public static void nangChiSoBongTaiC3(Player player) {
        nangChiSoInternal(player, CONFIG_C3);
    }

    // ── BT đặc biệt ────────────────────────────────────────
    public static void showInfoCombineDB(Player player) {
        showInfoCombineInternal(player, CONFIG_DB);
    }

    public static void nangChiSoBongTaiDB(Player player) {
        nangChiSoInternal(player, CONFIG_DB);
    }

    // ══════════════════════════════════════════════════════════
    //   Internal: showInfo & nangChiSo dùng config
    // ══════════════════════════════════════════════════════════

    private static void showInfoCombineInternal(Player player, StatsConfig cfg) {
        if (player.combineNew.itemsCombine.size() != 3) {
            showRequirement(player, cfg);
            return;
        }

        Item bongTai = null;
        Item honBongTai = null;
        Item daXanhLam = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item != null && item.isNotNullItem() && item.template != null) {
                int id = item.template.id;
                if (id == cfg.bongTaiId) bongTai = item;
                else if (id == cfg.honBongTaiId) honBongTai = item;
                else if (id == cfg.daXanhLamId) daXanhLam = item;
            }
        }

        if (bongTai == null || honBongTai == null || daXanhLam == null) {
            showRequirement(player, cfg);
            return;
        }

        player.combineNew.gemCombine = cfg.gem;
        player.combineNew.ratioCombine = cfg.ratio;

        int currentHon = getQuantity(honBongTai);
        String resultText;
        if (cfg.bongTaiId == 1965) {
            int cap = getCurrentCap(bongTai);
            if (cap <= 1) {
                resultText = "+1 dòng chỉ số ngẫu nhiên (Max 10%)";
            } else if (cap == 2) {
                resultText = "+2 dòng chỉ số ngẫu nhiên (Max 15%, có thể trùng nhau)";
            } else {
                resultText = "+3 dòng chỉ số ngẫu nhiên (Max 20%, có thể trùng nhau)";
            }
        } else {
            resultText = cfg.numStats == 1
                    ? "+1 Chỉ số ngẫu nhiên"
                    : "+2 dòng chỉ số ngẫu nhiên (có thể trùng nhau)";
        }

        String npcSay = "|2|" + cfg.name + "\n\n";
        npcSay += "|2|Tỉ lệ thành công: " + cfg.ratio + "%\n";

        boolean missingDa = daXanhLam.quantity < 1;
        boolean missingHon = currentHon < cfg.requiredHon;
        boolean missingGem = player.inventory.gem < cfg.gem;

        npcSay += (missingHon ? "|7|" : "|2|") + "Cần " + cfg.requiredHon + " " + honBongTai.template.name + "\n";
        npcSay += (missingDa ? "|7|" : "|2|") + "Cần 1 " + daXanhLam.template.name + "\n";
        npcSay += (missingGem ? "|7|" : "|2|") + "Cần: " + cfg.gem + " ngọc\n";
        npcSay += "|1|Kết quả: " + resultText;

        if (missingDa || missingHon || missingGem) {
            if (missingDa) {
                npcSay += "\n|2|Còn thiếu " + (1 - daXanhLam.quantity) + " " + daXanhLam.template.name;
            } else if (missingHon) {
                npcSay += "\n|2|Còn thiếu " + (cfg.requiredHon - currentHon) + " " + honBongTai.template.name;
            } else {
                npcSay += "\n|2|Còn thiếu " + (cfg.gem - player.inventory.gem) + " ngọc";
            }
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Nâng cấp\n" + cfg.gem + " ngọc", "Từ chối");
        }
    }

    private static void nangChiSoInternal(Player player, StatsConfig cfg) {
        try {
            Item honBongTai = player.combineNew.itemsCombine.stream()
                    .filter(it -> it != null && it.isNotNullItem() && it.template != null && it.template.id == cfg.honBongTaiId)
                    .findFirst()
                    .orElse(null);
            int currentHon = getQuantity(honBongTai);
            Item daXanhLam = InventoryService.gI().findItemBag(player, cfg.daXanhLamId);

            if (currentHon < cfg.requiredHon || daXanhLam == null || daXanhLam.quantity < 1) {
                Service.gI().sendThongBao(player, "Không đủ vật phẩm để thực hiện.");
                return;
            }
            if (player.inventory.gem < player.combineNew.gemCombine) {
                Service.gI().sendThongBao(player,
                        "Bạn không đủ ngọc, còn thiếu " + (player.combineNew.gemCombine - player.inventory.gem) + " ngọc nữa!");
                return;
            }

            player.inventory.gem -= player.combineNew.gemCombine;

            Item bongTai = player.combineNew.itemsCombine.stream()
                    .filter(it -> it != null && it.isNotNullItem() && it.template != null && it.template.id == cfg.bongTaiId)
                    .findFirst()
                    .orElse(null);

            if (bongTai == null) {
                Service.gI().sendThongBao(player, "Thiếu Bông tai Porata.");
                return;
            }

            boolean success = Util.isTrue(player.combineNew.ratioCombine, 100);
            if (success) {
                // Lưu lại option cấp hiện tại nếu cần giữ nguyên
                int preservedCap = -1;
                if (cfg.capValue == -1) {
                    preservedCap = getCurrentCap(bongTai);
                }

                int actualNumStats = cfg.numStats;
                int actualParamMax = PARAM_MAX;

                if (cfg.bongTaiId == 1965) {
                    int cap = (preservedCap > 0) ? preservedCap : 1;
                    if (cap == 1) {
                        actualNumStats = 1;
                        actualParamMax = 10;
                    } else if (cap == 2) {
                        actualNumStats = 2;
                        actualParamMax = 15;
                    } else if (cap >= 3) {
                        actualNumStats = 3;
                        actualParamMax = 20;
                    }
                }

                bongTai.itemOptions.clear();

                // Thêm chỉ số ngẫu nhiên
                for (int i = 0; i < actualNumStats; i++) {
                    byte optionId = randomOpt();
                    byte param = (byte) Util.nextInt(PARAM_MIN, actualParamMax);
                    bongTai.itemOptions.add(new ItemOption(optionId, param));
                }

                // Thêm option cấp
                if (cfg.capValue == -1) {
                    // Giữ nguyên option cấp (BT đặc biệt)
                    if (preservedCap > 0) {
                        bongTai.itemOptions.add(new ItemOption((short) 72, preservedCap));
                    }
                } else {
                    bongTai.itemOptions.add(new ItemOption((short) 72, cfg.capValue));
                }

                CombineService.gI().sendEffectSuccessCombine(player);
            } else {
                CombineService.gI().sendEffectFailCombine(player);
            }

            // Trừ vật phẩm
            subQuantity(player, honBongTai, cfg.requiredHon);
            InventoryService.gI().subQuantityItemsBag(player, daXanhLam, 1);

            Service.gI().sendMoney(player);
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().reOpenItemCombine(player);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════
    //   Utility methods
    // ══════════════════════════════════════════════════════════

    private static int getCurrentCap(Item bongTai) {
        if (bongTai == null || bongTai.itemOptions == null) return 0;
        for (ItemOption op : bongTai.itemOptions) {
            if (op.optionTemplate.id == 72) {
                return op.param;
            }
        }
        return 0;
    }

    private static byte randomOpt() {
        return UPGRADE_OPTIONS[Util.nextInt(0, UPGRADE_OPTIONS.length - 1)];
    }

    private static void showRequirement(Player player, StatsConfig cfg) {
        String bongTaiName;
        if (cfg.bongTaiId == 1965) {
            bongTaiName = "Bông tai Porata đặc biệt";
        } else if (cfg.bongTaiId == 1819) {
            bongTaiName = "Bông tai Porata cấp 3";
        } else {
            bongTaiName = "Bông tai Porata cấp 2";
        }
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                "Cần 1 " + bongTaiName + ", x" + cfg.requiredHon + " Hồn bông tai và 1 Đá xanh lam", "Đóng");
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