package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;

public class DuongTang extends Npc {

    public DuongTang(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (player.nPoint.power >= 5_000_000_000L) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ngươi có muốn đến Ngũ Hành Sơn không?", "Đến\nNgũ Hành Sơn", "Đóng");
            } else {
                this.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con cần đạt 5 tỷ sức mạnh mới có thể đến Ngũ Hành Sơn.", "Đóng");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.getIndexMenu() == ConstNpc.BASE_MENU) {
                if (select == 0) {
                    if (player.nPoint.power >= 5_000_000_000L) {
                        ChangeMapService.gI().changeMapNonSpaceship(player, 123, -1, -1);
                    } else {
                        Service.gI().sendThongBao(player, "Con cần đạt 5 tỷ sức mạnh mới có thể đến Ngũ Hành Sơn.");
                    }
                }
            }
        }
    }
}
