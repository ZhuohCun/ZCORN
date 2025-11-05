package net.zcscloud.zhuohcun.zcorn.entity;

import lombok.Data;
import net.zcscloud.zhuohcun.zcorn.common.entity.LogicEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Inheritance
@Data
@Table(name = "device")
public class Device extends LogicEntity {

    @Column
    protected String name;  

    @Column(name = "`type`")
    protected int type;  //1.soil sensor 2.temperature sensor 3.camera 4.watering device 5.drug sprayer 6.water sprayer

    @Column
    protected float cvalue;

    @Column
    protected String physicalid;  

    @Column
    protected int label;

    @Column
    protected int isoperator;

    @Column
    protected long turntime;

    @Column
    protected int isforcedoff;

    @Column
    protected int id$place;

    public String getName(){
        return this.name;
    }
    public float getCvalue(){
        return this.cvalue;
    }
    public int getId$place(){
        return this.id$place;
    }
    public int getType(){
        return this.type;
    }
    public int getLabel(){
        return this.label;
    }
    public int getIsoperator(){
        return this.isoperator;
    }
    public long getTurntime(){
        return this.turntime;
    }
    public int getIsforcedoff(){
        return this.isforcedoff;
    }

}
