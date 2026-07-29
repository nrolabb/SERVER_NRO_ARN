package nro.models.event;

import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.consts.ConstNpc;
import nro.models.shop.ShopService;

public class XeNuocMia extends Npc {

    public XeNuocMia(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (!canOpenNpc(player)) {
            return;
        }
        createOtherMenu(player,
                ConstNpc.BASE_MENU,
                "Giải khát đê, mua đồ đổi đệ tử đi!",
                "Shop\nĐổi đệ",
                "Đóng");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.idMark.isBaseMenu()) {
            if (select == 0) {
                ShopService.gI().opendShop(player, "DOI_SKILL_DE", false);
            }
        }
    }
}
