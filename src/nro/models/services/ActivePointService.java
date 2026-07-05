package nro.models.services;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.player.Player;
import nro.models.utils.Logger;

public class ActivePointService {

    public static final int[] ACTIVE_POINT_BOSS_IDS = {
        BossID.KUKU,
        BossID.KARIN,
        BossID.TRUNG_UY_TRANG,
        BossID.O_DO1,
        BossID.MAP_DAU_DINH,
        BossID.RAMBO,
        BossID.SO_4,
        BossID.SO_3,
        BossID.SO_2,
        BossID.SO_1,
        BossID.TIEU_DOI_TRUONG,
        BossID.FIDE,
        BossID.ANDROID_19,
        BossID.DR_KORE,
        BossID.POC,
        BossID.PIC,
        BossID.KING_KONG,
        BossID.ANDROID_13,
        BossID.ANDROID_14,
        BossID.ANDROID_15,
        BossID.XEN_BO_HUNG,
        BossID.XEN_CON_1,
        BossID.XEN_CON_2,
        BossID.XEN_CON_3,
        BossID.XEN_CON_4,
        BossID.XEN_CON_5,
        BossID.XEN_CON_6,
        BossID.XEN_CON_7,
        BossID.DRABURA,
        BossID.DRABURA_2,
        BossID.DRABURA_3,
        BossID.BUI_BUI,
        BossID.BUI_BUI_2,
        BossID.YA_CON,
        BossID.MABU_12H,
        BossID.SIEU_BO_HUNG
    };

    private static ActivePointService i;
    private boolean tableReady;

    public static ActivePointService gI() {
        if (i == null) {
            i = new ActivePointService();
        }
        return i;
    }

    private void ensureTable() throws Exception {
        if (tableReady) {
            return;
        }
        LocalManager.executeUpdate("CREATE TABLE IF NOT EXISTS `player_active_point` ("
                + "`player_id` INT NOT NULL,"
                + "`point` INT NOT NULL DEFAULT 0,"
                + "`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (`player_id`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");
        tableReady = true;
    }

    public void load(Player player) {
        if (player == null) {
            return;
        }
        LocalResultSet rs = null;
        try {
            ensureTable();
            rs = LocalManager.executeQuery("SELECT point FROM player_active_point WHERE player_id = ? LIMIT 1", player.id);
            if (rs.next()) {
                player.activePoint = Math.max(0, rs.getInt("point"));
            }
        } catch (Exception e) {
            Logger.logException(ActivePointService.class, e, "Lỗi load điểm năng động player " + player.id);
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
    }

    public void save(Player player) {
        if (player == null || player.id <= 0) {
            return;
        }
        try {
            ensureTable();
            LocalManager.executeUpdate(
                    "INSERT INTO player_active_point(player_id, point) VALUES(?, ?) "
                    + "ON DUPLICATE KEY UPDATE point = VALUES(point)",
                    player.id,
                    Math.max(0, player.activePoint));
        } catch (Exception e) {
            Logger.logException(ActivePointService.class, e, "Lỗi save điểm năng động player " + player.id);
        }
    }

    public void addPoint(Player player, int amount, String reason) {
        if (player == null || amount <= 0 || player.isBot || player.isBoss || player.isPet) {
            return;
        }
        player.activePoint += amount;
        save(player);
        Service.gI().sendNangDong(player);
        Service.gI().sendThongBao(player, "Bạn nhận được +" + amount + " điểm năng động"
                + (reason != null && !reason.isEmpty() ? " từ " + reason : "")
                + ". Hiện có: " + player.activePoint);
    }

    public void addBossKillPoint(Player player, Boss boss) {
        if (boss == null || boss.activePointRewarded || !isActivePointBoss((int) boss.id)) {
            return;
        }
        boss.activePointRewarded = true;
        addPoint(player, 1, "tiêu diệt boss");
    }

    public boolean isActivePointBoss(int bossId) {
        for (int id : ACTIVE_POINT_BOSS_IDS) {
            if (id == bossId) {
                return true;
            }
        }
        return false;
    }
}
