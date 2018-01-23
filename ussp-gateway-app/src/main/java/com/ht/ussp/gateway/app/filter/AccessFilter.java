package com.ht.ussp.gateway.app.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ht.ussp.gateway.app.config.JwtSettings;
import com.ht.ussp.gateway.app.config.WebSecurityConfig;
import com.ht.ussp.gateway.app.exception.JwtExpiredTokenException;
import com.ht.ussp.gateway.app.feignClients.RoleClient;
import com.ht.ussp.gateway.app.jwt.RawAccessJwtToken;
import com.ht.ussp.gateway.app.jwt.TokenExtractor;
import com.ht.ussp.gateway.app.model.ResponseModal;
import com.ht.ussp.gateway.app.util.SysStatus;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

/**
 * 
 * @ClassName: AccessFilter
 * @Description: 请求过滤
 * @author wim qiuwenwu@hongte.info
 * @date 2018年1月17日 下午2:01:58
 */
public class AccessFilter extends ZuulFilter {

	private static Logger log = LoggerFactory.getLogger(AccessFilter.class);

	@Autowired
	private TokenExtractor tokenExtractor;

	@Autowired
	private JwtSettings jwtSettings;

	@Autowired
	private RoleClient roleClient;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * 定义该过滤器是否要执行
	 */
	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		Jws<Claims> jwsClaims;
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();
		// 必须带Authorization
		String tokenPayload = request.getHeader(WebSecurityConfig.AUTHENTICATION_HEADER_NAME);
		String app = request.getHeader("app");
		ctx.getResponse().setCharacterEncoding("UTF-8");
		if (StringUtils.isEmpty(tokenPayload)) {
			ctx.setSendZuulResponse(false);
			try {
				mapper.writeValue(ctx.getResponse().getWriter(),new ResponseModal(SysStatus.TOKEN_IS_NULL));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		// 验证JWT
		RawAccessJwtToken AccessToken = new RawAccessJwtToken(tokenExtractor.extract(tokenPayload));
		try {
			jwsClaims = AccessToken.parseClaims(jwtSettings.getTokenSigningKey());
		} catch (BadCredentialsException ex) {
			ctx.setSendZuulResponse(false);
			try {
				mapper.writeValue(ctx.getResponse().getWriter(),new ResponseModal(SysStatus.TOKEN_IS_VALID));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;

		}catch(JwtExpiredTokenException e2) {
			
			try {
				mapper.writeValue(ctx.getResponse().getWriter(),new ResponseModal(SysStatus.TOKEN_IS_EXPIRED));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		
		// 判断请求是否有权限通过
		String uri = request.getRequestURI().toString();
		String validateUrl = uri.substring(uri.indexOf("/", uri.indexOf("/") + 1));
		log.info("------------validateUrl------" + validateUrl);
		String userId = jwsClaims.getBody().get("userId").toString();
		String orgCode = jwsClaims.getBody().get("orgCode").toString();
		StringBuffer api_key = new StringBuffer();
		api_key.append(userId).append(":").append(app).append(":").append("api");

		if (!roleClient.IsHasAuth(api_key.toString(), validateUrl)) {
			ctx.setSendZuulResponse(false);
			try {
				mapper.writeValue(ctx.getResponse().getWriter(),new ResponseModal(SysStatus.HAS_NO_ACCESS));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		ctx.addZuulRequestHeader("userId", userId);
		ctx.addZuulRequestHeader("orgCode", orgCode);

		ctx.getResponse().setHeader("Content-type", "text/html;charset=UTF-8");
		ctx.setSendZuulResponse(true);
		ctx.setResponseStatusCode(200);
		return null;

	}

	/**
	 * 过滤器类型，
	 * pre:表示在请求之前执行 
	 * routing: 表示请求时被调用
	 * post:在route和error过滤器之后被调用
	 * error:处理请求发生错误时被调用
	 */
	@Override
	public String filterType() {
		return "pre";
	}

	/**
	 * 过滤器的执行顺序
	 */
	@Override
	public int filterOrder() {
		return 0;
	}

}
