package nro.models.boss.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BossSkillTemplate {
    private int id;
    private int formId;
    private int skillId;
    private int skillLevel;
    private int cooldown;
}
