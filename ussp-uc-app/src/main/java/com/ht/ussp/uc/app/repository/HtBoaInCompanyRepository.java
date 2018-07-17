package com.ht.ussp.uc.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.ht.ussp.uc.app.domain.HtBoaInCompany;
import com.ht.ussp.uc.app.model.BoaInBusiOrgInfo;

 
public interface HtBoaInCompanyRepository extends JpaSpecificationExecutor<HtBoaInCompany>, JpaRepository<HtBoaInCompany, Long>   {

    List<HtBoaInCompany> findByBranchCode(String orgCode);

    @Query("SELECT new com.ht.ussp.uc.app.model.BoaInBusiOrgInfo (u.businessOrgCode,u.businessOrgName,u.parentOrgCode,u.businessGroup,u.branchCode,u.districtCode ,u.financeCode,u.approvalCode,u.activityCode,u.bmOrgCode,u.orgLevel,u.status,u.province,u.city,u.county,u.dataSource,u.isHeadDept,u.isAppRovalDept,u.deptAddress,u.deptTel,u.deptUser,u.businessPhone,c.branchNameContract,c.branchAddressContract,c.branchPhoneContract,c.branchPledgeEmailContract,c.legalPerson,c.horizontalText,c.uniteCode,c.cosText,c.guid,c.highestDebtRatio)  FROM HtBoaInBusinessOrg u, HtBoaInCompany c WHERE u.businessOrgCode = c.branchCode   AND c.branchCode= ?1 GROUP BY u")
    List<BoaInBusiOrgInfo> listCompnayInfo(String branchCode);
    
}
