package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class SaonelClanDungeonBoss extends ClanDungeonBoss {

    public SaonelClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, SAONEL, BossesData.getFirst(-932));
    }
}
