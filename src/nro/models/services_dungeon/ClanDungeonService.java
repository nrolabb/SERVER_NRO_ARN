package nro.models.services_dungeon;

import java.util.ArrayList;
import java.util.List;
import nro.models.map.Zone;
import nro.models.map.phoban.ClanDungeon;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Util;

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

    public void joinClanDungeon(Player player) {
        if (player.clan == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có bang hội");
            return;
        }
        if (player.clan.clanDungeon != null && player.clan.clanDungeon.isOpened()) {
            nro.models.map.service.ChangeMapService.gI().changeMapInYard(player, ClanDungeon.MAP_START, -1, 100);
            player.clan.clanDungeon.sendTextClanDungeon();
            return;
        }
        if (player.clan.haveGoneClanDungeon && !Util.isAfterMidnight(player.clan.lastTimeOpenClanDungeon)) {
            Service.gI().sendThongBao(player, "Bang hội đã đi phó bản bang hôm nay");
            return;
        }

        ClanDungeon clanDungeon = null;
        for (ClanDungeon dungeon : this.clanDungeons) {
            if (dungeon.getClan() == null) {
                clanDungeon = dungeon;
                break;
            }
        }
        if (clanDungeon == null) {
            Service.gI().sendThongBao(player, "Phó bản bang hội đang quá tải, vui lòng quay lại sau");
            return;
        }
        clanDungeon.open(player);
    }
}
