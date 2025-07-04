package net.zcscloud.zhuohcun.zcorn.common.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@Entity
public abstract class LogicEntity{
    @Id
    @Column(insertable = false)
    protected int id;  

    @Column(insertable = false)
    protected int isdeleted;

    public int getIsdeleted() {
        return isdeleted;
    }
    public int getId() {
        return id;
    }
}
