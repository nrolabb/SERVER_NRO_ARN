package nro.models.server;

public class ModFunc {
    public static boolean isMultiLevelBienHinh = true; // Bật tắt cơ chế biến hình từng cấp

    // ================== CẤU HÌNH ITEM NHIỆM VỤ RƠI TỪ QUÁI ==================
    public static int ITEM_NV_TINH_THE_BANG = 2173; // Rơi từ Ếch băng (Mob 124, Map 182 - Task 29)
    public static int ITEM_NV_LONG_VU_DAI_BANG = 2174; // Rơi từ Đại bàng (Mob 120, Map 181 - Task 30)
    public static int ITEM_NV_THIT_TRAU_RUNG = 2175; // Rơi từ Trâu rừng (Mob 125, Map 181 - Task 30)
    public static int ITEM_NV_MAT_ONG_RUNG = 2176; // Rơi từ Ong cánh bướm (Mob 115, Map 186 - Task 31)
    public static int ITEM_NV_RANG_RONG_XANH = 2177; // Rơi từ Rồng xanh (Mob 113, Map 178 - Task 32)
    public static int ITEM_NV_LINH_HON_TU_THAN = 2178; // Rơi từ Grim Reaper (Mob 112, Map 178 - Task 32)
    public static int ITEM_NV_VAY_KHUNG_LONG = 2179; // Rơi từ Khủng long giáp (Mob 116, Map 177 - Task 33)
    public static int ITEM_NV_MAT_GAU_DA = 2180; // Rơi từ Gấu mặt chó (Mob 122, Map 169 - Task 34)
    public static int ITEM_NV_TAI_LON_BUOM = 2181; // Rơi từ Lợn cánh bướm (Mob 123, Map 173 - Task 34)

    // Tỉ lệ rơi item nhiệm vụ (%) giao động từ 30-50%
    public static int DROP_RATE_TASK_ITEM_MIN = 30;
    public static int DROP_RATE_TASK_ITEM_MAX = 50;

    public static int getRateDropTaskItem() {
        return nro.models.utils.Util.nextInt(DROP_RATE_TASK_ITEM_MIN, DROP_RATE_TASK_ITEM_MAX);
    }
}
