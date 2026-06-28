package nro.models.services;

import java.util.ArrayList;
import java.util.List;
import nro.models.clan.Clan;
import nro.models.clan.ClanIntrinsicTemplate;
import nro.models.network.Message;
import nro.models.player.Player;
import nro.models.utils.Logger;

public class ClanIntrinsicService {

    private static ClanIntrinsicService instance;

    private final List<ClanIntrinsicTemplate> templates = new ArrayList<>();

    private ClanIntrinsicService() {
        templates.add(new ClanIntrinsicTemplate((byte) 1, "Sinh lực bang", "Tăng HP tối đa cho thành viên bang", (short) 20, (byte) 10, ClanIntrinsicTemplate.EFFECT_HP, (short) 1));
        templates.add(new ClanIntrinsicTemplate((byte) 2, "Khí lực bang", "Tăng KI tối đa cho thành viên bang", (short) 21, (byte) 10, ClanIntrinsicTemplate.EFFECT_MP, (short) 1));
        templates.add(new ClanIntrinsicTemplate((byte) 3, "Sức mạnh bang", "Tăng sức đánh cho thành viên bang", (short) 22, (byte) 10, ClanIntrinsicTemplate.EFFECT_DAME, (short) 1));
        templates.add(new ClanIntrinsicTemplate((byte) 4, "Phòng thủ bang", "Tăng giáp cho thành viên bang", (short) 23, (byte) 10, ClanIntrinsicTemplate.EFFECT_DEF, (short) 1));
    }

    public static ClanIntrinsicService gI() {
        if (instance == null) {
            instance = new ClanIntrinsicService();
        }
        return instance;
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
