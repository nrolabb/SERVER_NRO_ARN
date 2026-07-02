package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.ClanService;
import nro.models.shop.ShopService;
import nro.models.map.service.ChangeMapService;

public class GiuMaDauBo extends Npc {

    public GiuMaDauBo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "• Nếu ngươi có Bông Tai Porata:\n"
                            + "  - Có thể úp được Mảnh Vỡ Bông Tai Cấp 2\n"
                            + "  - Có thể úp được Mảnh Hồn Bông Tai\n"
                            + "  - Đôi khi còn rơi cả Đá Xanh Lam\n\n"
                            + "• Nếu ngươi có Bông Tai Porata Cấp 2:\n"
                            + "  - Có thể úp thêm Mảnh Vỡ Bông Tai Cấp 3\n\n"
                            + "• Nếu ngươi có Cải trang Bulma Sexy:\n"
                            + "  - Tỉ lệ úp sẽ là x1.5\n"
                            + "Hãy chuẩn bị kỹ trước khi tham gia thử thách!",
                    this.mapId != 153 ? "Về lãnh\nđịa bang" : "Chúc\nmay mắn!", "Cửa hàng\nbang", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (select == 0 && this.mapId != 153) {
            ChangeMapService.gI().changeMapBySpaceShip(player, 153, -1, -1);
        } else if (select == 1) {
            ShopService.gI().opendShop(player, "SHOP_CLAN", false);
            ClanService.gI().sendClanBox(player);
        }
    }
}
