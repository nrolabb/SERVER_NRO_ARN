package nro.models.puppet;

import java.util.ArrayList;
import nro.models.map.Zone;
import nro.models.map.service.ChangeMapService;
import nro.models.mob.Mob;
import nro.models.player.NewPet;
import nro.models.player.Player;
import nro.models.services.PlayerService;
import nro.models.services.Service;
import nro.models.services.SkillService;
import nro.models.skill.Skill;
import nro.models.utils.SkillUtil;
import nro.models.utils.Util;

/** A player-owned combat clone. It is not registered in BossManager. */
public class PuppetBoss extends NewPet {

    private static final int ATTACK_RANGE = 500;
    private static final int MOVE_TO_MELEE_RANGE = 80;

    private final PuppetTemplate puppetTemplate;
    private long expiresAt;
    private long lastTimeAttack;
    private Zone lastTimerZone;
    private boolean hiddenByMasterDeath;

    public PuppetBoss(Player master, PuppetTemplate template, int remainingMinutes) {
        super(master,
                template.getHeadId() >= 0 ? template.getHeadId() : master.getHead(),
                template.getBodyId() >= 0 ? template.getBodyId() : master.getBody(),
                template.getLegId() >= 0 ? template.getLegId() : master.getLeg());
        this.puppetTemplate = template;
        this.name = template.getName();
        this.gender = master.gender;
        this.expiresAt = System.currentTimeMillis() + remainingMinutes * 60_000L;

        // A puppet behaves like a summoned clone, not a normal player/boss cycle.
        this.isBoss = false;
        this.typePk = master.typePk;
        for (int i = 0; i < 15; i++) {
            this.inventory.itemsBody.add(nro.models.services.ItemService.gI().createItemNull());
        }
        initStats();
        copyCombatSkills(master);
    }

    private void initStats() {
        nPoint.power = 1;
        nPoint.tiemNang = 1;
        nPoint.limitPower = 1;
        nPoint.hpg = Math.max(1, puppetTemplate.getHp());
        nPoint.mpg = Math.max(1, puppetTemplate.getKi());
        nPoint.dameg = Math.max(1, puppetTemplate.getDame());
        nPoint.critg = Math.max(0, puppetTemplate.getCrit());
        nPoint.stamina = 1000;
        nPoint.maxStamina = 1000;
        nPoint.setBasePoint();
        nPoint.setFullHpMp();
    }

    private void copyCombatSkills(Player owner) {
        playerSkill.skills.clear();
        for (Skill skill : owner.playerSkill.skills) {
            if (skill != null && skill.template != null && isSupportedCombatSkill(skill.template.id)) {
                playerSkill.skills.add(new Skill(skill));
            }
        }
        if (playerSkill.skills.isEmpty()) {
            int meleeSkill = owner.gender == 0 ? Skill.DRAGON : owner.gender == 1 ? Skill.DEMON : Skill.GALICK;
            int beamSkill = owner.gender == 0 ? Skill.KAMEJOKO : owner.gender == 1 ? Skill.MASENKO : Skill.ANTOMIC;
            Skill melee = SkillUtil.createSkill(meleeSkill, 1);
            Skill beam = SkillUtil.createSkill(beamSkill, 1);
            if (melee != null) {
                playerSkill.skills.add(melee);
            }
            if (beam != null) {
                playerSkill.skills.add(beam);
            }
        }
    }

    private boolean isSupportedCombatSkill(int skillId) {
        return switch (skillId) {
            case Skill.DRAGON, Skill.DEMON, Skill.GALICK, Skill.KAIOKEN, Skill.LIEN_HOAN,
                    Skill.KAMEJOKO, Skill.MASENKO, Skill.ANTOMIC, Skill.DICH_CHUYEN_TUC_THOI -> true;
            default -> false;
        };
    }

    @Override
    public void update() {
        if (master == null) {
            return;
        }
        if (master.beforeDispose || !PuppetService.gI().isEquipped(master, puppetTemplate.getItemTemplateId())) {
            PuppetService.gI().remove(master);
            return;
        }
        if (System.currentTimeMillis() >= expiresAt) {
            PuppetService.gI().expire(master, this);
            return;
        }

        // Chủ chết hoặc đang ở khoảng chuyển map: chỉ đưa khôi lỗi ra khỏi
        // zone, không dispose object và không xóa item/thời gian.
        if (master.isDie() || master.zone == null) {
            if (isDie() && zone != null && !PuppetService.gI().handleDeath(master, this)) {
                return;
            }
            hideUntilMasterReturns();
            PuppetService.gI().syncRemainingTime(master, this);
            return;
        }

        if (hiddenByMasterDeath) {
            showBesideMaster();
        }
        super.update();
        if (zone == null || zone != master.zone) {
            return;
        }
        if (zone != lastTimerZone) {
            lastTimerZone = zone;
            PuppetService.gI().sendEquippedTimer(master);
        }
        if (isDie() && !PuppetService.gI().handleDeath(master, this)) {
            return;
        }
        nPoint.mp = nPoint.mpMax;
        followOwner();
        attackNearestTarget();
        PuppetService.gI().syncRemainingTime(master, this);
    }

    private void hideUntilMasterReturns() {
        if (zone != null) {
            ChangeMapService.gI().exitMap(this);
        }
        hiddenByMasterDeath = true;
        lastTimerZone = null;
    }

    private void showBesideMaster() {
        if (master == null || master.isDie() || master.zone == null) {
            return;
        }
        joinMapMaster();
        hiddenByMasterDeath = false;
        PuppetService.gI().sendEquippedTimer(master);
    }

    @Override
    protected boolean autoReviveWhenDead() {
        return false;
    }

    private void followOwner() {
        if (Util.getDistance(this, master) > ATTACK_RANGE) {
            PlayerService.gI().playerMove(this, master.location.x + Util.nextInt(-40, 40), master.location.y);
        }
    }

    private void attackNearestTarget() {
        if (playerSkill.skills.isEmpty() || !Util.canDoWithTime(lastTimeAttack, 500)) {
            return;
        }
        Mob mob = findNearestMob();
        Player boss = mob == null ? findNearestEnemyBoss() : null;
        if (mob == null && boss == null) {
            return;
        }
        Skill skill = playerSkill.skills.get(Util.nextInt(0, playerSkill.skills.size() - 1));
        playerSkill.skillSelect = skill;
        if (!SkillService.gI().canUseSkillWithCooldown(this) || !SkillService.gI().canUseSkillWithMana(this)) {
            return;
        }
        if (isMelee(skill.template.id)) {
            int targetX = mob != null ? mob.location.x : boss.location.x;
            int targetY = mob != null ? mob.location.y : boss.location.y;
            if ((mob != null ? Util.getDistance(this, mob) : Util.getDistance(this, boss)) > MOVE_TO_MELEE_RANGE) {
                PlayerService.gI().playerMove(this, targetX + Util.nextInt(-30, 30), targetY);
            }
        }
        SkillService.gI().useSkill(this, boss, mob, -1, null);
        lastTimeAttack = System.currentTimeMillis();
    }

    private boolean isMelee(int skillId) {
        return skillId == Skill.DRAGON || skillId == Skill.DEMON || skillId == Skill.GALICK
                || skillId == Skill.KAIOKEN || skillId == Skill.LIEN_HOAN;
    }

    private Mob findNearestMob() {
        if (zone == null) {
            return null;
        }
        Mob nearest = null;
        int distance = ATTACK_RANGE;
        for (Mob mob : zone.mobs) {
            if (mob == null || mob.isDie()) {
                continue;
            }
            int currentDistance = Util.getDistance(this, mob);
            if (currentDistance <= distance) {
                distance = currentDistance;
                nearest = mob;
            }
        }
        return nearest;
    }

    private Player findNearestEnemyBoss() {
        if (zone == null) {
            return null;
        }
        Player nearest = null;
        int distance = ATTACK_RANGE;
        // Copy to avoid a concurrent zone-list modification during map changes.
        for (Player boss : new ArrayList<>(zone.getBosses())) {
            if (boss == null || boss == this || boss instanceof PuppetBoss || boss.isDie()) {
                continue;
            }
            int currentDistance = Util.getDistance(this, boss);
            if (currentDistance <= distance) {
                distance = currentDistance;
                nearest = boss;
            }
        }
        return nearest;
    }

    public int getRemainingMinutes() {
        long remaining = Math.max(0L, expiresAt - System.currentTimeMillis());
        return (int) Math.min(Short.MAX_VALUE, (remaining + 59_999L) / 60_000L);
    }

    public int getRemainingSeconds() {
        long remaining = Math.max(0L, expiresAt - System.currentTimeMillis());
        return (int) Math.min(Integer.MAX_VALUE, (remaining + 999L) / 1000L);
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void addMinutes(int minutes) {
        expiresAt = Math.max(expiresAt, System.currentTimeMillis()) + minutes * 60_000L;
    }

    public void subtractMinutes(int minutes) {
        expiresAt = Math.max(System.currentTimeMillis(), expiresAt - minutes * 60_000L);
    }

    public short getPuppetItemId() {
        return puppetTemplate.getItemTemplateId();
    }
}
