package nro.models.boss.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nro.models.boss.BossData;
import nro.models.consts.AppearType;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossFormTemplate {
    private int id;
    private int bossId;
    private int formOrder;
    private String name;
    private long hpMin;
    private long hpMax;
    private int dame;
    private short outfitHead;
    private short outfitBody;
    private short outfitLeg;
    private short outfitBag;
    private short outfitAura;
    private short outfitEff;
    private String textStart;
    private String textMid;
    private String textEnd;

    @Builder.Default
    private List<BossSkillTemplate> skills = new ArrayList<>();

    public BossData toBossData(byte gender, int secondsRest, int[] mapJoin, int[] appearTogether) {
        short[] outfit = new short[]{outfitHead, outfitBody, outfitLeg, outfitBag, outfitAura, outfitEff};

        int[] hp;
        if (hpMin == hpMax) {
            hp = new int[]{(int) hpMin};
        } else {
            hp = new int[]{(int) hpMin, (int) hpMax};
        }

        int[][] skillTemp;
        if (skills != null && !skills.isEmpty()) {
            skillTemp = new int[skills.size()][3];
            for (int i = 0; i < skills.size(); i++) {
                BossSkillTemplate sk = skills.get(i);
                skillTemp[i] = new int[]{sk.getSkillId(), sk.getSkillLevel(), sk.getCooldown()};
            }
        } else {
            skillTemp = new int[0][0];
        }

        String[] textS = parseTexts(textStart);
        String[] textM = parseTexts(textMid);
        String[] textE = parseTexts(textEnd);

        AppearType appearType = formOrder == 0 ? AppearType.DEFAULT_APPEAR : AppearType.ANOTHER_LEVEL;

        BossData bossData = new BossData(
                this.name != null ? this.name : "",
                gender,
                outfit,
                this.dame,
                hp,
                mapJoin != null ? mapJoin : new int[0],
                skillTemp,
                textS,
                textM,
                textE,
                secondsRest,
                appearType
        );

        if (appearTogether != null && appearTogether.length > 0) {
            bossData.setBossesAppearTogether(appearTogether);
        }

        return bossData;
    }

    public static String[] parseTexts(String text) {
        if (text == null || text.trim().isEmpty() || text.equals("[]")) {
            return new String[0];
        }
        text = text.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            try {
                Object obj = JSONValue.parse(text);
                if (obj instanceof JSONArray) {
                    JSONArray arr = (JSONArray) obj;
                    String[] res = new String[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        res[i] = String.valueOf(arr.get(i));
                    }
                    return res;
                }
            } catch (Exception ignored) {
            }
        }
        if (text.contains("|")) {
            return text.split("\\|");
        }
        if (text.contains("\n")) {
            return text.split("\n");
        }
        return new String[]{text};
    }
}
