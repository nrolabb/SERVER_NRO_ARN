package nro.models.boss.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nro.models.boss.BossData;
import nro.models.consts.BossType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossTemplate {
    private int id;
    private String name;
    private String type;
    private String subType;
    private byte gender;
    private boolean enabled;
    private int spawnCount;
    private int respawnDelay;
    private int despawnTimeout;
    private boolean isNotify;
    private boolean isZone01Disabled;
    private Integer requireTaskId;
    private String extraConfig;

    @Builder.Default
    private List<BossFormTemplate> forms = new ArrayList<>();

    @Builder.Default
    private List<Integer> mapJoin = new ArrayList<>();

    @Builder.Default
    private List<Integer> bossesAppearTogether = new ArrayList<>();

    @Builder.Default
    private List<BossRewardTemplate> rewards = new ArrayList<>();

    public BossData[] toBossDataArray() {
        if (forms == null || forms.isEmpty()) {
            return new BossData[0];
        }

        // Sắp xếp các form theo formOrder
        forms.sort(Comparator.comparingInt(BossFormTemplate::getFormOrder));

        int[] mapJoinArr = mapJoin.stream().mapToInt(Integer::intValue).toArray();
        int[] appearTogetherArr = bossesAppearTogether.stream().mapToInt(Integer::intValue).toArray();

        BossData[] arr = new BossData[forms.size()];
        for (int i = 0; i < forms.size(); i++) {
            BossFormTemplate form = forms.get(i);
            arr[i] = form.toBossData(this.gender, this.respawnDelay, mapJoinArr, appearTogetherArr);
        }
        return arr;
    }

    public BossType getMappedBossType() {
        if (subType != null) {
            String upper = subType.toUpperCase();
            if (upper.contains("HALLOWEEN")) return BossType.HALLOWEEN_EVENT;
            if (upper.contains("TRUNG_THU") || upper.contains("TRUNGTHU")) return BossType.TRUNGTHU_EVENT;
            if (upper.contains("TET")) return BossType.TET_EVENT;
            if (upper.contains("NOEL") || upper.contains("CHRISTMAS")) return BossType.CHRISTMAS_EVENT;
            if (upper.contains("HUNG_VUONG") || upper.contains("HUNGVUONG")) return BossType.HUNGVUONG_EVENT;
            if (upper.contains("SUMMER")) return BossType.SUMMER_EVENT;
        }
        return null;
    }
}
