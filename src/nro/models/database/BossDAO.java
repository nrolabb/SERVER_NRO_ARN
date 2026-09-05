package nro.models.database;

import nro.models.boss.template.BossFormTemplate;
import nro.models.boss.template.BossRewardTemplate;
import nro.models.boss.template.BossSkillTemplate;
import nro.models.boss.template.BossTemplate;
import nro.models.utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class BossDAO {

    public static Map<Integer, BossTemplate> loadBossTemplates(Connection con) {
        Map<Integer, BossTemplate> templates = new HashMap<>();
        Map<Integer, BossFormTemplate> formsById = new HashMap<>();

        try {
            // 1. Tải boss_template
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM boss_template");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    BossTemplate template = BossTemplate.builder()
                            .id(id)
                            .name(rs.getString("name"))
                            .type(rs.getString("type"))
                            .subType(rs.getString("sub_type"))
                            .gender(rs.getByte("gender"))
                            .enabled(rs.getBoolean("enabled"))
                            .spawnCount(rs.getInt("spawn_count"))
                            .respawnDelay(rs.getInt("respawn_delay"))
                            .despawnTimeout(rs.getInt("despawn_timeout"))
                            .isNotify(rs.getBoolean("is_notify"))
                            .isZone01Disabled(rs.getBoolean("is_zone_0_1_disabled"))
                            .requireTaskId((Integer) rs.getObject("require_task_id"))
                            .extraConfig(rs.getString("extra_config"))
                            .build();
                    templates.put(id, template);
                }
            }

            // 2. Tải boss_form
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM boss_form ORDER BY boss_id ASC, form_order ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int formId = rs.getInt("id");
                    int bossId = rs.getInt("boss_id");
                    BossFormTemplate form = BossFormTemplate.builder()
                            .id(formId)
                            .bossId(bossId)
                            .formOrder(rs.getInt("form_order"))
                            .name(rs.getString("name"))
                            .hpMin(rs.getLong("hp_min"))
                            .hpMax(rs.getLong("hp_max"))
                            .dame(rs.getInt("dame"))
                            .outfitHead(rs.getShort("outfit_head"))
                            .outfitBody(rs.getShort("outfit_body"))
                            .outfitLeg(rs.getShort("outfit_leg"))
                            .outfitBag(rs.getShort("outfit_bag"))
                            .outfitAura(rs.getShort("outfit_aura"))
                            .outfitEff(rs.getShort("outfit_eff"))
                            .textStart(rs.getString("text_start"))
                            .textMid(rs.getString("text_mid"))
                            .textEnd(rs.getString("text_end"))
                            .build();

                    formsById.put(formId, form);
                    BossTemplate bossTemplate = templates.get(bossId);
                    if (bossTemplate != null) {
                        bossTemplate.getForms().add(form);
                    }
                }
            }

            // 3. Tải boss_skill
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM boss_skill ORDER BY form_id ASC, id ASC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int formId = rs.getInt("form_id");
                    BossSkillTemplate skill = BossSkillTemplate.builder()
                            .id(rs.getInt("id"))
                            .formId(formId)
                            .skillId(rs.getInt("skill_id"))
                            .skillLevel(rs.getInt("skill_level"))
                            .cooldown(rs.getInt("cooldown"))
                            .build();

                    BossFormTemplate form = formsById.get(formId);
                    if (form != null) {
                        form.getSkills().add(skill);
                    }
                }
            }

            // 4. Tải boss_map
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM boss_map");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int bossId = rs.getInt("boss_id");
                    int mapId = rs.getInt("map_id");
                    BossTemplate bossTemplate = templates.get(bossId);
                    if (bossTemplate != null && !bossTemplate.getMapJoin().contains(mapId)) {
                        bossTemplate.getMapJoin().add(mapId);
                    }
                }
            }

            // 5. Tải boss_appear_together
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM boss_appear_together");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int bossId = rs.getInt("boss_id");
                    int subBossId = rs.getInt("sub_boss_id");
                    BossTemplate bossTemplate = templates.get(bossId);
                    if (bossTemplate != null) {
                        bossTemplate.getBossesAppearTogether().add(subBossId);
                    }
                }
            }

            // 6. Tải boss_reward
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM boss_reward");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int bossId = rs.getInt("boss_id");
                    BossRewardTemplate reward = BossRewardTemplate.builder()
                            .id(rs.getInt("id"))
                            .bossId(bossId)
                            .itemId(rs.getInt("item_id"))
                            .quantityMin(rs.getInt("quantity_min"))
                            .quantityMax(rs.getInt("quantity_max"))
                            .rate(rs.getDouble("rate"))
                            .itemOptions(rs.getString("item_options"))
                            .eventPoint(rs.getInt("event_point"))
                            .activePoint(rs.getInt("active_point"))
                            .build();

                    BossTemplate bossTemplate = templates.get(bossId);
                    if (bossTemplate != null) {
                        bossTemplate.getRewards().add(reward);
                    }
                }
            }

            Logger.success("Loaded " + templates.size() + " BossTemplates from Database!");
        } catch (Exception e) {
            Logger.logException(BossDAO.class, e, "Lỗi khi nạp BossTemplates từ DB");
        }

        return templates;
    }
}
