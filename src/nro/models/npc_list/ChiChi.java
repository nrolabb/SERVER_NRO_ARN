// package nro.models.npc_list;

// import nro.models.consts.ConstNpc;
// import nro.models.item.Item;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import nro.models.npc.Npc;
// import nro.models.player.Player;
// import nro.models.services.InventoryService;
// import nro.models.server.Manager;
// import nro.models.services.Service;
// import nro.models.services_func.Input;
// import nro.models.shop.ShopService;

// /**
//  *
//  * @author By Mr Blue
//  *
//  */
// public class ChiChi extends Npc {

//     public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
//         super(mapId, status, cx, cy, tempId, avartar);
//     }

//     @Override
//     public void openBaseMenu(Player player) {
//         if (canOpenNpc(player)) {
//             List<String> menu = new ArrayList<>(Arrays.asList(
//                     "Top\nHộp quà\nthiếu nhi\n2025",
//                     "Top\nNước mía",
//                     "Top\nKem trái cây",
//                     "Cửa hàng",
//                     "Đóng"));

//             String[] menus = menu.toArray(new String[0]);

//             createOtherMenu(player, ConstNpc.BASE_MENU,
//                     "Bạn muốn hỏi chi?", menus);
//         }

//     }

//     @Override
//     public void confirmMenu(Player player, int select) {
//         if (canOpenNpc(player)) {
//             int soLuong = 0;
//             if (this.mapId == 5) {
//                 if (player.idMark.isBaseMenu()) {
//                     switch (select) {
//                         case 3:
//                             ShopService.gI().opendShop(player, "SHOP_CHI_CHI", false);
//                             break;
//                         case 0:
//                             createOtherMenu(player, ConstNpc.PHAO_BONG_VIP,
//                                     "Sự kiện đua top Hộp quà thiếu nhi nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
//                                     "Top 100\nHộp quà\nthiếu nhi\n2025",
//                                     "Xem điểm",
//                                     "Đóng");
//                             break;
//                         case 1:
//                             createOtherMenu(player, ConstNpc.PHAO_BONG,
//                                     "Sự kiện đua top Nước mía nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
//                                     "Top 100\nNước mía",
//                                     "Xem điểm",
//                                     "Đóng");
//                             break;
//                         case 2:
//                             createOtherMenu(player, ConstNpc.GOKU_DAY,
//                                     "Sự kiện đua top Kem trái cây nhận quà khủng\n Kết thúc và trao giải sau (....)\nHạn chót nhận giải: (15 ngày nữa)\nĐến gặp ChiChi để nhận giải nhé\nChi tiết xem tại diễn đàn, Fanpage",
//                                     "Top 100\nKem trái cây",
//                                     "Xem điểm",
//                                     "Đóng");
//                             break;
//                     }
//                 } else if (player.idMark.getIndexMenu() == ConstNpc.PHAO_BONG_VIP) {
//                     switch (select) {
//                         case 0:
//                             Service.gI().showListTop(player, Manager.Topsukien);
//                             break;
//                         case 1:
//                             Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien + " điểm Hộp quà thiếu nhi.");
//                             break;
//                     }
//                 } else if (player.idMark.getIndexMenu() == ConstNpc.PHAO_BONG) {
//                     switch (select) {
//                         case 0:
//                             Service.gI().showListTop(player, Manager.Topsukien1);
//                             break;
//                         case 1:
//                             Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien1 + " điểm Nước mía.");
//                             break;
//                     }
//                 } else if (player.idMark.getIndexMenu() == ConstNpc.GOKU_DAY) {
//                     switch (select) {
//                         case 0:
//                             Service.gI().showListTop(player, Manager.Topsukien2);
//                             break;
//                         case 1:
//                             Service.gI().sendThongBao(player, "Bạn có " + player.point_sukien2 + " điểm Kem trái cây.");
//                             break;
//                     }
//                 }
//             }
//         }
//     }
// }
package nro.models.npc_list;

import nro.models.consts.ConstNpc;
import nro.models.item.Item;
import nro.models.npc.Npc;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.Service;
import nro.models.utils.Util;

public class ChiChi extends Npc {

    private static final int REQUIRED_PIECE = 10;
    private static final int CHARM_DURATION_MINUTES = 30 * 24 * 60;
    private static final int[][] COSTUME_EXCHANGES = {
        {1978, 528},
        {1979, 527},
        {1980, 526},
        {1981, 634},
        {1982, 633},
        {1983, 583},
        {1984, 550},
        {1985, 551},
        {1986, 552},
        {1987, 525},
        {1988, 524},
        {1989, 407},
        {1990, 406},
        {1991, 405},
        {1992, 433},
        {1993, 432},
        {1994, 431},
        {1995, 430},
        {1996, 429}
    };
    private static final String[] COSTUME_MENU = {
        "Xên\nhoàn thiện",
        "Xên bọ\nhung 2",
        "Xên bọ\nhung 1",
        "King\nKong",
        "Pic",
        "Poc",
        "Android\n13",
        "Android\n14",
        "Android\n15",
        "Android\n20",
        "Android\n19",
        "Fide\ncấp 3",
        "Fide\ncấp 2",
        "Fide\ncấp 1",
        "Tiểu đội\ntrưởng",
        "Số 1",
        "Số 2",
        "Số 3",
        "Số 4",
        "Đóng"
    };

    public ChiChi(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

@Override
public void openBaseMenu(Player player) {
    if (canOpenNpc(player)) {
        createOtherMenu(
                player,
                ConstNpc.BASE_MENU,
                "Chi sẽ giúp bạn loại bỏ những trang bị có HSD ở hành trang\n(trang bị đang mặc không bị ảnh hưởng)\nHãy đọc kỹ rồi chọn nhé",
                "Hủy bỏ\ntrang bị\ncó hsd",
                "Thông tin\nsự kiện mới",
                "Đổi\ncải trang",
                "Đổi\nbùa",
                "Đóng"
        );
    }
}



@Override
public void confirmMenu(Player player, int select) {
    if (!canOpenNpc(player)) return;

    if (player.idMark.isBaseMenu()) {
        switch (select) {
            case 0:
                vutTrangBiHSD(player);
                break;

            case 1:
                Service.gI().sendThongBaoOK(player,
                        "Sự kiện mới sẽ update sau 7 ngày Open Server.");
                break;

            case 2:
                createOtherMenu(player, ConstNpc.MENU_CHI_CHI_EXCHANGE_COSTUME,
                        "Dùng 10 mảnh tương ứng để đổi 1 cải trang:\nCải trang ở Tương Lai sẽ mạnh hơn ở Hiện Tại,\nCải trang Xên Bọ Hung sẽ có thể SĐCM.",
                        COSTUME_MENU);
                break;

            case 3:
                createOtherMenu(player, ConstNpc.MENU_CHI_CHI_EXCHANGE_CHARM,
                        "Dùng 10 mảnh Bùa để đổi bùa đặc biệt.\nBùa nhận được sẽ có tác dụng ngay 30 ngày.",
                        "Bùa X3",
                        "Bùa X4",
                        "Đóng");
                break;

            case 4:
                // Đóng
                break;
        }
    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_CHI_CHI_EXCHANGE_COSTUME) {
        if (select >= 0 && select < COSTUME_EXCHANGES.length) {
            exchangeCostume(player, COSTUME_EXCHANGES[select][0], COSTUME_EXCHANGES[select][1]);
        }
    } else if (player.idMark.getIndexMenu() == ConstNpc.MENU_CHI_CHI_EXCHANGE_CHARM) {
        switch (select) {
            case 0 ->
                exchangeCharmX3(player);
            case 1 ->
                exchangeCharmX4(player);
        }
    }
}

private void exchangeCharmX3(Player player) {
    Item piece = InventoryService.gI().findItemBag(player, 1998);
    if (piece == null || piece.quantity < REQUIRED_PIECE) {
        Service.gI().sendThongBao(player, "Bạn chưa đủ " + REQUIRED_PIECE + " mảnh Bùa X3.");
        return;
    }
    int requiredEmpty = piece.quantity == REQUIRED_PIECE ? 1 : 2;
    if (InventoryService.gI().getCountEmptyBag(player) < requiredEmpty) {
        Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống.");
        return;
    }

    InventoryService.gI().subQuantityItemsBag(player, piece, REQUIRED_PIECE);

    Item charm = ItemService.gI().createNewItem((short) 671, 1);
    Item nextPiece = ItemService.gI().createNewItem((short) 1999, 1);
    InventoryService.gI().addItemBag(player, charm);
    InventoryService.gI().addItemBag(player, nextPiece);
    player.charms.addTimeCharms(671, CHARM_DURATION_MINUTES);
    InventoryService.gI().sendItemBags(player);
    Service.gI().sendThongBao(player, "Đổi thành công Bùa X3");
}

private void exchangeCharmX4(Player player) {
    Item piece = InventoryService.gI().findItemBag(player, 1999);
    if (piece == null || piece.quantity < REQUIRED_PIECE) {
        Service.gI().sendThongBao(player, "Bạn chưa đủ " + REQUIRED_PIECE + " mảnh bùa X4.");
        return;
    }
    int requiredEmpty = piece.quantity == REQUIRED_PIECE ? 0 : 1;
    if (InventoryService.gI().getCountEmptyBag(player) < requiredEmpty) {
        Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống.");
        return;
    }

    InventoryService.gI().subQuantityItemsBag(player, piece, REQUIRED_PIECE);

    Item charm = ItemService.gI().createNewItem((short) 672, 1);
    InventoryService.gI().addItemBag(player, charm);
    player.charms.addTimeCharms(672, CHARM_DURATION_MINUTES);
    InventoryService.gI().sendItemBags(player);
    Service.gI().sendThongBao(player, "Đổi thành công Bùa X4.");
}

private void exchangeCostume(Player player, int pieceId, int costumeId) {
    Item piece = InventoryService.gI().findItemBag(player, pieceId);
    if (piece == null || piece.quantity < REQUIRED_PIECE) {
        Service.gI().sendThongBao(player, "Bạn chưa đủ " + REQUIRED_PIECE + " mảnh cải trang.");
        return;
    }
    if (InventoryService.gI().getCountEmptyBag(player) <= 0 && piece.quantity > REQUIRED_PIECE) {
        Service.gI().sendThongBao(player, "Hành trang đã đầy.");
        return;
    }

    InventoryService.gI().subQuantityItemsBag(player, piece, REQUIRED_PIECE);

    Item costume = ItemService.gI().createNewItem((short) costumeId);
    addCostumeOptions(costume, costumeId);
    InventoryService.gI().addItemBag(player, costume);
    InventoryService.gI().sendItemBags(player);
    Service.gI().sendThongBao(player, "Đổi thành công " + REQUIRED_PIECE + " mảnh lấy " + costume.template.name + ".");
}

private void addCostumeOptions(Item costume, int costumeId) {
    if (isXenCostume(costumeId)) {
        int param = Util.nextInt(5, 30);
        costume.itemOptions.add(new Item.ItemOption(50, param));
        costume.itemOptions.add(new Item.ItemOption(77, param));
        costume.itemOptions.add(new Item.ItemOption(103, param));
        costume.itemOptions.add(new Item.ItemOption(5, param));
        costume.itemOptions.add(new Item.ItemOption(93, Util.nextInt(3, 5)));
    } else if (isSpecialCostume(costumeId)) {
        int param = Util.nextInt(5, 20);
        costume.itemOptions.add(new Item.ItemOption(50, param));
        costume.itemOptions.add(new Item.ItemOption(77, param));
        costume.itemOptions.add(new Item.ItemOption(103, param));
        costume.itemOptions.add(new Item.ItemOption(101, param));
        costume.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 3)));
    } else {
        int param = Util.nextInt(5, 25);
        costume.itemOptions.add(new Item.ItemOption(50, param));
        costume.itemOptions.add(new Item.ItemOption(77, param));
        costume.itemOptions.add(new Item.ItemOption(103, param));
        costume.itemOptions.add(new Item.ItemOption(101, param));
        costume.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 5)));
    }
}

private boolean isXenCostume(int costumeId) {
    return costumeId == 526 || costumeId == 527 || costumeId == 528;
}

private boolean isSpecialCostume(int costumeId) {
    return costumeId == 429 || costumeId == 430 || costumeId == 431 || costumeId == 432
            || costumeId == 433 || costumeId == 405 || costumeId == 406 || costumeId == 407;
}

    /**
     * Vứt (xóa hẳn) toàn bộ trang bị trong hành trang có option id = 93
     */
  private void vutTrangBiHSD(Player player) {
    boolean daXoa = false;

    for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
        Item item = player.inventory.itemsBag.get(i);

        if (item == null || !item.isNotNullItem()) continue;

        for (int j = 0; j < item.itemOptions.size(); j++) {
            if (item.itemOptions.get(j).optionTemplate.id == 93) {

                // ✅ TẠO ITEM RỖNG (KHÔNG DÙNG NULL)
                Item empty = new Item();
                empty.template = null;

                player.inventory.itemsBag.set(i, empty);
                daXoa = true;
                break;
            }
        }
    }

    if (!daXoa) {
        Service.gI().sendThongBao(player,
                "Hành trang không có trang bị nào có hạn sử dụng.");
        return;
    }

    InventoryService.gI().sendItemBags(player);
    Service.gI().sendThongBao(player,
            "Đã vứt bỏ toàn bộ trang bị có hạn sử dụng.");
}

}
