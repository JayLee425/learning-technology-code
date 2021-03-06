package top.imyzt.learning.security.browser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import top.imyzt.learning.security.browser.support.SimpleResponse;
import top.imyzt.learning.security.browser.support.SocialUserInfo;
import top.imyzt.learning.security.core.properties.SecurityProperties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author imyzt
 * @date 2019/6/6
 * @description 浏览器应用客户端请求控制器
 */
@RestController
@Slf4j
public class BrowserSecurityController {

    /**
     * 页面缓存
     */
    private RequestCache requestCache = new HttpSessionRequestCache();

    /**
     * Spring 重定向工具
     */
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    /**
     * HTML后缀
     */
    private static final String HTML_SUFFIX = ".html";

    private final SecurityProperties securityProperties;

    public BrowserSecurityController(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Autowired
    private ProviderSignInUtils providerSignInUtils;

    /**
     * 当需要身份认证时, 跳转到这里
     */
    @RequestMapping(value = "authentication/require")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public SimpleResponse requireAuthentication(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        SavedRequest savedRequest = requestCache.getRequest(req, resp);

        if (null != savedRequest) {

            // 引发跳转的请求(认证之后重定向地址)
            String targetUrl = savedRequest.getRedirectUrl();
            log.info("引发跳转的请求是: {}", targetUrl);

            // 如果是HTML页面请求
            if (StringUtils.endsWithIgnoreCase(targetUrl, HTML_SUFFIX)) {
                // 重定向到登录页面
                redirectStrategy.sendRedirect(req, resp, securityProperties.getBrowser().getLoginPage());
            }
        }

        // 如果是AJAX异步请求
        return SimpleResponse.of("访问的服务需要身份认证, 请引导用户到登录页");
    }

    /**
     * 获取社交登录用户信息,封装到SocialUserInfo中提供前台注册本应用时展示
     */
    @GetMapping("/social/user")
    public SocialUserInfo getUserInfo(HttpServletRequest request) {
        // 从session中拿用户信息,在org.springframework.social.security.SocialAuthenticationFilter.doAuthentication()方法335行中放入session中
        Connection<?> connection = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
        SocialUserInfo userInfo = new SocialUserInfo();
        return userInfo.setHeadImg(connection.getImageUrl())
                .setNickname(connection.getDisplayName())
                .setProviderId(connection.getKey().getProviderId())
                .setProviderUserId(connection.getKey().getProviderUserId());
    }

}
