package nro.models.services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import nro.models.data.LocalManager;
import nro.models.item.Item;
import nro.models.player.Player;

public class SpinRewardService {
    private static SpinRewardService instance;

    public static SpinRewardService gI() {
        if (instance == null) {
            instance = new SpinRewardService();
        }
        return instance;
    }

    public void showConfirmClaim(Player player, nro.models.npc.Npc npc) {
        if (player == null || player.getSession() == null) {
            return;
        }

        List<SpinReward> rewards = new ArrayList<>();
        try (Connection con = LocalManager.getConnection(); 
             PreparedStatement ps = con.prepareStatement("SELECT id, reward_id, quantity, item_options FROM spin_reward WHERE player_id = ? AND status = 0 LIMIT 10")) {
            ps.setLong(1, player.getSession().userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rewards.add(new SpinReward(rs.getLong("id"), rs.getInt("reward_id"), rs.getInt("quantity"), rs.getString("item_options")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, không thể lấy phần thưởng lúc này.");
            return;
        }

        if (rewards.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có phần thưởng vòng quay nào để nhận.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Con đang có ").append(rewards.size()).append(" phần thưởng vòng quay chưa nhận:\n");
        for (SpinReward r : rewards) {
            String itemName = nro.models.services.ItemService.gI().getTemplate((short)r.rewardId).name;
            sb.append("- ").append(r.quantity).append("x ").append(itemName).append("\n");
        }
        sb.append("\nCon có muốn nhận ngay không?");
        
        npc.createOtherMenu(player, nro.models.consts.ConstNpc.CONFIRM_CLAIM_SPIN_REWARD, sb.toString(), "Nhận", "Đóng");
    }

    public void claimReward(Player player) {
        if (player == null || player.getSession() == null) {
            return;
        }

        List<SpinReward> rewards = new ArrayList<>();
        try (Connection con = LocalManager.getConnection(); 
             PreparedStatement ps = con.prepareStatement("SELECT id, reward_id, quantity, item_options FROM spin_reward WHERE player_id = ? AND status = 0 LIMIT 10")) {
            ps.setLong(1, player.getSession().userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rewards.add(new SpinReward(rs.getLong("id"), rs.getInt("reward_id"), rs.getInt("quantity"), rs.getString("item_options")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi xảy ra, không thể lấy phần thưởng lúc này.");
            return;
        }

        if (rewards.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có phần thưởng vòng quay nào để nhận.");
            return;
        }

        int emptyBag = InventoryService.gI().getCountEmptyBag(player);
        if (emptyBag < rewards.size()) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống, cần ít nhất " + rewards.size() + " ô trống.");
            return;
        }

        int claimedCount = 0;
        try (Connection con = LocalManager.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE spin_reward SET status = 1, claimed_at = NOW() WHERE id = ?")) {
             
            for (SpinReward r : rewards) {
                Item item = ItemService.gI().createNewItem((short) r.rewardId, r.quantity);
                
                if (r.itemOptions != null && !r.itemOptions.isEmpty() && !r.itemOptions.equals("[]")) {
                    try {
                        JSONArray jarr = (JSONArray) JSONValue.parse(r.itemOptions);
                        if (jarr != null) {
                            for (int i = 0; i < jarr.size(); i++) {
                                JSONObject jobj = (JSONObject) jarr.get(i);
                                int optId = Integer.parseInt(jobj.get("id").toString());
                                int optParam = Integer.parseInt(jobj.get("param").toString());
                                item.itemOptions.add(new Item.ItemOption(optId, optParam));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing item_options for spin reward: " + r.itemOptions);
                    }
                }
                
                InventoryService.gI().addItemBag(player, item);
                
                ps.setLong(1, r.id);
                ps.addBatch();
                claimedCount++;
            }
            ps.executeBatch();
            
        } catch (Exception e) {
            e.printStackTrace();
            Service.gI().sendThongBao(player, "Có lỗi xảy ra khi nhận thưởng.");
            return;
        }

        if (claimedCount > 0) {
            InventoryService.gI().sendItemBags(player);
            Service.gI().sendThongBao(player, "Đã nhận thành công " + claimedCount + " phần thưởng từ Vòng Quay!");
        }
    }

    private static class SpinReward {
        long id;
        int rewardId;
        int quantity;
        String itemOptions;
        SpinReward(long id, int rewardId, int quantity, String itemOptions) {
            this.id = id;
            this.rewardId = rewardId;
            this.quantity = quantity;
            this.itemOptions = itemOptions;
        }
    }
}
