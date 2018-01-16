package com.ht.ussp.uc.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ht.ussp.uc.app.domain.HtBoaOutOperatorLog;

/**
 * 
 * @ClassName: HtBoaOutOperatorLogRepository
 * @Description: 操作日志表持久层
 * @author adol yaojiehong@hongte.info
 * @date 2018年1月13日 上午10:37:44
 */
public interface HtBoaOutOperatorLogRepository
        extends JpaRepository<HtBoaOutOperatorLog, Long>,
        JpaSpecificationExecutor<HtBoaOutOperatorLog> {

}