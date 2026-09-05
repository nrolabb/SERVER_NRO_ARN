package nro.models.boss.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nro.models.boss.Boss;
import nro.models.item.Item;
import nro.models.map.ItemMap;
import nro.models.player.Player;
import nro.models.services.ActivePointService;
import nro.models.services.Service;
import nro.models.utils.Logger;
import nro.models.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossRewardTemplate {
    private int id;
    private int bossId;
    private int itemId;
    private int quantityMin;
    private int quantityMax;
    private double rate;
    private String itemOptions;
    private int eventPoint;
    private int activePoint;

    public void dropReward(Boss boss, Player plKill) {
        if (boss == null || boss.zone == null) {
            return;
        }

        // Kiểm tra tỉ lệ rơi đồ
        if (rate > 0) {
            // Quy đổi sang thang 10,000 (0.01% -> 100%)
            int rateChance = (int) Math.round(rate * 100);
            if (rateChance >= 10000 || Util.nextInt(1, 10000) <= rateChance) {
                int quantity = 1;
                if (quantityMax > quantityMin) {
                    quantity = Util.nextInt(quantityMin, quantityMax);
                } else if (quantityMin > 0) {
                    quantity = quantityMin;
                }

                if (quantity > 0 && itemId > 0) {
                    int x = boss.location.x + Util.nextInt(-60, 60);
                    int y = boss.zone.map.yPhysicInTop(x, boss.location.y - 24);
                    long playerId = plKill != null ? plKill.id : -1;

                    ItemMap itemMap = new ItemMap(boss.zone, itemId, quantity, x, y, playerId);
                    List<Item.ItemOption> options = parseOptions(this.itemOptions);
                    if (!options.isEmpty()) {
                        itemMap.options.addAll(options);
                    }
                    Service.gI().dropItemMap(boss.zone, itemMap);
                }
            }
        }

        // Thưởng điểm sự kiện cho người hạ gục
        if (plKill != null) {
            if (eventPoint > 0 && plKill.event != null) {
                plKill.event.addEventPoint(eventPoint);
                Service.gI().sendThongBao(plKill, "+" + eventPoint + " Điểm sự kiện");
            }
            if (activePoint > 0) {
                ActivePointService.gI().addPoint(plKill, activePoint, "tiêu diệt boss");
            }
        }
    }

    private List<Item.ItemOption> parseOptions(String jsonStr) {
        List<Item.ItemOption> list = new ArrayList<>();
        if (jsonStr == null || jsonStr.trim().isEmpty() || jsonStr.equals("[]")) {
            return list;
        }
        try {
            Object obj = JSONValue.parse(jsonStr);
            if (obj instanceof JSONArray) {
                JSONArray arr = (JSONArray) obj;
                for (Object item : arr) {
                    if (item instanceof JSONArray) {
                        JSONArray optArr = (JSONArray) item;
                        if (optArr.size() >= 2) {
                            int optId = Integer.parseInt(String.valueOf(optArr.get(0)));
                            int optParam = Integer.parseInt(String.valueOf(optArr.get(1)));
                            list.add(new Item.ItemOption(optId, optParam));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(BossRewardTemplate.class, e, "Lỗi parse itemOptions boss_reward id=" + id);
        }
        return list;
    }
}
