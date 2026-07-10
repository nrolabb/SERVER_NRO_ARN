package nro.models.event;

import nro.models.event_list.TopUp;
import nro.models.event_list.TrungThu;
import nro.models.event_list.HungVuong;
import nro.models.event_list.Christmas;
import nro.models.event_list.Halloween;
import nro.models.event_list.LunarNewYear;
import nro.models.event_list.Default;
import nro.models.event_list.InternationalWomensDay;
import nro.models.event.summer.SummerEventManager;

public class EventManager {

    private static EventManager instance;

    public static boolean LUNNAR_NEW_YEAR = false;

    public static boolean INTERNATIONAL_WOMANS_DAY = false;

    public static boolean CHRISTMAS = false;

    public static boolean HALLOWEEN = false;

    public static boolean HUNG_VUONG = true;

    public static boolean TRUNG_THU = false;

    public static boolean TOP_UP = true;

    public static boolean SUMMER = false;

    public static EventManager gI() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public void init() {
        EventConfig.load();
        LUNNAR_NEW_YEAR = EventConfig.isActive(EventConfig.LUNAR_NEW_YEAR);
        INTERNATIONAL_WOMANS_DAY = EventConfig.isActive(EventConfig.INTERNATIONAL_WOMENS_DAY);
        CHRISTMAS = EventConfig.isActive(EventConfig.CHRISTMAS);
        HALLOWEEN = EventConfig.isActive(EventConfig.HALLOWEEN);
        HUNG_VUONG = EventConfig.isActive(EventConfig.HUNG_VUONG);
        TRUNG_THU = EventConfig.isActive(EventConfig.TRUNG_THU);
        TOP_UP = EventConfig.isActive(EventConfig.TOP_UP);
        SUMMER = EventConfig.isActive(EventConfig.SUMMER);

        new Default().init();
        if (LUNNAR_NEW_YEAR) {
            new LunarNewYear().init();
        }
        if (INTERNATIONAL_WOMANS_DAY) {
            new InternationalWomensDay().init();
        }
        if (HALLOWEEN) {
            new Halloween().init();
        }
        if (CHRISTMAS) {
            new Christmas().init();
        }
        if (HUNG_VUONG) {
            new HungVuong().init();
        }
        if (TRUNG_THU) {
            new TrungThu().init();
        }
        if (TOP_UP) {
            new TopUp().init();
        }
        if (SUMMER) {
            SummerEventManager.gI().init();
        }
    }
}
