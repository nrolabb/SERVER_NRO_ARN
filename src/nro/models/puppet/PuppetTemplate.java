package nro.models.puppet;

import lombok.Data;

@Data
public class PuppetTemplate {

    private int id;
    private short itemTemplateId;
    private String name;
    private int hp;
    private int ki;
    private int dame;
    private int crit;
    private short headId;
    private short bodyId;
    private short legId;
    private int initialTimeMinutes;
}
