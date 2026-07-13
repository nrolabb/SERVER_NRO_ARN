package nro.models.database;

import java.util.ArrayList;
import java.util.List;
import nro.models.data.LocalManager;
import nro.models.data.LocalResultSet;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.puppet.PuppetTemplate;
import org.json.simple.JSONArray;

public final class PuppetDAO {

    private PuppetDAO() {
    }

    public static List<PuppetTemplate> loadTemplates() throws Exception {
        List<PuppetTemplate> templates = new ArrayList<>();
        LocalResultSet rs = null;
        try {
            rs = LocalManager.executeQuery("SELECT `id`, `id_temp`, `name`, `hp`, `ki`, `dame`, `crit`, "
                    + "`head_id`, `body_id`, `leg_id`, `time_remaining` FROM `puppet_template`");
            while (rs.next()) {
                PuppetTemplate template = new PuppetTemplate();
                template.setId(readInt(rs, "id"));
                template.setItemTemplateId(readShort(rs, "id_temp"));
                template.setName(rs.getString("name"));
                template.setHp(readInt(rs, "hp"));
                template.setKi(readInt(rs, "ki"));
                template.setDame(readInt(rs, "dame"));
                template.setCrit(readInt(rs, "crit"));
                template.setHeadId(readShort(rs, "head_id"));
                template.setBodyId(readShort(rs, "body_id"));
                template.setLegId(readShort(rs, "leg_id"));
                template.setInitialTimeMinutes(readInt(rs, "time_remaining"));
                templates.add(template);
            }
        } finally {
            if (rs != null) {
                rs.dispose();
            }
        }
        return templates;
    }

    /** ResultSetImpl stores JDBC values with their driver-specific Number type. */
    private static int readInt(LocalResultSet rs, String column) throws Exception {
        Object value = rs.getObject(column);
        if (value instanceof Number number) {
            return Math.toIntExact(number.longValue());
        }
        if (value == null) {
            throw new IllegalStateException("Cột " + column + " của puppet_template không được NULL");
        }
        return Integer.parseInt(value.toString());
    }

    private static short readShort(LocalResultSet rs, String column) throws Exception {
        int value = readInt(rs, column);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Cột " + column + " vượt giới hạn SMALLINT: " + value);
        }
        return (short) value;
    }

    /**
     * Persists the stone consumption and the equipped puppet's option 9 in one
     * database statement. The caller must synchronize on the player while
     * changing the in-memory inventory and invoking this method.
     */
    public static void saveBagAndBody(Player player) throws Exception {
        String itemsBody = serializeItems(player.inventory.itemsBody);
        String itemsBag = serializeItems(player.inventory.itemsBag);
        int updated = LocalManager.executeUpdate(
                "UPDATE `player` SET `items_body` = ?, `items_bag` = ? WHERE `id` = ?",
                itemsBody, itemsBag, player.id);
        if (updated != 1) {
            throw new IllegalStateException("Không thể lưu hành trang khôi lỗi cho player " + player.id);
        }
    }

    @SuppressWarnings("unchecked")
    private static String serializeItems(List<Item> items) {
        JSONArray result = new JSONArray();
        JSONArray dataItem = new JSONArray();
        for (Item item : items) {
            JSONArray emptyOptions = new JSONArray();
            if (item != null && item.isNotNullItem()) {
                dataItem.add(item.template.id);
                dataItem.add(item.quantity);
                JSONArray options = new JSONArray();
                for (Item.ItemOption option : item.itemOptions) {
                    JSONArray dataOption = new JSONArray();
                    dataOption.add(option.optionTemplate.id);
                    dataOption.add(option.param);
                    options.add(dataOption.toJSONString());
                }
                dataItem.add(options.toJSONString());
            } else {
                dataItem.add(-1);
                dataItem.add(0);
                dataItem.add(emptyOptions.toJSONString());
            }
            dataItem.add(item != null ? item.createTime : 0L);
            result.add(dataItem.toJSONString());
            dataItem.clear();
        }
        return result.toJSONString();
    }
}
