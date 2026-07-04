package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class CumberClanDungeonBoss extends ClanDungeonBoss {

    public CumberClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, CUMBER, cloneData(BossesData.CUMBER), cloneData(BossesData.SUPER_CUMBER));
    }
}
