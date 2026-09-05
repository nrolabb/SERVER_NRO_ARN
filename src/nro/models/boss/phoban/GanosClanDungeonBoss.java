package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class GanosClanDungeonBoss extends ClanDungeonBoss {

    public GanosClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, GANOS, BossesData.getFirst(-929));
    }
}
