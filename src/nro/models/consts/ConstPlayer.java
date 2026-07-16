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

    public static final byte[][] AURABIENHINH = {
        {7, 7, 13, 6, 31},
        {7, 7, 13, 6, 31},
        {7, 7, 13, 6, 31}
    };
    public static final short[][] HEADBIENHINH = {
        {1992, 1993, 1994, 1995, 1996},
        {1997, 1998, 1999, 2000, 2001},
        {2002, 2003, 2004, 2005, 2006}
    };
    public static final short[] BODYBIENHINH = {1986, 1988, 1990};
    public static final short[] LEGBIENHINH = {1987, 1989, 1991};

    public static final int[] HEADMONKEY = {192, 195, 196, 199, 197, 200, 198};

    public static final byte TRAI_DAT = 0;
    public static final byte NAMEC = 1;
    public static final byte XAYDA = 2;

    //type pk
    public static final byte NON_PK = 0;
    public static final byte PK_PVP = 3;
    public static final byte PK_PVP_2 = 4;
    public static final byte PK_ALL = 5;

    //type fushion
    public static final byte NON_FUSION = 0;
    public static final byte LUONG_LONG_NHAT_THE = 4;
    public static final byte HOP_THE_PORATA = 6;
    public static byte HOP_THE_PORATA2 = 8;
    public static byte HOP_THE_PORATA3 = 9;
    public static final byte HOP_THE_GOGETA = 10;
}
