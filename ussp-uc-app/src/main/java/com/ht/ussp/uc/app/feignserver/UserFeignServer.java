package com.ht.ussp.uc.app.feignserver;

import java.net.URLDecoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.ht.ussp.common.Constants;
import com.ht.ussp.core.Result;
import com.ht.ussp.uc.app.domain.HtBoaInContrast;
import com.ht.ussp.uc.app.domain.HtBoaInLogin;
import com.ht.ussp.uc.app.domain.HtBoaInUser;
import com.ht.ussp.uc.app.feignclients.UaaClient;
import com.ht.ussp.uc.app.model.ResponseModal;
import com.ht.ussp.uc.app.service.HtBoaInContrastService;
import com.ht.ussp.uc.app.service.HtBoaInLoginService;
import com.ht.ussp.uc.app.service.HtBoaInUserService;
import com.ht.ussp.uc.app.vo.BmUserVo;
import com.ht.ussp.uc.app.vo.LoginInfoVo;
import com.ht.ussp.uc.app.vo.UserMessageVo;
import com.ht.ussp.util.EncryptUtil;
import com.ht.ussp.util.md5.DESC;
import com.ht.ussp.util.md5.DecodeResult;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

 
@RestController
@RequestMapping(value = "/userFeignServer")
@Log4j2
public class UserFeignServer{

    @Autowired
    private HtBoaInUserService htBoaInUserService;
    @Autowired
    private HtBoaInLoginService htBoaInLoginService;
    @Autowired
   	private HtBoaInContrastService htBoaInContrastService;
    @Autowired
    private UaaClient uaaClient;
    
    /**
     * 保存信贷用户信息<br>
     */
    @ApiOperation(value = "获取网关token")
    @PostMapping(value = "/getUCToken")
    public Result getUCToken(@RequestParam("userInfo") String userInfo) {
    	try {
    		DecodeResult dResult = JSON.parseObject(userInfo,DecodeResult.class);
        	//解密参数
        	if(dResult==null) {
        		return Result.buildFail("9999", "解密参数出错");
        	}
        	if(StringUtils.isEmpty(dResult.getStrJson())) {
        		return Result.buildFail("9999", "请求参数JSON为空");
        	}
        	if(StringUtils.isEmpty(dResult.getKey())) {
        		return Result.buildFail("9999", "验证签名key为空");
        	}
        	
        	String reqBody=DESC.Decode(URLDecoder.decode(dResult.getStrJson(),"UTF-8"));
        	if(StringUtils.isEmpty(reqBody)) {
        		return Result.buildFail("9999", "请求参数为空");
        	}
        	//验证签名
        	String key = DESC.getMD5(String.format("Json:%s-Key:%s", dResult.getStrJson(), DESC.disturbKey));
        	if(!dResult.getKey().equals(key.toUpperCase())) {
        		return Result.buildFail("9999", "验证签名不匹配");
        	}
        	//解密请求参数
        	BmUserVo bmUserVo = JSON.parseObject(reqBody,BmUserVo.class);
        	if(bmUserVo==null) {
        		return Result.buildFail("9999", "解析请求参数json出错");
        	}
        	if(StringUtils.isEmpty(bmUserVo.getBmUserId())) {
        		return Result.buildFail("9999", "bmUserId不能为空");
        	}
        	if(StringUtils.isEmpty(bmUserVo.getEmail())) {
        		return Result.buildFail("9999", "email不能为空");
        	}
        	if(StringUtils.isEmpty(bmUserVo.getApp())) {
        		return Result.buildFail("9999", "app不能为空");
        	}
        	if(StringUtils.isEmpty(bmUserVo.getMobile())) {
        		return Result.buildFail("9999", "手机号不能为空");
        	}
        	HtBoaInContrast htBoaInContrast = htBoaInUserService.saveBmUserInfo(bmUserVo);
			if (htBoaInContrast!=null) {
				ResponseModal result = uaaClient.createUCToken(htBoaInContrast.getUcBusinessId(), htBoaInContrast.getBmBusinessId(), 29, 25);
				return Result.buildSuccess(result);
			}else {
				return Result.buildFail("9999","未找到用户信息");
			}
		} catch (Exception e) {
			return Result.buildFail(e.getMessage(),e.getMessage());
		}
    }
    
    @ApiOperation(value = "修改用户状态 用户状态 0 正常  1禁用 2离职  4冻结 5锁定")
    @PostMapping("/changUserState")
    public Result changUserState(@RequestParam("bmUserId")String bmUserId,@RequestParam("status")String status) {
    	if(StringUtils.isEmpty(bmUserId)) {
    		return Result.buildFail();
    	} 
    	try {
    		List<HtBoaInContrast> listHtBoaInContrast = htBoaInContrastService.getHtBoaInContrastListByBmUserId(bmUserId, "20");
        	HtBoaInContrast htBoaInContrast  = null;
        	//查看是否存在用户，如果存在用户则不用新增用户
        	 if(listHtBoaInContrast!=null&&!listHtBoaInContrast.isEmpty()) {
        		 htBoaInContrast = listHtBoaInContrast.get(0);
        	 }
        	 if(htBoaInContrast==null) {
        		 return Result.buildFail("9999","未找到相关用户信息");
        	 }
        	HtBoaInUser htBoaInUser =  htBoaInUserService.findByUserId(htBoaInContrast.getUcBusinessId());
        	if(htBoaInUser!=null) {
        		//htBoaInUser.setStatus(status);
        		htBoaInUserService.update(htBoaInUser);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return Result.buildSuccess();
    }
    
    @ApiOperation(value = "获取指定userId用户信息,userId为空并且bmUserId不为空，则根据信贷userId查询UC用户信息（userId：Uc用户id,bmUserId:信贷用户id,app:系统编码）")
    @PostMapping(value = "/getUserInfoByUserId")
    public LoginInfoVo getUserInfoByUserId(@RequestParam("userId")String userId, @RequestParam("bmUserId")String bmUserId, @RequestParam("app") String app) {
    	if(StringUtils.isEmpty(userId)) {
    		List<HtBoaInContrast> listHtBoaInContrast= htBoaInContrastService.getHtBoaInContrastListByBmUserId(bmUserId,"20");
    		if(listHtBoaInContrast==null||listHtBoaInContrast.isEmpty()) {
    			return null;
    		}else {
    			userId=listHtBoaInContrast.get(0).getUcBusinessId();
    		}
    	}
        return htBoaInUserService.queryUserInfo(userId,app);
    }
    
    private Result checkIsExist(BmUserVo bmUserVo,String userId) {
    	HtBoaInUser htBoaInUser = null;
    	if(bmUserVo.getJobNumber()!=null&&!"".equals(bmUserVo.getJobNumber())) {
    		htBoaInUser = htBoaInUserService.findByJobNumber(bmUserVo.getJobNumber());
    		if(htBoaInUser!=null&&!htBoaInUser.getUserId().equals(userId)) {
        		return Result.buildFail("工号已经存在或不可用","工号已经存在或不可用");
             } 
    	}
        if(bmUserVo.getMobile()!=null) {
    		htBoaInUser = htBoaInUserService.findByMobile(bmUserVo.getMobile());
    		if(htBoaInUser!=null&&!htBoaInUser.getUserId().equals(userId)) {
        		return Result.buildFail("手机号已经存在或不可用","手机号已经存在或不可用");
             } 
    	}
       if(bmUserVo.getEmail()!=null) {
    		htBoaInUser = htBoaInUserService.findByEmail(bmUserVo.getEmail());
    		if(htBoaInUser!=null&&!htBoaInUser.getUserId().equals(userId)) {
        		return Result.buildFail("邮箱已经存在或不可用","邮箱已经存在或不可用");
             } 
    	}
		if (bmUserVo.getBmUserId() != null) {
			HtBoaInLogin htBoaInLogin = htBoaInLoginService.findByLoginId(bmUserVo.getBmUserId());
			if (htBoaInLogin != null && !htBoaInLogin.getUserId().equals(userId)) {
				return Result.buildFail("用户名已经存在或不可用", "用户名已经存在或不可用");
			} else if (htBoaInLogin != null && htBoaInLogin.getUserId().equals(userId)) {
				 
			} else {
				HtBoaInLogin htBoaInLogins = htBoaInLoginService.findByUserId(userId);
				htBoaInLogins.setLoginId(bmUserVo.getBmUserId());
				htBoaInLoginService.update(htBoaInLogins);
			}

		}
		return Result.buildSuccess();
    }
    
    /**
     * 新增信贷用户信息<br>
     *
     * @param user 用户信息数据对象
     * @return com.ht.ussp.core.Result
     * @author 谭荣巧
     * @Date 2018/1/14 12:08
     */
    public Result addAsync(@RequestBody UserMessageVo userMessageVo, @RequestHeader("userId") String loginUserId, @RequestHeader("app") String app) {
        if (userMessageVo != null) {
            HtBoaInUser user = new HtBoaInUser();
            user.setDataSource(Constants.USER_DATASOURCE_3);
            user.setEmail(userMessageVo.getEmail());
            user.setIsOrgUser(1);
            user.setJobNumber(userMessageVo.getJobNumber());
            user.setMobile(userMessageVo.getMobile());
            user.setOrgCode(userMessageVo.getOrgCode());
            user.setUserName(userMessageVo.getUserName());
            user.setIdNo(userMessageVo.getIdNo());
            user.setRootOrgCode(userMessageVo.getRootOrgCode());
            user.setOrgPath(userMessageVo.getOrgPath());
            user.setUserType("10");
            user.setCreateOperator(loginUserId);
            user.setUpdateOperator(loginUserId);
            user.setDelFlag(0);
            
            HtBoaInLogin loginInfo = new HtBoaInLogin();
           // loginInfo.setLoginId(UUID.randomUUID().toString().replace("-", ""));
            //loginInfo.setUserId(userId);
            loginInfo.setLoginId(userMessageVo.getLoginId()); //作为用户的登录账号，修改为不是自动生成
            loginInfo.setCreateOperator(loginUserId);
            loginInfo.setUpdateOperator(loginUserId);
            loginInfo.setStatus(Constants.USER_STATUS_0);
            loginInfo.setPassword(EncryptUtil.passwordEncrypt("123456"));
            loginInfo.setFailedCount(0);
            loginInfo.setRootOrgCode(userMessageVo.getOrgCode());
            loginInfo.setDelFlag(0);
            boolean isAdd = htBoaInUserService.saveUserInfoAndLoginInfo(user, loginInfo);
            if (isAdd) {
                return Result.buildSuccess();
            }
        }
        return Result.buildFail();
    }
    
    @ApiOperation(value = "更新用户信息")
    public Result updateAsync(@RequestBody UserMessageVo userMessageVo, @RequestHeader("userId") String loginUserId) {
        if (userMessageVo != null) {
        	HtBoaInUser htBoaInUser = null;
        	if(userMessageVo.getJobNumber()!=null&&!"".equals(userMessageVo.getJobNumber())) {
        		htBoaInUser = htBoaInUserService.findByJobNumber(userMessageVo.getJobNumber());
        		if(htBoaInUser!=null&&!htBoaInUser.getUserId().equals(userMessageVo.getUserId())) {
            		return Result.buildFail("工号已经存在或不可用","工号已经存在或不可用");
                 } 
        	}
            if(userMessageVo.getMobile()!=null) {
        		htBoaInUser = htBoaInUserService.findByMobile(userMessageVo.getMobile());
        		if(htBoaInUser!=null&&!htBoaInUser.getUserId().equals(userMessageVo.getUserId())) {
            		return Result.buildFail("手机号已经存在或不可用","手机号已经存在或不可用");
                 } 
        	}
           if(userMessageVo.getEmail()!=null) {
        		htBoaInUser = htBoaInUserService.findByEmail(userMessageVo.getEmail());
        		if(htBoaInUser!=null&&!htBoaInUser.getUserId().equals(userMessageVo.getUserId())) {
            		return Result.buildFail("邮箱已经存在或不可用","邮箱已经存在或不可用");
                 } 
        	}
			if (userMessageVo.getLoginId() != null) {
				HtBoaInLogin htBoaInLogin = htBoaInLoginService.findByLoginId(userMessageVo.getLoginId());
				if (htBoaInLogin != null && !htBoaInLogin.getUserId().equals(userMessageVo.getUserId())) {
					return Result.buildFail("用户名已经存在或不可用", "用户名已经存在或不可用");
				} else if (htBoaInLogin != null && htBoaInLogin.getUserId().equals(userMessageVo.getUserId())) {
					 
				} else {
					HtBoaInLogin htBoaInLogins = htBoaInLoginService.findByUserId(userMessageVo.getUserId());
					htBoaInLogins.setLoginId(userMessageVo.getLoginId());
					htBoaInLoginService.update(htBoaInLogins);
				}

			}
        	
        	HtBoaInUser user = new HtBoaInUser();
        	user.setUserId(userMessageVo.getUserId());
            user.setEmail(userMessageVo.getEmail());
            user.setJobNumber(userMessageVo.getJobNumber());
            user.setMobile(userMessageVo.getMobile());
            user.setOrgCode(userMessageVo.getOrgCode());
            user.setUserName(userMessageVo.getUserName());
            user.setIdNo(userMessageVo.getIdNo());
            user.setRootOrgCode(userMessageVo.getRootOrgCode());
            user.setOrgPath(userMessageVo.getOrgPath());
            user.setUpdateOperator(loginUserId);
            boolean isUpdate = htBoaInUserService.updateUserByUserId(user);
            if (isUpdate) {
                return Result.buildSuccess();
            }
        }
        return Result.buildFail();
    }
 
    @ApiOperation(value = "校验数据的重复性  返回true：可用  false：不可用")
    public Result checkUserExist(String jobnum,String mobile,String email,String loginid) {
    	HtBoaInUser htBoaInUser = null;
    	if(jobnum!=null) {
    		htBoaInUser = htBoaInUserService.findByJobNumber(jobnum);
    	}else if(mobile!=null) {
    		htBoaInUser = htBoaInUserService.findByMobile(mobile);
    	}else if(email!=null) {
    		htBoaInUser = htBoaInUserService.findByEmail(email);
    	}else if(loginid!=null) {
    		HtBoaInLogin htBoaInLogin = htBoaInLoginService.findByLoginId(loginid);
    		if(htBoaInLogin==null) {
    	       return Result.buildSuccess();
            }else {
         	   return Result.buildFail();
            }
    	}
		if(htBoaInUser==null) {
	       return Result.buildSuccess();
        }else {
     	   return Result.buildFail();
        }
    }
 
}