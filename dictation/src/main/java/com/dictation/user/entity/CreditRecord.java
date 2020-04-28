package com.dictation.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @ClassName CreditRecord
 * @Description
 * @Author zlc
 * @Date 2020-04-23 20:15
 */
@TableName("tbl_credit_record")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreditRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String reason;

    private Integer increment;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;



}