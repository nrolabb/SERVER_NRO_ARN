package nro.models.boss;

import nro.models.boss.template.BossRewardTemplate;
import nro.models.boss.template.BossTemplate;
import nro.models.consts.BossType;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.services.TaskService;
import nro.models.utils.Util;

public class DynamicBoss extends Boss {

    private final BossTemplate template;
    private long spawnTime;

    public DynamicBoss(BossTemplate template) throws Exception {
        super(
                template.getId(),
                !template.isNotify(),
                template.isZone01Disabled(),
                template.toBossDataArray()
        );
        this.template = template;
    }

    public DynamicBoss(BossType bossType, BossTemplate template) throws Exception {
        super(
                bossType,
                template.getId(),
                !template.isNotify(),
                template.isZone01Disabled(),
                template.toBossDataArray()
        );
        this.template = template;
    }

    public BossTemplate getTemplate() {
        return this.template;
    }

    @Override
    public void joinMap() {
        super.joinMap();
        this.spawnTime = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (template != null && template.getDespawnTimeout() > 0) {
            if (Util.canDoWithTime(spawnTime, template.getDespawnTimeout() * 1000L)) {
                this.leaveMapNew();
            }
            if (this.zone != null && this.zone.getNumOfPlayers() > 0) {
                this.spawnTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void leaveMap() {
        super.leaveMap();
        if (this.data != null && this.currentLevel < this.data.length - 1) {
            ChangeMapService.gI().exitMap(this);
            this.zone = null;
            this.lastZone = null;
        }
    }

    @Override
    public void reward(Player plKill) {
        // 1. Kiểm tra hoàn thành nhiệm vụ
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);

        // 2. Trao thưởng theo danh sách quà cấu hình trong DB
        if (template != null && template.getRewards() != null && !template.getRewards().isEmpty()) {
            for (BossRewardTemplate r : template.getRewards()) {
                r.dropReward(this, plKill);
            }
        }
    }
}
