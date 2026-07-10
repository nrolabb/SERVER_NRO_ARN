package nro.models.event.summer;

import nro.models.boss.BossID;

public class BigMom extends SummerEventBoss {

    public BigMom() throws Exception {
        super(BossID.BIG_MOM, SummerEventManager.BIG_MOM_DATA);
    }
}
