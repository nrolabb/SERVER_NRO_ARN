package nro.models.boss;

import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.utils.Util;

public class BossDropHelper {

    public static void dropCostumePiece(Boss boss, Player plKill, int itemId) {
        if (boss == null || boss.zone == null) {
            return;
        }
        int x = boss.location.x + Util.nextInt(-60, 60);
        int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
        long playerId = plKill != null ? plKill.id : -1;
        Service.gI().dropItemMap(boss.zone, new ItemMap(boss.zone, itemId, 1, x, y, playerId));
    }

    public static int randomItem(int... itemIds) {
        return itemIds[Util.nextInt(itemIds.length)];
    }
}
