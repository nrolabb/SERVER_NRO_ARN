package nro.models.event.summer;

import java.util.ArrayList;
import java.util.List;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.consts.BossStatus;
import static nro.models.consts.BossType.SUMMER_EVENT;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.MapService;
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
    public void active() {
        Player target = this.getPlayerAttack();
        if (target != null && !target.isDie() && Util.getDistance(this, target) > 120) {
            this.moveToPlayer(target);
        }
        super.active();
    }

    @Override
    public void joinMap() {
        Map map = MapService.gI().getMapById(SummerEventManager.MAP_HAI_TAC);
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            Logger.warningln("[SUMMER BOSS] Khong tim thay map " + SummerEventManager.MAP_HAI_TAC
                    + " de sinh " + this.name);
            this.changeStatus(BossStatus.RESPAWN);
            return;
        }

        List<Zone> availableZones = new ArrayList<>();
        for (Zone candidate : map.zones) {
            if (candidate.getBosses().isEmpty()) {
                availableZones.add(candidate);
            }
        }
        List<Zone> zonesToChoose = availableZones.isEmpty() ? map.zones : availableZones;
        Zone randomZone = zonesToChoose.get(Util.nextInt(0, zonesToChoose.size() - 1));

        this.joinMapByZone(randomZone);
        Service.gI().sendFlagBag(this);
        this.notifyJoinMap();
        this.changeStatus(BossStatus.CHAT_S);
        this.wakeupAnotherBossWhenAppear();

        Logger.successln("[SUMMER BOSS] " + this.name
                + " sinh tai map " + this.zone.map.mapName
                + " (ID: " + this.zone.map.mapId + ")"
                + ", khu " + this.zone.zoneId
                + ", toa do (" + this.location.x + ", " + this.location.y + ")");
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
        int dropCount = Util.nextInt(5, 10);
        for (int i = 0; i < dropCount; i++) {
            SummerEventManager.DropReward reward = SummerEventManager.gI().randomBossDrop();
            int x = this.location.x + Util.nextInt(-100, 100);
            int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);
            ItemMap itemMap = new ItemMap(this.zone, reward.itemId, reward.quantity, x, y, plKill.id);
            itemMap.setDropTiming(30000, 40000);
            for (int[] option : reward.options) {
                itemMap.options.add(new Item.ItemOption(option[0], option[1]));
            }
            Service.gI().dropItemMap(this.zone, itemMap);
        }
    }
}
