package nro.models.npc_list;

import java.time.LocalDate;
import java.time.LocalDateTime;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstTask;
import nro.models.item.Item;
import nro.models.services.AchievementService;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services_func.Input;

/**
 *
 * @author By Mr Blue
 *
 */
public class BoMong extends Npc {

    public BoMong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
       if (TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
        return;
    }


            if (canOpenNpc(player)) {
                if (this.mapId == 47 || this.mapId == 84 ||this.mapId == 21||this.mapId == 22||this.mapId == 23) {
                    this.createOtherMenu(player, ConstNpc.BASE_MENU,
                            "Ngươi muốn có thêm ngọc thì chịu khó làm vài nhiệm vụ sẽ được ngọc thưởng", "Nhiệm vụ\nhàng ngày", "Nhiệm vụ\nthành tích"
                             , "Đổi điểm"
                             , "Nạp Ngọc"
                            , "Điểm danh"
                            , "Từ chối");
                }
            }
        }
    

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (this.mapId == 47 || this.mapId == 84||this.mapId == 21||this.mapId == 22||this.mapId == 23) {
                if (player.idMark.isBaseMenu()) {
                    switch (select) {
                        case 0 -> {
                            if (player.playerTask.sideTask.template != null) {
                                String npcSay = "Nhiệm vụ hiện tại: " + player.playerTask.sideTask.getName() + " ("
                                        + player.playerTask.sideTask.getLevel() + ")"
                                        + "\nHiện tại đã hoàn thành: " + player.playerTask.sideTask.count + "/"
                                        + player.playerTask.sideTask.maxCount + " ("
                                        + player.playerTask.sideTask.getPercentProcess() + "%)\nSố nhiệm vụ còn lại trong ngày: "
                                        + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK;
                                this.createOtherMenu(player, ConstNpc.MENU_OPTION_PAY_SIDE_TASK,
                                        npcSay, "Trả nhiệm\nvụ", "Hủy nhiệm\nvụ");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK,
                                        "Tôi có vài nhiệm vụ theo cấp bậc, càng khó càng nhận được nhiều Vàng, Ngọc Xanh\n"
                                        + "và Điểm Năng Động (Dễ: +1 điểm, Bình thường: +4 điểm, Khó: +10 điểm)\n"
                                        + "Sức cậu có thể làm được cái nào?",
                                        "Dễ", "Bình thường", "Khó", "Từ chối");
                            }
                        }
                        case 1 -> {
                            AchievementService.gI().openAchievementUI(player);
                        }
                    
                        case 2 -> {
                            this.createOtherMenu(player, ConstNpc.MENU_OPTION_EXCHANGE_SIDE_TASK_POINT,
                                    "Điểm năng động hiện có: " + player.activePoint
                                    + "\n(Kiếm điểm từ: Nhiệm vụ ngày, Nhiệm vụ bang hội và Tiêu diệt Boss)"
                                    + "\n100 điểm đổi 1000 Ngọc"
                                    + "\n200 điểm đổi 2200 Ngọc"
                                    + "\n300 điểm đổi 3500 Ngọc"
                                    + "\n100 điểm đổi 1 Capsule bạc"
                                    + "\n300 điểm đổi 1 Capsule vàng",
                                    "1000 Ngọc", "2200 Ngọc", "3500 Ngọc", "Capsule bạc", "Capsule vàng", "Từ chối");
                        }
                        case 3 -> {
                            Input.gI().createFormTradeGem(player);
                        }
                        case 4 -> {
                            if (player.lastCheckIn != null) {
                                LocalDate last = player.lastCheckIn.toLocalDate();
                                LocalDate today = LocalDate.now();
                                if (last.isEqual(today)) {
                                    Service.gI().sendThongBao(player, "Bạn đã điểm danh hôm nay rồi!");
                                    return;
                                }
                            }
                            player.lastCheckIn = LocalDateTime.now();
                            player.inventory.gem += 100;
                            // Item item457 = ItemService.gI().createNewItem((short) 457);
                            // item457.quantity = 10;
                            // item457.itemOptions.add(new Item.ItemOption(30, 0));
                            // InventoryService.gI().addItemBag(player, item457);
                            PlayerService.gI().sendInfoHpMpMoney(player);
                            InventoryService.gI().sendItemBags(player);

                            Service.gI().sendThongBao(player, "Điểm danh thành công! Bạn nhận được 100 ngọc.");
                        }

                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_LEVEL_SIDE_TASK) {
                    switch (select) {
                        case 0, 1, 2 ->
                            TaskService.gI().changeSideTask(player, (byte) select);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_PAY_SIDE_TASK) {
                    switch (select) {
                        case 0 ->
                            TaskService.gI().paySideTask(player);
                        case 1 ->
                            TaskService.gI().removeSideTask(player);
                    }
                } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_OPTION_EXCHANGE_SIDE_TASK_POINT) {
                    switch (select) {
                        case 0 ->
                            exchangeActivePoint(player, 100, 77, 1000);
                        case 1 ->
                            exchangeActivePoint(player, 200, 77, 2200);
                        case 2 ->
                            exchangeActivePoint(player, 300, 77, 3500);
                        case 3 ->
                            exchangeActivePoint(player, 100, 573, 1);
                        case 4 ->
                            exchangeActivePoint(player, 300, 574, 1);
                    }
                }
            }
        }
    }

    private void exchangeActivePoint(Player player, int pointCost, int itemId, int quantity) {
        if (player.activePoint < pointCost) {
            Service.gI().sendThongBao(player, "Bạn không đủ " + pointCost + " điểm năng động.");
            return;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống.");
            return;
        }
        player.activePoint -= pointCost;
        Item item = ItemService.gI().createNewItem((short) itemId);
        item.quantity = quantity;
        InventoryService.gI().addItemBag(player, item);
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Đổi thành công " + pointCost + " điểm lấy "
                + quantity + " " + item.template.name + ". Còn "
                + player.activePoint + " điểm.");
    }
}
