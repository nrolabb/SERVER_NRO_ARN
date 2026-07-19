package nro.models.services.shenron;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.map.service.NpcService;
import nro.models.services.InventoryService;
import nro.models.utils.Util;
import nro.models.consts.ConstItem;

public class Shenron_Super_Service {

    private static Shenron_Super_Service instance;

    public static Shenron_Super_Service gI() {
        if (instance == null) {
            instance = new Shenron_Super_Service();
        }
        return instance;
    }

    public void openMenuSummonShenron(Player pl) {
        pl.idMark.setShenronType(2); // 2 = Super Dragon
        NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON_SUPER_EVENT, -1, "Bạn có muốn gọi Rồng Siêu Cấp không ?",
                "Đồng ý", "Từ chối");
    }

    public void summonShenron(Player player) {
        if (player.zone.map.mapId == 0 || player.zone.map.mapId == 7 || player.zone.map.mapId == 14) {
            if (checkShenronBall(player)) {
                if (player.isShenronAppear || player.shenronEvent != null) {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                    return;
                }

                if (Util.canDoWithTime(player.lastTimeShenronAppeared, Shenron_Event.timeResummonShenron)) {
                    // Xóa ngọc rồng siêu cấp
                    try {
                        InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, ConstItem.NGOC_RONG_SIEU_CAP), 1);
                    } catch (Exception ex) {}
                    
                    // Xóa 7 viên ngọc rồng thường
                    for (int i = 14; i <= 20; i++) {
                        try {
                            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, i), 1);
                        } catch (Exception ex) {}
                    }
                    InventoryService.gI().sendItemBags(player);
                    
                    Shenron_Event shenron = new Shenron_Event();
                    shenron.setPlayer(player);
                    Shenron_Manager.gI().add(shenron);
                    player.shenronEvent = shenron;
                    shenron.setZone(player.zone);
                    shenron.activeShenron(true, Shenron_Event.DRAGON_SUPER_EVENT);
                    shenron.sendBlackGokuhesShenron();
                } else {
                    int timeLeft = (int) ((Shenron_Event.timeResummonShenron - (System.currentTimeMillis() - player.lastTimeShenronAppeared)) / 1000);
                    Service.gI().sendThongBao(player, "Vui lòng đợi " + (timeLeft < 7200 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.gI().sendThongBao(player, "Chỉ có thể gọi rồng ở các map Làng (Aru, Mori, Kakarot)");
        }
    }

    private boolean checkShenronBall(Player pl) {
        if (!InventoryService.gI().isExistItemBag(pl, ConstItem.NGOC_RONG_SIEU_CAP)) {
            Service.gI().sendThongBao(pl, "Bạn còn thiếu Ngọc Rồng Siêu Cấp");
            return false;
        }
        for (int i = 14; i <= 20; i++) {
            if (!InventoryService.gI().isExistItemBag(pl, i)) {
                Item it = ItemService.gI().createNewItem((short) i);
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên " + it.template.name);
                return false;
            }
        }
        return true;
    }
}
