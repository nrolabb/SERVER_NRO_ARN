package nro.models.boss;

import nro.models.boss.template.BossTemplate;
import nro.models.server.Manager;

/**
 * Quản lý nạp dữ liệu Boss tập trung từ Database.
 * Toàn bộ cấu hình các Boss (HP, Dame, Trang phục, Chiêu thức, Bản đồ, Lời thoại...)
 * đã được chuyển toàn diện vào Database MySQL (6 bảng boss_template, boss_form, boss_skill, boss_map, boss_appear_together, boss_reward).
 * Không còn bất kỳ dữ liệu Boss nào bị hardcode tại đây.
 */
public class BossesData {

    /**
     * Lấy toàn bộ các form của Boss theo BossID từ Database
     * @param bossId ID của boss trong BossID
     * @return Mảng BossData của các form
     */
    public static BossData[] get(int bossId) {
        if (Manager.BOSS_TEMPLATES != null) {
            BossTemplate template = Manager.BOSS_TEMPLATES.get(bossId);
            if (template != null) {
                return template.toBossDataArray();
            }
        }
        return new BossData[0];
    }

    /**
     * Lấy form đầu tiên của Boss theo BossID từ Database
     * @param bossId ID của boss trong BossID
     * @return BossData form 0
     */
    public static BossData getFirst(int bossId) {
        BossData[] arr = get(bossId);
        return arr.length > 0 ? arr[0] : null;
    }
}
