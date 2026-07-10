package nro.models.event.summer;

import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.map.Map;
import nro.models.map.service.MapService;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.models.player_system.Template.NpcTemplate;
import nro.models.server.Manager;
import nro.models.skill.Skill;
import nro.models.utils.Logger;
import nro.models.utils.Util;

public class SummerEventManager extends BossManager {

    public static final int MAP_HAI_TAC = 101;
    public static final int ACTIVE_POINT_BOSS_REWARD = 2;
    public static final int LUCKY_ROUND_GEM_COST = 10;
    public static final String SHOP_EXCHANGE = "SUMMER_EVENT_EXCHANGE";

    public static final int SAN_HO_XANH = 2016;
    public static final int SAN_HO_DO = 2017;
    public static final int SAN_HO_TIM = 2018;
    public static final int SAN_HO_VANG = 2019;
    public static final int NGOC_TRAI_DEN = 2020;
    public static final int NGOC_TRAI_TRANG = 2021;
    public static final int VAY_RONG_XANH = 2022;
    public static final int VAY_RONG_DEN = 2023;

    private static SummerEventManager instance;
    private boolean updateThreadStarted;

    public static SummerEventManager gI() {
        if (instance == null) {
            instance = new SummerEventManager();
        }
        return instance;
    }

    private SummerEventManager() {
    }

    public synchronized void init() {
        ensureVuaHaiTacTemplate();
        createEventNpcs();
        createBoss(BossID.BIG_MOM, 1);
        createBoss(BossID.KAIDO, 1);
        if (!updateThreadStarted) {
            updateThreadStarted = true;
            new Thread(this, "Update summer event boss").start();
            Logger.successln("[SUMMER EVENT] Da tao BigMom, Kaido va khoi dong luong cap nhat boss");
        }
    }

    private void ensureVuaHaiTacTemplate() {
        while (Manager.NPC_TEMPLATES.size() <= ConstNpc.VUA_HAI_TAC) {
            NpcTemplate placeholder = new NpcTemplate();
            placeholder.id = Manager.NPC_TEMPLATES.size();
            placeholder.name = "";
            placeholder.head = -1;
            placeholder.body = -1;
            placeholder.leg = -1;
            placeholder.avatar = 0;
            Manager.NPC_TEMPLATES.add(placeholder);
        }
        NpcTemplate template = Manager.NPC_TEMPLATES.get(ConstNpc.VUA_HAI_TAC);
        if (template == null || template.name == null || template.name.isEmpty()) {
            template = new NpcTemplate();
            template.id = ConstNpc.VUA_HAI_TAC;
            template.name = "Vua hải tặc";
            template.head = 2099;
            template.body = 2156;
            template.leg = 2100;
            template.avatar = 14527;
            Manager.NPC_TEMPLATES.set(ConstNpc.VUA_HAI_TAC, template);
        }
    }

    private void createEventNpcs() {
        createVuaHaiTacNextToBaHatMit(42);
        createVuaHaiTacNextToBaHatMit(43);
        createVuaHaiTacNextToBaHatMit(44);
    }

    private void createVuaHaiTacNextToBaHatMit(int mapId) {
        Map map = MapService.gI().getMapById(mapId);
        if (map == null || map.npcs == null) {
            return;
        }
        for (Npc npc : map.npcs) {
            if (npc.tempId == ConstNpc.VUA_HAI_TAC) {
                return;
            }
        }
        for (Npc npc : map.npcs) {
            if (npc.tempId == ConstNpc.BA_HAT_MIT) {
                map.addNpc(NpcFactory.createNPC(mapId, 1, npc.cx + 60, npc.cy, ConstNpc.VUA_HAI_TAC));
                return;
            }
        }
    }

    @Override
    public Boss createBoss(int bossID) {
        try {
            return switch (bossID) {
                case BossID.BIG_MOM ->
                    new BigMom();
                case BossID.KAIDO ->
                    new Kaido();
                default ->
                    super.createBoss(bossID);
            };
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public LuckyReward randomLuckyReward() {
        int total = 0;
        for (LuckyReward reward : LUCKY_REWARDS) {
            total += reward.rate;
        }
        int random = Util.nextInt(1, total);
        int current = 0;
        for (LuckyReward reward : LUCKY_REWARDS) {
            current += reward.rate;
            if (random <= current) {
                return reward;
            }
        }
        return LUCKY_REWARDS[0];
    }

    public DropReward randomBossDrop() {
        for (DropReward reward : BOSS_DROPS) {
            if (Util.isTrue(reward.rate, 10000)) {
                return reward;
            }
        }
        return null;
    }

    public static final LuckyReward[] LUCKY_REWARDS = {
        new LuckyReward(457, 1, 500, new int[][]{{30, 1}}),
        new LuckyReward(987, 3, 1000, new int[][]{}),
        new LuckyReward(SAN_HO_XANH, 5, 1800, new int[][]{}),
        new LuckyReward(SAN_HO_DO, 5, 1600, new int[][]{}),
        new LuckyReward(NGOC_TRAI_TRANG, 3, 1200, new int[][]{}),
        new LuckyReward(VAY_RONG_XANH, 1, 800, new int[][]{{30, 1}}),
        new LuckyReward(77, 50, 3100, new int[][]{})
    };

    public static final DropReward[] BOSS_DROPS = {
        new DropReward(SAN_HO_XANH, 1, 3000, new int[][]{}),
        new DropReward(SAN_HO_DO, 1, 2500, new int[][]{}),
        new DropReward(SAN_HO_TIM, 1, 2000, new int[][]{}),
        new DropReward(SAN_HO_VANG, 1, 1500, new int[][]{}),
        new DropReward(NGOC_TRAI_TRANG, 1, 1200, new int[][]{}),
        new DropReward(NGOC_TRAI_DEN, 1, 800, new int[][]{}),
        new DropReward(VAY_RONG_XANH, 1, 600, new int[][]{{30, 1}}),
        new DropReward(VAY_RONG_DEN, 1, 300, new int[][]{{30, 1}})
    };

    public static final BossData BIG_MOM_DATA = new BossData(
            "BigMom",
            ConstPlayer.XAYDA,
            new short[]{2104, 2105, 2106, -1, -1, -1},
            100000,
            new int[]{500000000},
            new int[]{MAP_HAI_TAC},
            new int[][]{{Skill.ANTOMIC, 7, 1000}, {Skill.GALICK, 7, 1000}},
            new String[]{},
            new String[]{"|-1|Biển cả là của ta!", "|-1|Để lại kho báu rồi chạy đi!"},
            new String[]{"|-1|Ta sẽ quay lại..."},
            900
    );

    public static final BossData KAIDO_DATA = new BossData(
            "Kaido",
            ConstPlayer.XAYDA,
            new short[]{2107, 2108, 2109, -1, -1, -1},
            120000,
            new int[]{700000000},
            new int[]{MAP_HAI_TAC},
            new int[][]{{Skill.DRAGON, 7, 1000}, {Skill.KAMEJOKO, 7, 1000}},
            new String[]{},
            new String[]{"|-1|Có gan thì tới đây!", "|-1|Ta là vua của vùng biển này!"},
            new String[]{"|-1|Không thể nào..."},
            1200
    );

    public static class LuckyReward {

        public final int itemId;
        public final int quantity;
        public final int rate;
        public final int[][] options;

        public LuckyReward(int itemId, int quantity, int rate, int[][] options) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.rate = rate;
            this.options = options;
        }

        public void addOptions(Item item) {
            for (int[] option : options) {
                item.itemOptions.add(new Item.ItemOption(option[0], option[1]));
            }
        }
    }

    public static class DropReward extends LuckyReward {

        public DropReward(int itemId, int quantity, int rate, int[][] options) {
            super(itemId, quantity, rate, options);
        }
    }
}
