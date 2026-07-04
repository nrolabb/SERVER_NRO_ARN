package nro.models.boss.phoban;

import nro.models.boss.BossesData;
import nro.models.map.phoban.ClanDungeon;

public class SieuBoHungClanDungeonBoss extends ClanDungeonBoss {

    public SieuBoHungClanDungeonBoss(ClanDungeon clanDungeon, int id) throws Exception {
        super(clanDungeon, id, SIEU_BO_HUNG,
                cloneData(BossesData.SIEU_BO_HUNG_1), cloneData(BossesData.SIEU_BO_HUNG_2));
    }
}
