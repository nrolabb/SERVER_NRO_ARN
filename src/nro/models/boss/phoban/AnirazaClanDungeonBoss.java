package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class AnirazaClanDungeonBoss extends ClanDungeonBoss {

    public AnirazaClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, ANIRAZA, BossesData.getFirst(-928));
    }
}
