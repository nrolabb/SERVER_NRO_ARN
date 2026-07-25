package nro.models.services.shenron;

import nro.models.network.Message;
import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.player.Player;
import lombok.Getter;
import lombok.Setter;
import nro.models.map.Zone;
import nro.models.server.Client;
import nro.models.services.ItemService;
import nro.models.services.InventoryService;
import nro.models.map.service.NpcService;
import nro.models.services.Service;
import nro.models.services.IntrinsicService;
import nro.models.skill.Skill;
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
            = "Ta là Rồng Băng, ngươi có 5 phút để đưa ra 1 điều ước:\n1) Đổi skill 1, 2 đệ tử.\n2) Đổi skill 2, 3 đệ tử.\n3) Đổi skill 3, 4 đệ tử.\n4) Đổi skill 4, 5 đệ tử.";

    public static final String[] SHENRON_WISHES
            = new String[]{"Đổi skill 1-2 đệ", "Đổi skill 2-3 đệ", "Đổi skill 3-4 đệ", "Đổi skill 4-5 đệ"};

    public static final String SHENRON_HALLOWEEN_SAY
            = "Ta là Rồng Xương, ngươi có 5 phút để đưa ra 1 điều ước:\n1) Đổi skill 1, 2 đệ tử.\n2) Đổi skill 2, 3 đệ tử.\n3) Đổi skill 3, 4 đệ tử.\n4) Đổi skill 4, 5 đệ tử.";

    public static final String[] SHENRON_HALLOWEEN_WISHES
            = new String[]{"Đổi skill 1-2 đệ", "Đổi skill 2-3 đệ", "Đổi skill 3-4 đệ", "Đổi skill 4-5 đệ"};

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

    private boolean changePetSkills(int firstSkill, int secondSkill) {
        if (player.pet == null) {
            Service.gI().sendThongBao(player, "Ngươi làm gì có đệ tử?");
            sendBlackGokuhesShenron();
            return false;
        }
        if (!hasPetSkill(firstSkill) || !hasPetSkill(secondSkill)) {
            Service.gI().sendThongBao(player, "Ít nhất đệ tử ngươi phải có chiêu " + secondSkill + " chứ!");
            sendBlackGokuhesShenron();
            return false;
        }
        openPetSkill(firstSkill);
        openPetSkill(secondSkill);
        return true;
    }

    private boolean hasPetSkill(int skillNumber) {
        int index = skillNumber - 1;
        return player.pet.playerSkill.skills.size() > index
                && player.pet.playerSkill.skills.get(index).skillId != -1;
    }

    private void openPetSkill(int skillNumber) {
        switch (skillNumber) {
            case 1:
                Skill skill = SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1);
                if (skill != null) {
                    skill.coolDown = 1000;
                    player.pet.playerSkill.skills.set(0, skill);
                }
                break;
            case 2:
                player.pet.openSkill2();
                break;
            case 3:
                player.pet.openSkill3();
                break;
            case 4:
                player.pet.openSkill4();
                break;
            case 5:
                player.pet.openSkill5();
                break;
        }
    }

    public void confirmWish() {
        switch (player.idMark.getShenronType()) {
            case 0:
                switch (this.select) {
                    case 0:
                        if (!changePetSkills(1, 2)) {
                            return;
                        }
                        break;
                    case 1:
                        if (!changePetSkills(2, 3)) {
                            return;
                        }
                        break;
                    case 2:
                        if (!changePetSkills(3, 4)) {
                            return;
                        }
                        break;
                    case 3:
                        if (!changePetSkills(4, 5)) {
                            return;
                        }
                        break;
                }
                break;
            case 1:
                switch (this.select) {
                    case 0:
                        if (!changePetSkills(1, 2)) {
                            return;
                        }
                        break;
                    case 1:
                        if (!changePetSkills(2, 3)) {
                            return;
                        }
                        break;
                    case 2:
                        if (!changePetSkills(3, 4)) {
                            return;
                        }
                        break;
                    case 3:
                        if (!changePetSkills(4, 5)) {
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
