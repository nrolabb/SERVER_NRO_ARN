package nro.models.services_dungeon;

import java.util.ArrayList;
import java.util.List;
import nro.models.map.Map;
import nro.models.map.Zone;
import nro.models.map.service.MapService;
import nro.models.map.phoban.ClanDungeon;
import nro.models.player.Player;
import nro.models.services.Service;

public class ClanDungeonService {

    private static ClanDungeonService instance;

    public static ClanDungeonService gI() {
        if (instance == null) {
            instance = new ClanDungeonService();
        }
        return instance;
    }

    private final List<ClanDungeon> clanDungeons;

    private ClanDungeonService() {
        this.clanDungeons = new ArrayList<>();
        for (int i = 0; i < ClanDungeon.AVAILABLE; i++) {
            this.clanDungeons.add(new ClanDungeon(i));
        }
    }

    public void addMapClanDungeon(int id, Zone zone) {
        if (id >= 0 && id < this.clanDungeons.size()) {
            this.clanDungeons.get(id).addZone(zone);
        }
    }

    public int getMaxOpenedClanDungeon() {
        return this.clanDungeons.size();
    }

    public int getOpenedClanDungeonCount() {
        int count = 0;
        for (ClanDungeon dungeon : this.clanDungeons) {
            if (dungeon.isOpened()) {
                count++;
            }
        }
        return count;
    }

    public boolean isFullOpenedClanDungeon() {
        return getOpenedClanDungeonCount() >= getMaxOpenedClanDungeon();
    }

    public Zone getNormalZone(int mapId) {
        Zone fallback = null;
        Map map = MapService.gI().getMapById(mapId);
        if (map == null || map.zones == null) {
            return null;
        }
        for (Zone zone : map.zones) {
            if (!isNormalClanDungeonZone(zone)) {
                continue;
            }
            if (fallback == null) {
                fallback = zone;
            }
            if (zone.getNumOfPlayers() < zone.maxPlayer) {
                return zone;
            }
        }
        return fallback;
    }

    public boolean isNormalClanDungeonZone(Zone zone) {
        return zone != null && zone.map != null
                && zone.map.mapId >= ClanDungeon.MAP_START && zone.map.mapId <= ClanDungeon.MAP_END
                && zone.zoneId < ClanDungeon.NORMAL_AVAILABLE;
    }

    public boolean isDungeonInstanceZone(Zone zone) {
        return zone != null && zone.map != null
                && zone.map.mapId >= ClanDungeon.MAP_START && zone.map.mapId <= ClanDungeon.MAP_END
                && zone.zoneId >= ClanDungeon.NORMAL_AVAILABLE;
    }

    public boolean isOpenedDungeonZone(Zone zone) {
        if (zone == null || zone.map == null) {
            return false;
        }
        for (ClanDungeon dungeon : this.clanDungeons) {
            if (dungeon.isOpened() && dungeon.getMapById(zone.map.mapId) == zone) {
                return true;
            }
        }
        return false;
    }

    public void joinClanDungeon(Player player) {
        if (player.clan == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có bang hội");
            return;
        }
        if (player.clan.clanDungeon != null && player.clan.clanDungeon.isOpened()) {
            Zone startZone = player.clan.clanDungeon.getMapById(ClanDungeon.MAP_START);
            if (startZone != null) {
                nro.models.map.service.ChangeMapService.gI().changeMapInYard(player, startZone, 100);
            }
            player.clan.clanDungeon.sendTextClanDungeon();
            return;
        }
        if (!player.clan.canOpenClanDungeonToday()) {
            Service.gI().sendThongBao(player, "Bang hội đã đi phó bản bang hôm nay");
            return;
        }

        ClanDungeon clanDungeon = null;
        for (ClanDungeon dungeon : this.clanDungeons) {
            if (dungeon.getClan() == null && isEmptyDungeonZone(dungeon)) {
                clanDungeon = dungeon;
                break;
            }
        }
        if (clanDungeon == null) {
            Service.gI().sendThongBao(player, "Phó bản bang hội đang quá tải, vui lòng đợi còn slot trống");
            return;
        }
        clanDungeon.open(player);
    }

    private boolean isEmptyDungeonZone(ClanDungeon dungeon) {
        for (Zone zone : dungeon.getZones()) {
            if (zone.getNumOfPlayers() > 0) {
                return false;
            }
        }
        return true;
    }
}
