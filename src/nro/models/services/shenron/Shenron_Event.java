package nro.models.services.shenron;

import nro.models.network.Message;
import nro.models.consts.ConstNpc;
import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.player.Player;
import lombok.Getter;
import lombok.Setter;
import nro.models.map.Zone;
import nro.models.server.Client;
import nro.models.services.ItemService;
import nro.models.services.InventoryService;
import nro.models.services.ItemTimeService;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.services.IntrinsicService;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class Shenron_Event {

    @Setter
    @Getter
    private Player player;

    @Setter
    @Getter
    private Zone zone;

    public long playerId;
    public boolean isPlayerDisconnect;
    public byte select;
    public int shenronType;
    public boolean leaveMap;

    public static final byte WISHED = 0;
    public static final byte TIME_UP = 1;

    public static final byte DRAGON_EVENT = 1;
    public static final byte DRAGON_SUPER_EVENT = 50;

    public long lastTimeShenronWait;
    public static int timeResummonShenron = 60000;
    public static int timeShenronWait = 60000;

    public static final String SHENRONEVENT_SAY
            = "Ta sẽ ban cho người 1 điều ước, ngươi có 5 phút, hãy chọn đi:\n1) Đổi skill 3, 4 đệ tử (có thể trùng skill trước đó).\n2) Thay đổi nội tại.\n3) Cải trang siêu thần HSD 90 ngày.\n4) Cải trang Black Gohan Rose HSD 90 ngày.";

    public static final String[] SHENRON_WISHES
            = new String[]{"Điều ước 1", "Điều ước 2", "Điều ước 3", "Điều ước 4"};

    public static final String SHENRON_HALLOWEEN_SAY
            = "Ta là Rồng Bí Ngô, ngươi có 5 phút để đưa ra 1 điều ước:\n1) Rồng Xương (+10% chỉ số).\n2) 20 Triệu Vàng.\n3) 1000 Ngọc Xanh.\n4) Đổi chiêu đệ tử.";

    public static final String[] SHENRON_HALLOWEEN_WISHES
            = new String[]{"Rồng Xương", "20Tr Vàng", "1000 Ngọc", "Đổi chiêu đệ"};

    public static final String SHENRON_SUPER_SAY
            = "Ta là Rồng Siêu Cấp, ngươi có 5 phút để đưa ra 1 điều ước VIP:\n1) Gói Tài Sản (50Tr Vàng & 5000 Ngọc).\n2) Đổi Nội Tại VIP.\n3) Cải trang Black Gohan Rose.\n4) Đệ tử siêu cấp (+20 Tỷ SM).";

    public static final String[] SHENRON_SUPER_WISHES
            = new String[]{"Tài Sản VIP", "Nội Tại VIP", "Cải Trang", "+20Tỷ SM đệ"};

    public boolean shenronLeave;

    public void update() {
        try {
            if (!shenronLeave) {
                if (isPlayerDisconnect) {
                    Player pl = Client.gI().getPlayer(playerId);
                    if (pl != null) {
                        player = pl;
                        if (player.zone != null && player.zone.map.mapId != 0 && player.zone.map.mapId != 7 && player.zone.map.mapId != 14
                                && player.zone.map.mapId != 21 && player.zone.map.mapId != 22 && player.zone.map.mapId != 23) {
                            player.shenronEvent = this;
                            zone = player.zone;
                            player.idMark.setShenronType(shenronType);
                            isPlayerDisconnect = false;
                            reSummonShenron();
                        }
                    }
                }
                if (Util.canDoWithTime(lastTimeShenronWait, timeShenronWait)) {
                    leaveMap = true;
                    NpcService.gI().createMenuRongThieng(player, ConstNpc.IGNORE_MENU, "Còn cái nịt =))\nCó không ước mất đừng tìm.", "Xin vĩnh biệt cụ........");
                    shenronLeave();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reSummonShenron() {
        activeShenron(true, player.idMark.getShenronType() == 2 ? DRAGON_SUPER_EVENT : DRAGON_EVENT);
        sendBlackGokuhesShenron();
    }

    public void sendBlackGokuhesShenron() {
        if (player.idMark.getShenronType() == 1) {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM, SHENRON_HALLOWEEN_SAY, SHENRON_HALLOWEEN_WISHES);
        } else if (player.idMark.getShenronType() == 2) {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_SUPER_EVENT_CONFIRM, SHENRON_SUPER_SAY, SHENRON_SUPER_WISHES);
        } else {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHOW_SHENRON_EVENT_CONFIRM, SHENRONEVENT_SAY, SHENRON_WISHES);
        }
    }

    public void showConfirmShenron(byte select) {
        this.select = select;
        String wish = null;
        switch (player.idMark.getShenronType()) {
            case 0:
                wish = SHENRON_WISHES[select];
                break;
            case 1:
                wish = SHENRON_HALLOWEEN_WISHES[select];
                break;
            case 2:
                wish = SHENRON_SUPER_WISHES[select];
                break;
        }
        if (player.idMark.getShenronType() == 2) {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_SUPER_EVENT_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
        } else {
            NpcService.gI().createMenuRongThieng(player, ConstNpc.SHENRON_EVENT_CONFIRM, "Ngươi có chắc muốn ước?", wish, "Từ chối");
        }
    }

    public void activeShenron(boolean appear, byte type) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(appear ? 0 : (byte) 1);
            if (appear) {
                msg.writer().writeShort(player.zone.map.mapId);
                msg.writer().writeShort(player.zone.map.bgId);
                msg.writer().writeByte(player.zone.zoneId);
                msg.writer().writeInt((int) player.id);
                msg.writer().writeUTF("null");
                msg.writer().writeShort(player.location.x);
                msg.writer().writeShort(player.location.y);
                msg.writer().writeByte(type);
                playerId = player.id;
                shenronType = player.idMark.getShenronType();
                zone.shenronType = shenronType;
                lastTimeShenronWait = System.currentTimeMillis();
                player.isShenronAppear = true;
            }
            Service.gI().sendMessAllPlayerInMap(player, msg);
        } catch (Exception e) {
        }
    }

    public void confirmWish() {
        switch (player.idMark.getShenronType()) {
            case 0:
                switch (this.select) {
                     case 0: //thay chiêu 3-4 đệ tử
                        if (player.pet != null) {
                            if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                player.pet.openSkill3();
                                if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                    player.pet.openSkill4();
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                    case 1:
                        if (player.getSession().player.nPoint.power >= 10_000_000_000L) {
                        IntrinsicService.gI().doinoitai(player);
                          } else {
                            Service.gI().sendThongBao(player, "10Tỷ Sức Mạnh?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                        
                    case 2:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            byte gender = this.player.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 905
                                    : gender == ConstPlayer.NAMEC ? 907 : 911));
                            avtVip.itemOptions.add(new Item.ItemOption(50, 22));
                            avtVip.itemOptions.add(new Item.ItemOption(47, 400));
                            avtVip.itemOptions.add(new Item.ItemOption(108, 30));
                            avtVip.itemOptions.add(new Item.ItemOption(33, 1));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 90));
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 3:
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            byte gender = this.player.gender;
                            Item avtVip = ItemService.gI().createNewItem((short) (gender == ConstPlayer.TRAI_DAT ? 883
                                    : gender == ConstPlayer.NAMEC ? 883 : 883));
                            avtVip.itemOptions.add(new Item.ItemOption(50, 24));
                            avtVip.itemOptions.add(new Item.ItemOption(14, 3));
                            avtVip.itemOptions.add(new Item.ItemOption(103, 19));
                            avtVip.itemOptions.add(new Item.ItemOption(80, 10));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 90));
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 4:// Tăng hp, ki, sd
                        int timeRX = player.itemTime.timeRX / 1000 + 1800;
                        int maxTimeInSeconds = 32767;
                        if (timeRX >= maxTimeInSeconds) {
                            Service.gI().sendThongBao(player, "Ước ít thôi con :v");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemTime.isUseRX = true;
                        player.itemTime.timeRX = timeRX * 1000;
                        player.itemTime.lastTimeUseRX = System.currentTimeMillis();
                        ItemTimeService.gI().sendItemTime(player, 6581, timeRX);
                        player.nPoint.calPoint();
                        player.nPoint.setHp((int) player.nPoint.hpMax);
                        player.nPoint.setMp((int) player.nPoint.mpMax);
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        break;
                    case 99: //quần đang đeo lên 1 cấp
                        Item item = this.player.inventory.itemsBody.get(1);
                        if (item.isNotNullItem()) {
                            int level = 0;
                            for (Item.ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id == 72) {
                                    level = io.param;
                                    if (level < 7) {
                                        io.param++;
                                    }
                                    break;
                                }
                            }
                            if (level < 7) {
                                if (level == 0) {
                                    item.itemOptions.add(new Item.ItemOption(72, 1));
                                }
                                for (Item.ItemOption io : item.itemOptions) {
                                    if (InventoryService.gI().optionCanUpgrade(io.optionTemplate.id)) {
                                        io.param += (io.param * 10 / 100);
                                    }
                                }
                                InventoryService.gI().sendItemBody(player);
                            } else {
                                Service.gI().sendThongBao(player, "Quần của ngươi đã đạt cấp tối đa");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi hiện tại có mang quần đâu");
                            sendBlackGokuhesShenron();
                            return;
                        }
                }
                break;
            case 1:
                switch (this.select) {
                    case 0: // Rồng Xương
                        int timeRX = player.itemTime.timeRX / 1000 + 1800;
                        int maxTimeInSeconds = 32767;
                        if (timeRX >= maxTimeInSeconds) {
                            Service.gI().sendThongBao(player, "Ước ít thôi con :v");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        player.itemTime.isUseRX = true;
                        player.itemTime.timeRX = timeRX * 1000;
                        player.itemTime.lastTimeUseRX = System.currentTimeMillis();
                        ItemTimeService.gI().sendItemTime(player, 6581, timeRX);
                        player.nPoint.calPoint();
                        player.nPoint.setHp((int) player.nPoint.hpMax);
                        player.nPoint.setMp((int) player.nPoint.mpMax);
                        Service.gI().point(player);
                        Service.gI().Send_Info_NV(player);
                        break;
                    case 1: // 20Tr Vàng
                        player.inventory.gold += 20000000;
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được 20 triệu vàng");
                        break;
                    case 2: // 1000 Ngọc
                        player.inventory.gem += 1000;
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được 1000 ngọc xanh");
                        break;
                    case 3: // Đổi chiêu 3-4 đệ tử
                        if (player.pet != null) {
                            if (player.pet.playerSkill.skills.get(2).skillId != -1) {
                                player.pet.openSkill3();
                                if (player.pet.playerSkill.skills.get(3).skillId != -1) {
                                    player.pet.openSkill4();
                                }
                            } else {
                                Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                                sendBlackGokuhesShenron();
                                return;
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                }
                break;
            case 2:
                switch (this.select) {
                    case 0: // Tài Sản VIP
                        player.inventory.gold += 50000000;
                        player.inventory.gem += 5000;
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Bạn nhận được 50 triệu vàng và 5000 ngọc xanh");
                        break;
                    case 1: // Nội Tại VIP
                        if (player.nPoint.power >= 1_000_000_000L) {
                            IntrinsicService.gI().doinoitai(player);
                        } else {
                            Service.gI().sendThongBao(player, "Cần 1 Tỷ Sức Mạnh để đổi nội tại");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                    case 2: // Cải Trang
                        if (InventoryService.gI().getCountEmptyBag(player) > 0) {
                            Item avtVip = ItemService.gI().createNewItem((short) 883); // Black Gohan Rose
                            avtVip.itemOptions.add(new Item.ItemOption(50, 30));
                            avtVip.itemOptions.add(new Item.ItemOption(77, 30));
                            avtVip.itemOptions.add(new Item.ItemOption(103, 30));
                            avtVip.itemOptions.add(new Item.ItemOption(93, 30)); // 30 days
                            InventoryService.gI().addItemBag(player, avtVip);
                            InventoryService.gI().sendItemBags(player);
                        } else {
                            Service.gI().sendThongBao(player, "Hành trang đã đầy");
                            reSummonShenron();
                            return;
                        }
                        break;
                    case 3: // +20Tỷ SM đệ
                        if (player.pet != null) {
                            player.pet.nPoint.power += 20_000_000_000L;
                            player.pet.nPoint.tiemNang += 20_000_000_000L;
                            Service.gI().point(player);
                            Service.gI().Send_Info_NV(player);
                            Service.gI().sendThongBao(player, "Đệ tử của bạn nhận được 20 Tỷ Sức Mạnh và Tiềm Năng");
                        } else {
                            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
                            sendBlackGokuhesShenron();
                            return;
                        }
                        break;
                }
                break;
        }
        shenronLeave();
    }

    public void shenronLeave() {
        if (!shenronLeave) {
            shenronLeave = true;
            if (player != null && player.zone != null) {
                player.shenronEvent = null;
                if (!leaveMap) {
                    NpcService.gI().createTutorial(player, 0, "Điều ước của ngươi đã được thực hiện...tạm biệt");
                }
                activeShenron(false, DRAGON_EVENT);
                player.isShenronAppear = false;
                select = -1;
            }
            zone.shenronType = -1;
            player.lastTimeShenronAppeared = System.currentTimeMillis();
            Shenron_Manager.gI().remove(this);
        }
    }
}
