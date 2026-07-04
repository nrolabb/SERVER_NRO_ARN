package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.ClanService;
import nro.models.services.Service;
import nro.models.services_dungeon.ClanDungeonService;
import nro.models.shop.ShopService;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.NpcService;
import nro.models.utils.TimeUtil;
import nro.models.utils.Util;
import nro.models.map.phoban.ClanDungeon;

public class GiuMaDauBo extends Npc {

    public GiuMaDauBo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            this.createOtherMenu(player, ConstNpc.BASE_MENU,
                    "• Nếu ngươi có Bông Tai Porata:\n"
                            + "  - Có thể úp được Mảnh Vỡ Bông Tai Cấp 2\n"
                            + "  - Có thể úp được Mảnh Hồn Bông Tai\n"
                            + "  - Đôi khi còn rơi cả Đá Xanh Lam\n\n"
                            + "• Nếu ngươi có Bông Tai Porata Cấp 2:\n"
                            + "  - Có thể úp thêm Mảnh Vỡ Bông Tai Cấp 3\n\n"
                            + "• Nếu ngươi có Cải trang Bulma Sexy:\n"
                            + "  - Tỉ lệ úp sẽ là x1.5\n"
                            + "Hãy chuẩn bị kỹ trước khi tham gia thử thách!",
                    this.mapId != 153 ? "Về lãnh\nđịa bang" : "Phó bản\nBang", "Cửa hàng\nbang", "Đóng");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (select == 0 && this.mapId != 153) {
            ChangeMapService.gI().changeMapBySpaceShip(player, 153, -1, -1);
        } else if (select == 0) {
            openClanDungeonMenu(player);
        } else if (select == 1) {
            ShopService.gI().opendShop(player, "SHOP_CLAN", false);
            ClanService.gI().sendClanBox(player);
        }
    }

    private void openClanDungeonMenu(Player player) {
        if (player.clan == null) {
            NpcService.gI().createTutorial(player, tempId, this.avartar, "Chỉ tiếp các bang hội, miễn tiếp khách vãng lai");
            return;
        }
        if (!player.getSession().actived) {
            Service.gI().sendThongBao(player, "Vui lòng mở thành viên trước");
            return;
        }
        if (player.clan.clanDungeon != null && player.clan.clanDungeon.isOpened()) {
            ClanDungeonService.gI().joinClanDungeon(player);
            return;
        }
        if (player.clan.haveGoneClanDungeon && !Util.isAfterMidnight(player.clan.lastTimeOpenClanDungeon)) {
            String name = player.clan.playerOpenClanDungeon != null ? player.clan.playerOpenClanDungeon.name : "một thành viên trong bang";
            NpcService.gI().createTutorial(player, tempId, this.avartar,
                    "Bang hội đã đi phó bản bang hôm nay\nNgười mở: " + name
                    + "\nThời gian: " + TimeUtil.formatTime(player.clan.lastTimeOpenClanDungeon, "HH:mm")
                    + "\nHẹn gặp lại ngày mai");
            return;
        }
        int sameClan = 0;
        for (Player pl : player.zone.getPlayers()) {
            if (pl != null && pl.clan != null && pl.clan.equals(player.clan)) {
                sameClan++;
            }
        }
        if (sameClan < ClanDungeon.N_PLAYER_MAP) {
            NpcService.gI().createTutorial(player, tempId, this.avartar,
                    "Cần có ít nhất " + ClanDungeon.N_PLAYER_MAP
                    + " thành viên cùng bang trong map 153 mới có thể bắt đầu phó bản bang hội");
            return;
        }
        ClanDungeonService.gI().joinClanDungeon(player);
    }
}
