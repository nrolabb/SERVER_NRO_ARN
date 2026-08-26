package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.map.service.NpcService;
import nro.models.map.service.ChangeMapService;
import nro.models.services.TaskService;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Kibit extends Npc {

    public Kibit(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            switch (this.mapId) {
                case 180 -> {
                    if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                        return;
                    }
                    String optionSave = (player.mapIdSaved == 180) ? "Hủy lưu\ntọa độ" : "Lưu tọa độ";
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ta là Kibit - Hộ vệ tối cao của Thần Shin.\nRừng Cổ Lâm rất nguy hiểm, con cần cẩn trọng khi đi qua đó!",
                            "Nhiệm vụ", optionSave, "Phép thuật\nTrị thương", "Về\nThánh địa Kaio", "Từ chối");
                }
                case 50 ->
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                            "Đến\nKaio", "Đến\nLàng Kaioshin", "Từ chối");
                case 52 -> {
                    if (player.playerTask.taskMain.id >= 29) {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                                "Đến\nLàng Kaioshin", "Từ chối");
                    } else {
                        this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                                "Từ chối");
                    }
                }
                case 114 -> {
                    if (player.cFlag != 9) {
                        NpcService.gI().createTutorial(player, tempId, this.avartar,
                                "Ngươi hãy về phe của mình mà thể hiện");
                        return;
                    }
                    this.createOtherMenu(player, ConstNpc.BASE_MENU, "Ta có thể giúp gì cho ngươi ?",
                            "Về nhà", "Từ chối");
                }
                default -> {
                }
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 180) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> {
                            if (player.playerTask.taskMain.id == 31 || player.playerTask.taskMain.id == 32) {
                                NpcService.gI().createTutorial(player, tempId, this.avartar,
                                        "Nhiệm vụ của con:\n" + player.playerTask.taskMain.name + "\n" + player.playerTask.taskMain.detail);
                            } else {
                                NpcService.gI().createTutorial(player, tempId, this.avartar,
                                        "Hiện tại con chưa có nhiệm vụ nào với ta. Hãy kiểm tra với Thần Shin hoặc Tổ Sư Kaio nhé!");
                            }
                        }
                        case 1 -> {
                            if (player.mapIdSaved == 180) {
                                player.mapIdSaved = -1;
                                nro.models.services.Service.gI().sendThongBao(player, "Đã hủy lưu tọa độ! Khi bị đánh bại và chọn quay về, bạn sẽ hồi sinh tại nhà của hành tinh.");
                            } else {
                                player.mapIdSaved = 180;
                                nro.models.services.Service.gI().sendThongBao(player, "Đã lưu tọa độ Làng Kaioshin! Khi bị đánh bại và chọn quay về, bạn sẽ hồi sinh tại đây và được hồi phục đầy đủ HP, KI.");
                            }
                        }
                        case 2 -> {
                            player.nPoint.setHp((int) player.nPoint.hpMax);
                            player.nPoint.setMp((int) player.nPoint.mpMax);
                            nro.models.services.Service.gI().point(player);
                            nro.models.services.Service.gI().Send_Info_NV(player);
                            nro.models.services.Service.gI().sendThongBao(player, "Kibit đã dùng phép thuật hồi phục toàn bộ thể lực cho bạn!");
                        }
                        case 3 ->
                            ChangeMapService.gI().changeMap(player, 50, -1, 318, 336);
                    }
                }
            } else if (this.mapId == 50) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 ->
                            ChangeMapService.gI().changeMap(player, 48, -1, 354, 240);
                        case 1 -> {
                            if (player.playerTask.taskMain.id < 29) {
                                nro.models.services.Service.gI().sendThongBao(player, "Con phải hoàn thành nhiệm vụ tiêu diệt Ma Bư mới có thể đến Làng Kaioshin!");
                                return;
                            }
                            ChangeMapService.gI().changeMap(player, 180, -1, 800, 264);
                        }
                    }
                }
            } else if (this.mapId == 52) {
                if (player.idMark.isBaseMenu()) {
                    if (select == 0 && player.playerTask.taskMain.id >= 29) {
                        ChangeMapService.gI().changeMap(player, 180, -1, 800, 264);
                    }
                }
            } else if (this.mapId == 114) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> {
                            if (player.cFlag != 9) {
                                return;
                            }
                            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, 0, -1);
                        }
                    }
                }
            }
        }
    }
}
