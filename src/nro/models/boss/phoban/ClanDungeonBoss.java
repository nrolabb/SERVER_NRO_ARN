package nro.models.boss.phoban;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.consts.BossStatus;
import nro.models.consts.BossType;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.map.phoban.ClanDungeon;
import nro.models.player.Player;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.utils.Util;

public abstract class ClanDungeonBoss extends Boss {

    public static final int COOLER = 0;
    public static final int GOLDEN_FRIEZA = 1;
    public static final int CUMBER = 2;
    public static final int SIEU_BO_HUNG = 3;
    private static final int FINAL_BOSS_RESPAWN_TIME = 180_000;

    protected final ClanDungeon clanDungeon;
    protected final int type;
    private int nextFinalBossHp;
    private int nextFinalBossKi;
    private int nextFinalBossDame;
    private boolean notifiedAfterDeath;
    private boolean notifiedTwoMinutes;
    private boolean notifiedOneMinute;
    private boolean notifiedTenSeconds;

    protected ClanDungeonBoss(ClanDungeon clanDungeon, int id, int type, BossData... data) throws Exception {
        super(BossType.PHOBAN, id, true, true, data);
        this.clanDungeon = clanDungeon;
        this.type = type;
        this.secondsRest = 60;
    }

    public static ClanDungeonBoss cooler(ClanDungeon clanDungeon, int id) throws Exception {
        return new CoolerClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss goldenFrieza(ClanDungeon clanDungeon, int id) throws Exception {
        return new GoldenFriezaClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss cumber(ClanDungeon clanDungeon, int id) throws Exception {
        return new CumberClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss sieuBoHung(ClanDungeon clanDungeon, int id) throws Exception {
        return new SieuBoHungClanDungeonBoss(clanDungeon, id);
    }

    protected static BossData cloneData(BossData data) {
        return new BossData(data.getName(), data.getGender(), data.getOutfit(), data.getDame(), data.getHp(),
                data.getMapJoin(), data.getSkillTemp(), data.getTextS(), data.getTextM(), data.getTextE(), 60);
    }

    @Override
    public void initBase() {
        super.initBase();
        if (!isFinalMapBoss() || nextFinalBossHp <= 0) {
            return;
        }
        this.nPoint.hpg = nextFinalBossHp;
        this.nPoint.mpg = nextFinalBossKi;
        this.nPoint.dameg = nextFinalBossDame;
        this.nPoint.calPoint();
        this.nPoint.hp = this.nPoint.hpMax;
        this.nPoint.mp = this.nPoint.mpMax;
    }

    @Override
    public void reward(Player plKill) {
        if (this.type == GOLDEN_FRIEZA) {
            dropGoldenFriezaReward(plKill);
        } else {
            dropCommonBossReward(plKill, this.type == SIEU_BO_HUNG ? 20 : 100);
        }
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (clanDungeon != null) {
            clanDungeon.addPoint(20);
        }
    }

    private void dropGoldenFriezaReward(Player plKill) {
        plKill.event.addEventPoint(5);
        Service.gI().sendThongBao(plKill, "+5 Point");
        ItemMap item = new ItemMap(zone, 629, 1, this.location.x + Util.nextInt(-50, 50),
                this.zone.map.yPhysicInTop(this.location.x, this.location.y - 24), plKill.id);
        item.options.add(new Item.ItemOption(30, 1));
        item.options.add(new Item.ItemOption(50, 20));
        item.options.add(new Item.ItemOption(77, 20));
        item.options.add(new Item.ItemOption(103, 20));
        item.options.add(new Item.ItemOption(93, 20));
        Service.gI().dropItemMap(this.zone, item);
    }

    private void dropCommonBossReward(Player plKill, int percentDropMain) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        if (this.type != SIEU_BO_HUNG && Util.isTrue(30, 100)) {
            ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x, y, plKill.id);
            if (it != null) {
                Service.gI().dropItemMap(zone, it);
            }
            return;
        }

        if (Util.isTrue(percentDropMain, 100)) {
            int[] dropItems = {
                241, 253, 265, 277,
                233, 245, 257, 269,
                237, 249, 261, 273,
                281
            };
            int itemId = dropItems[Util.nextInt(dropItems.length)];
            ItemMap it = new ItemMap(zone, itemId, 1, x, y, plKill.id);
            it.options.add(new Item.ItemOption(107, randomStar()));

            switch (itemId) {
                case 241:
                case 233:
                case 237:
                    it.options.add(new Item.ItemOption(47, Util.nextInt(400, 550)));
                    break;
                case 253:
                case 245:
                case 249:
                    it.options.add(new Item.ItemOption(6, Util.nextInt(22000, 27000)));
                    it.options.add(new Item.ItemOption(27, Util.nextInt(3000, 5000)));
                    break;
                case 265:
                case 261:
                case 257:
                    it.options.add(new Item.ItemOption(0, Util.nextInt(2100, 2400)));
                    break;
                case 277:
                case 269:
                case 273:
                    it.options.add(new Item.ItemOption(7, Util.nextInt(22000, 26000)));
                    it.options.add(new Item.ItemOption(28, Util.nextInt(4000, 6000)));
                    break;
                case 281:
                    it.options.add(new Item.ItemOption(14, Util.nextInt(11, 13)));
                    break;
            }
            Service.gI().dropItemMap(zone, it);
        }

        if (this.type == SIEU_BO_HUNG && Util.isTrue(20, 100)) {
            int itemId = Util.isTrue(50, 100) ? 16 : 17;
            Service.gI().dropItemMap(zone, new ItemMap(zone, itemId, 1, x + 10, y, plKill.id));
        }
    }

    private int randomStar() {
        int rd = Util.nextInt(100);
        if (rd < 70) {
            return 0;
        }
        if (rd < 95) {
            return 1;
        }
        return 2;
    }

    private boolean isFinalMapBoss() {
        return this.type == CUMBER || this.type == SIEU_BO_HUNG;
    }

    private int scaleNextFinalBossStat(int value) {
        long scaled = value + (value / 2L);
        return (int) Math.max(1, Math.min(Integer.MAX_VALUE, scaled));
    }

    private boolean canRespawnFinalBoss() {
        return clanDungeon != null && clanDungeon.isOpened() && isFinalMapBoss();
    }

    private void resetFinalBossRespawnNotifies() {
        notifiedAfterDeath = false;
        notifiedTwoMinutes = false;
        notifiedOneMinute = false;
        notifiedTenSeconds = false;
    }

    private void notifyFinalBossRespawn(String timeLeft) {
        if (clanDungeon == null || clanDungeon.getClan() == null) {
            return;
        }
        String text = this.name + " sẽ xuất hiện sau " + timeLeft + ".";
        for (Player pl : clanDungeon.getClan().membersInGame) {
            if (pl != null && pl.zone != null && pl.zone.map.mapId == ClanDungeon.MAP_END) {
                Service.gI().sendBigMessage(pl, 1139, text);
            }
        }
    }

    @Override
    public void rest() {
        if (!canRespawnFinalBoss()) {
            super.rest();
            return;
        }
        long elapsed = System.currentTimeMillis() - this.lastTimeRest;
        long timeLeft = FINAL_BOSS_RESPAWN_TIME - elapsed;
        if (!notifiedAfterDeath) {
            notifyFinalBossRespawn("3 phút");
            notifiedAfterDeath = true;
        }
        if (!notifiedTwoMinutes && timeLeft <= 120_000) {
            notifyFinalBossRespawn("2 phút");
            notifiedTwoMinutes = true;
        }
        if (!notifiedOneMinute && timeLeft <= 60_000) {
            notifyFinalBossRespawn("1 phút");
            notifiedOneMinute = true;
        }
        if (!notifiedTenSeconds && timeLeft <= 10_000) {
            notifyFinalBossRespawn("10 giây");
            notifiedTenSeconds = true;
        }
        if (elapsed >= FINAL_BOSS_RESPAWN_TIME) {
            resetFinalBossRespawnNotifies();
            this.changeStatus(BossStatus.RESPAWN);
        }
    }

    @Override
    public void autoLeaveMap() {
    }

    @Override
    public void leaveMap() {
        boolean respawnFinalBoss = canRespawnFinalBoss();
        if (respawnFinalBoss) {
            nextFinalBossHp = scaleNextFinalBossStat(this.nPoint.hpMax);
            nextFinalBossKi = scaleNextFinalBossStat(this.nPoint.mpMax);
            nextFinalBossDame = scaleNextFinalBossStat(this.nPoint.dame);
        }
        if (this.zone != null) {
            nro.models.map.service.ChangeMapService.gI().exitMap(this);
        }
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        if (respawnFinalBoss) {
            resetFinalBossRespawnNotifies();
        }
        this.changeStatus(BossStatus.REST);
    }
}
