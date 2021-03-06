package com.ht.ussp.uc.app.model;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "BoaInAppInfo", description = "系统信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoaInAppInfo {

	@ApiModelProperty(value = "id", dataType = "long")
	long id;
	
    @ApiModelProperty(value = "系统编号", dataType = "string")
    String app;

    @ApiModelProperty(value = "系统名称", dataType = "string")
    String name;

    @ApiModelProperty(value = "系统名称中文", dataType = "string")
    String nameCn;

    @ApiModelProperty(value = "状态", dataType = "string")
    String status;

    @ApiModelProperty(value = "创建人", dataType = "string")
    String createOperator;

    @ApiModelProperty(value = "创建时间", dataType = "date")
    Date createdDatetime;

    @ApiModelProperty(value = "更新人", dataType = "date")
    String updateOperator;

    @ApiModelProperty(value = "最后修改时间", dataType = "string")
    Date lastModifiedDatetime;
    
    @ApiModelProperty(value = "状态", dataType = "int")
    int delFlag;
    
    @ApiModelProperty(value = "是否管理员", dataType = "string")
    private String controller;

    @ApiModelProperty(value = "系统类别", dataType = "int")//0外部系统 1内部系统
    private Integer isOS;
    
    @ApiModelProperty(value = "是否管理员", dataType = "string")
    private String tips ;

    @ApiModelProperty(value = "登录次数", dataType = "int")//0外部系统 1内部系统
    private Integer maxLoginCount;
    
    @ApiModelProperty(value = "token", dataType = "string")
    private String sysToken ;
    
    @ApiModelProperty(value = "isPush", dataType = "string")// 是否推送mq消息  0不推送 1推送
    private String isPush ;
    
    public BoaInAppInfo(String app, String name, String nameCn,
            String status, String createOperator,
            Date createdDatetime, String updateOperator,
            Date lastModifiedDatetime,Integer delFlag,Long id,Integer isOS,Integer maxLoginCount,String tips,String sysToken,String isPush) {
        this.app = app;
        this.name = name;
        this.nameCn = nameCn;
        this.status = status;
        this.createOperator = createOperator;
        this.createdDatetime = createdDatetime;
        this.updateOperator = updateOperator;
        this.lastModifiedDatetime = lastModifiedDatetime;
        this.delFlag = delFlag;
        this.id = id;
        this.isOS = isOS;
        this.maxLoginCount = maxLoginCount;
        this.tips = tips;
        this.sysToken = sysToken;
        this.isPush = isPush;
    }
    
    public BoaInAppInfo(String app, String name, String nameCn,
            String status, String createOperator,
            Date createdDatetime, String updateOperator,
            Date lastModifiedDatetime,int delFlag,Long id,String controller) {
        this.app = app;
        this.name = name;
        this.nameCn = nameCn;
        this.status = status;
        this.createOperator = createOperator;
        this.createdDatetime = createdDatetime;
        this.updateOperator = updateOperator;
        this.lastModifiedDatetime = lastModifiedDatetime;
        this.delFlag = delFlag;
        this.id = id;
        this.controller=controller;
    }

}
