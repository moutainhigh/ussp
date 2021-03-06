package com.ht.ussp.gateway.app.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.sleuth.Tracer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ht.ussp.common.SysStatus;
import com.ht.ussp.core.Result;
import com.ht.ussp.gateway.app.feignClients.OucClient;
import com.ht.ussp.gateway.app.feignClients.RoleClient;
import com.ht.ussp.gateway.app.feignClients.UaaClient;
import com.ht.ussp.gateway.app.model.ValidateOutJwtVo;
import com.ht.ussp.util.FastJsonUtil;
import com.ht.ussp.util.PatternUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @ClassName: OutSystemFilter
 * @Description: 外部用户鉴权过滤器
 * @author wim qiuwenwu@hongte.info
 * @date 2018年5月17日 下午2:15:57
 */
@Log4j2
public class OutSystemFilter extends ZuulFilter {

	@Value("${ht.outUrl.permit}")
	private String outUrlPermit;
	
	@Value("${ht.outUrl.deny}")
	private String outUrldeny;
	
	@Value("${ht.ignoreUrl.web}")
	private String htIgnoreUrlWeb;
	
	@Autowired
	private RoleClient roleClient;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	protected ZuulProperties zuulProperties;
	
	@Autowired
	private UaaClient UaaClient;

	@Autowired
	private OucClient oucClient;
	
    @Autowired
    private Tracer tracer;
	
	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.getResponse().setCharacterEncoding("UTF-8");
		HttpServletRequest request = ctx.getRequest();
		String app = request.getHeader("app");
		String ieme=request.getHeader("ieme");
		String uri = request.getRequestURI().toString();
		log.info("----------------url:"+request.getRequestURL());
		tracer.addTag("app", app);
	
		// 鉴权app不能为空
		if (StringUtils.isEmpty(app)) {
			// 不鉴权的URL直接路由
			if (isIgnoreUrl(uri, htIgnoreUrlWeb)) {
				ctx.set("isNotOutSystem", true);
				return null;
			}
			ctx.setSendZuulResponse(false);
			try {
				mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.APP_CANNOT_NULL));
			} catch (IOException e) {
				log.error("write response result app cannot null:"+e.getMessage());
			}finally {
				try {
					ctx.getResponse().getWriter().close();
				} catch (IOException e) {
					log.error("close io exception:"+e.getMessage());
				}
			}
			return null;
		}else {
			//app不为空，判断是否为外部系统用户
			Boolean flag = roleClient.isOS(app);
			if(flag==true) {
				
				//如果是外部系统，下一过滤器不执行
				ctx.set("isNotOutSystem", false);
				//不需要鉴权的请求
				if (isIgnoreUrl(uri, outUrlPermit)) {
					return null;
				}
				 //需要鉴权的请求
				String tokenPayload = request.getHeader("Authorization");

				// TOKEN和IEME都不能为空
				if (StringUtils.isEmpty(tokenPayload)||StringUtils.isEmpty(ieme)) {
					ctx.setSendZuulResponse(false);
					try {
						mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.TOKEN_AND_IEME_NOT_NULL));
					} catch (Exception e) {
						log.error("write response result token and ieme not null:"+e.getMessage());
					}finally {
						try {
							ctx.getResponse().getWriter().close();
						} catch (IOException e) {
							log.error("close io exception:"+e.getMessage());
						}
					}
					return null;
				}
				
				//验证外部系统ACCESSTOKEN 
				log.debug("====validate out system token:"+tokenPayload);
				Result result=UaaClient.validateOutUserJwt(tokenPayload);
			
				if ("0000".equals(result.getReturnCode())) {
					String str = FastJsonUtil.objectToJson(result.getData());
					ValidateOutJwtVo voj = FastJsonUtil.jsonToPojo(str, ValidateOutJwtVo.class);
					String iemeTemp=voj.getIeme();
					String userId=voj.getUserId();
					//来源非法
					if(!iemeTemp.equals(ieme)) {
						ctx.setSendZuulResponse(false);
						try {
							mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.ORIGIN_VALID));
						} catch (Exception e) {
							log.error("write response result origin valid:"+e.getMessage());
						}finally {
							try {
								ctx.getResponse().getWriter().close();
							} catch (IOException e) {
								log.error("close io exception:"+e.getMessage());
							}
						}
						return null;
					}
					
					final  String HEADER_PREFIX = "Bearer ";
					String token=tokenPayload.substring(HEADER_PREFIX.length(), tokenPayload.length());
					log.info("---------extract tokenPayload's token:"+token);
					Result validateResult=oucClient.validateToken(app, userId, token);
					String returnCode=validateResult.getReturnCode();
					
					if(!"0000".equals(returnCode)){
						switch(returnCode) {
						case "9936":
							try {
								mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.REDIS_TOKEN_NULL));
							} catch (Exception e) {
								log.debug("缓存中没有该TOKEN:"+e.getMessage());
							}finally {
								try {
									ctx.getResponse().getWriter().close();
								} catch (IOException e) {
									log.debug("close io exception:"+e.getMessage());
								}
							}
							break;	
						case "9937":
							try {
								mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.REDIS_TOKEN_VALID));
							} catch (Exception e) {
								log.debug("TOKEN与缓存中的不匹配:"+e.getMessage());
							}finally {
								try {
									ctx.getResponse().getWriter().close();
								} catch (IOException e) {
									log.debug("close io exception:"+e.getMessage());
								}
							} 
							break;
						default:
							try {
								mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.REDIS_TOKEN_FAIL));
							} catch (Exception e) {
								log.debug("执行失败:"+e.getMessage());
							}finally {
								try {
									ctx.getResponse().getWriter().close();
								} catch (IOException e) {
									log.debug("close io exception:"+e.getMessage());
								}
							} 
							break;
							
						}
						ctx.setSendZuulResponse(false);
						return null;
					}
					
					log.debug("====validate out system success,the userId is:"+userId);
					ctx.addZuulRequestHeader("userId", userId);
					ctx.addZuulRequestHeader("ieme", iemeTemp);
					 tracer.addTag("userId", userId);
					 tracer.addTag("ieme", ieme);
					ctx.setSendZuulResponse(true);
						return null;
				} else if ("9922".equals(result.getReturnCode())) {
					try {
						mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.TOKEN_IS_VALID));
					} catch (Exception e) {
						log.debug("write response result token is valid:"+e.getMessage());
					}finally {
						try {
							ctx.getResponse().getWriter().close();
						} catch (IOException e) {
							log.debug("close io exception:"+e.getMessage());
						}
					} 
					return null;
				}else if ("9921".equals(result.getReturnCode())) {
					try {
						mapper.writeValue(ctx.getResponse().getWriter(), Result.buildFail(SysStatus.TOKEN_IS_EXPIRED));
					} catch (Exception e) {
						log.error("write response result token is expired:"+e.getMessage());
					}finally {
						try {
							ctx.getResponse().getWriter().close();
						} catch (IOException e) {
							log.error("close io exception:"+e.getMessage());
						}
					} 
					return null;
				}
				}else {
				//不是外部系统执行下一过滤器
				ctx.set("isNotOutSystem", true);
			}
			
			
		}
		
		
		return null;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 0;
	}
	
	
	private boolean isIgnoreUrl(String url, String ignoreUrl) {
		if (!StringUtils.isEmpty(ignoreUrl)) {
			String[] ignoreUrlHttps = ignoreUrl.split(",");
			for (String ignoreUrlHttp : ignoreUrlHttps) {
				if (PatternUtil.compile(ignoreUrlHttp).match(url)) {
					return true;
				}
			}
		}
		return false;
	}
}
