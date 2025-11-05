package net.zcscloud.zhuohcun.zcorn.entity;

import lombok.Data;
import net.zcscloud.zhuohcun.zcorn.common.entity.LogicEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Data
@Inheritance
public class Place extends LogicEntity {
    @Column
    protected String name;  

    @Column
    protected String address;  

    @Column
    protected String description;  

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public String getDescription() {
        return description;
    }
}
