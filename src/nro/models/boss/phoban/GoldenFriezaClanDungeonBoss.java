package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class GoldenFriezaClanDungeonBoss extends ClanDungeonBoss {

    public GoldenFriezaClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, GOLDEN_FRIEZA, cloneData(BossesData.GOLDEN_FRIEZA));
    }
}
