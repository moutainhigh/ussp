package com.ht.ussp.uaa.app.security.ajax;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ht.ussp.uaa.app.feignClient.RoleClient;
import com.ht.ussp.uaa.app.jwt.JwtToken;
import com.ht.ussp.uaa.app.jwt.JwtTokenFactory;
import com.ht.ussp.uaa.app.model.ResponseModal;
import com.ht.ussp.uaa.app.vo.UserVo;
import com.ht.ussp.util.FastJsonUtil;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @ClassName: AjaxAwareAuthenticationSuccessHandler
 * @Description: 用户验证成功后的处理
 * @author wim qiuwenwu@hongte.info
 * @date 2018年1月6日 上午11:47:40
 */
@Log4j2
@Component
public class AjaxAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
	private final ObjectMapper mapper;
	private final JwtTokenFactory tokenFactory;
	@Autowired
	private RoleClient roleClient;

	@Autowired
	public AjaxAwareAuthenticationSuccessHandler(final ObjectMapper mapper, final JwtTokenFactory tokenFactory) {
		this.mapper = mapper;
		this.tokenFactory = tokenFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, String> tokenMap = new HashMap<String, String>();
		map=FastJsonUtil.objectToPojo(authentication.getPrincipal(),HashMap.class);
		String code=map.get("code").toString();
		UserVo userVo=FastJsonUtil.objectToPojo(map.get("userVo"),UserVo.class);
		JwtToken accessToken = tokenFactory.createAccessJwtToken(userVo);
		JwtToken refreshToken = tokenFactory.createRefreshToken(userVo);
		
		tokenMap.put("code", code);
		tokenMap.put("token", accessToken.getToken());
		tokenMap.put("refreshToken", refreshToken.getToken());

		// 查找并保存资源
		ResponseModal saveResources = roleClient.saveResources(userVo);
		 if(!"0000".equals(saveResources.getStatus_code())) {
          	throw new AuthenticationCredentialsNotFoundException(saveResources.getStatus_code());
          }
		
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		try {
			mapper.writeValue(response.getWriter(), tokenMap);
		} catch (IOException e) {
			log.debug("write response result tokenMap:"+e.getMessage());
		}finally {
			try {
				response.getWriter().close();
			} catch (IOException e) {
				log.debug("close io exception:"+e.getMessage());
			}
		}

		clearAuthenticationAttributes(request);
	}

	/**
	 * 
	 * @Title: clearAuthenticationAttributes 
	 * @Description: 清除有可能存储的authentication 
	 * @return void 
	 * @throws
	 */
	protected final void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (session == null) {
			return;
		}

		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
}
