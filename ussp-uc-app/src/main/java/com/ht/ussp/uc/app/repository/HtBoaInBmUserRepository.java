package com.ht.ussp.uc.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.ht.ussp.uc.app.domain.HtBoaInBmUser;

 
/**
 * 信贷用户表
 * @author tangxs
 *
 */
@Repository
public interface HtBoaInBmUserRepository extends JpaSpecificationExecutor<HtBoaInBmUser>, JpaRepository<HtBoaInBmUser, Long> {

	HtBoaInBmUser findByUserId(String bmBusinessId);

    
}