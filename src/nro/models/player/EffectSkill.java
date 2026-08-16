package nro.models.player;

import lombok.Setter;
import nro.models.mob.Mob;
import nro.models.services.EffectSkillService;
import nro.models.services.ItemTimeService;
import nro.models.services.Service;
import nro.models.utils.Util;

/**
 *
 * @author By Mr Blue
 * 
 */

public class EffectSkill {

    @Setter
    private Player player;

    //thái dương hạ san
    public boolean isStun;
    public long lastTimeStartStun;
    public int timeStun;

    //khiên năng lượng
    public boolean isShielding;
    public long lastTimeShieldUp;
    public int timeShield;

    //biến khỉ
    public boolean isMonkey;
    public byte levelMonkey;
    public long lastTimeUpMonkey;
    public int timeMonkey;

    //Bình
    public boolean isBinh;
    public int typeBinh;
    public long lastTimeUpBinh;
    public int timeBinh;
    public Player playerUseMafuba;

    //Hóa đá
    public boolean isStone;
    public long lastTimeStone;
    public int timeStone;

    //Mabu Hold
    public boolean isMabuHold;

    //Làm chậm
    public boolean isLamCham;
    public long lastTimeLamCham;
    public int timeLamCham;

    //Tàn hình
    public boolean isTanHinh;
    public long lastTimeTanHinh;
    public int timeTanHinh;

    //PK Commeson
    public boolean isPKCommeson;
    public long lastTimePKCommeson;
    public int timePKCommeson;

    //PK Sieu Than Thuy
    public boolean isPKSTT;
    public long lastTimePKSTT;
    public int timePKSTT;

    //Chibi
    public boolean isChibi;
    public long lastTimeChibi;
    public int timeChibi;

    //tái tạo năng lượng
    public boolean isCharging;
    public int countCharging;

    //huýt sáo
    public int tiLeHPHuytSao;
    public long lastTimeHuytSao;

    //thôi miên
    public boolean isThoiMien;
    public long lastTimeThoiMien;
    public int timeThoiMien;

    //trói
    public boolean useTroi;
    public boolean anTroi;
    public long lastTimeTroi;
    public int timeTroi;
    public Player plTroi;
    public Player plAnTroi;
    public Mob mobAnTroi;

    //dịch chuyển tức thời
    public boolean isBlindDCTT;
    public long lastTimeBlindDCTT;
    public int timeBlindDCTT;

    //socola
    public boolean isSocola;
    public long lastTimeSocola;
    public int timeSocola;
    public int countPem1hp;

    //halloween
    public boolean isHalloween;
    public long lastTimeHalloween;
    public int timeHalloween;
    public int idOutfitHalloween;

    //Use Mafuba
    public boolean isUseMafuba;
    public long lastTimeUseMafuba;
    public int timeUseMafuba;

    //Use Skill Monkey
    public boolean isUseSkillMonkey;
    public long lastTimeUseSkillMonkey;
    public int timeUseSkillMonkey;

    //Bien hinh
    public boolean isBienHinh;
    public long lastTimeBienHinh;
    public int timeBienHinh;
    public int levelBienHinh;
    public int frameBienHinh;
    public long lastTimeFrameBienHinh;
    public boolean wasSpineBienHinh;
    public boolean isPreparingBienHinh;
    public long lastTimePrepareBienHinh;
    public int timePrepareBienHinh;
    public int pendingBienHinhSkillLevel;
    public boolean showPreviewBienHinh;
    public long lastTimePreviewBienHinh;


    //Intrinsic
    public boolean isIntrinsic;
    public long lastTimeUseSkill;
    public int skillID;
    public int cooldown;

    //Dame Buff
    public boolean isDameBuff;
    public long lastTimeDameBuff;
    public int timeDameBuff;
    public int tileDameBuff;

    //Trị thương buff
    public boolean isTriThuong;
    public long lastTimeTriThuong;
    public int timeTriThuong;
    public int countTriThuong;

    //Buff Pea 10-11
    public boolean isBuffPea;
    public long lastTimeBuffPea;
    public int countBuffPea;

    public boolean isBodyChangeTechnique;
    boolean isXinbato;

    public EffectSkill(Player player) {
        this.player = player;
    }

    public void removeSkillEffectWhenDie() {
        if (isPreparingBienHinh) {
            EffectSkillService.gI().cancelPrepareBienHinh(player);
        }
        if (isMonkey) {
            EffectSkillService.gI().monkeyDown(player);
        }
        if (isUseSkillMonkey) {
            EffectSkillService.gI().finishUseMonkey(player);
        }
        if (isBinh) {
            EffectSkillService.gI().BinhDown(player);
        }
        if (isShielding) {
            EffectSkillService.gI().removeShield(player);
            ItemTimeService.gI().removeItemTime(player, 3784);
        }
        if (useTroi) {
            EffectSkillService.gI().removeUseTroi(this.player);
        }
        if (isStun) {
            EffectSkillService.gI().removeStun(this.player);
        }
        if (isThoiMien) {
            EffectSkillService.gI().removeThoiMien(this.player);
        }
        if (isBlindDCTT) {
            EffectSkillService.gI().removeBlindDCTT(this.player);
        }
        if (isStone) {
            EffectSkillService.gI().removeStone(this.player);
        }
        if (isLamCham) {
            EffectSkillService.gI().removeLamCham(this.player);
        }
        if (isTanHinh) {
            EffectSkillService.gI().removeTanHinh(this.player);
        }
        if (isMabuHold) {
            EffectSkillService.gI().removeMabuHold(this.player);
        }
        if (isDameBuff) {
            EffectSkillService.gI().removeDameBuff(this.player);
        }
        if (isBienHinh) {
            EffectSkillService.gI().downBienHinh(this.player);
        }
        if (isBuffPea) {
            isBuffPea = false;
            Service.gI().removeEffPlayer(this.player, 78);
        }
    }

    public void update() {
        if (isPreparingBienHinh && !nro.models.services.EffectSkillService.gI().isUseSpineBienHinh(player) && Util.canDoWithTime(lastTimePreviewBienHinh, 150)) {
            showPreviewBienHinh = !showPreviewBienHinh;
            lastTimePreviewBienHinh = System.currentTimeMillis();
            Service.gI().Send_Caitrang(player);
        }
        if (isPreparingBienHinh && Util.canDoWithTime(lastTimePrepareBienHinh, timePrepareBienHinh > 0 ? timePrepareBienHinh : 2000)) {
            EffectSkillService.gI().finishBienHinh(player);
        }
        if (isBienHinh && Util.canDoWithTime(lastTimeBienHinh, timeBienHinh)) {
            EffectSkillService.gI().downBienHinh(player);
        }
        if (isBienHinh && wasSpineBienHinh != nro.models.services.EffectSkillService.gI().isUseSpineBienHinh(player)) {
            wasSpineBienHinh = !wasSpineBienHinh;
            Service.gI().player(player);
            Service.gI().Send_Caitrang(player);
            int iconLvFirst = (player.gender == nro.models.consts.ConstPlayer.TRAI_DAT) ? 31254 : (player.gender == nro.models.consts.ConstPlayer.NAMEC) ? 31266 : (player.gender == nro.models.consts.ConstPlayer.XAYDA) ? 31260 : 0;
            int timeIcon = (int) ((lastTimeBienHinh + timeBienHinh - System.currentTimeMillis()) / 1000);
            if (timeIcon > 0) {
                nro.models.services.ItemTimeService.gI().sendItemTime(player, iconLvFirst + levelBienHinh - 1, timeIcon);
            }
            for (nro.models.player.Player p : player.zone.getPlayers()) {
                if (p.id != player.id) {
                    Service.gI().playerInfoUpdate(player, p, player.name, player.getHead(), player.getBody(), player.getLeg());
                }
            }
        }
        if (isMonkey && (Util.canDoWithTime(lastTimeUpMonkey, timeMonkey))) {
            EffectSkillService.gI().monkeyDown(player);
        }
        if (isBinh && (Util.canDoWithTime(lastTimeUpBinh, timeBinh))) {
            EffectSkillService.gI().BinhDown(player);
        }
        if (isShielding && (Util.canDoWithTime(lastTimeShieldUp, timeShield))) {
            EffectSkillService.gI().removeShield(player);
        }
        if (useTroi && Util.canDoWithTime(lastTimeTroi, timeTroi)
                || plAnTroi != null && plAnTroi.isDie()
                || useTroi && isHaveEffectSkill()) {
            EffectSkillService.gI().removeUseTroi(this.player);
        }
        if (isStun && Util.canDoWithTime(lastTimeStartStun, timeStun)) {
            EffectSkillService.gI().removeStun(this.player);
        }
        if (isThoiMien && (Util.canDoWithTime(lastTimeThoiMien, timeThoiMien))) {
            EffectSkillService.gI().removeThoiMien(this.player);
        }
        if (isBlindDCTT && (Util.canDoWithTime(lastTimeBlindDCTT, timeBlindDCTT))) {
            EffectSkillService.gI().removeBlindDCTT(this.player);
        }
        if (isSocola && (Util.canDoWithTime(lastTimeSocola, timeSocola))) {
            EffectSkillService.gI().removeSocola(this.player);
        }
        if (tiLeHPHuytSao != 0 && Util.canDoWithTime(lastTimeHuytSao, 30000)) {
            EffectSkillService.gI().removeHuytSao(this.player);
        }
        if (isStone && Util.canDoWithTime(lastTimeStone, timeStone)) {
            EffectSkillService.gI().removeStone(this.player);
        }
        if (isLamCham && Util.canDoWithTime(lastTimeLamCham, timeLamCham)) {
            EffectSkillService.gI().removeLamCham(this.player);
        }
        if (isTanHinh && Util.canDoWithTime(lastTimeTanHinh, timeTanHinh)) {
            EffectSkillService.gI().removeTanHinh(this.player);
        }
        if (isPKCommeson && Util.canDoWithTime(lastTimePKCommeson, timePKCommeson)) {
            EffectSkillService.gI().removePKCommeson(this.player);
        }
        if (isPKSTT && Util.canDoWithTime(lastTimePKSTT, timePKSTT)) {
            EffectSkillService.gI().removePKSTT(this.player);
        }
        if (isChibi && Util.canDoWithTime(lastTimeChibi, timeChibi)) {
            EffectSkillService.gI().removeChibi(this.player);
        }
//        if (isHalloween && Util.canDoWithTime(lastTimeHalloween, timeHalloween)) {
//            EffectSkillService.gI().removeHalloween(this.player);
//        }
        if (isUseMafuba && Util.canDoWithTime(lastTimeUseMafuba, timeUseMafuba)) {
            EffectSkillService.gI().finishUseMafuba(player);
        }
        if (isUseSkillMonkey && Util.canDoWithTime(lastTimeUseSkillMonkey, timeUseSkillMonkey)) {
            EffectSkillService.gI().finishUseMonkey(player);
        }
        if (isIntrinsic && Util.canDoWithTime(lastTimeUseSkill, cooldown)) {
            EffectSkillService.gI().releaseCooldownSkillByIntrinsic(player);
        }
        if (isDameBuff && Util.canDoWithTime(lastTimeDameBuff, timeDameBuff)) {
            EffectSkillService.gI().removeDameBuff(this.player);
        }
        if (isTriThuong) {
            if (Util.canDoWithTime(lastTimeTriThuong, 1000)) {
                lastTimeTriThuong = System.currentTimeMillis();
                countTriThuong++;
                int heal = (int) (player.nPoint.hpMax * 20 / 100);
                player.nPoint.addHp(heal);
                nro.models.services.PlayerService.gI().sendInfoHpMp(player);
                Service.gI().Send_Info_NV(player);
                if (countTriThuong >= 5) {
                    isTriThuong = false;
                    Service.gI().removeEffPlayer(player, 78);
                }
            }
        }
        if (isBuffPea) {
            if (Util.canDoWithTime(lastTimeBuffPea, 1000)) {
                lastTimeBuffPea = System.currentTimeMillis();
                countBuffPea++;
                int heal = (int) (player.nPoint.hpMax * 10 / 100);
                player.nPoint.addHp(heal);
                nro.models.services.PlayerService.gI().sendInfoHpMp(player);
                Service.gI().Send_Info_NV(player);
                if (countBuffPea >= 3) {
                    isBuffPea = false;
                    Service.gI().removeEffPlayer(player, 78);
                }
            }
        }
    }

    public boolean isHaveEffectSkill() {
        return (isStun || isBlindDCTT || anTroi || isThoiMien || isStone || isMabuHold || isUseSkillMonkey) && !player.isDie();
    }

    public void dispose() {
        this.player = null;
        this.plAnTroi = null;
        this.plTroi = null;
        this.playerUseMafuba = null;
        this.mobAnTroi = null;
    }
}
