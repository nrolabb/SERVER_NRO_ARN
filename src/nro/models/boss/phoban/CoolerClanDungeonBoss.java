package nro.models.boss.phoban;

import nro.models.boss.BossID;
import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class CoolerClanDungeonBoss extends ClanDungeonBoss {

    public CoolerClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, COOLER, BossesData.get(BossID.COOLER));
    }
}
