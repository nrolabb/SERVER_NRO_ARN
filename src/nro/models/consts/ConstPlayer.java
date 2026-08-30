package nro.models.consts;

public class ConstPlayer {

        public static final int FOLLOWER_PET_SLOT = 9;

        public static final int HERO1_BIEN_HINH_FRAME_COUNT = 44;
        public static final int HERO1_BIEN_HINH_ICON_START = 32101;
        public static short HERO1_BIEN_HINH_HEAD = -1;
        public static short HERO1_BIEN_HINH_BODY_START = -1;
        public static short HERO1_BIEN_HINH_LEG = -1;

        public static short getHero1BienHinhBody(int frame) {
                int safeFrame = Math.max(1, Math.min(HERO1_BIEN_HINH_FRAME_COUNT, frame));
                if (HERO1_BIEN_HINH_BODY_START < 0) {
                        return -1;
                }
                return (short) (HERO1_BIEN_HINH_BODY_START + safeFrame - 1);
        }

        public static final short[][] AURABIENHINH = {
                        { 7, 93, 88, 86, 84, 6, 95 }, // Trái Đất
                        { 7, 88, 86, 91, 12, 90, 64 }, // Namec
                        { 7, 92, 88, 84, 87, 90, 45 } // Xayda
        };
        public static final short[][] HEADBIENHINH = {
                        { 2444, 2445, 2446, 2431, 2447, 2448, 2449 }, // Trái Đất
                        { 2452, 2453, 2454, 2455, 2456, 2457, 2458 }, // Namec
                        { 2435, 2436, 2437, 2428, 2438, 2439, 2440 } // Xayda
        };
        public static final short[][] BODYBIENHINH = {
                        { 2450, 2450, 2450, 2432, 2450, 2450, 2450 }, // Trái Đất
                        { 2459, 2459, 2461, 2459, 2459, 2459, 2459 }, // Namec
                        { 2442, 2442, 2442, 2429, 2442, 2442, 2442 } // Xayda
        };
        public static final short[][] LEGBIENHINH = {
                        { 2451, 2451, 2451, 2433, 2451, 2451, 2451 }, // Trái Đất
                        { 2460, 2460, 2460, 2460, 2460, 2460, 2460 }, // Namec
                        { 2443, 2443, 2443, 2430, 2443, 2443, 2443 } // Xayda
        };

        public static final short[][] HEADBIENHINH_SPINE = {
                        { 2426, 2323, 2317, 2299, 2326, 2329, 2332 }, // Trái Đất
                        { 2352, 2355, 2339, 2406, 2409, 2351, 2400 }, // Namec
                        { 2287, 2286, 2288, 2423, 2289, 2290, 2292 } // Xayda
        };
        public static final short[][] BODYBIENHINH_SPINE = {
                        { 2324, 2324, 2318, 2300, 2327, 2330, 2333 }, // Trái Đất
                        { 2353, 2356, 2340, 2407, 2410, 2349, 2401 }, // Namec
                        { 2294, 2294, 2294, 2424, 2294, 2294, 2294 } // Xayda
        };
        public static final short[][] LEGBIENHINH_SPINE = {
                        { 2325, 2325, 2319, 2301, 2328, 2331, 2334 }, // Trái Đất
                        { 2354, 2357, 2341, 2408, 2411, 2350, 2402 }, // Namec
                        { 2295, 2295, 2295, 2425, 2295, 2295, 2295 } // Xayda
        };

        public static final int[] HEADMONKEY = { 192, 195, 196, 199, 197, 200, 198 };

        public static final byte TRAI_DAT = 0;
        public static final byte NAMEC = 1;
        public static final byte XAYDA = 2;

        // type pk
        public static final byte NON_PK = 0;
        public static final byte PK_PVP = 3;
        public static final byte PK_PVP_2 = 4;
        public static final byte PK_ALL = 5;

        // type fushion
        public static final byte NON_FUSION = 0;
        public static final byte LUONG_LONG_NHAT_THE = 4;
        public static final byte HOP_THE_PORATA = 6;
        public static byte HOP_THE_PORATA2 = 8;
        public static byte HOP_THE_PORATA3 = 9;
        public static final byte HOP_THE_GOGETA = 10;
}
