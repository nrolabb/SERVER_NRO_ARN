package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.shop.ShopService;


public class NongDan extends Npc {
    public NongDan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            // Hiển thị menu khi người chơi click vào NPC
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Chào cậu, tôi có bán vài món đồ hịn đây!",
                    "Cửa hàng", "Thu hoạch\nnhanh", "Gieo hạt\nnhanh", "Phun thuốc\nnhanh", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                switch (select) {
                    case 0: // Khi người chơi chọn "Cửa hàng"
                        // "Hạt giống" phải khớp với tag_name trong database
                        ShopService.gI().opendShop(player, "Hạt giống", false);
                        break;
                    case 1: // Thu hoạch nhanh
                        nro.models.services.FarmService.gI().harvestAllPlots(player);
                        break;
                    case 2: // Gieo hạt nhanh
                        nro.models.services.FarmService.gI().openSeedSelectionMenuMass(player);
                        break;
                    case 3: // Phun thuốc nhanh
                        nro.models.services.FarmService.gI().usePesticideAll(player);
                        break;
                }
            }
        }
    }
}
