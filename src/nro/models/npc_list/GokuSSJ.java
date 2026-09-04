package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.map.service.ChangeMapService;
import nro.models.network.Message;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.EffectSkillService;
import nro.models.services.Service;
import nro.models.services.SpineService;
import nro.models.skill.Skill;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class GokuSSJ extends Npc {

    public GokuSSJ(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 80:
                case 195:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta mới hạ Fide, nhưng nó đã kịp đào 1 cái lỗ\nHành tinh này sắp nổ tung rồi\nMau lượn thôi",
                            "Đến\nYardat", "Đến\nKaio", "Nâng cấp\nBiến hình");
                    break;
                case 131:
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Đây là đâu? Xong cmnr", "Bó tay", "Về chỗ cũ");
                    break;
                default:
                    super.openBaseMenu(player);
                    break;
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.idMark.isBaseMenu()) {
                if (this.mapId == 131) {
                    if (select == 1) {
                        int targetMap = player.idMark.getMapIdGokuSSJ() == 195 ? 195 : 80;
                        ChangeMapService.gI().changeMapBySpaceShip(player, targetMap, -1, 870);
                    }
                } else if (this.mapId == 80 || this.mapId == 195) {
                    switch (select) {
                        case 0 -> {
                            player.idMark.setMapIdGokuSSJ(this.mapId);
                            ChangeMapService.gI().changeMapBySpaceShip(player, 131, -1, 870);
                        }
                        case 1 ->
                            ChangeMapService.gI().changeMapBySpaceShip(player, 95, -1, 870);
                        case 2 ->
                            showUpgradeBienHinhMenu(player);
                    }
                }
            } else {
                switch (player.idMark.getIndexMenu()) {
                    case 7 ->
                        handleNangCapBienHinh(player, select);
                }
            }
        }
    }

    private void showUpgradeBienHinhMenu(Player player) {
        Skill skill = SkillUtil.getSkillbyId(player, Skill.BIEN_HINH);
        if (skill != null && skill.point >= 4) {
            Service.gI().sendThongBaoOK(player, "Kỹ năng Biến hình của ngươi đã đạt cấp " + skill.point + ".\nHãy đến gặp Whis tại Hành tinh Whis để tiếp tục nâng cấp từ cấp 4 đến 7!");
            return;
        }
        int currentPoint = (skill == null) ? 0 : skill.point;
        int successRate = Math.max(1, 30 - (currentPoint * 4));
        String message = "|1|Ta sẽ giúp ngươi nâng cấp kỹ năng Biến Hình (Cấp 1 - 4)\n"
                + "|7|Cấp hiện tại: " + currentPoint + " -> Cấp tiếp theo: " + (currentPoint + 1) + "\n"
                + "|2|Tiêu tốn: 10 Tỷ Tiềm Năng Sức Mạnh\n"
                + "|2|Tỷ lệ thành công: " + successRate + "%";
        createOtherMenu(player, 7, message, "Nâng cấp", "Từ chối");
    }

    private void handleNangCapBienHinh(Player player, int select) {
        if (select != 0) {
            return;
        }

        Skill skill = SkillUtil.getSkillbyId(player, Skill.BIEN_HINH);
        if (skill != null && skill.point >= 4) {
            Service.gI().sendThongBaoOK(player, "Kỹ năng Biến hình của ngươi đã đạt cấp " + skill.point + ".\nHãy đến gặp Whis tại Hành tinh Whis để tiếp tục nâng cấp từ cấp 4 đến 7!");
            return;
        }
        if (player.nPoint.tiemNang < 10_000_000_000L) {
            Service.gI().sendThongBaoOK(player, "Bạn không đủ 10 Tỷ Tiềm năng sức mạnh!");
            return;
        }

        player.nPoint.tiemNang -= 10_000_000_000L;
        Service.gI().point(player);

        int currentPoint = (skill == null) ? 0 : skill.point;
        int successRate = Math.max(1, 30 - (currentPoint * 4));
        boolean success = Util.isTrue(successRate, 100);

        // Gửi animation spine mặc định
        EffectSkillService ess = EffectSkillService.gI();
        int targetLevel = success ? currentPoint + 1 : Math.max(1, currentPoint);
        String skin = ess.getBienHinhSpineSkin(player, targetLevel);
        SpineService.gI().sendSpineSkillEffect(player,
                ess.getBienHinhSpinePath(player, targetLevel),
                EffectSkillService.BIEN_HINH_SPINE_ANIM,
                skin, EffectSkillService.TIME_TRANSFORM_BIEN_HINH_SPINE);

        // Chờ animation xong mới áp dụng kết quả
        Util.setTimeout(() -> {
            if (player != null && player.zone != null) {
                if (success) {
                    Skill newSkill = SkillUtil.createSkill(Skill.BIEN_HINH, currentPoint + 1);
                    SkillUtil.setSkill(player, newSkill);
                    try {
                        Message msg = new Message(62);
                        msg.writer().writeShort(newSkill.skillId);
                        player.sendMessage(msg);
                        msg.cleanup();
                    } catch (Exception e) {
                    }
                    npcChat(player, "Chúc mừng con nhé! Kỹ năng Biến hình đã đạt cấp " + newSkill.point);
                } else {
                    npcChat(player, "Ngu dốt! Nâng cấp thất bại!");
                }
            }
        }, EffectSkillService.TIME_TRANSFORM_BIEN_HINH_SPINE);
    }
}
