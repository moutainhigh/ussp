package com.ht.ussp.uc.app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.esotericsoftware.minlog.Log;
import com.ht.ussp.core.PageResult;
import com.ht.ussp.core.ReturnCodeEnum;
import com.ht.ussp.uc.app.config.AmqpConfig;
import com.ht.ussp.uc.app.domain.HtBoaInApp;
import com.ht.ussp.uc.app.domain.HtBoaInUserApp;
import com.ht.ussp.uc.app.model.BoaInAppInfo;
import com.ht.ussp.uc.app.model.PageConf;
import com.ht.ussp.uc.app.model.SelfBoaInUserInfo;
import com.ht.ussp.uc.app.repository.HtBoaInAppRepository;
import com.ht.ussp.uc.app.repository.HtBoaInUserAppRepository;
import com.ht.ussp.uc.app.vo.ResVo;

/**
 * @author wim qiuwenwu@hongte.info
 * @ClassName: HtBoaInUserAppService
 * @Description: 用户与系统关联业务处理
 * @date 2018年1月9日 下午6:13:03
 */
@Service
public class HtBoaInUserAppService {
	@Autowired
    private HtBoaInUserAppRepository htBoaInUserAppRepository;
	@Autowired
    private HtBoaInAppRepository htBoaInAppRepository;
	@Autowired
	private RabbitMessagingTemplate rabbitMessagingTemplate;

    //resType:view\group\module\btn\tab\api
    public List<ResVo> getResVoList(String app, String userId, String resType) {
        //判断是否管理

        //如果是管理，直接通过app查询资源

        //如果不是管理，则需要通过岗位、角色获取资源

        return new ArrayList<>();
    }

    /**
     * @return HtBoaInUserApp
     * @throws
     * @Title: findUserAndAppInfo
     * @Description: 验证用户
     */
    public String findUserAndAppInfo(String userId, String app) {
        try {
            List<HtBoaInUserApp> htBoaInUserApp = htBoaInUserAppRepository.findByuserId(userId);
            if (htBoaInUserApp.isEmpty()) {
                return null;
            }
            if (htBoaInUserApp.size() > 0) {
                for (int i = 0; i < htBoaInUserApp.size(); i++) {
                    if (app.equals(htBoaInUserApp.get(i).getApp())) {
                        return htBoaInUserApp.get(i).getController();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public PageResult listUserAppByPage(PageConf pageConf, Map<String, String> query) {
        PageResult result = new PageResult();
        Sort sort = null;
        Pageable pageable = null;
        List<Order> orders = new ArrayList<Order>();
        if (null != pageConf.getSortNames()) {
            for (int i = 0; i < pageConf.getSortNames().size(); i++) {
                orders.add(new Order(pageConf.getSortOrders().get(i), pageConf.getSortNames().get(i)));
            }
            sort = new Sort(orders);
        }

        if (null != pageConf.getPage() && null != pageConf.getSize())
            pageable = new PageRequest(pageConf.getPage(), pageConf.getSize(), sort);

        String search = "";
        String userId = "";
        if (query != null && query.size() > 0 && query.get("userId") != null) {
            userId = query.get("userId");
        }

        if (query != null && query.size() > 0 && query.get("keyWord") != null) {
            search = query.get("keyWord");
        }

        if (null == search || 0 == search.trim().length())
            search = "%%";
        else
            search = "%" + search + "%";

        Page<BoaInAppInfo> pageData = this.htBoaInUserAppRepository.listUserAppByPageWeb(pageable, search, userId);

        if (pageData != null) {
            result.count(pageData.getTotalElements()).data(pageData.getContent());
        }
        result.returnCode(ReturnCodeEnum.SUCCESS.getReturnCode()).codeDesc(ReturnCodeEnum.SUCCESS.getCodeDesc());
        return result;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PageResult getUserInfoForAppByPage(PageConf pageConf, Map<String, String> query) {
        PageResult result = new PageResult();
        Sort sort = null;
        Pageable pageable = null;
        List<Order> orders = new ArrayList<Order>();
        if (null != pageConf.getSortNames()) {
            for (int i = 0; i < pageConf.getSortNames().size(); i++) {
                orders.add(new Order(pageConf.getSortOrders().get(i), pageConf.getSortNames().get(i)));
            }
            sort = new Sort(orders);
        }

        if (null != pageConf.getPage() && null != pageConf.getSize())
            pageable = new PageRequest(pageConf.getPage(), pageConf.getSize(), sort);

        String search = "";
        String appCode = "";
        if (query != null && query.size() > 0 && query.get("appCode") != null) {
        	appCode = query.get("appCode");
        }

        if (query != null && query.size() > 0 && query.get("keyWord") != null) {
            search = query.get("keyWord");
        }

        if (null == search || 0 == search.trim().length())
            search = "%%";
        else
            search = "%" + search + "%";

        Page<SelfBoaInUserInfo> pageData = this.htBoaInUserAppRepository.getUserInfoForAppByPage(pageable, search, appCode);

        if (pageData != null) {
            result.count(pageData.getTotalElements()).data(pageData.getContent());
        }
        result.returnCode(ReturnCodeEnum.SUCCESS.getReturnCode()).codeDesc(ReturnCodeEnum.SUCCESS.getCodeDesc());
        return result;
    }

    public List<SelfBoaInUserInfo> getUserInfoForApp(String appCode) {
        return this.htBoaInUserAppRepository.getUserInfoForApp( appCode);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PageResult listAllUserAppByPage(PageConf pageConf, Map<String, String> query) {
        PageResult result = new PageResult();
        Sort sort = null;
        Pageable pageable = null;
        List<Order> orders = new ArrayList<Order>();
        if (null != pageConf.getSortNames()) {
            for (int i = 0; i < pageConf.getSortNames().size(); i++) {
                orders.add(new Order(pageConf.getSortOrders().get(i), pageConf.getSortNames().get(i)));
            }
            sort = new Sort(orders);
        }

        if (null != pageConf.getPage() && null != pageConf.getSize())
            pageable = new PageRequest(pageConf.getPage(), pageConf.getSize(), sort);

        String search = "";
        String userId = "";
        if (query != null && query.size() > 0 && query.get("userId") != null) {
            userId = query.get("userId");
        }

        if (query != null && query.size() > 0 && query.get("keyWord") != null) {
            search = query.get("keyWord");
        }

        if (null == search || 0 == search.trim().length())
            search = "%%";
        else
            search = "%" + search + "%";

        Page<BoaInAppInfo> pageData = this.htBoaInUserAppRepository.listAllUserAppByPage(pageable, search);

        if (pageData != null) {
            result.count(pageData.getTotalElements()).data(pageData.getContent());
        }
        result.returnCode(ReturnCodeEnum.SUCCESS.getReturnCode()).codeDesc(ReturnCodeEnum.SUCCESS.getCodeDesc());
        return result;
    }

    public HtBoaInUserApp findById(Long id) {
        return this.htBoaInUserAppRepository.findById(id);
    }

    public HtBoaInUserApp add(HtBoaInUserApp u) {
        return this.htBoaInUserAppRepository.saveAndFlush(u);
    }

    public HtBoaInUserApp update(HtBoaInUserApp u) {
        return this.htBoaInUserAppRepository.save(u);
    }

    public void delete(HtBoaInUserApp u) {
        this.htBoaInUserAppRepository.delete(u);
    }
    
    public List<HtBoaInUserApp> getUserAppList(String app, String userId) {
    	List<HtBoaInUserApp> listHtBoaInUserApp = htBoaInUserAppRepository.getUserAppList(app,userId);
        return listHtBoaInUserApp;
    }

	public List<HtBoaInUserApp> getUserAppListByUserId(String userId) {
		return htBoaInUserAppRepository.findByUserIdAndDelFlag(userId,0);
	}
	
	public List<HtBoaInUserApp> findByUserIdAndApp(String userId,String app) {
		return htBoaInUserAppRepository.findByUserIdAndApp(userId, app);
	}
	
	/**
	 * 
	 * @param app  为空则都要推送
	 * @param opType  addAppUser/delAppUser:用户关联系统      addUserRole/delUserRole用户角色   updateUserOrg
	 * @param o
	 */
	public void pushMq(String app,String opType,String o) {
		if(StringUtils.isNotEmpty(app)) {
			List<HtBoaInApp> listHtBoaInApp =  htBoaInAppRepository.findByApp(app);
			if(listHtBoaInApp!=null&&!listHtBoaInApp.isEmpty()) {
				
				HtBoaInApp htBoaInApp = listHtBoaInApp.get(0);
				Log.info("--------o-------"+o);
				Log.info("--------htBoaInApp-------"+htBoaInApp);
				Log.info("--------htBoaInApp app-------"+htBoaInApp.getApp());
				Log.info("--------app-------"+app);
				Log.info("--------htBoaInApp status-------"+htBoaInApp.getStatus());
				Log.info("--------htBoaInApp ispush-------"+htBoaInApp.getIsPush());
				if(htBoaInApp!=null) {
					if("0".equals(htBoaInApp.getStatus())&&"1".equals(htBoaInApp.getIsPush())) { //推送添加的人员
						Map<String,String> reMap = new HashMap<String,String>();
						reMap.put(opType, o);
						Log.info("--------o-------"+opType);
						rabbitMessagingTemplate.convertAndSend(AmqpConfig.UC_EXCHANGE, AmqpConfig.UC_ROUTING_NODELAY,reMap);
					}
				}
			}
		}else {
			Map<String,String> reMap = new HashMap<String,String>();
			reMap.put(opType, o);
			rabbitMessagingTemplate.convertAndSend(AmqpConfig.UC_EXCHANGE, AmqpConfig.UC_ROUTING_NODELAY,reMap);
		}
	}
}
