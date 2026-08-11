package nro.models.combine;

import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.server.Manager;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;
import nro.models.consts.ConstNpc;

public class NangCapThuCuoi {

    private static final int[] ABSOLUTE_OPTIONS = { 0, 6, 7, 47 };
    private static final int[] PERCENT_OPTIONS = { 5, 14, 16, 50, 77, 80, 81, 95, 96, 97, 100, 103, 108 };

    private static boolean isMountItem(int itemId) {
        return itemId == 1468 || itemId == 1734 || itemId == 1886 || itemId == 1947 || itemId == 1948 || itemId == 1949;
    }

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Hãy bỏ Thú cưỡi vào đây");
            return;
        }

        Item mount = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (isMountItem(item.template.id)) {
                mount = item;
            }
        }

        if (mount == null || !mount.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Hãy bỏ Thú cưỡi cần nâng cấp vào đây");
            return;
        }

        int level = 0;
        int exp = 0;
        for (ItemOption opt : mount.itemOptions) {
            if (opt.optionTemplate.id == 257) {
                exp = opt.param;
            } else if (opt.optionTemplate.id == 72) {
                level = opt.param;
            }
        }

        if (level >= 5) {
            Service.gI().sendThongBao(player, "Thú cưỡi đã đạt cấp tối đa");
            return;
        }

        Item thoiVang = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457) {
                thoiVang = item;
                break;
            }
        }
        int tvQuantity = thoiVang != null ? thoiVang.quantity : 0;

        boolean isLackTV = tvQuantity < 100;
        boolean isLackGem = player.inventory.gem < 1000;
        boolean isLackExp = exp < Manager.THU_CUOI_EXP_REQ;

        int rate = Manager.THU_CUOI_UPGRADE_PERCENT - (level * 5);
        if (rate < 0) rate = 0;

        String tvString = (isLackTV ? "|7|" : "|1|") + "Yêu cầu 100 Thỏi Vàng (hiện có: " + Util.numberToMoney(tvQuantity) + ")\n";
        String gemString = (isLackGem ? "|7|" : "|1|") + "Yêu cầu 1.000 Ngọc Xanh (hiện có: " + Util.numberToMoney(player.inventory.gem) + ")\n";
        String expString = (isLackExp ? "|7|" : "|1|") + "Yêu cầu " + Util.numberToMoney(Manager.THU_CUOI_EXP_REQ) + " điểm kinh nghiệm (hiện có: " + Util.numberToMoney(exp) + ")\n";

        String info = "|2|Thú cưỡi hiện tại: Cấp " + level + "\n"
                + "|2|Sau khi nâng cấp: Cấp " + (level + 1) + "\n"
                + "|1|Chỉ số ngẫu nhiên được cộng thêm:\n"
                + "|1|+ " + Manager.THU_CUOI_ADD_PERCENT + "% (chỉ số %)\n"
                + "|1|+ " + Manager.THU_CUOI_ADD_ABSOLUTE + "% (chỉ số cộng thẳng)\n"
                + expString
                + tvString
                + gemString
                + "|1|Tỷ lệ thành công: " + rate + "%\n"
                + "|7|Thất bại sẽ bị trừ 1.000 điểm kinh nghiệm";

        player.combineNew.goldCombine = 0;
        player.combineNew.gemCombine = 1000;
        player.combineNew.ratioCombine = rate;

        nro.models.npc.Npc npc = player.idMark.getNpcChose();
        if (npc == null) {
            npc = CombineService.gI().baHatMit;
        }
        npc.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, info,
                "Nâng cấp\n1.000 ngọc\n100 vàng", "Từ chối");
    }

    public static void upgradeMount(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Hãy bỏ 1 Thú cưỡi vào đây");
            return;
        }

        Item mount = null;
        for (Item item : player.combineNew.itemsCombine) {
            if (isMountItem(item.template.id)) {
                mount = item;
            }
        }

        if (mount == null || !mount.isNotNullItem()) {
            Service.gI().sendThongBao(player, "Hãy bỏ Thú cưỡi cần nâng cấp vào đây");
            return;
        }

        int level = 0;
        ItemOption levelOpt = null;
        for (ItemOption opt : mount.itemOptions) {
            if (opt.optionTemplate.id == 72) {
                level = opt.param;
                levelOpt = opt;
                break;
            }
        }

        if (level >= 5) {
            Service.gI().sendThongBao(player, "Thú cưỡi đã đạt cấp tối đa");
            return;
        }

        Item thoiVang = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457) {
                thoiVang = item;
                break;
            }
        }

        if (thoiVang == null || thoiVang.quantity < 100) {
            Service.gI().sendThongBao(player, "Cần 100 Thỏi Vàng trong hành trang để nâng cấp");
            return;
        }
        if (player.inventory.gem < 1000) {
            Service.gI().sendThongBao(player, "Bạn không có đủ 1.000 Ngọc Xanh");
            return;
        }

        ItemOption expOption = null;
        for (ItemOption opt : mount.itemOptions) {
            if (opt.optionTemplate.id == 257) {
                expOption = opt;
                break;
            }
        }

        if (expOption == null || expOption.param < Manager.THU_CUOI_EXP_REQ) {
            Service.gI().sendThongBao(player, "Thú cưỡi không đủ điểm kinh nghiệm để nâng cấp");
            return;
        }

        // Thực hiện trừ nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, 100);
        player.inventory.subGem(1000);
        Service.gI().sendMoney(player);

        int rate = Manager.THU_CUOI_UPGRADE_PERCENT - (level * 5);
        if (rate < 0) rate = 0;

        if (Util.isTrue(rate, 100)) {
            // Thành công
            expOption.param -= Manager.THU_CUOI_EXP_REQ;
            
            if (levelOpt != null) {
                levelOpt.param += 1;
            } else {
                mount.itemOptions.add(new ItemOption(72, 1));
            }

            // Tăng 1 trong các chỉ số
            List<ItemOption> eligibleOptions = new ArrayList<>();
            for (ItemOption opt : mount.itemOptions) {
                if (isPercentOption(opt.optionTemplate.id) || isAbsoluteOption(opt.optionTemplate.id)) {
                    eligibleOptions.add(opt);
                }
            }

            if (!eligibleOptions.isEmpty()) {
                ItemOption targetOpt = eligibleOptions.get(Util.nextInt(0, eligibleOptions.size() - 1));
                if (isPercentOption(targetOpt.optionTemplate.id)) {
                    targetOpt.param += Manager.THU_CUOI_ADD_PERCENT;
                } else if (isAbsoluteOption(targetOpt.optionTemplate.id)) {
                    targetOpt.param += (int) ((double) targetOpt.param * Manager.THU_CUOI_ADD_ABSOLUTE / 100.0);
                }
            }
            
            Service.gI().sendThongBao(player, "Nâng cấp thành công Thú cưỡi lên cấp " + (level + 1));
            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            // Thất bại
            expOption.param -= 1000;
            if (expOption.param < 0) {
                expOption.param = 0;
            }
            Service.gI().sendThongBao(player, "Nâng cấp thất bại, Thú cưỡi bị trừ 1.000 điểm kinh nghiệm");
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static boolean isPercentOption(int id) {
        for (int p : PERCENT_OPTIONS) {
            if (p == id) return true;
        }
        return false;
    }

    private static boolean isAbsoluteOption(int id) {
        for (int a : ABSOLUTE_OPTIONS) {
            if (a == id) return true;
        }
        return false;
    }
}
