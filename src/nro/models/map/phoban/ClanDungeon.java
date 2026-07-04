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

    public static final int NORMAL_AVAILABLE = 5;
    public static final int AVAILABLE = 10;
    public static final int N_PLAYER_MAP = 1;
    public static final int TIME_CLAN_DUNGEON = 1_800_000;

    public static final int MAP_START = 156;
    public static final int MAP_END = 159;

    private final int id;
    private final List<Zone> zones;
    private Clan clan;
    private long lastTimeOpen;
    private boolean opened;
    private int point;
    private boolean clearedMap156;
    private boolean clearedMap157;
    private boolean clearedMap158;
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
        this.clearedMap156 = false;
        this.clearedMap157 = false;
        this.clearedMap158 = false;
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
        spawnBosses();
    }

    private void saveMobStates() {
        mobStates.clear();
        for (Zone zone : this.zones) {
            for (Mob mob : zone.mobs) {
                mobStates.add(new MobState(mob));
            }
        }
    }

    private void spawnBosses() {
        try {
            Zone map158 = getMapById(158);
            Zone map159 = getMapById(159);
            if (map158 != null) {
                addBoss(ClanDungeonBoss.cooler(this, nextBossId(0)), map158);
                addBoss(ClanDungeonBoss.goldenFrieza(this, nextBossId(1)), map158);
            }
            if (map159 != null) {
                addBoss(ClanDungeonBoss.cumber(this, nextBossId(2)), map159);
                addBoss(ClanDungeonBoss.sieuBoHung(this, nextBossId(3)), map159);
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
        if (!clearedMap156 && point >= 200) {
            clearMapByPoint(156, 200);
            clearedMap156 = true;
        }
        if (!clearedMap157 && point >= 500) {
            clearMapByPoint(157, 500);
            clearedMap157 = true;
        }
        if (!clearedMap158 && point >= 1000) {
            clearMapByPoint(158, 1000);
            clearedMap158 = true;
        }
    }

    private void clearMapByPoint(int mapId, int requiredPoint) {
        Zone zone = getMapById(mapId);
        if (zone == null) {
            return;
        }
        clearMobs(zone);
        clearBosses(zone);
        notifyNextMap(requiredPoint, mapId + 1);
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

    private void clearBosses(Zone zone) {
        for (Iterator<Boss> it = bosses.iterator(); it.hasNext();) {
            Boss boss = it.next();
            if (boss != null && boss.zone != null && boss.zone.equals(zone)) {
                boss.leaveMap();
                OtherBossManager.gI().removeBoss(boss);
                it.remove();
            }
        }
    }

    private void notifyNextMap(int requiredPoint, int nextMapId) {
        if (clan == null) {
            return;
        }
        String text = "Bang hội đã đạt " + requiredPoint + " điểm tích lũy.\n"
                + "Quái và boss khu vực này đã rút lui, hãy sang map " + nextMapId + ".";
        for (Player pl : this.clan.membersInGame) {
            if (pl != null && pl.zone != null && MapService.gI().isMapClanDungeon(pl.zone.map.mapId)) {
                Service.gI().sendBigMessage(pl, 1139, text);
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
            Service.gI().sendThongBao(player, "Đã hết thời gian phó bản bang hội");
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
