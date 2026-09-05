package nro.models.boss.Boss_Manager;

import nro.models.boss.Boss;
import nro.models.boss.BossID;
import nro.models.boss.Boss_mini.AnTrom;
import nro.models.boss.Broly.Broly;
import nro.models.boss.MajinBuu_12h.BuiBui;
import nro.models.boss.MajinBuu_12h.BuiBui2;
import nro.models.boss.MajinBuu_12h.Cadic;
import nro.models.boss.MajinBuu_12h.Drabura;
import nro.models.boss.MajinBuu_12h.Drabura2;
import nro.models.boss.MajinBuu_12h.Drabura3;
import nro.models.boss.MajinBuu_12h.Goku;
import nro.models.boss.MajinBuu_12h.Mabu;
import nro.models.boss.MajinBuu_12h.Yacon;
import nro.models.boss.MajinBuu_14h.Mabu2H;
import nro.models.boss.MajinBuu_14h.SuperBu;
import nro.models.boss.Tau_PayPay.TaoPaiPai;
import nro.models.player.Player;
import nro.models.network.Message;
import nro.models.map.service.MapService;
import nro.models.boss.Android.Android13;
import nro.models.boss.Android.Android14;
import nro.models.boss.Android.Android15;
import nro.models.boss.Android.Android19;
import nro.models.boss.Android.DrKore;
import nro.models.boss.Android.KingKong;
import nro.models.boss.Android.Pic;
import nro.models.boss.Android.Poc;
import nro.models.boss.Black_Goku.BlackGoku;
import nro.models.boss.Boss_mini.Odo;
import nro.models.boss.Boss_mini.RongNhi;
import nro.models.boss.Boss_mini.SoiHecQuyn;
import nro.models.boss.Boss_mini.Virut;
import nro.models.boss.Boss_mini.MatTroi;
import nro.models.boss.Cell.SieuBoHung;
import nro.models.boss.Cell.XENCON1;
import nro.models.boss.Cell.XENCON2;
import nro.models.boss.Cell.XENCON3;
import nro.models.boss.Cell.XENCON4;
import nro.models.boss.Cell.XENCON5;
import nro.models.boss.Cell.XENCON6;
import nro.models.boss.Cell.XENCON7;
import nro.models.boss.Cell.XenBoHung;
import nro.models.boss.Cold.Cooler;
import nro.models.boss.trai_dat.BIDO;
import nro.models.boss.trai_dat.BOJACK;
import nro.models.boss.trai_dat.BUJIN;
import nro.models.boss.trai_dat.KOGU;
import nro.models.boss.trai_dat.SUPER_BOJACK;
import nro.models.boss.trai_dat.ZANGYA;
import nro.models.boss.Frieza.Fide;
import nro.models.boss.tieu_doi_sat_thu_namek.SO1_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.SO2_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.SO3_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.SO4_NM;
import nro.models.boss.tieu_doi_sat_thu_namek.TDT_NM;
import nro.models.boss.Nappa.Kuku;
import nro.models.boss.Nappa.Ku;
import nro.models.boss.Nappa.MapDauDinh;
import nro.models.boss.Nappa.Rambo;
import nro.models.boss.tieu_doi_sat_thu.SO1;
import nro.models.boss.tieu_doi_sat_thu.SO2;
import nro.models.boss.tieu_doi_sat_thu.SO3;
import nro.models.boss.tieu_doi_sat_thu.SO4;
import nro.models.boss.tieu_doi_sat_thu.TDT;
import nro.models.boss.event.Halloween.BiMa;
import nro.models.boss.event.Halloween.Doi;
import nro.models.boss.event.Halloween.MaTroi;
import nro.models.boss.event_hung_vuong.SonTinh;
import nro.models.boss.event_hung_vuong.ThuyTinh;
import nro.models.boss.event_trung_thu.KhiDot;
import nro.models.boss.event_trung_thu.NguyetThan;
import nro.models.boss.event_trung_thu.NhatThan;
import nro.models.boss.event_tet.LanCon;
import nro.models.boss.event_noel.OngGiaNoel;
import nro.models.boss.Baby.Baby;
import nro.models.boss.Baby.B;
import nro.models.boss.cumber.Cumber;
import nro.models.boss.DynamicBoss;
import nro.models.boss.template.BossTemplate;
import nro.models.server.Manager;
import java.util.ArrayList;
import java.util.List;
import nro.models.map.Zone;
import nro.models.mob_bigboss.GauTuongCuop;
import nro.models.server.Maintenance;
import nro.models.server.ServerManager;
import nro.models.utils.Functions;
import nro.models.utils.Logger;

import java.io.FileWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;
    private long lastTimeWriteJSON = 0;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;

    public List<Boss> getBosses() {
        return this.bosses;
    }

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public void reloadBosses() {
        try (java.sql.Connection con = nro.models.data.LocalManager.getConnection()) {
            Manager.BOSS_TEMPLATES.clear();
            Manager.BOSS_TEMPLATES.putAll(nro.models.database.BossDAO.loadBossTemplates(con));
            Logger.success("Reloaded " + Manager.BOSS_TEMPLATES.size() + " boss templates from database!");
        } catch (Exception e) {
            Logger.logException(BossManager.class, e, "Lỗi reloadBosses");
        }
    }

    public void loadBoss() {
        // Tải các Boss cấu hình trong DB (có enabled = true)
        if (Manager.BOSS_TEMPLATES != null && !Manager.BOSS_TEMPLATES.isEmpty()) {
            java.util.Set<Integer> subBossIds = new java.util.HashSet<>();
            for (BossTemplate bt : Manager.BOSS_TEMPLATES.values()) {
                if (bt.getBossesAppearTogether() != null) {
                    subBossIds.addAll(bt.getBossesAppearTogether());
                }
            }

            java.util.Set<String> excludedSubTypes = java.util.Set.of(
                    "CLAN_DUNGEON", "LUYEN_TAP", "YARDRAT", "MABU_12H", "VO_DAI_HAT_MIT", "DHVT_23"
            );

            int count = 0;
            for (BossTemplate template : Manager.BOSS_TEMPLATES.values()) {
                if (template.isEnabled()) {
                    // Boss con đi cùng boss khác sẽ do boss cha khởi tạo và quản lý
                    if (subBossIds.contains(template.getId())) {
                        continue;
                    }
                    // Các boss phụ bản, luyện tập, võ đài, hang mabu 12h do hệ thống riêng quản lý
                    if (template.getSubType() != null && excludedSubTypes.contains(template.getSubType().toUpperCase().trim())) {
                        continue;
                    }
                    int spawnCount = template.getId() == BossID.BROLY ? Math.max(template.getSpawnCount(), 10) : template.getSpawnCount();
                    this.createBoss(template.getId(), spawnCount);
                    count++;
                }
            }
            Logger.success("Successfully spawned " + count + " active Bosses from DB!");
        } else {
            Logger.warning("Chưa có BossTemplate nào trong CSDL để spawn!");
        }
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            // 1. Ưu tiên tạo theo các Class script logic chuyên biệt
            Boss boss = switch (bossID) {
                case BossID.BROLY -> new Broly();
                case BossID.KU -> new Ku();
                case BossID.KUKU -> new Kuku();
                case BossID.MAP_DAU_DINH -> new MapDauDinh();
                case BossID.RAMBO -> new Rambo();
                case BossID.SO_4 -> new SO4();
                case BossID.SO_3 -> new SO3();
                case BossID.SO_2 -> new SO2();
                case BossID.SO_1 -> new SO1();
                case BossID.TIEU_DOI_TRUONG -> new TDT();
                case BossID.SO_4_NM -> new SO4_NM();
                case BossID.SO_3_NM -> new SO3_NM();
                case BossID.SO_2_NM -> new SO2_NM();
                case BossID.SO_1_NM -> new SO1_NM();
                case BossID.TIEU_DOI_TRUONG_NM -> new TDT_NM();
                case BossID.BUJIN -> new BUJIN();
                case BossID.KOGU -> new KOGU();
                case BossID.ZANGYA -> new ZANGYA();
                case BossID.BIDO -> new BIDO();
                case BossID.BOJACK -> new BOJACK();
                case BossID.SUPER_BOJACK -> new SUPER_BOJACK();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN -> new TaoPaiPai();
                case BossID.DRABURA -> new Drabura();
                case BossID.BUI_BUI -> new BuiBui();
                case BossID.BUI_BUI_2 -> new BuiBui2();
                case BossID.YA_CON -> new Yacon();
                case BossID.DRABURA_2 -> new Drabura2();
                case BossID.GOKU -> new Goku();
                case BossID.CADIC -> new Cadic();
                case BossID.MABU_12H -> new Mabu();
                case BossID.DRABURA_3 -> new Drabura3();
                case BossID.MABU -> new Mabu2H();
                case BossID.SUPERBU -> new SuperBu();
                case BossID.FIDE -> new Fide();
                case BossID.DR_KORE -> new DrKore();
                case BossID.ANDROID_19 -> new Android19();
                case BossID.ANDROID_13 -> new Android13();
                case BossID.ANDROID_14 -> new Android14();
                case BossID.ANDROID_15 -> new Android15();
                case BossID.PIC -> new Pic();
                case BossID.POC -> new Poc();
                case BossID.KING_KONG -> new KingKong();
                case BossID.XEN_BO_HUNG -> new XenBoHung();
                case BossID.SIEU_BO_HUNG -> new SieuBoHung();
                case BossID.XEN_CON_1 -> new XENCON1();
                case BossID.XEN_CON_2 -> new XENCON2();
                case BossID.XEN_CON_3 -> new XENCON3();
                case BossID.XEN_CON_4 -> new XENCON4();
                case BossID.XEN_CON_5 -> new XENCON5();
                case BossID.XEN_CON_6 -> new XENCON6();
                case BossID.XEN_CON_7 -> new XENCON7();
                case BossID.COOLER -> new Cooler();
                case BossID.KHIDOT -> new KhiDot();
                case BossID.NGUYETTHAN -> new NguyetThan();
                case BossID.NHATTHAN -> new NhatThan();
                case BossID.BIMA -> new BiMa();
                case BossID.MATROI -> new MaTroi();
                case BossID.DOI -> new Doi();
                case BossID.ONG_GIA_NOEL -> new OngGiaNoel();
                case BossID.SON_TINH -> new SonTinh();
                case BossID.THUY_TINH -> new ThuyTinh();
                case BossID.LAN_CON -> new LanCon();
                case BossID.SOI_HEC_QUYN1 -> new SoiHecQuyn();
                case BossID.O_DO1 -> new Odo();
                case BossID.Virut -> new Virut();
                case BossID.MAT_TROI -> new MatTroi();
                case BossID.BLACK_GOKU -> new BlackGoku();
                case BossID.CUMBER -> new Cumber();
                case BossID.AN_TROM -> new AnTrom();
                case BossID.RONG_NHI -> new RongNhi();
                case BossID.BABY -> new Baby();
                case BossID.B -> new B();
                default -> null;
            };

            if (boss != null) {
                return boss;
            }

            // 2. Nếu không có class logic riêng, tạo DynamicBoss từ DB
            if (Manager.BOSS_TEMPLATES != null) {
                BossTemplate template = Manager.BOSS_TEMPLATES.get(bossID);
                if (template != null) {
                    return new DynamicBoss(template);
                }
            }

            Logger.warning("Không tìm thấy cấu hình cho Boss ID: " + bossID + " trong DB hoặc code!");
            return null;
        } catch (Exception e) {
            Logger.error("Lỗi createBoss ID " + bossID + ": " + e + "\n");
            return null;
        }
    }

    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.idMark.setMenuType(3);
        Message msg = null;
        try {
            List<Boss> allBosses = new ArrayList<>(this.bosses);
            allBosses.addAll(BrolyManager.gI().getBosses());

            List<Boss> filtered = allBosses.stream().filter(boss -> {
                if (boss == null || boss.data == null || boss.data.length == 0 || boss.data[0] == null) {
                    return false;
                }
                int[] mapJoin = boss.data[0].getMapJoin();
                if (mapJoin != null && mapJoin.length > 0) {
                    int mId = mapJoin[0];
                    if (MapService.gI().isMapBossFinal(mId)
                            || MapService.gI().isMapHuyDiet(mId)
                            || MapService.gI().isMapCadic(mId)
                            || MapService.gI().isMapYardart(mId)
                            || MapService.gI().isMapMaBu(mId)
                            || MapService.gI().isMapBlackBallWar(mId)) {
                        return false;
                    }
                }
                return true;
            }).sorted((b1, b2) -> {
                boolean b1InMap = (b1.zone != null);
                boolean b2InMap = (b2.zone != null);
                if (b1InMap && !b2InMap) return -1;
                if (!b1InMap && b2InMap) return 1;
                return 0;
            }).toList();

            // Giới hạn tối đa 100 boss để không bị tràn sbyte (-128..127) trong client C#
            int count = Math.min(filtered.size(), 100);

            msg = new Message(-96);
            msg.writer().writeByte(0); // typeTop
            msg.writer().writeUTF("Boss (" + count + "/" + filtered.size() + ")"); // topName
            msg.writer().writeByte(count); // b49 (sbyte)

            for (int i = 0; i < count; i++) {
                Boss boss = filtered.get(i);
                msg.writer().writeInt(i + 1); // rank
                msg.writer().writeInt(i + 1); // pId

                short head = -1;
                short body = -1;
                short leg = -1;
                if (boss.data != null && boss.data.length > 0 && boss.data[0] != null && boss.data[0].getOutfit() != null) {
                    short[] outfit = boss.data[0].getOutfit();
                    if (outfit.length > 0) head = outfit[0];
                    if (outfit.length > 1) body = outfit[1];
                    if (outfit.length > 2) leg = outfit[2];
                }

                msg.writer().writeShort(head);
                msg.writer().writeShort(-1); // headICON (Client Controller.cs dòng 271 luôn đọc short này)
                msg.writer().writeShort(body);
                msg.writer().writeShort(leg);

                String name = "Boss";
                if (boss.data != null && boss.data.length > 0 && boss.data[0] != null && boss.data[0].getName() != null) {
                    name = boss.data[0].getName();
                }
                msg.writer().writeUTF(name);

                String status = boss.bossStatus != null ? boss.bossStatus.toString() : "UNKNOWN";
                msg.writer().writeUTF(status);

                if (boss.zone != null && boss.zone.map != null) {
                    msg.writer().writeUTF(boss.zone.map.mapName + " (" + boss.zone.map.mapId + ") k" + boss.zone.zoneId);
                } else {
                    msg.writer().writeUTF("Chưa xuất hiện");
                }
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(BossManager.class, e, "Lỗi showListBoss");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream().filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie()).findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && boss.zone != null && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst().orElse(null);
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            try {
                long st = System.currentTimeMillis();
                for (Boss boss : this.bosses) {
                    boss.update();
                }
                
                if (st - lastTimeWriteJSON > 2000) {
                    lastTimeWriteJSON = st;
                    try {
                        JSONArray bossList = new JSONArray();
                        for (Boss boss : this.bosses) {
                            if (boss == null || boss.isDie()) continue;
                            JSONObject obj = new JSONObject();
                            obj.put("id", boss.id);
                            
                            try {
                                obj.put("name", boss.data[0].getName());
                            } catch (Exception ex) {
                                obj.put("name", "Unknown");
                            }
                            
                            obj.put("status", boss.bossStatus.toString());
                            if (boss.zone != null && boss.zone.map != null) {
                                obj.put("map", boss.zone.map.mapName);
                                obj.put("zone", boss.zone.zoneId);
                            } else {
                                obj.put("map", "N/A");
                                obj.put("zone", "N/A");
                            }
                            bossList.add(obj);
                        }
                        try (FileWriter file = new FileWriter("C:\\NROServer\\lighthakai\\website\\nroforum\\bosses.json")) {
                            file.write(bossList.toJSONString());
                            file.flush();
                        }
                    } catch (Exception e) {}
                }
                
                long sleepTime = 1500 - (System.currentTimeMillis() - st);
                Thread.sleep(Math.max(sleepTime, 10));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
            }
        }
    }
}
