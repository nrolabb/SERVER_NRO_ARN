package nro.models.boss.giao_vien_whis;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import nro.models.boss.Boss;
import nro.models.boss.BossData;
import nro.models.boss.BossID;
import nro.models.consts.BossStatus;
import nro.models.consts.ConstPlayer;
import static nro.models.consts.BossType.SUMMER_EVENT;
import nro.models.event.EventConfig;
import nro.models.item.Item;
import nro.models.player.Player;
import nro.models.services.ChatGlobalService;
import nro.models.services.InventoryService;
import nro.models.services.ItemService;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.map.service.ChangeMapService;
import nro.models.map.service.MapService;
import nro.models.server.ServerNotify;
import nro.models.skill.Skill;
import nro.models.utils.Util;
import nro.models.utils.Logger;

/** Boss đố vui không chiến đấu tại map Whis (154). */
public class GiaoVienWhis extends Boss {

    private static final int GIFT_ITEM_ID = 1776;
    private static final long QUESTION_TIME = 10_000L;
    private static final long BREAK_TIME = 20_000L;
    private static final long APPEAR_TIME = 10 * 60_000L;
    private static final int RECENT_QUESTION_COUNT = 8;
    private static final int LEGACY_CHAT_LENGTH = 600;

    private static final int[][] PLANET_MAPS = {
        {1, 2, 3, 4, 5, 6},       // Gần làng Aru - Trái Đất
        {8, 9, 10, 11, 12, 13},   // Gần làng Mori - Namếc
        {15, 16, 17, 18, 19, 20}  // Gần làng Kakarot - Xayda
    };

    private static final Question[] QUESTIONS = {
        new Question("Cái gì càng lấy đi càng lớn?", "cái hố", "hố"),
        new Question("Con gì đầu dê mình ốc?", "con dốc", "dốc"),
        new Question("Cái gì có răng mà không có miệng?", "cái lược", "lược"),
        new Question("Cái gì có cổ mà không có đầu?", "cái áo", "áo"),
        new Question("Tháng nào có 28 ngày?", "tất cả", "mọi tháng", "12 tháng"),
        new Question("Một năm có bao nhiêu tháng?", "12", "mười hai"),
        new Question("Một tuần có bao nhiêu ngày?", "7", "bảy"),
        new Question("2 cộng 3 bằng bao nhiêu?", "5", "năm"),
        new Question("Từ tiếng Anh 'cat' nghĩa là gì?", "con mèo", "mèo"),
        new Question("Từ tiếng Anh 'dog' nghĩa là gì?", "con chó", "chó"),
        new Question("Từ tiếng Anh 'book' nghĩa là gì?", "quyển sách", "cuốn sách", "sách"),
        new Question("Từ tiếng Anh 'water' nghĩa là gì?", "nước"),
        new Question("Từ tiếng Anh 'sun' nghĩa là gì?", "mặt trời"),
        new Question("Từ tiếng Anh 'moon' nghĩa là gì?", "mặt trăng"),
        new Question("Từ tiếng Anh 'teacher' nghĩa là gì?", "giáo viên", "thầy giáo", "cô giáo"),
        new Question("Từ tiếng Anh 'friend' nghĩa là gì?", "bạn", "bạn bè"),
        new Question("Từ tiếng Anh 'family' nghĩa là gì?", "gia đình"),
        new Question("Từ tiếng Anh 'happy' nghĩa là gì?", "vui", "vui vẻ", "hạnh phúc"),
        new Question("'Con mèo' trong tiếng Anh là gì?", "cat"),
        new Question("'Con chó' trong tiếng Anh là gì?", "dog"),
        new Question("'Quyển sách' trong tiếng Anh là gì?", "book"),
        new Question("'Nước' trong tiếng Anh là gì?", "water"),
        new Question("'Mặt trời' trong tiếng Anh là gì?", "sun"),
        new Question("'Mặt trăng' trong tiếng Anh là gì?", "moon"),
        new Question("'Giáo viên' trong tiếng Anh là gì?", "teacher"),
        new Question("'Bạn bè' trong tiếng Anh là gì?", "friend", "friends"),
        new Question("'Gia đình' trong tiếng Anh là gì?", "family"),
        new Question("'Màu đỏ' trong tiếng Anh là gì?", "red"),
        new Question("'Cảm ơn' trong tiếng Anh là gì?", "thank you", "thanks"),
        new Question("'Xin chào' trong tiếng Anh là gì?", "hello", "hi"),
        new Question("Cái gì càng lau càng ướt?", "cái khăn", "khăn"),
        new Question("Con gì mang được miếng gỗ lớn nhưng không mang được hòn sỏi?", "con sông", "sông"),
        new Question("Cái gì đi khắp thế giới mà vẫn ở một góc?", "con tem", "tem"),
        new Question("Cái gì có nhiều lá nhưng không phải cây?", "quyển sách", "cuốn sách", "sách"),
        new Question("Cái gì có kim nhưng không dùng để may?", "đồng hồ", "cái đồng hồ"),
        new Question("Cái gì có mắt mà không nhìn thấy?", "cây kim", "kim"),
        new Question("Cái gì có chân nhưng không biết đi?", "cái bàn", "bàn"),
        new Question("Cái gì đập thì sống, không đập thì chết?", "trái tim", "tim"),
        new Question("Con gì không gáy mà người ta vẫn gọi là gà?", "gà mái", "con gà mái"),
        new Question("Ở đâu đường xá nhiều nhất?", "bản đồ", "trên bản đồ"),
        new Question("10 trừ 4 bằng bao nhiêu?", "6", "sáu"),
        new Question("6 nhân 2 bằng bao nhiêu?", "12", "mười hai"),
        new Question("15 chia 3 bằng bao nhiêu?", "5", "năm"),
        new Question("Số nào đứng sau số 99?", "100", "một trăm"),
        new Question("Nửa của 20 là bao nhiêu?", "10", "mười"),
        new Question("Một giờ có bao nhiêu phút?", "60", "sáu mươi"),
        new Question("Một phút có bao nhiêu giây?", "60", "sáu mươi"),
        new Question("Hình có ba cạnh gọi là hình gì?", "tam giác", "hình tam giác"),
        new Question("Hành tinh chúng ta đang sống tên gì?", "trái đất"),
        new Question("Mặt trời mọc ở hướng nào?", "đông", "hướng đông"),
        new Question("Từ tiếng Anh 'apple' nghĩa là gì?", "quả táo", "trái táo", "táo"),
        new Question("Từ tiếng Anh 'banana' nghĩa là gì?", "quả chuối", "trái chuối", "chuối"),
        new Question("Từ tiếng Anh 'school' nghĩa là gì?", "trường học", "trường"),
        new Question("Từ tiếng Anh 'student' nghĩa là gì?", "học sinh"),
        new Question("Từ tiếng Anh 'house' nghĩa là gì?", "ngôi nhà", "căn nhà", "nhà"),
        new Question("Từ tiếng Anh 'door' nghĩa là gì?", "cánh cửa", "cửa"),
        new Question("Từ tiếng Anh 'window' nghĩa là gì?", "cửa sổ"),
        new Question("Từ tiếng Anh 'flower' nghĩa là gì?", "bông hoa", "hoa"),
        new Question("Từ tiếng Anh 'tree' nghĩa là gì?", "cái cây", "cây"),
        new Question("Từ tiếng Anh 'rain' nghĩa là gì?", "mưa", "cơn mưa"),
        new Question("Từ tiếng Anh 'beautiful' nghĩa là gì?", "đẹp", "xinh đẹp"),
        new Question("Từ tiếng Anh 'strong' nghĩa là gì?", "mạnh", "mạnh mẽ"),
        new Question("Từ tiếng Anh 'small' nghĩa là gì?", "nhỏ", "bé", "nhỏ bé"),
        new Question("Từ tiếng Anh 'morning' nghĩa là gì?", "buổi sáng", "sáng"),
        new Question("Từ tiếng Anh 'night' nghĩa là gì?", "ban đêm", "buổi tối", "đêm"),
        new Question("'Quả táo' trong tiếng Anh là gì?", "apple"),
        new Question("'Quả chuối' trong tiếng Anh là gì?", "banana"),
        new Question("'Trường học' trong tiếng Anh là gì?", "school"),
        new Question("'Học sinh' trong tiếng Anh là gì?", "student"),
        new Question("'Ngôi nhà' trong tiếng Anh là gì?", "house", "home"),
        new Question("'Cánh cửa' trong tiếng Anh là gì?", "door"),
        new Question("'Cửa sổ' trong tiếng Anh là gì?", "window"),
        new Question("'Bông hoa' trong tiếng Anh là gì?", "flower"),
        new Question("'Cái cây' trong tiếng Anh là gì?", "tree"),
        new Question("'Mưa' trong tiếng Anh là gì?", "rain"),
        new Question("'Màu xanh dương' trong tiếng Anh là gì?", "blue"),
        new Question("'Màu xanh lá' trong tiếng Anh là gì?", "green"),
        new Question("'Màu vàng' trong tiếng Anh là gì?", "yellow"),
        new Question("'Buổi sáng' trong tiếng Anh là gì?", "morning"),
        new Question("'Chúc ngủ ngon' trong tiếng Anh là gì?", "good night")
    };

    private static final List<Integer> GLOBAL_QUESTION_BAG = new ArrayList<>();
    private static final List<Integer> GLOBAL_RECENT_QUESTIONS = new ArrayList<>();

    private static final List<GiaoVienWhis> INSTANCES = new CopyOnWriteArrayList<>();

    private Question currentQuestion;
    private boolean acceptingAnswers;
    private boolean onBreak;
    private long phaseStartedAt;
    private long lastMoveTime;
    private int moveDelay = 3_000;
    private int lastAdviceIndex = -1;
    private int lastCountdown = 6;
    private long appearedAt;
    private int roamTargetX = -1;
    private long lastMoveStepAt;

    public GiaoVienWhis(int planet) throws Exception {
        super(SUMMER_EVENT, BossID.GIAO_VIEN_WHIS, true, false, createData(planet));
        INSTANCES.add(this);
    }

    private static BossData createData(int planet) {
        if (planet < 0 || planet >= PLANET_MAPS.length) {
            throw new IllegalArgumentException("Hành tinh Giáo viên Whis không hợp lệ: " + planet);
        }
        return new BossData(
                "Giáo viên Whis", ConstPlayer.TRAI_DAT,
                new short[]{838, 839, 840, -1, -1, -1},
                1, new int[]{2_000_000_000}, PLANET_MAPS[planet],
                new int[][]{{Skill.DRAGON, 1, 60_000}},
                new String[]{}, new String[]{}, new String[]{}, 5 * 60);
    }

    /** Được gọi từ luồng chat; synchronized đảm bảo chỉ người đúng đầu tiên nhận quà. */
    public static void tryAnswer(Player player, String text) {
        for (GiaoVienWhis teacher : INSTANCES) {
            if (teacher.zone == player.zone) {
                teacher.acceptAnswer(player, text);
                return;
            }
        }
    }

    @Override
    public void update() {
        if (!EventConfig.isActive(EventConfig.SUMMER)) {
            acceptingAnswers = false;
            currentQuestion = null;
            if (zone != null) {
                ChangeMapService.gI().exitMap(this);
            }
            lastTimeRest = System.currentTimeMillis();
            changeStatus(BossStatus.REST);
            return;
        }
        super.update();
    }

    @Override
    public void joinMap() {
        super.joinMap();
        if (zone != null && (bossStatus == BossStatus.CHAT_S || bossStatus == BossStatus.ACTIVE)) {
            appearedAt = System.currentTimeMillis();
            acceptingAnswers = false;
            currentQuestion = null;
            ServerNotify.gI().notify("BOSS " + name + " vừa xuất hiện tại "
                    + zone.map.mapName + " - khu " + zone.zoneId + ".");
            Logger.successln("[GIAO VIEN WHIS] " + name
                    + " xuat hien tai " + zone.map.mapName
                    + " (map " + zone.map.mapId + ")"
                    + " - khu " + zone.zoneId
                    + " - toa do (" + location.x + ", " + location.y + ")");
        }
    }

    private synchronized void acceptAnswer(Player player, String text) {
        if (!acceptingAnswers || currentQuestion == null || zone == null
                || player.zone != zone || System.currentTimeMillis() - phaseStartedAt >= QUESTION_TIME
                || !currentQuestion.matches(text)) {
            return;
        }

        Item gift = ItemService.gI().createNewItem((short) GIFT_ITEM_ID, 1);
        if (!InventoryService.gI().addItemBag(player, gift)) {
            Service.gI().sendThongBao(player, "Đáp án đúng nhưng hành trang đã đầy, hãy chừa một ô trống!");
            return;
        }

        acceptingAnswers = false;
        InventoryService.gI().sendItemBags(player);
        Service.gI().sendThongBao(player, "Bạn nhận được Hộp quà SK từ Giáo viên Whis.");
        Service.gI().chat(this, "Chính xác! " + player.name + " trả lời nhanh nhất.");
        ChatGlobalService.gI().ThongBaoRoiDo(player,
                player.name + " đã nhận được Hộp quà SK từ Giáo viên Whis!");
        beginBreak();
    }

    @Override
    public void doneChatS() {
        startQuestion();
    }

    @Override
    public void active() {
        changeToTypeNonPK();
        if (appearedAt > 0 && Util.canDoWithTime(appearedAt, APPEAR_TIME)) {
            acceptingAnswers = false;
            currentQuestion = null;
            Service.gI().chat(this, "Buổi học hôm nay kết thúc. Hẹn gặp lại sau 5 phút!");
            changeStatus(BossStatus.LEAVE_MAP);
            return;
        }
        updateQuiz();
        if (onBreak) {
            roam();
        } else {
            roamTargetX = -1;
        }
    }

    private void updateQuiz() {
        long elapsed = System.currentTimeMillis() - phaseStartedAt;
        if (!onBreak) {
            if (acceptingAnswers && elapsed >= QUESTION_TIME) {
                synchronized (this) {
                    if (acceptingAnswers) {
                        acceptingAnswers = false;
                        Service.gI().chat(this, "Hết giờ! Đáp án: " + currentQuestion.answers[0] + ".");
                        beginBreak();
                    }
                }
            }
            return;
        }

        int adviceIndex = (int) (elapsed / 5_000L);
        if (elapsed < 15_000L && adviceIndex != lastAdviceIndex) {
            String[] advice = {
                "Học mỗi ngày một chút, con sẽ tiến bộ rất nhanh.",
                "Bình tĩnh suy nghĩ, đáp án thường đơn giản hơn con tưởng.",
                "Chuẩn bị xong chưa? Câu tiếp theo sắp bắt đầu!"
            };
            lastAdviceIndex = adviceIndex;
            Service.gI().chat(this, advice[adviceIndex]);
        }

        long remaining = BREAK_TIME - elapsed;
        if (remaining <= 5_000L && remaining > 0) {
            int countdown = (int) Math.ceil(remaining / 1_000D);
            if (countdown != lastCountdown) {
                lastCountdown = countdown;
                Service.gI().chat(this, String.valueOf(countdown));
            }
        }
        if (elapsed >= BREAK_TIME) {
            startQuestion();
        }
    }

    private void startQuestion() {
        roamTargetX = -1;
        MapService.gI().sendPlayerStop(this);
        currentQuestion = nextQuestion();
        onBreak = false;
        acceptingAnswers = true;
        phaseStartedAt = System.currentTimeMillis();
        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        Service.gI().chatWithDuration(this,
                padForLegacyClient("Câu hỏi: " + currentQuestion.text), (int) QUESTION_TIME);
    }

    private static String padForLegacyClient(String text) {
        StringBuilder padded = new StringBuilder(text);
        while (padded.length() < LEGACY_CHAT_LENGTH) {
            padded.append('\u200B');
        }
        return padded.toString();
    }

    private void beginBreak() {
        onBreak = true;
        phaseStartedAt = System.currentTimeMillis();
        lastAdviceIndex = -1;
        lastCountdown = 6;
    }

    private static synchronized Question nextQuestion() {
        if (GLOBAL_QUESTION_BAG.isEmpty()) {
            for (int i = 0; i < QUESTIONS.length; i++) {
                if (!GLOBAL_RECENT_QUESTIONS.contains(i)) {
                    GLOBAL_QUESTION_BAG.add(i);
                }
            }
            Collections.shuffle(GLOBAL_QUESTION_BAG);
        }
        int index = GLOBAL_QUESTION_BAG.remove(GLOBAL_QUESTION_BAG.size() - 1);
        GLOBAL_RECENT_QUESTIONS.add(index);
        if (GLOBAL_RECENT_QUESTIONS.size() > RECENT_QUESTION_COUNT * 3) {
            GLOBAL_RECENT_QUESTIONS.remove(0);
        }
        return QUESTIONS[index];
    }

    private void roam() {
        if (zone == null) {
            return;
        }
        if (roamTargetX < 0) {
            if (!Util.canDoWithTime(lastMoveTime, moveDelay)) {
                return;
            }
            int minX = 80;
            int maxX = Math.max(minX, zone.map.mapWidth - 80);
            roamTargetX = Util.nextInt(minX, maxX);
        }
        if (!Util.canDoWithTime(lastMoveStepAt, 250)) {
            return;
        }
        lastMoveStepAt = System.currentTimeMillis();
        int distance = roamTargetX - location.x;
        int step = Math.min(35, Math.abs(distance));
        int nextX = location.x + (distance < 0 ? -step : step);
        int nextY = zone.map.yPhysicInTop(nextX, location.y);
        if (!canWalkTo(nextX, nextY, distance > 0)) {
            roamTargetX = -1;
            lastMoveTime = System.currentTimeMillis();
            moveDelay = Util.nextInt(500, 1_500);
            return;
        }
        PlayerService.gI().playerMove(this, nextX, nextY);
        if (nextX == roamTargetX) {
            roamTargetX = -1;
            lastMoveTime = System.currentTimeMillis();
            moveDelay = Util.nextInt(1_000, 3_000);
        }
    }

    private boolean canWalkTo(int nextX, int nextY, boolean movingRight) {
        if (nextX < 60 || nextX > zone.map.mapWidth - 60 || nextY <= 0) {
            return false;
        }
        // Không bước qua vực, bậc cao hoặc xuyên tile chắn hai bên nhân vật.
        if (Math.abs(nextY - location.y) > 24) {
            return false;
        }
        int sideX = nextX + (movingRight ? 18 : -18);
        int wallType = movingRight ? 4 : 8;
        return !zone.map.tileTypeAt(sideX, nextY - 24, wallType)
                && !zone.map.tileTypeAt(sideX, nextY - 48, wallType);
    }

    @Override
    public synchronized int injured(Player attacker, long damage, boolean piercing, boolean isMobAttack) {
        return 0;
    }

    @Override
    public void die(Player killer) {
        // Giáo viên không thể bị tiêu diệt.
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replace('đ', 'd').replace('Đ', 'D')
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private static final class Question {
        private final String text;
        private final String[] answers;

        private Question(String text, String... answers) {
            this.text = text;
            this.answers = answers;
        }

        private boolean matches(String answer) {
            String candidate = normalize(answer);
            for (String expected : answers) {
                if (candidate.equals(normalize(expected))) {
                    return true;
                }
            }
            return false;
        }
    }
}
