package nro.models.combine;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class EpLinhThach {

    private static final int FIRST_STONE_ID = 2027;
    private static final int MAX_STONE_ID = 2030;
    private static final int REQUIRED_QUANTITY = 5;
    private static final long[] POTENTIAL_COSTS = {10_000_000L, 100_000_000L, 1_000_000_000L};
    private static final int[] SUCCESS_RATES = {30, 20, 5};

    public static void showInfoCombine(Player player) {
        Item stone = getValidStone(player);
        if (stone == null) {
            showError(player, "Chỉ được chọn 5 Linh Thạch cùng cấp từ cấp 1 đến cấp 3.");
            return;
        }

        long cost = getPotentialCost(stone);
        int successRate = getSuccessRate(stone);
        String text = "|2|Ép 5 " + stone.template.name + " thành 1 "
                + ItemService.gI().getTemplate((short) (stone.template.id + 1)).name
                + "\n|7|Cần " + Util.numberToMoney(cost) + " điểm tiềm năng"
                + "\n|7|Tỉ lệ thành công: " + successRate + "%"
                + "\n|7|Thất bại chỉ mất điểm tiềm năng";
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                text, "Ép Linh Thạch", "Từ chối");
    }

    public static void epLinhThach(Player player) {
        Item stone = getValidStone(player);
        if (stone == null) {
            Service.gI().sendThongBao(player, "Linh Thạch không hợp lệ hoặc không đủ 5 viên.");
            return;
        }

        long cost = getPotentialCost(stone);
        if (player.nPoint.tiemNang < cost) {
            Service.gI().sendThongBao(player, "Bạn còn thiếu "
                    + Util.numberToMoney(cost - player.nPoint.tiemNang) + " điểm tiềm năng.");
            return;
        }

        player.nPoint.tiemNang -= cost;
        Service.gI().point(player);

        if (Util.nextInt(100) < getSuccessRate(stone)) {
            Item upgradedStone = ItemService.gI().createNewItem((short) (stone.template.id + 1));
            InventoryService.gI().subQuantityItemsBag(player, stone, REQUIRED_QUANTITY);
            InventoryService.gI().addItemBag(player, upgradedStone);
            InventoryService.gI().sendItemBags(player);
            CombineService.gI().sendEffectSuccessCombine(player);
            Service.gI().sendThongBao(player, "Ép Linh Thạch thành công! Nhận được " + upgradedStone.template.name + ".");
        } else {
            CombineService.gI().sendEffectFailCombine(player);
            Service.gI().sendThongBao(player, "Ép Linh Thạch thất bại, bạn chỉ mất điểm tiềm năng.");
        }
        CombineService.gI().reOpenItemCombine(player);
    }

    private static Item getValidStone(Player player) {
        if (player.combineNew.itemsCombine.size() != 1) {
            return null;
        }
        Item stone = player.combineNew.itemsCombine.get(0);
        if (stone == null || !stone.isNotNullItem()
                || stone.template.id < FIRST_STONE_ID || stone.template.id >= MAX_STONE_ID
                || stone.quantity < REQUIRED_QUANTITY) {
            return null;
        }
        return stone;
    }

    private static long getPotentialCost(Item stone) {
        return POTENTIAL_COSTS[stone.template.id - FIRST_STONE_ID];
    }

    private static int getSuccessRate(Item stone) {
        return SUCCESS_RATES[stone.template.id - FIRST_STONE_ID];
    }

    private static void showError(Player player, String message) {
        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, message, "Đóng");
    }
}
