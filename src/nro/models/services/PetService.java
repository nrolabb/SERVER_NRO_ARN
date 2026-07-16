package nro.models.services;

import nro.models.consts.ConstPlayer;
import nro.models.item.Item;
import nro.models.player.NewPet;
import nro.models.player.Pet;
import nro.models.player.Player;
import nro.models.map.service.ChangeMapService;
import nro.models.player_system.Template.ItemTemplate;
import nro.models.server.Manager;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 *
 */
public class PetService {

    private static PetService instance;

    public static PetService gI() {
        if (instance == null) {
            instance = new PetService();
        }
        return instance;
    }

    public void createNormalPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createNormalPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, int gender, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false, (byte) gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createMabuPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, true, false, false, false);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Oa oa oa...");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createUubPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, true, false, false, (byte) player.gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }
    
    public void createJirenPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, false,true, (byte) player.gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Xin hãy thu nhận tao làm đệ tử");
            } catch (Exception e) {
            }
        }).start();
    }

    public void createKidBeerPet(Player player, byte... limitPower) {
        new Thread(() -> {
            try {
                createNewPet(player, false, false, true, false, (byte) player.gender);
                if (limitPower != null && limitPower.length == 1) {
                    player.pet.nPoint.limitPower = limitPower[0];
                }
                Thread.sleep(1000);
                Service.gI().chatJustForMe(player, player.pet, "Hãy hợp tác với ta, Kakarot!");
            } catch (Exception e) {
            }
        }).start();
    }

    public void changeNormalPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, gender, limitPower);
    }

    public void changeNormalPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createNormalPet(player, limitPower);
    }

    public void changeMabuPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, limitPower);
    }

    public void changeUubPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createUubPet(player, player.pet.gender, limitPower);
    }

    public void changeKidBeerPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createKidBeerPet(player, player.pet.gender, limitPower);
    }
    
    public void changeJirenPet(Player player) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createKidBeerPet(player, player.pet.gender, limitPower);
    }

    public void changeMabuPet(Player player, int gender) {
        byte limitPower = player.pet.nPoint.limitPower;
        if (player.fusion.typeFusion != ConstPlayer.NON_FUSION) {
            player.pet.unFusion();
        }
        ChangeMapService.gI().exitMap(player.pet);
        player.pet.dispose();
        player.pet = null;
        createMabuPet(player, gender, limitPower);
    }

    public void changeNamePet(Player player, String name) {
        try {
            if (!InventoryService.gI().isExistItemBag(player, 400)) {
                Service.gI().sendThongBao(player, "Bạn cần thẻ đặt tên đệ tử, mua tại Santa");
                return;
            } else if (Util.haveSpecialCharacter(name)) {
                Service.gI().sendThongBao(player, "Tên không được chứa ký tự đặc biệt");
                return;
            } else if (name.length() > 10) {
                Service.gI().sendThongBao(player, "Tên quá dài");
                return;
            }
            ChangeMapService.gI().exitMap(player.pet);
            player.pet.name = "$" + name.toLowerCase().trim();
            InventoryService.gI().subQuantityItemsBag(player, InventoryService.gI().findItemBag(player, 400), 1);
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã đặt cho con tên " + name);
                } catch (Exception e) {
                }
            }).start();
        } catch (Exception ex) {

        }
    }

    private int[] getDataPetNormal() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(20, 45); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetMabu() {
        int[] petData = new int[5];
        petData[0] = Util.nextInt(40, 105) * 20; //hp
        petData[1] = Util.nextInt(40, 105) * 20; //mp
        petData[2] = Util.nextInt(50, 120); //dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetUub() {
        int[] petData = new int[5];
        petData[0] = 400_000; // hp
        petData[1] = 400_000; // mp
        petData[2] = 20_000;  // dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private int[] getDataPetKidBeer() {
        int[] petData = new int[5];
        petData[0] = 400_000; // hp
        petData[1] = 400_000; // mp
        petData[2] = 20_000;  // dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }
    
    private int[] getDataPetJiren() {
        int[] petData = new int[5];
        petData[0] = 400_000; // hp
        petData[1] = 400_000; // mp
        petData[2] = 20_000;  // dame
        petData[3] = Util.nextInt(9, 50); //def
        petData[4] = Util.nextInt(0, 2); //crit
        return petData;
    }

    private void createNewPet(Player player, boolean isMabu, boolean isUub, boolean isKidBeer, boolean isJiren, byte... gender) {
        int[] data;

        if (isMabu) {
            data = getDataPetMabu();
        } else if (isUub) {
            data = getDataPetUub();
        } else if (isKidBeer) {
            data = getDataPetKidBeer();
        } else if (isJiren) {
            data = getDataPetJiren();
        } else {
            data = getDataPetNormal();
        }

        Pet pet = new Pet(player);

        pet.name = "$" + (isMabu ? "Mabư" : isUub ? "Goku vô cực" : isKidBeer ? "Kid Beer" : isJiren ? "Kid Jiren" : "Đệ tử");

        pet.gender = (gender != null && gender.length != 0) ? gender[0] : (byte) Util.nextInt(0, 2);

        pet.id = player.isPl() ? -player.id : -Math.abs(player.id) - 100000;

        pet.nPoint.power = isUub ? 40000000000L : isMabu ? 1500000L : isKidBeer ? 40000000000L : isJiren ? 40000000000L : 2000L;

        pet.typePet = (byte) (isMabu ? 1 : isUub ? 2 : isKidBeer ? 3 : isJiren ? 4 : 0);

        pet.nPoint.stamina = 1000;
        pet.nPoint.maxStamina = 1000;
        pet.nPoint.hpg = data[0];
        pet.nPoint.mpg = data[1];
        pet.nPoint.dameg = data[2];
        pet.nPoint.defg = data[3];
        pet.nPoint.critg = data[4];

        int itemBodySize = 15;
        for (int i = 0; i < itemBodySize; i++) {
            pet.inventory.itemsBody.add(ItemService.gI().createItemNull());
        }

        pet.playerSkill.skills.add(SkillUtil.createSkill(Util.nextInt(0, 2) * 2, 1));
        for (int i = 0; i < 4; i++) {
            pet.playerSkill.skills.add(SkillUtil.createEmptySkill());
        }

        pet.nPoint.setFullHpMp();
        player.pet = pet;
    }

    public static void Pet2(Player pl, int h, int b, int l) {
        if (pl.newPet != null) {
            pl.newPet.dispose();
        }
        pl.newPet = new NewPet(pl, (short) h, (short) b, (short) l);
        pl.newPet.name = "$";
        pl.newPet.gender = pl.gender;
        pl.newPet.nPoint.tiemNang = 1;
        pl.newPet.nPoint.power = 1;
        pl.newPet.nPoint.limitPower = 1;
        pl.newPet.nPoint.hpg = 500000000;
        pl.newPet.nPoint.mpg = 500000000;
        pl.newPet.nPoint.hp = 500000000;
        pl.newPet.nPoint.mp = 500000000;
        pl.newPet.nPoint.dameg = 1;
        pl.newPet.nPoint.defg = 1;
        pl.newPet.nPoint.critg = 1;
        pl.newPet.nPoint.stamina = 1;
        pl.newPet.nPoint.setBasePoint();
        pl.newPet.nPoint.setFullHpMp();
    }

    public boolean hasFollowerPetParts(ItemTemplate template) {
        return template != null
                && template.type == 27
                && template.head >= 0
                && template.body >= 0
                && template.leg >= 0;
    }

    public boolean isValidFollowerPetTemplate(ItemTemplate template) {
        return hasFollowerPetParts(template)
                && Manager.isPartType(template.head, 0)
                && Manager.isPartType(template.body, 1)
                && Manager.isPartType(template.leg, 2);
    }

    public boolean isFollowerPetItem(Item item) {
        return item != null
                && item.isNotNullItem()
                && item.template.type == 27
                && (isValidFollowerPetTemplate(item.template)
                || getLegacyFollowerPetParts(item.template.id) != null);
    }

    public boolean summonFollowerPet(Player player, Item item) {
        if (player == null || item == null || !item.isNotNullItem() || item.template.type != 27) {
            return false;
        }
        if (isValidFollowerPetTemplate(item.template)) {
            Pet2(player, item.template.head, item.template.body, item.template.leg);
            return true;
        }
        short[] parts = getLegacyFollowerPetParts(item.template.id);
        if (parts == null) {
            return false;
        }
        Pet2(player, parts[0], parts[1], parts[2]);
        return true;
    }

    public void removeFollowerPet(Player player) {
        if (player != null && player.newPet != null) {
            player.newPet.dispose();
            player.newPet = null;
        }
    }

    private short[] getLegacyFollowerPetParts(int itemId) {
        return switch (itemId) {
            case 892 -> new short[]{882, 883, 884};
            case 893 -> new short[]{885, 886, 887};
            case 908 -> new short[]{891, 892, 893};
            case 909 -> new short[]{894, 895, 896};
            case 910 -> new short[]{897, 898, 899};
            case 916 -> new short[]{925, 926, 927};
            case 917 -> new short[]{928, 929, 930};
            case 918 -> new short[]{931, 932, 933};
            case 919 -> new short[]{934, 935, 936};
            case 936 -> new short[]{718, 719, 720};
            case 942 -> new short[]{966, 967, 968};
            case 943 -> new short[]{969, 970, 971};
            case 944 -> new short[]{972, 973, 974};
            case 967 -> new short[]{1050, 1051, 1052};
            case 1008 -> new short[]{1074, 1075, 1076};
            case 1039 -> new short[]{1089, 1090, 1091};
            case 1040 -> new short[]{1092, 1093, 1094};
            case 1046 -> new short[]{-1, -1, -1};
            case 1107 -> new short[]{1155, 1156, 1157};
            case 1114 -> new short[]{1158, 1159, 1160};
            case 1188 -> new short[]{1183, 1184, 1185};
            case 1202, 1203 -> new short[]{1201, 1202, 1203};
            case 1207 -> new short[]{1077, 1078, 1079};
            case 1224 -> new short[]{1227, 1228, 1229};
            case 1225 -> new short[]{1233, 1234, 1235};
            case 1226 -> new short[]{1230, 1231, 1232};
            case 1243 -> new short[]{1245, 1246, 1247};
            case 1244 -> new short[]{1248, 1249, 1250};
            case 1256 -> new short[]{1267, 1268, 1269};
            case 1318 -> new short[]{1299, 1300, 1301};
            case 1347 -> new short[]{1302, 1303, 1304};
            case 1414 -> new short[]{1341, 1342, 1343};
            case 1435 -> new short[]{1347, 1348, 1349};
            case 1452 -> new short[]{1365, 1366, 1367};
            case 1458 -> new short[]{1368, 1369, 1370};
            case 1482 -> new short[]{1398, 1399, 1400};
            case 1497 -> new short[]{1401, 1402, 1403};
            case 1550 -> new short[]{1428, 1429, 1430};
            case 1551 -> new short[]{1425, 1426, 1427};
            case 1564 -> new short[]{1437, 1438, 1439};
            case 1568 -> new short[]{1443, 1444, 1445};
            case 1573 -> new short[]{1446, 1447, 1448};
            case 1596, 1597 -> new short[]{1473, 1474, 1475};
            case 1611 -> new short[]{1488, 1494, 1495};
            case 1620, 1621 -> new short[]{1496, 1497, 1498};
            case 1622 -> new short[]{1488, 1489, 1490};
            case 1629 -> new short[]{1505, 1506, 1507};
            case 1630 -> new short[]{1508, 1509, 1510};
            case 1631 -> new short[]{1513, 1516, 1517};
            case 1633 -> new short[]{1523, 1524, 1525};
            case 1654 -> new short[]{1526, 1529, 1530};
            case 1668 -> new short[]{1550, 1551, 1552};
            case 1682 -> new short[]{1558, 1559, 1560};
            case 1683 -> new short[]{1561, 1562, 1563};
            case 1686 -> new short[]{1572, 1573, 1574};
            case 1727 -> new short[]{1616, 1617, 1618};
            case 1729 -> new short[]{1621, 1622, 1623};
            case 1750 -> new short[]{1464, 1465, 1466};
            case 1765 -> new short[]{1662, 1663, 1664};
            case 1766 -> new short[]{1665, 1666, 1667};
            case 1767 -> new short[]{1668, 1669, 1670};
            case 1768 -> new short[]{1671, 1672, 1673};
            case 1769 -> new short[]{1674, 1675, 1676};
            case 1770 -> new short[]{1677, 1678, 1679};
            case 1771 -> new short[]{1680, 1681, 1682};
            case 1789 -> new short[]{1724, 1725, 1726};
            case 1900 -> new short[]{1968, 1969, 1970};
            default -> null;
        };
    }
}
