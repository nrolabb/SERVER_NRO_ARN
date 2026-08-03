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

    // Boss type constants
    public static final int COOLER = 0;
    public static final int GOLDEN_FRIEZA = 1;
    public static final int ANIRAZA = 2;
    public static final int GANOS = 3;
    public static final int CAWAY = 4;
    public static final int PIRIMA = 5;
    public static final int SAONEL = 6;

    protected final ClanDungeon clanDungeon;
    protected final int type;

    protected ClanDungeonBoss(ClanDungeon clanDungeon, int id, int type, BossData... data) throws Exception {
        super(BossType.PHOBAN, id, true, true, data);
        this.clanDungeon = clanDungeon;
        this.type = type;
        this.secondsRest = 60;
    }

    public int getType() {
        return this.type;
    }

    // Factory methods for existing bosses (Fize vàng, Colder - map 185 phase 1)
    public static ClanDungeonBoss cooler(ClanDungeon clanDungeon, int id) throws Exception {
        return new CoolerClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss goldenFrieza(ClanDungeon clanDungeon, int id) throws Exception {
        return new GoldenFriezaClanDungeonBoss(clanDungeon, id);
    }

    // Factory methods for new bosses
    public static ClanDungeonBoss aniraza(ClanDungeon clanDungeon, int id) throws Exception {
        return new AnirazaClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss ganos(ClanDungeon clanDungeon, int id) throws Exception {
        return new GanosClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss caway(ClanDungeon clanDungeon, int id) throws Exception {
        return new CawayClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss pirima(ClanDungeon clanDungeon, int id) throws Exception {
        return new PirimaClanDungeonBoss(clanDungeon, id);
    }

    public static ClanDungeonBoss saonel(ClanDungeon clanDungeon, int id) throws Exception {
        return new SaonelClanDungeonBoss(clanDungeon, id);
    }

    protected static BossData cloneData(BossData data) {
        return new BossData(data.getName(), data.getGender(), data.getOutfit(), data.getDame(), data.getHp(),
                data.getMapJoin(), data.getSkillTemp(), data.getTextS(), data.getTextM(), data.getTextE(), 60);
    }

    /**
     * Capsule reward per boss type
     */
    protected int getCapsuleReward() {
        return switch (this.type) {
            case ANIRAZA -> 20;
            case GANOS, CAWAY -> 50;
            case COOLER, GOLDEN_FRIEZA -> 100;
            case PIRIMA, SAONEL -> 150;
            default -> 20;
        };
    }

    @Override
    public void reward(Player plKill) {
        dropCommonBossReward(plKill);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
        if (clanDungeon != null) {
            int capsule = getCapsuleReward();
            clanDungeon.addPoint(capsule);
            clanDungeon.onBossKilled(this);
        }
    }

    private void dropCommonBossReward(Player plKill) {
        int x = this.location.x;
        int y = this.zone.map.yPhysicInTop(x, this.location.y - 24);

        if (Util.isTrue(30, 100)) {
            ItemMap it = ItemService.gI().randDoTLBoss(this.zone, 1, x, y, plKill.id);
            if (it != null) {
                Service.gI().dropItemMap(zone, it);
            }
            return;
        }

        if (Util.isTrue(80, 100)) {
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

    @Override
    public void rest() {
        // No respawn for clan dungeon bosses in the new system
        // Boss stays dead until dungeon ends
    }

    @Override
    public void autoLeaveMap() {
    }

    @Override
    public void leaveMap() {
        if (this.zone != null) {
            nro.models.map.service.ChangeMapService.gI().exitMap(this);
        }
        this.lastZone = null;
        this.lastTimeRest = System.currentTimeMillis();
        this.changeStatus(BossStatus.REST);
    }
}
