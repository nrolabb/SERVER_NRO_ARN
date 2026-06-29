package nro.models.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nro.models.clan.Clan;
import nro.models.clan.ClanIntrinsicTemplate;
import nro.models.data.LocalManager;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.utils.Logger;

public class ClanIntrinsicService {

    private static final String TABLE_NAME = "clan_intrinsic_template";

    private static ClanIntrinsicService instance;

    private final List<ClanIntrinsicTemplate> templates = new ArrayList<>();

    private ClanIntrinsicService() {
        loadTemplates();
    }

    public static ClanIntrinsicService gI() {
        if (instance == null) {
            instance = new ClanIntrinsicService();
        }
        return instance;
    }

    private void loadTemplates() {
        templates.clear();
        try (Connection con = LocalManager.getConnection()) {
            ensureTable(con);
            ensureUpgradeCostBaseColumn(con);
            seedDefaultTemplates(con);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, name, description, icon_id, max_level, effect_type, value_per_level, upgrade_cost_base "
                    + "FROM " + TABLE_NAME + " ORDER BY id");
                    ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    templates.add(new ClanIntrinsicTemplate(
                            rs.getByte("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getShort("icon_id"),
                            rs.getByte("max_level"),
                            rs.getByte("effect_type"),
                            rs.getShort("value_per_level"),
                            rs.getInt("upgrade_cost_base")));
                }
            }
        } catch (Exception e) {
            Logger.logException(ClanIntrinsicService.class, e, "Không thể load nội tại bang từ DB");
        }

        if (templates.isEmpty()) {
            loadFallbackTemplates();
        }
    }

    private void ensureTable(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "id TINYINT NOT NULL,"
                + "name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,"
                + "description VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,"
                + "icon_id SMALLINT NOT NULL DEFAULT 0,"
                + "max_level TINYINT NOT NULL DEFAULT 10,"
                + "effect_type TINYINT NOT NULL DEFAULT 1,"
                + "value_per_level SMALLINT NOT NULL DEFAULT 1,"
                + "upgrade_cost_base INT NOT NULL DEFAULT 100,"
                + "PRIMARY KEY (id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC")) {
            ps.executeUpdate();
        }
    }

    private void ensureUpgradeCostBaseColumn(Connection con) {
        try (PreparedStatement ps = con.prepareStatement(
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN upgrade_cost_base INT NOT NULL DEFAULT 100 AFTER value_per_level")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() == null || !e.getMessage().toLowerCase().contains("duplicate")) {
                Logger.logException(ClanIntrinsicService.class, e, "Không thể tự thêm cột upgrade_cost_base");
            }
        } catch (Exception e) {
            Logger.logException(ClanIntrinsicService.class, e, "Không thể tự thêm cột upgrade_cost_base");
        }
    }

    private void seedDefaultTemplates(Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO " + TABLE_NAME
                + " (id, name, description, icon_id, max_level, effect_type, value_per_level, upgrade_cost_base) VALUES "
                + "(1, ?, ?, 20, 10, ?, 1, 100), "
                + "(2, ?, ?, 21, 10, ?, 1, 100), "
                + "(3, ?, ?, 22, 10, ?, 1, 100), "
                + "(4, ?, ?, 23, 10, ?, 1, 100), "
                + "(5, ?, ?, 24, 10, ?, 1, 100)")) {
            ps.setString(1, "Sinh lực bang");
            ps.setString(2, "Tăng HP tối đa cho thành viên bang");
            ps.setByte(3, ClanIntrinsicTemplate.EFFECT_HP);
            ps.setString(4, "Khí lực bang");
            ps.setString(5, "Tăng KI tối đa cho thành viên bang");
            ps.setByte(6, ClanIntrinsicTemplate.EFFECT_MP);
            ps.setString(7, "Sức mạnh bang");
            ps.setString(8, "Tăng sức đánh cho thành viên bang");
            ps.setByte(9, ClanIntrinsicTemplate.EFFECT_DAME);
            ps.setString(10, "Phòng thủ bang");
            ps.setString(11, "Tăng giáp cho thành viên bang");
            ps.setByte(12, ClanIntrinsicTemplate.EFFECT_DEF);
            ps.setString(13, "Chí mạng bang");
            ps.setString(14, "Tăng chí mạng cho thành viên bang");
            ps.setByte(15, ClanIntrinsicTemplate.EFFECT_CRIT);
            ps.executeUpdate();
        }
    }

    private void loadFallbackTemplates() {
        templates.add(new ClanIntrinsicTemplate((byte) 1, "Sinh lực bang", "Tăng HP tối đa cho thành viên bang", (short) 20, (byte) 10, ClanIntrinsicTemplate.EFFECT_HP, (short) 1, 100));
        templates.add(new ClanIntrinsicTemplate((byte) 2, "Khí lực bang", "Tăng KI tối đa cho thành viên bang", (short) 21, (byte) 10, ClanIntrinsicTemplate.EFFECT_MP, (short) 1, 100));
        templates.add(new ClanIntrinsicTemplate((byte) 3, "Sức mạnh bang", "Tăng sức đánh cho thành viên bang", (short) 22, (byte) 10, ClanIntrinsicTemplate.EFFECT_DAME, (short) 1, 100));
        templates.add(new ClanIntrinsicTemplate((byte) 4, "Phòng thủ bang", "Tăng giáp cho thành viên bang", (short) 23, (byte) 10, ClanIntrinsicTemplate.EFFECT_DEF, (short) 1, 100));
        templates.add(new ClanIntrinsicTemplate((byte) 5, "Chí mạng bang", "Tăng chí mạng cho thành viên bang", (short) 24, (byte) 10, ClanIntrinsicTemplate.EFFECT_CRIT, (short) 1, 100));
    }

    public ClanIntrinsicTemplate getTemplate(byte id) {
        for (ClanIntrinsicTemplate template : templates) {
            if (template.id == id) {
                return template;
            }
        }
        return null;
    }

    public int getBonus(Clan clan, byte effectType) {
        if (clan == null) {
            return 0;
        }
        int bonus = 0;
        for (ClanIntrinsicTemplate template : templates) {
            if (template.effectType == effectType) {
                bonus += template.getValue(clan.getClanIntrinsicLevel(template.id));
            }
        }
        return bonus;
    }

    public void sendClanIntrinsics(Player player, byte action) {
        if (player == null || player.clan == null) {
            if (player != null) {
                Service.gI().sendThongBao(player, "Bạn chưa có bang hội");
            }
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-58);
            msg.writer().writeByte(action);
            msg.writer().writeByte(templates.size());
            for (ClanIntrinsicTemplate template : templates) {
                byte level = player.clan.getClanIntrinsicLevel(template.id);
                msg.writer().writeByte(template.id);
                msg.writer().writeShort(template.icon);
                msg.writer().writeUTF(template.name);
                msg.writer().writeUTF(template.description);
                msg.writer().writeByte(level);
                msg.writer().writeByte(template.maxLevel);
                msg.writer().writeShort(template.getValue(level));
                msg.writer().writeShort(template.getNextValue(level));
                msg.writer().writeInt(template.getUpgradeCost(level));
                msg.writer().writeBoolean(level < template.maxLevel && (player.clan.isLeader(player) || player.clan.isDeputy(player)));
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(ClanIntrinsicService.class, e, "Lỗi sendClanIntrinsics");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void upgradeClanIntrinsic(Player player, byte intrinsicId) {
        if (player == null || player.clan == null) {
            if (player != null) {
                Service.gI().sendThongBao(player, "Bạn chưa có bang hội");
            }
            return;
        }
        Clan clan = player.clan;
        if (!clan.isLeader(player) && !clan.isDeputy(player)) {
            Service.gI().sendThongBao(player, "Chỉ bang chủ hoặc bang phó mới có quyền nâng cấp nội tại bang!");
            sendClanIntrinsics(player, (byte) 4);
            return;
        }
        ClanIntrinsicTemplate template = getTemplate(intrinsicId);
        if (template == null) {
            Service.gI().sendThongBao(player, "Nội tại bang không hợp lệ!");
            return;
        }
        byte level = clan.getClanIntrinsicLevel(intrinsicId);
        if (level >= template.maxLevel) {
            Service.gI().sendThongBao(player, "Nội tại bang đã đạt cấp tối đa!");
            sendClanIntrinsics(player, (byte) 4);
            return;
        }
        int cost = template.getUpgradeCost(level);
        if (clan.capsuleClan < cost) {
            Service.gI().sendThongBao(player, "Bang hội không đủ Capsule bang để nâng cấp!");
            sendClanIntrinsics(player, (byte) 4);
            return;
        }
        clan.capsuleClan -= cost;
        clan.setClanIntrinsicLevel(intrinsicId, (byte) (level + 1));
        clan.update();
        for (Player member : clan.membersInGame) {
            if (member != null) {
                member.nPoint.calPoint();
                Service.gI().point(member);
                ClanService.gI().sendMyClan(member);
            }
        }
        Service.gI().sendThongBao(player, "Đã nâng cấp " + template.name + " lên cấp " + (level + 1));
        sendClanIntrinsics(player, (byte) 4);
    }
}
