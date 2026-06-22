package nro.models.boss.Boss_mini;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import static nro.models.consts.BossType.ANTROM;
import nro.models.consts.ConstPlayer;
import nro.models.consts.ConstTaskBadges;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.services.PlayerService;
import nro.models.skill.Skill;
import nro.models.task.BadgesTaskService;
import nro.models.utils.Util;

public class AnTrom extends Boss {

    private long goldAnTrom;
    private long lastTimeAnTrom;
    private long lastTimeJoinMap;
    private static final long timeChangeMap = 1000;

    public AnTrom() throws Exception {
        super(BossID.AN_TROM, new BossData(
                "Ăn Cướp ",
                ConstPlayer.TRAI_DAT,
                new short[]{201, 202, 203, -1, -1, -1},
                1,
                new int[]{100},
                new int[]{79, 80, 81, 82, 83, 84, 92, 93, 94, 96, 97, 98, 99, 100, 102, 103, 104, 105, 106, 107, 108, 109, 110}, //map join
                new int[][]{
                    {Skill.THAI_DUONG_HA_SAN, 1, 5000}},
                new String[]{}, //text chat 1
                new String[]{}, //text chat 2
                new String[]{}, //text chat 3
                3600));

    }

    @Override
    public Zone getMapJoin() {
        int mapId = this.data[this.currentLevel].getMapJoin()[Util.nextInt(0, this.data[this.currentLevel].getMapJoin().length - 1)];
        return MapService.gI().getMapById(mapId).zones.get(0);
    }

    @Override
    public Player getPlayerAttack() {
        return super.getPlayerAttack();
    }

    @Override
    public int injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            damage = 1;
            this.nPoint.subHP(damage);
            if (isDie()) {
                this.setDie(plAtt);
                die(plAtt);
            }
            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            SkillService.gI().useSkill(this, plAtt, null, -1, null);
            return (int) damage;
        } else {
            return 0;
        }
    }

    @Override
    public void attack() {
        if (Util.canDoWithTime(this.lastTimeAttack, 100) && this.typePk == ConstPlayer.PK_ALL) {
            this.lastTimeAttack = System.currentTimeMillis();
            try {
                Player pl = this.getPlayerAttack();
                if (pl == null || pl.isDie()) {
                    return;
                }

                if (Util.getDistance(this, pl) <= 40) {
                    if (!Util.canDoWithTime(this.lastTimeAnTrom, 500) || goldAnTrom > 10_000_000_000L) {
                        return;
                    }
                    int gold = 0;
                    if (pl.isPl()) {
                        if (pl.inventory.gold >= 2000000) {
                            gold = Util.nextInt(200000, 1000000);
                        } else if (pl.inventory.gold >= 1000000000) {
                            gold = Util.nextInt(4000, 5000);

                        } else if (pl.inventory.gold >= 1000000) {
                            gold = Util.nextInt(1000, 2000);
                        }
                        this.chat("Haha đã trộm được " + Util.numberToMoney(goldAnTrom) + " Vàng");

                        if (gold > 0) {
                            pl.inventory.gold -= gold;
                            goldAnTrom += gold;
                            Service.gI().stealMoney(pl, -gold);
                            ItemMap itemMap = new ItemMap(this.zone, 190, gold, (this.location.x + pl.location.x) / 2, this.location.y, this.id);
                            Service.gI().dropItemMap(this.zone, itemMap);
                            Service.gI().sendToAntherMePickItem(this, itemMap.itemMapId);
                            this.zone.removeItemMap(itemMap);
                            this.lastTimeAnTrom = System.currentTimeMillis();

                        }
                    }
                } else {
                    if (Util.isTrue(1, 2)) {
                        this.moveToPlayer(pl);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void moveTo(int x, int y) {
        byte dir = (byte) (this.location.x - x < 0 ? 1 : -1);
        byte move = (byte) Util.nextInt(30, 40);
        PlayerService.gI().playerMove(this, this.location.x + (dir == 1 ? move : -move), y);
    }

    // @Override
    // public void die(Player plKill) {
    //     BadgesTaskService.updateCountBagesTask(plKill, ConstTaskBadges.BI_MOC_SACH_TUI, 1);
    //     this.reward(plKill);
    //     this.changeStatus(BossStatus.DIE);
    // }

  @Override
public void reward(Player plKill) {

    if (plKill == null) return;

    // Random 50% - 50%
    if (Util.isTrue(1, 2)) {

        // ===== 50% RƠI 10 NGỌC XANH (ID 77) - AI CŨNG NHẶT ĐƯỢC =====
        for (int i = 0; i < 10; i++) {

            int x = this.location.x + Util.nextInt(-50, 50);
            int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

            ItemMap item77 = new ItemMap(
                    this.zone,
                    77,
                    1,
                    x,
                    y,
                    -1 // <-- ai cũng nhặt được
            );

            Service.gI().dropItemMap(this.zone, item77);
        }

        Service.gI().sendThongBao(plKill, "Boss rơi 50 Ngọc Xanh!");

    } else {

        // ===== 50% RƠI 190 SỐ LƯỢNG 200.000 - CHỈ NGƯỜI KILL NHẶT =====
        int xGold = this.location.x + Util.nextInt(-20, 20);
        int yGold = this.zone.map.yPhysicInTop(xGold, this.location.y - 24);

        ItemMap itemGold = new ItemMap(
                this.zone,
                1507,
                1,
                xGold,
                yGold,
                plKill.id // <-- chỉ người hạ boss nhặt được
        );

        Service.gI().dropItemMap(this.zone, itemGold);

        Service.gI().sendThongBaoOK(plKill, "Bạn nhận được cái nịt!");
    }

    // +1 lượt quay
    plKill.luckyRoundPoint += 1;
    Service.gI().sendThongBao(plKill, "+1 lần Quay Tay");
}
    private long st;

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        this.attack();
        if (Util.canDoWithTime(st, 900000)) {
            this.changeStatus(BossStatus.LEAVE_MAP);
        }
    }

   @Override
public void joinMap() {

    int hpRandom = Util.nextInt(150, 200);

    this.name = "Ăn Trộm";
    this.nPoint.hpMax = hpRandom;
    this.nPoint.hp = this.nPoint.hpMax;
    this.nPoint.dameg = hpRandom / 10;

    goldAnTrom = 0;
    this.joinMap2();
    st = System.currentTimeMillis();
}

    public void joinMap2() {
        if (this.zone == null) {
            if (this.parentBoss != null) {
                this.zone = parentBoss.zone;
            } else if (this.lastZone == null) {
                this.zone = getMapJoin();
            } else {
                this.zone = this.lastZone;
            }
        }
        if (this.zone != null) {
            try {
                int zoneid = 0;
                this.zone = this.zone.map.zones.get(zoneid);
                ChangeMapService.gI().changeMap(this, this.zone, -1, -1);

                this.changeStatus(BossStatus.CHAT_S);
            } catch (Exception e) {
                this.changeStatus(BossStatus.REST);
            }
        } else {
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void leaveMap() {
        ChangeMapService.gI().exitMap(this);
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }

}
