package nro.models.combine;

import nro.models.item.Item;
import nro.models.item.Item.ItemOption;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.Service;
import nro.models.server.Manager;
import nro.models.consts.ConstNpc;
import nro.models.utils.Util;
import java.util.ArrayList;
import java.util.List;

public class NangCapPetRong {

    private static final int[] ABSOLUTE_OPTIONS = { 0, 6, 7, 47 };
    private static final int[] PERCENT_OPTIONS = { 5, 14, 16, 50, 77, 80, 81, 95, 96, 97, 100, 103, 108 };

    public static void showInfoCombine(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Hãy bỏ 1 Pet Rồng Nhí vào đây");
            return;
        }

        Item pet = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id >= 1765 && item.template.id <= 1771) {
                pet = item;
            }
        }

        if (pet == null) {
            Service.gI().sendThongBao(player, "Cần 1 Pet Rồng Nhí để nâng cấp");
            return;
        }
        if (pet.template.id == 1771) {
            Service.gI().sendThongBao(player, "Pet Rồng Nhí đã đạt cấp tối đa");
            return;
        }

        int exp = 0;
        for (ItemOption opt : pet.itemOptions) {
            if (opt.optionTemplate.id == 257) {
                exp = opt.param;
                break;
            }
        }

        if (exp < Manager.PET_RONG_EXP_REQ) {
            Service.gI().sendThongBao(player, "Pet Rồng Nhí cần đạt " + Util.numberToMoney(Manager.PET_RONG_EXP_REQ)
                    + " điểm kinh nghiệm (hiện có: " + Util.numberToMoney(exp) + ")");
            return;
        }

        int level = pet.template.id - 1765;
        int rate = Manager.PET_RONG_UPGRADE_PERCENT - (level * 5);
        if (rate < 0)
            rate = 0;

        String info = "|2|Nâng cấp Pet Rồng Nhí lên cấp " + (level + 1) + "\n"
                + "|1|Yêu cầu 100 Thỏi Vàng\n"
                + "|1|Yêu cầu 1.000 Ngọc Xanh\n"
                + "|1|Tỷ lệ thành công: " + rate + "%\n"
                + "|7|Thất bại sẽ bị trừ 1.000 EXP Pet";

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

    public static void nangCapPetRong(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            Service.gI().sendThongBao(player, "Hãy bỏ 1 Pet Rồng Nhí vào đây");
            return;
        }

        Item pet = null;

        for (Item item : player.combineNew.itemsCombine) {
            if (item.template.id >= 1765 && item.template.id <= 1771) {
                pet = item;
            }
        }

        if (pet == null || pet.template.id == 1771) {
            Service.gI().sendThongBao(player, "Vật phẩm không hợp lệ hoặc đã đạt cấp tối đa");
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
        for (ItemOption opt : pet.itemOptions) {
            if (opt.optionTemplate.id == 257) {
                expOption = opt;
                break;
            }
        }

        if (expOption == null || expOption.param < Manager.PET_RONG_EXP_REQ) {
            Service.gI().sendThongBao(player, "Pet Rồng Nhí không đủ điểm kinh nghiệm để nâng cấp");
            return;
        }

        // Thực hiện trừ nguyên liệu
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, 100);
        player.inventory.subGem(1000);
        Service.gI().sendMoney(player);

        int level = pet.template.id - 1765;
        int rate = Manager.PET_RONG_UPGRADE_PERCENT - (level * 5);
        if (rate < 0)
            rate = 0;

        if (Util.isTrue(rate, 100)) {
            // Thành công
            expOption.param -= Manager.PET_RONG_EXP_REQ;
            pet.template = nro.models.services.ItemService.gI().getTemplate((short) (pet.template.id + 1));

            // Tăng 1 trong 3 chỉ số đầu tiên
            List<ItemOption> eligibleOptions = new ArrayList<>();
            for (int i = 0; i < Math.min(3, pet.itemOptions.size()); i++) {
                ItemOption opt = pet.itemOptions.get(i);
                if (isPercentOption(opt.optionTemplate.id) || isAbsoluteOption(opt.optionTemplate.id)) {
                    eligibleOptions.add(opt);
                }
            }

            if (!eligibleOptions.isEmpty()) {
                ItemOption targetOpt = eligibleOptions.get(Util.nextInt(0, eligibleOptions.size() - 1));
                if (isPercentOption(targetOpt.optionTemplate.id)) {
                    targetOpt.param += Manager.PET_RONG_ADD_PERCENT;
                } else if (isAbsoluteOption(targetOpt.optionTemplate.id)) {
                    targetOpt.param += (int) ((double) targetOpt.param * Manager.PET_RONG_ADD_ABSOLUTE / 100.0);
                }
            }
            
            Service.gI().sendThongBao(player, "Nâng cấp thành công Pet Rồng Nhí cấp " + (level + 1));
            CombineService.gI().sendEffectSuccessCombine(player);
        } else {
            // Thất bại
            expOption.param -= 1000;
            if (expOption.param < 0) {
                expOption.param = 0;
            }
            Service.gI().sendThongBao(player, "Nâng cấp thất bại, Pet bị trừ 1.000 điểm kinh nghiệm");
            CombineService.gI().sendEffectFailCombine(player);
        }

        InventoryService.gI().sendItemBags(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static boolean isPercentOption(int id) {
        for (int p : PERCENT_OPTIONS) {
            if (p == id)
                return true;
        }
        return false;
    }

    private static boolean isAbsoluteOption(int id) {
        for (int a : ABSOLUTE_OPTIONS) {
            if (a == id)
                return true;
        }
        return false;
    }
}
