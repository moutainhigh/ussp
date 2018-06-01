package com.ht.ussp.uc.app.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ht.ussp.uc.app.service.DingDingService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;


/**
 * 任务Handler示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、继承"IJobHandler"：“com.xxl.job.core.handler.IJobHandler”；
 * 2、注册到Spring容器：添加“@Component”注解，被Spring容器扫描为Bean实例；
 * 3、注册到执行器工厂：添加“@JobHandler(value="自定义jobhandler名称")”注解，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 4、执行日志：需要通过 "XxlJobLogger.log" 打印执行日志；
 *
 * @author xuxueli 2015-12-19 19:43:36
 */
@JobHandler(value = "ConvertOrgTimerJobHandler03")
@Component
public class ConvertOrgTimerJobHandler03 extends IJobHandler {
	@Autowired
	private DingDingService dingDingService;
	
    @Override
    public ReturnT<String> execute(String param) throws Exception {
    	XxlJobLogger.log("---------------------定时任务开始(转换机构)--------------------->");
    	dingDingService.convertOrg();
    	XxlJobLogger.log("---------------------定时任务结束(转换机构)--------------------->");
        return SUCCESS;
    }
}