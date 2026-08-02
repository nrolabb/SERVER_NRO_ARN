package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.data.LocalManager;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.Service;
import nro.models.services.TaskService;
import nro.models.services_func.Input;

public class OngMoori extends Npc {

    public OngMoori(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                java.util.List<String> menus = new java.util.ArrayList<>();
                menus.add("Nhập\nGiftcode");
                menus.add("Mở\nTV Free");
                menus.add("Nhận quà\nVòng Quay");
                if (!player.getSession().actived) {
                    menus.add("Kích hoạt\ntài khoản");
                }
                menus.add("Nhận ngọc");
                if (player.getSession().xacNhanGioiThieu == 0) {
                    menus.add("Nhập mã\ngiới thiệu");
                } else {
                    menus.add("Xem mã\ngiới thiệu");
                }
                menus.add("Đóng");
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "Con cố gắng theo Quy Lão Kame học thành tài,\nđừng lo lắng cho ta.",
                        menus.toArray(new String[0])
                );
            }
        }
    }
@Override
public void confirmMenu(Player player, int select) {
    if (!canOpenNpc(player)) return;

    if (player.idMark.isBaseMenu()) {
        int index = 0;
        if (select == index++) {
            // Nhập giftcode
            Input.gI().createFormGiftCode(player);
            return;
        }
        
        if (select == index++) {
            // Check nhiệm vụ
            if (player.playerTask.taskMain.id <= 21) {
                Service.gI().sendThongBao(player, "Xong Nv Fide!");
                return;
            }

            // Đã active chưa
            if (player.getSession().actived) {
                Service.gI().sendThongBao(player, "Tài khoản của con đã mở TV Free rồi!");
                return;
            }

            // Set active trong session
            player.getSession().actived = true;

            // Update DB (PHẢI try-catch)
            try {
                LocalManager.executeUpdate("UPDATE account SET active = 1 WHERE id = ?", player.getSession().userId);
            } catch (Exception e) {
                Service.gI().sendThongBao(player, "Có lỗi xảy ra, vui lòng thử lại!");
                e.printStackTrace();
                return;
            }

            Service.gI().sendThongBao(player, "Mở TV Free thành công!!");
            return;
        }
        
        if (select == index++) {
            nro.models.services.SpinRewardService.gI().showConfirmClaim(player, this);
            return;
        }
        
        if (!player.getSession().actived) {
            if (select == index++) {
                this.createOtherMenu(player, ConstNpc.CONFIRM_KICH_HOAT_TAI_KHOAN,
                        "Phí kích hoạt là " + nro.models.utils.Util.formatNumber(nro.models.server.Manager.ACTIVATION_FEE) + " VNĐ.\nBạn được mở khóa toàn bộ tính năng,\nđược thưởng 10.000 Ngọc xanh và 1.000 thỏi vàng.",
                        "Đồng ý", "Từ chối");
                return;
            }
        }
        
        if (select == index++) {
            Input.gI().createFormDoiNgoc(player);
            return;
        }
        
        if (player.getSession().xacNhanGioiThieu == 0) {
            if (select == index++) {
                if (!player.getSession().actived) {
                    Service.gI().sendThongBao(player, "Vui lòng kích hoạt tài khoản để sử dụng chức năng này!");
                    return;
                }
                Input.gI().createFormNhapMaGioiThieu(player);
                return;
            }
        } else {
            if (select == index++) {
                Service.gI().sendThongBaoOK(player, "Bạn đã nhập mã giới thiệu rồi!\nMã giới thiệu của bạn là: " + player.getSession().maGioiThieu);
                return;
            }
        }
    } else if (player.idMark.getIndexMenu() == ConstNpc.CONFIRM_KICH_HOAT_TAI_KHOAN) {
        if (select == 0) {
            if (player.getSession().actived) {
                Service.gI().sendThongBao(player, "Tài khoản của bạn đã được kích hoạt rồi!");
                return;
            }
            if (nro.models.database.PlayerDAO.subvnd(player, nro.models.server.Manager.ACTIVATION_FEE)) {
                player.getSession().actived = true;
                try {
                    LocalManager.executeUpdate("UPDATE account SET active = 1 WHERE id = ?", player.getSession().userId);
                    if (player.getSession().xacNhanGioiThieu != 0) {
                        LocalManager.executeUpdate("UPDATE account SET vnd = vnd + 10000 WHERE id = ?", player.getSession().xacNhanGioiThieu);
                    }
                } catch (Exception e) {}
                
                player.inventory.gem += 10000;
                Service.gI().sendMoney(player);
                
                nro.models.item.Item item = nro.models.services.ItemService.gI().createNewItem((short) 457, 1000);
                nro.models.services.InventoryService.gI().addItemBag(player, item);
                nro.models.services.InventoryService.gI().sendItemBags(player);
                
                Service.gI().sendThongBao(player, "Kích hoạt tài khoản thành công! Bạn nhận được 10.000 Ngọc xanh và 1.000 thỏi vàng.");
            } else {
                Service.gI().sendThongBao(player, "Số dư VNĐ không đủ để kích hoạt, vui lòng nạp thêm!");
            }
        }
    } else if (player.idMark.getIndexMenu() == ConstNpc.CONFIRM_CLAIM_SPIN_REWARD) {
        if (select == 0) {
            nro.models.services.SpinRewardService.gI().claimReward(player);
        }
    }
}}