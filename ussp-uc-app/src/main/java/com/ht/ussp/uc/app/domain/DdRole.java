package com.ht.ussp.uc.app.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;


 
@Entity
@Data
@Table(name="DD_ROLE")
@NamedQuery(name="DdRole.findAll", query="SELECT h FROM DdRole h")
public class DdRole implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="CREATE_OPERATOR")
	private String createOperator;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="CREATED_DATETIME")
	private Date createdDatetime;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="LAST_MODIFIED_DATETIME")
	private Date lastModifiedDatetime;

	@Column(name="ROLEID")
	private String roleId;

	@Column(name="ROLENAME")
	private String roleName;

	@Column(name="GROUPNAME")
	private String groupName;

	@Column(name="UPDATE_OPERATOR")
	private String updateOperator;

}