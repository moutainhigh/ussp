package com.ht.ussp.uc.app.domain;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * 
* @ClassName: HtBoaInExtendRole
* @Description: 扩展角色表
* @author wim qiuwenwu@hongte.info
* @date 2018年1月5日 下午2:48:59
 */
@Entity
@Table(name="HT_BOA_IN_EXTEND_ROLE")
@NamedQuery(name="HtBoaInExtendRole.findAll", query="SELECT h FROM HtBoaInExtendRole h")
public class HtBoaInExtendRole implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	@Column(name="BASE_ROLE_CODE")
	private String baseRoleCode;

	@Column(name="CREATE_OPERATOR")
	private String createOperator;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATED_DATETIME")
	private Date createdDatetime;

	@Column(name="DEL_FLAG")
	private int delFlag;

	@Column(name="JPA_VERSION")
	private int jpaVersion;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_MODIFIED_DATETIME")
	private Date lastModifiedDatetime;

	@Column(name="SUPER_ROLE_CODE")
	private String superRoleCode;

	@Column(name="UPDATE_OPERATOR")
	private String updateOperator;

	public HtBoaInExtendRole() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBaseRoleCode() {
		return this.baseRoleCode;
	}

	public void setBaseRoleCode(String baseRoleCode) {
		this.baseRoleCode = baseRoleCode;
	}

	public String getCreateOperator() {
		return this.createOperator;
	}

	public void setCreateOperator(String createOperator) {
		this.createOperator = createOperator;
	}

	public Date getCreatedDatetime() {
		return this.createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public int getDelFlag() {
		return this.delFlag;
	}

	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}

	public int getJpaVersion() {
		return this.jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public Date getLastModifiedDatetime() {
		return this.lastModifiedDatetime;
	}

	public void setLastModifiedDatetime(Date lastModifiedDatetime) {
		this.lastModifiedDatetime = lastModifiedDatetime;
	}

	public String getSuperRoleCode() {
		return this.superRoleCode;
	}

	public void setSuperRoleCode(String superRoleCode) {
		this.superRoleCode = superRoleCode;
	}

	public String getUpdateOperator() {
		return this.updateOperator;
	}

	public void setUpdateOperator(String updateOperator) {
		this.updateOperator = updateOperator;
	}

}