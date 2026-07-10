package nro.models.event.summer;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import static nro.models.consts.BossType.SUMMER_EVENT;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.ActivePointService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public abstract class SummerEventBoss extends Boss {

    public SummerEventBoss(int id, BossData data) throws Exception {
        super(SUMMER_EVENT, id, true, false, data);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        if (this.zone != null && this.zone.map != null) {
            Logger.successln("[SUMMER BOSS] " + this.name
                    + " sinh tai map " + this.zone.map.mapName
                    + " (ID: " + this.zone.map.mapId + ")"
                    + ", khu " + this.zone.zoneId
                    + ", toa do (" + this.location.x + ", " + this.location.y + ")");
        }
    }

    @Override
    public void reward(Player plKill) {
        ActivePointService.gI().addPoint(plKill, SummerEventManager.ACTIVE_POINT_BOSS_REWARD, "tiêu diệt boss hải tặc");
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        dropSummerItem(plKill);
    }

    private void dropSummerItem(Player plKill) {
        if (this.zone == null || plKill == null) {
            return;
        }
        SummerEventManager.DropReward reward = SummerEventManager.gI().randomBossDrop();
        if (reward == null) {
            return;
        }
        int x = this.location.x + Util.nextInt(-50, 50);
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
        ItemMap itemMap = new ItemMap(this.zone, reward.itemId, reward.quantity, x, y, plKill.id);
        for (int[] option : reward.options) {
            itemMap.options.add(new Item.ItemOption(option[0], option[1]));
        }
        Service.gI().dropItemMap(this.zone, itemMap);
    }
}
