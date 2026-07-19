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

public class Shenron_Halloween_Service {

    private static Shenron_Halloween_Service instance;

    public static final short NGOC_RONG_1_SAO = ConstItem.BI_NGO_1_SAO;
    public static final short NGOC_RONG_2_SAO = ConstItem.BI_NGO_2_SAO;
    public static final short NGOC_RONG_3_SAO = ConstItem.BI_NGO_3_SAO;
    public static final short NGOC_RONG_4_SAO = ConstItem.BI_NGO_4_SAO;
    public static final short NGOC_RONG_5_SAO = ConstItem.BI_NGO_5_SAO;
    public static final short NGOC_RONG_6_SAO = ConstItem.BI_NGO_6_SAO;
    public static final short NGOC_RONG_7_SAO = ConstItem.BI_NGO_7_SAO;

    public static Shenron_Halloween_Service gI() {
        if (instance == null) {
            instance = new Shenron_Halloween_Service();
        }
        return instance;
    }

    public void openMenuSummonShenron(Player pl) {
        pl.idMark.setShenronType(1); // 1 = Halloween Dragon
        NpcService.gI().createMenuConMeo(pl, ConstNpc.SUMMON_SHENRON_HALLOWEEN_EVENT, -1, "Bạn có muốn gọi Rồng Bí Ngô không ?",
                "Đồng ý", "Từ chối");
    }

    public void summonShenron(Player player) {
        if (player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14) {
            if (checkShenronBall(player)) {
                if (player.isShenronAppear || player.shenronEvent != null) {
                    Service.gI().sendThongBao(player, "Không thể thực hiện");
                    return;
                }

                if (Util.canDoWithTime(player.lastTimeShenronAppeared, Shenron_Event.timeResummonShenron)) {
                    for (int i = NGOC_RONG_1_SAO; i <= NGOC_RONG_7_SAO; i++) {
                        try {
                            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, i), 1);
                        } catch (Exception ex) {
                        }
                    }
                    InventoryService.gI().sendItemBags(player);
                    Shenron_Event shenron = new Shenron_Event();
                    shenron.setPlayer(player);
                    Shenron_Manager.gI().add(shenron);
                    player.shenronEvent = shenron;
                    shenron.setZone(player.zone);
                    shenron.activeShenron(true, Shenron_Event.DRAGON_EVENT);
                    shenron.sendBlackGokuhesShenron();
                } else {
                    int timeLeft = (int) ((Shenron_Event.timeResummonShenron - (System.currentTimeMillis() - player.lastTimeShenronAppeared)) / 1000);
                    Service.gI().sendThongBao(player, "Vui lòng đợi " + (timeLeft < 7200 ? timeLeft + " giây" : timeLeft / 60 + " phút") + " nữa");
                }
            }
        } else {
            Service.gI().sendThongBao(player, "Không thể gọi rồng ở đây");
        }
    }

    private boolean checkShenronBall(Player pl) {
        for (int i = NGOC_RONG_1_SAO; i <= NGOC_RONG_7_SAO; i++) {
            if (!InventoryService.gI().isExistItemBag(pl, i)) {
                Item it = ItemService.gI().createNewItem((short) i);
                Service.gI().sendThongBao(pl, "Bạn còn thiếu 1 viên " + it.template.name);
                return false;
            }
        }
        return true;
    }
}
