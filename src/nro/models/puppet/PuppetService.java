package nro.models.puppet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import nro.models.database.PuppetDAO;
import nro.models.item.Item;
import nro.models.map.service.ChangeMapService;
import nro.models.player.Player;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.ItemTimeService;
import nro.models.services.Service;
import nro.models.utils.Logger;

public final class PuppetService {

    public static final int BODY_SLOT = 14;
    public static final int TIME_OPTION_ID = 9;
    private static final int FIRST_STONE_ID = 2027;
    private static final int LAST_STONE_ID = 2030;
    private static final int FIRST_PUPPET_ID = 2041;
    private static final int LAST_PUPPET_ID = 2045;
    private static final int DEATH_PENALTY_MINUTES = 5;
    private static final int[] STONE_MINUTES = {1, 6, 40, 240};

    private static PuppetService instance;
    private volatile Map<Short, PuppetTemplate> templates = Collections.emptyMap();
    private volatile boolean loaded;

    private PuppetService() {
    }

    public static PuppetService gI() {
        if (instance == null) {
            instance = new PuppetService();
        }
        return instance;
    }

    public synchronized void reload() {
        Map<Short, PuppetTemplate> loadedTemplates = new HashMap<>();
        try {
            for (PuppetTemplate template : PuppetDAO.loadTemplates()) {
                loadedTemplates.put(template.getItemTemplateId(), template);
            }
            templates = Collections.unmodifiableMap(loadedTemplates);
            loaded = true;
            Logger.success("Loaded puppet templates (" + templates.size() + ")\n");
        } catch (Exception e) {
            loaded = false;
            Logger.logException(PuppetService.class, e, "Lỗi tải bảng puppet_template");
        }
    }

    private void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    public PuppetTemplate getTemplate(int itemTemplateId) {
        ensureLoaded();
        return templates.get((short) itemTemplateId);
    }

    public boolean isPuppetItem(int itemTemplateId) {
        return getTemplate(itemTemplateId) != null;
    }

    public boolean isPuppetItemId(int itemTemplateId) {
        return itemTemplateId >= FIRST_PUPPET_ID && itemTemplateId <= LAST_PUPPET_ID;
    }

    public boolean isSpiritStone(int itemTemplateId) {
        return itemTemplateId >= FIRST_STONE_ID && itemTemplateId <= LAST_STONE_ID;
    }

    public void usePuppetItem(Player player, Item usedItem, int bagIndex) {
        if (player == null || player.isPet || player.isNewPet || player.isBoss) {
            Service.gI().sendThongBao(player, "Chỉ người chơi mới có thể sử dụng khôi lỗi");
            return;
        }
        if (usedItem == null || !usedItem.isNotNullItem() || !isPuppetItemId(usedItem.template.id)) {
            Service.gI().sendThongBao(player, "Vật phẩm sử dụng không phải khôi lỗi");
            return;
        }
        // Một số client gửi index = -1 và chỉ gửi id item. Tìm lại đúng ô túi
        // thay vì bỏ qua lệnh sử dụng.
        if (bagIndex < 0) {
            bagIndex = InventoryService.gI().getIndexItem(player, player.inventory.itemsBag, usedItem);
        }
        if (bagIndex < 0 || bagIndex >= player.inventory.itemsBag.size()) {
            Service.gI().sendThongBao(player, "Không tìm thấy khôi lỗi trong hành trang");
            return;
        }
        Item item = player.inventory.itemsBag.get(bagIndex);
        if (item != usedItem || !item.isNotNullItem() || !isPuppetItemId(item.template.id)) {
            Service.gI().sendThongBao(player, "Dữ liệu khôi lỗi trong hành trang không hợp lệ");
            return;
        }
        PuppetTemplate template = getTemplate(item.template.id);
        if (template == null) {
            // Cho phép admin thêm config khi server đang chạy rồi sử dụng lại.
            reload();
            template = templates.get((short) item.template.id);
        }
        if (template == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy cấu hình id_temp = "
                    + item.template.id + " trong bảng puppet_template");
            return;
        }
        ensureTimeOption(item, template.getInitialTimeMinutes());
        InventoryService.gI().itemBagToBody(player, bagIndex);
        onEquipmentChanged(player);
    }

    /** Called by InventoryService so drag/drop equip also follows puppet rules. */
    public void onEquipmentChanged(Player player) {
        if (player == null || player.isPet) {
            return;
        }
        Item equipped = getEquippedItem(player);
        if (equipped == null || getTemplate(equipped.template.id) == null) {
            remove(player);
            return;
        }
        PuppetTemplate template = getTemplate(equipped.template.id);
        boolean addedTimeOption = equipped.getOptionById(TIME_OPTION_ID) == null;
        Item.ItemOption timeOption = ensureTimeOption(equipped, template.getInitialTimeMinutes());
        if (addedTimeOption) {
            InventoryService.gI().sendItemBody(player);
        }
        if (timeOption.param <= 0) {
            remove(player);
            Service.gI().sendThongBao(player, "Khôi lỗi đã hết năng lượng, hãy dùng linh thạch để nạp thêm");
            InventoryService.gI().sendItemBody(player);
            return;
        }
        if (player.puppetBoss != null && player.puppetBoss.getPuppetItemId() == equipped.template.id) {
            return;
        }
        remove(player);
        player.puppetBoss = new PuppetBoss(player, template, timeOption.param);
        player.puppetBoss.joinMapMaster();
        sendEquippedTimer(player);
        Service.gI().sendThongBao(player, "Đã triệu hồi " + template.getName() + " trong " + timeOption.param + " phút");
    }

    public void charge(Player player, Item stone) {
        if (player == null || stone == null || !stone.isNotNullItem() || !isSpiritStone(stone.template.id)) {
            return;
        }
        if (player.isPet || player.isNewPet || player.isBoss) {
            Service.gI().sendThongBao(player, "Đệ tử không thể sử dụng linh thạch cho khôi lỗi");
            return;
        }
        synchronized (player) {
            Item puppetItem = getEquippedItem(player);
            PuppetTemplate template = puppetItem == null ? null : getTemplate(puppetItem.template.id);
            if (template == null) {
                Service.gI().sendThongBao(player, "Hãy trang bị khôi lỗi vào ô 14 trước");
                return;
            }
            int puppetLevel = getPuppetLevel(puppetItem.template.id);
            int stoneLevel = stone.template.id - FIRST_STONE_ID + 1;
            int minimumStoneLevel = Math.min(puppetLevel, 4);
            if (stoneLevel < minimumStoneLevel) {
                Service.gI().sendThongBao(player, "Khôi lỗi cấp " + puppetLevel
                        + " cần linh thạch từ cấp " + minimumStoneLevel + " trở lên");
                return;
            }
            int bagIndex = InventoryService.gI().getIndexItem(player, player.inventory.itemsBag, stone);
            if (bagIndex < 0 || stone.quantity <= 0) {
                return;
            }

            Item.ItemOption timeOption = ensureTimeOption(puppetItem, 0);
            int oldParam = timeOption.param;
            int oldQuantity = stone.quantity;
            long oldExpiresAt = player.puppetBoss != null ? player.puppetBoss.getExpiresAt() : 0L;
            int addedMinutes = STONE_MINUTES[stoneLevel - 1];

            if (player.puppetBoss != null) {
                player.puppetBoss.addMinutes(addedMinutes);
                timeOption.param = player.puppetBoss.getRemainingMinutes();
            } else {
                timeOption.param = Math.min(Short.MAX_VALUE, oldParam + addedMinutes);
            }
            boolean removeStone = oldQuantity == 1;
            if (removeStone) {
                player.inventory.itemsBag.set(bagIndex, ItemService.gI().createItemNull());
            } else {
                stone.quantity--;
            }

            try {
                PuppetDAO.saveBagAndBody(player);
            } catch (Exception e) {
                timeOption.param = oldParam;
                if (player.puppetBoss != null) {
                    player.puppetBoss.setExpiresAt(oldExpiresAt);
                }
                stone.quantity = oldQuantity;
                player.inventory.itemsBag.set(bagIndex, stone);
                Logger.logException(PuppetService.class, e, "Lỗi lưu thời gian khôi lỗi");
                Service.gI().sendThongBao(player, "Nạp linh thạch thất bại, vật phẩm chưa bị trừ");
                return;
            }

            if (removeStone) {
                stone.dispose();
            }
            InventoryService.gI().sendItemBags(player);
            InventoryService.gI().sendItemBody(player);
            if (player.puppetBoss == null) {
                onEquipmentChanged(player);
            }
            sendEquippedTimer(player);
            Service.gI().sendThongBao(player, "Đã nạp " + addedMinutes + " phút cho "
                    + puppetItem.template.name + ". Còn " + timeOption.param + " phút");
        }
    }

    public void syncRemainingTime(Player player, PuppetBoss puppet) {
        Item item = getEquippedItem(player);
        if (item == null || item.template.id != puppet.getPuppetItemId()) {
            return;
        }
        int remainingMinutes = puppet.getRemainingMinutes();
        Item.ItemOption option = ensureTimeOption(item, 0);
        if (option.param == remainingMinutes) {
            return;
        }
        synchronized (player) {
            int oldParam = option.param;
            option.param = remainingMinutes;
            try {
                PuppetDAO.saveBagAndBody(player);
                InventoryService.gI().sendItemBody(player);
                sendEquippedTimer(player);
            } catch (Exception e) {
                // Keep the larger persisted value on failure, so a DB error can
                // never make the player lose paid puppet time.
                option.param = oldParam;
                Logger.logException(PuppetService.class, e, "Lỗi đồng bộ thời gian khôi lỗi");
            }
        }
    }

    /**
     * Trừ 5 phút đúng một lần khi HP khôi lỗi về 0. Nếu vẫn còn thời gian thì
     * hồi sinh đầy HP; KI luôn được PuppetBoss giữ đầy trong vòng update.
     */
    public boolean handleDeath(Player player, PuppetBoss puppet) {
        if (player == null || puppet == null || player.puppetBoss != puppet) {
            return false;
        }
        synchronized (player) {
            Item item = getEquippedItem(player);
            if (item == null || item.template.id != puppet.getPuppetItemId()) {
                remove(player);
                return false;
            }

            Item.ItemOption timeOption = ensureTimeOption(item, 0);
            long oldExpiresAt = puppet.getExpiresAt();
            int oldParam = timeOption.param;
            puppet.subtractMinutes(DEATH_PENALTY_MINUTES);
            timeOption.param = puppet.getRemainingMinutes();

            try {
                PuppetDAO.saveBagAndBody(player);
            } catch (Exception e) {
                // Không áp dụng hình phạt nửa vời khi DB chưa ghi được.
                puppet.setExpiresAt(oldExpiresAt);
                timeOption.param = oldParam;
                Logger.logException(PuppetService.class, e, "Lỗi lưu phạt thời gian khi khôi lỗi chết");
                Service.gI().hsChar(puppet, puppet.nPoint.hpMax, puppet.nPoint.mpMax);
                Service.gI().sendThongBao(player, "Không thể lưu trạng thái khôi lỗi, chưa trừ thời gian");
                return true;
            }

            InventoryService.gI().sendItemBody(player);
            if (timeOption.param <= 0) {
                remove(player);
                Service.gI().sendThongBao(player, "Khôi lỗi đã chết và bị trừ 5 phút, thời gian hiệu lực đã hết");
                return false;
            }

            Service.gI().hsChar(puppet, puppet.nPoint.hpMax, puppet.nPoint.mpMax);
            sendEquippedTimer(player);
            Service.gI().sendThongBao(player, "Khôi lỗi đã chết, bị trừ 5 phút. Còn "
                    + timeOption.param + " phút");
            return true;
        }
    }

    public void expire(Player player, PuppetBoss puppet) {
        if (player == null || player.puppetBoss != puppet) {
            return;
        }
        Item item = getEquippedItem(player);
        if (item != null) {
            Item.ItemOption option = ensureTimeOption(item, 0);
            option.param = 0;
            try {
                synchronized (player) {
                    PuppetDAO.saveBagAndBody(player);
                }
            } catch (Exception e) {
                Logger.logException(PuppetService.class, e, "Lỗi lưu trạng thái khôi lỗi hết giờ");
            }
            InventoryService.gI().sendItemBody(player);
        }
        remove(player);
        Service.gI().sendThongBao(player, "Khôi lỗi đã hết thời gian hiệu lực");
    }

    public boolean isEquipped(Player player, int itemTemplateId) {
        Item item = getEquippedItem(player);
        return item != null && item.template.id == itemTemplateId;
    }

    public Item getEquippedItem(Player player) {
        if (player == null || player.inventory == null || player.inventory.itemsBody == null
                || player.inventory.itemsBody.size() <= BODY_SLOT) {
            return null;
        }
        Item item = player.inventory.itemsBody.get(BODY_SLOT);
        return item != null && item.isNotNullItem() ? item : null;
    }

    /** Hiển thị trên HUD bằng cùng message -106 với các item bổ huyết/bổ khí. */
    public void sendEquippedTimer(Player player) {
        Item item = getEquippedItem(player);
        if (item == null || !isPuppetItemId(item.template.id) || getTemplate(item.template.id) == null) {
            return;
        }
        int remainingSeconds;
        if (player.puppetBoss != null && player.puppetBoss.getPuppetItemId() == item.template.id) {
            remainingSeconds = player.puppetBoss.getRemainingSeconds();
        } else {
            Item.ItemOption option = item.getOptionById(TIME_OPTION_ID);
            remainingSeconds = option == null ? 0 : option.param * 60;
        }
        sendTimer(player, item.template.iconID, remainingSeconds);
    }

    private void sendTimer(Player player, int iconId, int remainingSeconds) {
        // Protocol ghi thời gian bằng short. Khi thời lượng lớn hơn giới hạn,
        // timer được làm mới mỗi lần option phút đồng bộ nên không bị số âm.
        int displaySeconds = Math.max(0, Math.min(Short.MAX_VALUE, remainingSeconds));
        ItemTimeService.gI().sendItemTime(player, iconId, displaySeconds);
    }

    private void removeTimer(Player player, int itemTemplateId) {
        nro.models.player_system.Template.ItemTemplate itemTemplate = ItemService.gI().getTemplate((short) itemTemplateId);
        if (itemTemplate != null) {
            ItemTimeService.gI().removeItemTime(player, itemTemplate.iconID);
        }
    }

    public void remove(Player player) {
        if (player == null || player.puppetBoss == null) {
            return;
        }
        PuppetBoss puppet = player.puppetBoss;
        player.puppetBoss = null;
        removeTimer(player, puppet.getPuppetItemId());
        if (puppet.zone != null) {
            ChangeMapService.gI().exitMap(puppet);
        }
        puppet.dispose();
    }

    private Item.ItemOption ensureTimeOption(Item item, int initialMinutes) {
        Item.ItemOption option = item.getOptionById(TIME_OPTION_ID);
        if (option == null) {
            option = new Item.ItemOption(TIME_OPTION_ID, Math.max(0, initialMinutes));
            item.itemOptions.add(option);
        }
        return option;
    }

    private int getPuppetLevel(int itemTemplateId) {
        return Math.max(1, Math.min(5, itemTemplateId - 2040));
    }
}
