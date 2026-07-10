package nro.models.event.summer;

import nro.models.consts.ConstNpc;
import nro.models.event.EventConfig;
import nro.models.item.Item;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.shop.ShopService;

public class VuaHaiTac extends Npc {

    private static final int MENU_LUCKY_ROUND = 9901;

    public VuaHaiTac(int mapId, int status, int cx, int cy, int tempId, int avatar) {
        super(mapId, status, cx, cy, tempId, avatar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (!EventConfig.isActive(EventConfig.SUMMER)) {
            Service.gI().sendThongBao(player, "Sự kiện hè chưa mở hoặc đã kết thúc.");
            return;
        }
        createOtherMenu(player, ConstNpc.BASE_MENU,
                "Ta là Vua hải tặc. Ngươi muốn ra khơi hay thử vận may?",
                "Quay số",
                "Tới map\nHải tặc",
                "Đổi đồ",
                "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (!EventConfig.isActive(EventConfig.SUMMER)) {
            Service.gI().sendThongBao(player, "Sự kiện hè chưa mở hoặc đã kết thúc.");
            return;
        }
        if (player.idMark.isBaseMenu()) {
            switch (select) {
                case 0 ->
                    createOtherMenu(player, MENU_LUCKY_ROUND,
                            "Quay số tốn " + SummerEventManager.LUCKY_ROUND_GEM_COST + " ngọc. Ngươi muốn thử chứ?",
                            "Quay",
                            "Từ chối");
                case 1 ->
                    ChangeMapService.gI().changeMapBySpaceShip(player, SummerEventManager.MAP_HAI_TAC, -1, -1);
                case 2 ->
                    ShopService.gI().opendShop(player, SummerEventManager.SHOP_EXCHANGE, false);
                default -> {
                }
            }
            return;
        }
        if (player.idMark.getIndexMenu() == MENU_LUCKY_ROUND && select == 0) {
            luckyRound(player);
        }
    }

    private void luckyRound(Player player) {
        if (InventoryService.gI().getCountEmptyBag(player) == 0) {
            Service.gI().sendThongBao(player, "Hành trang đã đầy.");
            return;
        }
        if (player.inventory.gem < SummerEventManager.LUCKY_ROUND_GEM_COST) {
            Service.gI().sendThongBao(player, "Bạn không đủ ngọc để quay số.");
            return;
        }
        player.inventory.gem -= SummerEventManager.LUCKY_ROUND_GEM_COST;
        Service.gI().sendMoney(player);

        SummerEventManager.LuckyReward reward = SummerEventManager.gI().randomLuckyReward();
        Item item = ItemService.gI().createNewItem((short) reward.itemId, reward.quantity);
        reward.addOptions(item);
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được " + reward.quantity + " " + item.template.name + ".");
    }
}
