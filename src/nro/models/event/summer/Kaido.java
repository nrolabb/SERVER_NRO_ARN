package nro.models.event.summer;

import nro.models.boss.BossID;

public class Kaido extends SummerEventBoss {

    public Kaido() throws Exception {
        super(BossID.KAIDO, SummerEventManager.KAIDO_DATA);
    }
}
