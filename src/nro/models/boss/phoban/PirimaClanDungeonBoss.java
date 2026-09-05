package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class PirimaClanDungeonBoss extends ClanDungeonBoss {

    public PirimaClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, PIRIMA, BossesData.getFirst(-931));
    }
}
