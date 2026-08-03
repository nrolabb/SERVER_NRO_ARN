package nro.models.map.phoban;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Data;
import nro.models.boss.Boss;
import nro.models.boss.Boss_Manager.OtherBossManager;
import nro.models.boss.phoban.ClanDungeonBoss;
import nro.models.clan.Clan;
import nro.models.map.ItemMap;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.ItemMapService;
import nro.models.map.service.MapService;
import nro.models.mob.Mob;
import nro.models.player.Player;
import nro.models.server.Maintenance;
import nro.models.services.ItemTimeService;
import nro.models.services.Service;
import nro.models.utils.Functions;
import nro.models.utils.Util;

@Data
public class ClanDungeon implements Runnable {

    public static final int AVAILABLE = 10;
    public static final int N_PLAYER_MAP = 1; // số người cần đứng cùng nhau trong map 153
    public static final int TIME_CLAN_DUNGEON = 1_800_000;
    public static final int COUNTDOWN_TIME = 30_000;

    public static final int MAP_START = 183;
    public static final int MAP_END = 185;

    public static final int MAP_1 = 183;
    public static final int MAP_2 = 184;
    public static final int MAP_3 = 185;

    public static final int THRESHOLD_MAP_1 = 100;
    public static final int THRESHOLD_MAP_2 = 500;

    private final int id;
    private final List<Zone> zones;
    private Clan clan;
    private long lastTimeOpen;
    private boolean opened;
    private int point;

    // Map clear flags (mobs cleared, boss spawned)
    private boolean map183BossSpawned;
    private boolean map184BossSpawned;

    // Boss death flags
    private boolean anirazaDead;
    private boolean ganosDead;
    private boolean cawayDead;
    private boolean coolerDead;
    private boolean goldenFriezaDead;
    private boolean pirimaDead;
    private boolean saonelDead;

    // Map 185 phase tracking
    private boolean map185Phase2Spawned;

    // Countdown after all bosses dead in map 185
    private boolean countdownStarted;
    private long countdownStartTime;

    private final List<Boss> bosses = new ArrayList<>();
    private final List<MobState> mobStates = new ArrayList<>();

    public ClanDungeon(int id) {
        this.id = id;
        this.zones = new ArrayList<>();
    }

    public void addZone(Zone zone) {
        this.zones.add(zone);
    }

    public Zone getMapById(int mapId) {
        for (Zone zone : zones) {
            if (zone.map.mapId == mapId) {
                return zone;
            }
        }
        return null;
    }

    public void open(Player player) {
        if (player == null || player.clan == null || player.zone == null) {
            return;
        }
        this.lastTimeOpen = System.currentTimeMillis();
        this.clan = player.clan;
        this.opened = true;
        this.point = 0;

        // Reset all flags
        this.map183BossSpawned = false;
        this.map184BossSpawned = false;
        this.anirazaDead = false;
        this.ganosDead = false;
        this.cawayDead = false;
        this.coolerDead = false;
        this.goldenFriezaDead = false;
        this.pirimaDead = false;
        this.saonelDead = false;
        this.map185Phase2Spawned = false;
        this.countdownStarted = false;

        player.clan.clanDungeon = this;
        player.clan.markOpenClanDungeon(player, this.lastTimeOpen);
        player.clan.update();

        init();
        moveClanMembersToDungeon(player);
        sendTextClanDungeon();
        new Thread(this, "Pho ban bang hoi: " + this.clan.name).start();
    }

    private void moveClanMembersToDungeon(Player opener) {
        List<Player> players = new ArrayList<>();
        Zone startZone = getMapById(MAP_START);
        if (startZone == null) {
            return;
        }
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.clan != null && pl.clan.equals(opener.clan) && pl.zone != null && !pl.isDie()
                    && (pl.zone.equals(opener.zone) || MapService.gI().isMapClanDungeon(pl.zone.map.mapId))) {
                players.add(pl);
            }
        }
        for (Player pl : players) {
            ChangeMapService.gI().changeMapInYard(pl, startZone, 100);
        }
    }

    private void init() {
        saveMobStates();
        long totalDamage = 0;
        long totalHp = 0;
        for (Player player : this.clan.membersInGame) {
            if (player != null && player.nPoint != null) {
                totalDamage += player.nPoint.dame;
                totalHp += player.nPoint.hpMax;
            }
        }
        int mobDame = (int) Math.min(Math.max(totalHp / 20, 1), 200_000_000L);
        int mobHp = (int) Math.min(Math.max(totalDamage * 10, 1), 2_000_000_000L);

        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                mob.point.dame = mobDame;
                mob.point.maxHp = mobHp;
                mob.point.hp = mob.point.maxHp;
                mob.lvMob = 0;
                mob.hoiSinhMobPhoBan();
            }
        }
        // Spawn map 185 phase 1 bosses immediately (Fize vàng + Colder)
        spawnMap185Phase1Bosses();
    }

    private void saveMobStates() {
        mobStates.clear();
        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                mobStates.add(new MobState(mob));
            }
        }
    }

    private void spawnMap185Phase1Bosses() {
        try {
            Zone map185 = getMapById(MAP_3);
            if (map185 != null) {
                addBoss(ClanDungeonBoss.goldenFrieza(this, nextBossId(0)), map185);
                addBoss(ClanDungeonBoss.cooler(this, nextBossId(1)), map185);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnAniraza() {
        try {
            Zone map183 = getMapById(MAP_1);
            if (map183 != null) {
                addBoss(ClanDungeonBoss.aniraza(this, nextBossId(2)), map183);
                map183BossSpawned = true;
                notifyBossAppear("Aniraza", MAP_1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnMap184Bosses() {
        try {
            Zone map184 = getMapById(MAP_2);
            if (map184 != null) {
                addBoss(ClanDungeonBoss.ganos(this, nextBossId(3)), map184);
                addBoss(ClanDungeonBoss.caway(this, nextBossId(4)), map184);
                map184BossSpawned = true;
                notifyBossAppear("Ganos và Caway", MAP_2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void spawnMap185Phase2Bosses() {
        try {
            Zone map185 = getMapById(MAP_3);
            if (map185 != null) {
                addBoss(ClanDungeonBoss.pirima(this, nextBossId(5)), map185);
                addBoss(ClanDungeonBoss.saonel(this, nextBossId(6)), map185);
                map185Phase2Spawned = true;
                notifyBossAppear("Pirima và Saonel", MAP_3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int nextBossId(int type) {
        return -500_000 - (this.id * 10) - type;
    }

    private void addBoss(Boss boss, Zone zone) {
        boss.zoneFinal = zone;
        boss.changeStatus(nro.models.consts.BossStatus.RESPAWN);
        bosses.add(boss);
    }

    public synchronized void addPoint(int pointAdd) {
        if (!opened || clan == null) {
            return;
        }
        this.point += pointAdd;
        this.clan.capsuleClan += pointAdd;
        this.clan.sendMyClanForAllMember();
        this.clan.update();
        sendTextClanDungeon();
        checkClearByPoint();
    }

    private void checkClearByPoint() {
        // Map 183: at 100 points, clear mobs and spawn Aniraza
        if (!map183BossSpawned && point >= THRESHOLD_MAP_1) {
            Zone map183 = getMapById(MAP_1);
            if (map183 != null) {
                clearMobs(map183);
                spawnAniraza();
                notifyThreshold(THRESHOLD_MAP_1, MAP_1);
            }
        }
        // Map 184: at 500 points, clear mobs and spawn Ganos + Caway
        if (!map184BossSpawned && point >= THRESHOLD_MAP_2) {
            Zone map184 = getMapById(MAP_2);
            if (map184 != null) {
                clearMobs(map184);
                spawnMap184Bosses();
                notifyThreshold(THRESHOLD_MAP_2, MAP_2);
            }
        }
    }

    /**
     * Called when a clan dungeon boss is killed
     */
    public synchronized void onBossKilled(ClanDungeonBoss boss) {
        if (!opened || clan == null) {
            return;
        }
        int bossType = boss.getType();
        switch (bossType) {
            case ClanDungeonBoss.ANIRAZA:
                anirazaDead = true;
                notifyMapUnlocked(MAP_2);
                break;
            case ClanDungeonBoss.GANOS:
                ganosDead = true;
                if (cawayDead) {
                    notifyMapUnlocked(MAP_3);
                }
                break;
            case ClanDungeonBoss.CAWAY:
                cawayDead = true;
                if (ganosDead) {
                    notifyMapUnlocked(MAP_3);
                }
                break;
            case ClanDungeonBoss.GOLDEN_FRIEZA:
                goldenFriezaDead = true;
                if (coolerDead && !map185Phase2Spawned) {
                    spawnMap185Phase2Bosses();
                }
                break;
            case ClanDungeonBoss.COOLER:
                coolerDead = true;
                if (goldenFriezaDead && !map185Phase2Spawned) {
                    spawnMap185Phase2Bosses();
                }
                break;
            case ClanDungeonBoss.PIRIMA:
                pirimaDead = true;
                if (saonelDead) {
                    startCountdown();
                }
                break;
            case ClanDungeonBoss.SAONEL:
                saonelDead = true;
                if (pirimaDead) {
                    startCountdown();
                }
                break;
        }
    }

    /**
     * Check if player can access map 184 (Aniraza must be dead)
     */
    public boolean canAccessMap184() {
        return anirazaDead;
    }

    /**
     * Check if player can access map 185 (Ganos + Caway must be dead)
     */
    public boolean canAccessMap185() {
        return ganosDead && cawayDead;
    }

    private void startCountdown() {
        if (countdownStarted) {
            return;
        }
        countdownStarted = true;
        countdownStartTime = System.currentTimeMillis();
        // Notify all players in dungeon
        if (clan == null) {
            return;
        }
        String text = "Tất cả boss đã bị tiêu diệt!\nBạn sẽ được đưa về lãnh địa bang sau 30 giây.";
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.zone != null && MapService.gI().isMapClanDungeon(pl.zone.map.mapId)) {
                Service.gI().sendBigMessage(pl, 1139, text);
            }
        }
    }

    private void notifyBossAppear(String bossName, int mapId) {
        if (clan == null) {
            return;
        }
        String text = "Boss " + bossName + " đã xuất hiện tại map " + mapId + "!";
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.zone != null && MapService.gI().isMapClanDungeon(pl.zone.map.mapId)) {
                Service.gI().sendBigMessage(pl, 1139, text);
            }
        }
    }

    private void notifyThreshold(int requiredPoint, int mapId) {
        if (clan == null) {
            return;
        }
        String text = "Bang hội đã đạt " + requiredPoint + " điểm tích lũy.\n"
                + "Quái khu vực đã rút lui, boss đã xuất hiện tại map " + mapId + "!";
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.zone != null && MapService.gI().isMapClanDungeon(pl.zone.map.mapId)) {
                Service.gI().sendBigMessage(pl, 1139, text);
            }
        }
    }

    private void notifyMapUnlocked(int nextMapId) {
        if (clan == null) {
            return;
        }
        String text = "Boss đã bị tiêu diệt! Có thể qua map " + nextMapId + ".";
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.zone != null && MapService.gI().isMapClanDungeon(pl.zone.map.mapId)) {
                Service.gI().sendBigMessage(pl, 1139, text);
            }
        }
    }

    private void clearMobs(Zone zone) {
        for (Mob mob : zone.mobs) {
            if (mob != null) {
                mob.point.maxHp = 0;
                if (!mob.isDie()) {
                    mob.startDie();
                } else {
                    mob.point.hp = -1;
                }
            }
        }
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && opened) {
            try {
                long startTime = System.currentTimeMillis();
                update();
                Functions.sleep(Math.max(150 - (System.currentTimeMillis() - startTime), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Check 30s countdown after all bosses killed in map 185
        if (countdownStarted && Util.canDoWithTime(countdownStartTime, COUNTDOWN_TIME)) {
            opened = false;
            finish();
            dispose();
            return;
        }
        // Check total dungeon time limit
        if (Util.canDoWithTime(lastTimeOpen, TIME_CLAN_DUNGEON)) {
            opened = false;
            finish();
            dispose();
        }
    }

    private void finish() {
        for (Zone zone : zones) {
            for (int i = zone.getPlayers().size() - 1; i >= 0; i--) {
                if (i < zone.getPlayers().size()) {
                    kickOut(zone.getPlayers().get(i));
                }
            }
        }
    }

    private void kickOut(Player player) {
        if (player != null && player.zone != null && MapService.gI().isMapClanDungeon(player.zone.map.mapId)) {
            if (countdownStarted) {
                Service.gI().sendThongBao(player, "Phó bản bang hội đã hoàn thành! Trở về lãnh địa bang.");
            } else {
                Service.gI().sendThongBao(player, "Đã hết thời gian phó bản bang hội");
            }
            ChangeMapService.gI().changeMapBySpaceShip(player, 153, -1, -1);
        }
    }

    public void sendTextClanDungeon() {
        if (clan == null) {
            return;
        }
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().sendTextClanDungeon(pl);
        }
    }

    private void removeTextClanDungeon() {
        if (clan == null) {
            return;
        }
        for (Player pl : this.clan.membersInGame) {
            ItemTimeService.gI().removeTextClanDungeon(pl);
        }
    }

    private void dispose() {
        removeTextClanDungeon();
        for (Boss boss : bosses) {
            if (boss != null) {
                if (boss.zone != null) {
                    boss.leaveMap();
                }
                OtherBossManager.gI().removeBoss(boss);
            }
        }
        for (Zone zone : zones) {
            for (int i = zone.items.size() - 1; i >= 0; i--) {
                ItemMap itemMap = zone.items.get(i);
                ItemMapService.gI().removeItemMap(itemMap);
            }
        }
        restoreMobStates();
        bosses.clear();
        if (clan != null) {
            clan.clanDungeon = null;
            clan.update();
        }
        clan = null;
        point = 0;
        opened = false;
    }

    private void restoreMobStates() {
        for (MobState state : mobStates) {
            state.restore();
        }
        mobStates.clear();
    }

    private static class MobState {

        private final Mob mob;
        private final int dame;
        private final int maxHp;
        private final int hp;
        private final int lvMob;
        private final int status;

        private MobState(Mob mob) {
            this.mob = mob;
            this.dame = mob.point.dame;
            this.maxHp = mob.point.maxHp;
            this.hp = mob.point.hp;
            this.lvMob = mob.lvMob;
            this.status = mob.status;
        }

        private void restore() {
            mob.point.dame = dame;
            mob.point.maxHp = maxHp;
            mob.point.hp = hp > 0 ? hp : maxHp;
            mob.lvMob = lvMob;
            mob.status = status;
            if (mob.point.hp > 0) {
                mob.hoiSinhMobPhoBan();
            }
        }
    }
}
