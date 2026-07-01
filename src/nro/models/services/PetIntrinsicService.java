package nro.models.services;

import nro.models.consts.ConstItem;
import nro.models.intrinsic.PetIntrinsic;
import nro.models.item.Item;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.utils.Util;

public class PetIntrinsicService {

    private static final long REQUIRED_POWER = 20_000_000_000L;
    private static final int REQUIRED_GOLD_BAR = 100;
    private static final byte[] TYPES = {
        PetIntrinsic.DAME,
        PetIntrinsic.HP,
        PetIntrinsic.MP,
        PetIntrinsic.CRIT,
        PetIntrinsic.EXP,
        PetIntrinsic.FUSION_DAME,
        PetIntrinsic.FUSION_HP,
        PetIntrinsic.FUSION_MP,
        PetIntrinsic.FUSION_CRIT,
        PetIntrinsic.SKILL_2_SPEED
    };

    private static PetIntrinsicService instance;

    public static PetIntrinsicService gI() {
        if (instance == null) {
            instance = new PetIntrinsicService();
        }
        return instance;
    }

    public void openOrChange(Player player) {
        if (player == null || player.pet == null) {
            Service.gI().sendThongBao(player, "Bạn chưa có đệ tử");
            return;
        }
        Pet pet = player.pet;
        if (pet.nPoint.power < REQUIRED_POWER) {
            Service.gI().sendThongBao(player, "Đệ tử cần đạt 20 tỷ sức mạnh");
            return;
        }
        Item thoiVang = InventoryService.gI().findItemBag(player, ConstItem.THOI_VANG);
        if (thoiVang == null || !thoiVang.isNotNullItem() || thoiVang.quantity < REQUIRED_GOLD_BAR) {
            Service.gI().sendThongBao(player, "Bạn không đủ 100 thỏi vàng");
            return;
        }
        InventoryService.gI().subQuantityItemsBag(player, thoiVang, REQUIRED_GOLD_BAR);
        InventoryService.gI().sendItemBags(player);

        if (pet.petIntrinsic == null) {
            pet.petIntrinsic = new PetIntrinsic();
        }
        byte type = TYPES[Util.nextInt(0, TYPES.length - 1)];
        short param = (short) (Util.isTrue(70, 100) ? Util.nextInt(5, 19) : Util.nextInt(20, 30));
        pet.petIntrinsic.set(type, param);
        pet.nPoint.calPoint();
        player.nPoint.calPoint();
        Service.gI().point(player);
        Service.gI().showInfoPet(player);
        Service.gI().sendThongBao(player, "Đệ tử nhận được Nội tại:\n" + pet.petIntrinsic.getDisplayName());
    }
}
