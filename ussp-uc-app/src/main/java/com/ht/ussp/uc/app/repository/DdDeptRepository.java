package com.ht.ussp.uc.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ht.ussp.uc.app.domain.DdDept;

 
public interface DdDeptRepository
        extends JpaRepository<DdDept, Long> {


	public DdDept findByDeptId(String deptId);
}
