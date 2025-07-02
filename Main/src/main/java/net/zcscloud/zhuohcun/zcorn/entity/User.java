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
@Table(name = "user")
public class User extends LogicEntity {
    @Column
    protected String username;

    @Column
    protected String password;

    public String getUserName() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public int getIsdeleted() {
        return super.getIsdeleted();
    }
}
