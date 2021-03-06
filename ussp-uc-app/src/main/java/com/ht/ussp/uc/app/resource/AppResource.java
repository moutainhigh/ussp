package com.ht.ussp.uc.app.resource;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ht.ussp.common.Constants;
import com.ht.ussp.common.SysStatus;
import com.ht.ussp.core.PageResult;
import com.ht.ussp.core.Result;
import com.ht.ussp.core.ReturnCodeEnum;
import com.ht.ussp.uc.app.domain.HtBoaInApp;
import com.ht.ussp.uc.app.domain.HtBoaInRole;
import com.ht.ussp.uc.app.dto.RoleDTO;
import com.ht.ussp.uc.app.model.BoaInAppInfo;
import com.ht.ussp.uc.app.model.PageConf;
import com.ht.ussp.uc.app.model.ResponseModal;
import com.ht.ussp.uc.app.service.HtBoaInAppService;
import com.ht.ussp.uc.app.service.HtBoaInRoleService;
import com.ht.ussp.uc.app.vo.PageVo;
import com.ht.ussp.util.LogicUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

/**
 * @author wim qiuwenwu@hongte.info
 * @ClassName: HtBoaInAppResource
 * @Description: 系统信息
 * @date 2018年1月5日 下午6:36:47
 */

@RestController
@RequestMapping(value = "/apps")
public class AppResource {

	private static final Logger log = LoggerFactory.getLogger(EchoResouce.class);

	@Autowired
	private HtBoaInAppService htBoaInAppService;

	@Autowired
	private HtBoaInRoleService htBoaInRoleService;

	@ApiOperation(value = "通过app编码获取角色编码和角色名称")
	@RequestMapping(value = { "/getRoleInfoList" }, method = RequestMethod.POST)
	public Result<List<RoleDTO>> getRoleInfoList(@RequestParam("app") String app) {
		List<RoleDTO> temp=new ArrayList<RoleDTO>();
		if(null==app&&app.length()==0) {
			return Result.buildFail(SysStatus.ERROR_PARAM);
			
		}
		try {
			List<HtBoaInRole> list=htBoaInRoleService.getAllByApp(app);
		if(null!=list&&!list.isEmpty()) {
			for(int i=0;i<list.size();i++) {
				RoleDTO rd=new RoleDTO();
			    rd.setRoleCode(list.get(i).getRoleCode());
				rd.setRoleName(list.get(i).getRoleName());
				temp.add(rd);
			}
			
		}else {
			return Result.buildFail(SysStatus.NO_RESULT);
		}
			
		return Result.buildSuccess(temp);
		}catch(Exception e) {
			return Result.buildFail();
		}

	}

	@ApiOperation(value = "查询所有系统信息")
	@RequestMapping(value = { "/getAllApp" }, method = RequestMethod.POST)
	public Result<List<HtBoaInApp>> getAllApp() {
		return Result.buildSuccess(htBoaInAppService.findAllApp("0"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ApiOperation(value = "对内：系统记录查询")
	@RequestMapping(value = { "/list" }, method = RequestMethod.POST)
	public PageResult<BoaInAppInfo> list(PageVo page) {
		PageResult result = new PageResult();
		PageConf pageConf = new PageConf();
		pageConf.setPage(page.getPage());
		pageConf.setSize(page.getLimit());
		pageConf.setSearch(page.getKeyWord());
		long sl = System.currentTimeMillis(), el = 0L;
		String msg = "成功";
		String logHead = "系统记录查询：role/in/list param-> {}";
		String logStart = logHead + " | START:{}";
		String logEnd = logHead + " {} | END:{}, COST:{}";
		log.debug(logStart, "pageConf: " + pageConf, sl);
		Page<BoaInAppInfo> pageData = (Page<BoaInAppInfo>) htBoaInAppService.findAllByPage(pageConf, page.getQuery());
		el = System.currentTimeMillis();
		log.debug(logEnd, "pageConf: " + pageConf, msg, el, el - sl);
		if (pageData != null) {
			result.count(pageData.getTotalElements()).data(pageData.getContent());
		}
		result.returnCode(ReturnCodeEnum.SUCCESS.getReturnCode()).codeDesc(ReturnCodeEnum.SUCCESS.getCodeDesc());
		return result;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	@ApiOperation(value = "对内：新增/编辑系统信息记录")
	@RequestMapping(value = { "/add" }, method = RequestMethod.POST)
	public Result add(@RequestBody BoaInAppInfo boaInAppInfo, @RequestHeader("userId") String userId) {
		long sl = System.currentTimeMillis(), el = 0L;
		ResponseModal r = null;
		String msg = "成功";
		String logHead = "系统记录查询：role/in/add param-> {}";
		String logStart = logHead + " | START:{}";
		String logEnd = logHead + " {} | END:{}, COST:{}";
		log.debug(logStart, "boaInAppInfo: " + boaInAppInfo, sl);
		HtBoaInApp u = null;
		if (boaInAppInfo.getId() > 0) {
			u = htBoaInAppService.findById(boaInAppInfo.getId());
			if (u == null) {
				u = new HtBoaInApp();
			}
		} else {
			u = new HtBoaInApp();
		}
		u.setApp(boaInAppInfo.getApp());
		u.setLastModifiedDatetime(new Date());
		u.setName(boaInAppInfo.getNameCn());
		u.setNameCn(boaInAppInfo.getNameCn());
		u.setMaxLoginCount(
				StringUtils.isNotEmpty(boaInAppInfo.getMaxLoginCount() + "") ? boaInAppInfo.getMaxLoginCount() : 0);
		u.setTips(boaInAppInfo.getTips());
		u.setSysToken(boaInAppInfo.getSysToken());
		u.setIsPush(boaInAppInfo.getIsPush());
		if (boaInAppInfo.getId() > 0) {
			u.setId(boaInAppInfo.getId());
			u.setUpdateOperator(userId);
			u = htBoaInAppService.update(u);
		} else {
			u.setCreatedDatetime(new Date());
			u.setIsOS(boaInAppInfo.getIsOS());
			u.setStatus("0");
			u.setCreateOperator(userId);
			u = htBoaInAppService.add(u);
		}
		el = System.currentTimeMillis();
		log.debug(logEnd, "boaInAppInfo: " + boaInAppInfo, msg, el, el - sl);
		return Result.buildSuccess();
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	@ApiOperation(value = "对内：禁用/启用系统", notes = "禁用/启用系统")
	@RequestMapping(value = { "/stop" }, method = RequestMethod.POST)
	public Result stop(Long id, String status, @RequestHeader("userId") String userId) {
		long sl = System.currentTimeMillis(), el = 0L;
		String msg = "成功";
		String logHead = "系统记录查询：role/in/add param-> {}";
		String logStart = logHead + " | START:{}";
		String logEnd = logHead + " {} | END:{}, COST:{}";
		// log.debug(logStart, "boaInAppInfo: " + boaInAppInfo, sl);
		HtBoaInApp u = null;
		if (id > 0) {
			u = htBoaInAppService.findById(id);
		} else {
			return Result.buildFail();
		}
		if (u == null) {
			return Result.buildFail();
		}
		u.setLastModifiedDatetime(new Date());
		u.setStatus(status);
		u.setUpdateOperator(userId);
		u = htBoaInAppService.update(u);

		el = System.currentTimeMillis();
		log.debug(logEnd, "boaInAppInfo: " + u, msg, el, el - sl);
		return Result.buildSuccess();
		// return new ResponseModal(200, msg, u);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "对内：物理删除系统记录", notes = "提交系统编号，可批量删除")
	@ApiImplicitParam(name = "codes", value = "系统编号集", required = true, dataType = "Codes")
	@RequestMapping(value = { "/deleteTrunc" }, method = RequestMethod.POST)
	public Result deleteTrunc(int id) {
		long sl = System.currentTimeMillis(), el = 0L;
		String msg = "成功";
		String logHead = "系统记录删除：role/in/delete param-> {}";
		String logStart = logHead + " | START:{}";
		String logEnd = logHead + " {} | END:{}, COST:{}";
		log.debug(logStart, "codes: " + id, sl);
		htBoaInAppService.delete(id);
		el = System.currentTimeMillis();
		log.debug(logEnd, "codes: " + id, msg, el, el - sl);
		return Result.buildSuccess();
		// return new ResponseModal(200, msg);
	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "对内：删除标记岗位记录", notes = "提交岗位编号，可批量删除")
	@RequestMapping(value = { "/delete" }, method = RequestMethod.POST)
	public Result delete(long id, @RequestHeader("userId") String userId) {
		long sl = System.currentTimeMillis(), el = 0L;
		String msg = "成功";
		String logHead = "岗位记录删除：position/in/delete param-> {}";
		String logStart = logHead + " | START:{}";
		String logEnd = logHead + " {} | END:{}, COST:{}";
		log.debug(logStart, "codes: " + id, sl);
		HtBoaInApp u = htBoaInAppService.findById(id);
		u.setDelFlag(Constants.DEL_1);
		u.setStatus(Constants.STATUS_1);
		u.setUpdateOperator(userId);
		u.setLastModifiedDatetime(new Date());
		htBoaInAppService.update(u);
		el = System.currentTimeMillis();
		log.debug(logEnd, "codes: " + id, msg, el, el - sl);
		return Result.buildSuccess();

	}

	@SuppressWarnings("rawtypes")
	@ApiOperation(value = "对内：机构编码是否可用  true：可用  false：不可用")
	@PostMapping(value = { "/isExistAppCode" }, produces = { "application/json" })
	public Result isExistAppCode(String appCode) {
		List<HtBoaInApp> listHtBoaInApp = htBoaInAppService.findByAppCode(appCode);
		if (listHtBoaInApp.isEmpty()) {
			return Result.buildSuccess();
		} else {
			return Result.buildFail();
		}
	}

	/**
	 * 导出
	 */
	@PostMapping(value = "/exportAppExcel")
	@ResponseBody
	public void exportAppExcel(HttpServletResponse response) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssms");
		String dateStr = sdf.format(new Date());
		// 指定下载的文件名
		response.setHeader("Content-Disposition", "attachment;filename=" + dateStr + ".xlsx");
		System.out.println(dateStr);
		response.setContentType("application/vnd.ms-excel;charset=UTF-8");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		XSSFWorkbook workbook = null;
		try {
			// 导出Excel对象
			workbook = htBoaInAppService.exportAppExcel();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		OutputStream output;
		try {
			output = response.getOutputStream();
			BufferedOutputStream bufferedOutPut = new BufferedOutputStream(output);
			bufferedOutPut.flush();
			workbook.write(bufferedOutPut);
			bufferedOutPut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 导入
	 * @param request
	 * @param response
	 */
	@PostMapping(value = "/importAppExcel")
	public Result importAppExcel(HttpServletRequest request, HttpServletResponse response,
			@RequestHeader("userId") String userId) {
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			List<MultipartFile> fileList = multipartRequest.getFiles("file");
			if (fileList.isEmpty()) {
				throw new Exception("文件不存在！");
			}
			MultipartFile file = fileList.get(0);
			if (file == null || file.isEmpty()) {
				throw new Exception("文件不存在！");
			}
			InputStream in = file.getInputStream();
			htBoaInAppService.importAppExcel(in, file, userId);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.buildFail();
		}
		return Result.buildSuccess();
	}

	/**
	 * 
	 * @Title: isOS 
	 * @Description: 判断是否为外部系统 
	 * @return Boolean
	 * @throws
	 * @author wim qiuwenwu@hongte.info 
	 * @date 2018年5月15日 上午10:56:29
	 */

	@GetMapping(value = "/isOS")
	@ApiOperation(value = "判断是否为外部系统")
	public Boolean isOS(@RequestParam("app") String app) {
		List<HtBoaInApp> HtBoaInApp = htBoaInAppService.findByAppCode(app);

		if (LogicUtil.isNullOrEmpty(HtBoaInApp)) {
			return false;
		} else if (HtBoaInApp.size() > 1) {
			log.info("系统编码不唯一");
			return false;

		} else if (HtBoaInApp.get(0).getIsOS() == null) {
			return false;
		} else if (HtBoaInApp.get(0).getIsOS() == 0) {
			return true;
		} else {
			return false;
		}

	}

}
