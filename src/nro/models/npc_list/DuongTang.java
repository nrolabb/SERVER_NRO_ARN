package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.shop.ShopService;
import nro.models.utils.Util;
import java.util.Timer;
import java.util.TimerTask;

public class DuongTang extends Npc {

    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (this.mapId == 122) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi muốn gì ở ta?", "Cửa hàng", "Về\nVách núi Aru",
                        "Đóng");
            } else if (this.mapId == 123) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi muốn gì ở ta?", "Về\nVách núi Aru", "Đóng");
            } else {
                if (player.nPoint.power >= 5_000_000_000L) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi có muốn đến Ngũ Hành Sơn không?",
                            "Đến\nNgũ Hành Sơn", "Đóng");
                } else {
                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Con cần đạt 5 tỷ sức mạnh mới có thể đến Ngũ Hành Sơn.", "Đóng");
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.getIndexMenu() == ConstNpc.BASE_MENU) {
                if (this.mapId == 122) {
                    if (select == 0) {
                        ShopService.gI().opendShop(player, "DUONG_TANG", true);
                    } else if (select == 1) {
                        ChangeMapService.gI().changeMapNonSpaceship(player, 42, 159, 228);
                    }
                } else if (this.mapId == 123) {
                    if (select == 0) {
                        ChangeMapService.gI().changeMapNonSpaceship(player, 42, 159, 228);
                    }
                } else {
                    if (select == 0) {
                        if (player.nPoint.power >= 5_000_000_000L) {
                            ChangeMapService.gI().changeMapNonSpaceship(player, 123, 103, 384);
                        } else {
                            Service.gI().sendThongBao(player, "Con cần đạt 5 tỷ sức mạnh mới có thể đến Ngũ Hành Sơn.");
                        }
                    }
                }
            }
        }
    }
}
