package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class CawayClanDungeonBoss extends ClanDungeonBoss {

    public CawayClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, CAWAY, cloneData(BossesData.CAWAY));
    }
}
