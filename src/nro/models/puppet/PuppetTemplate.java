package nro.models.puppet;

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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public short getItemTemplateId() { return itemTemplateId; }
    public void setItemTemplateId(short itemTemplateId) { this.itemTemplateId = itemTemplateId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    
    public int getKi() { return ki; }
    public void setKi(int ki) { this.ki = ki; }
    
    public int getDame() { return dame; }
    public void setDame(int dame) { this.dame = dame; }
    
    public int getCrit() { return crit; }
    public void setCrit(int crit) { this.crit = crit; }
    
    public short getHeadId() { return headId; }
    public void setHeadId(short headId) { this.headId = headId; }
    
    public short getBodyId() { return bodyId; }
    public void setBodyId(short bodyId) { this.bodyId = bodyId; }
    
    public short getLegId() { return legId; }
    public void setLegId(short legId) { this.legId = legId; }
    
    public int getInitialTimeMinutes() { return initialTimeMinutes; }
    public void setInitialTimeMinutes(int initialTimeMinutes) { this.initialTimeMinutes = initialTimeMinutes; }
}
