package nro.models.intrinsic;

public class PetIntrinsic {

    public static final byte NONE = 0;
    public static final byte DAME = 1;
    public static final byte HP = 2;
    public static final byte MP = 3;
    public static final byte CRIT = 4;
    public static final byte EXP = 5;
    public static final byte FUSION_DAME = 6;
    public static final byte FUSION_HP = 7;
    public static final byte FUSION_MP = 8;
    public static final byte FUSION_CRIT = 9;
    public static final byte SKILL_2_SPEED = 10;

    public byte type;
    public short param;

    public PetIntrinsic() {
        this.type = NONE;
        this.param = 0;
    }

    public boolean isOpened() {
        return this.type != NONE && this.param > 0;
    }

    public void set(byte type, short param) {
        this.type = type;
        this.param = param;
    }

    public String getDisplayName() {
        if (!isOpened()) {
            return "Chưa mở (Cần 20 tỷ SM)";
        }
        return switch (this.type) {
            case DAME -> "Tăng " + this.param + "% sức đánh đệ tử";
            case HP -> "Tăng " + this.param + "% HP đệ tử";
            case MP -> "Tăng " + this.param + "% KI đệ tử";
            case CRIT -> "Tăng " + this.param + "% chí mạng đệ tử";
            case EXP -> "Tăng " + this.param + "% tiềm năng đệ tử";
            case FUSION_DAME -> "Tăng " + this.param + "% sức đánh khi hợp thể";
            case FUSION_HP -> "Tăng " + this.param + "% HP khi hợp thể";
            case FUSION_MP -> "Tăng " + this.param + "% KI khi hợp thể";
            case FUSION_CRIT -> "Tăng " + this.param + "% chí mạng khi hợp thể";
            case SKILL_2_SPEED -> "Giảm " + this.param + "% hồi chiêu skill 2";
            default -> "Chưa mở (Cần 20 tỷ SM)";
        };
    }
}
