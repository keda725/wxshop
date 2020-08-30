package com.github.kb.wxshop.config;

import com.github.kb.wxshop.service.VerificationCodeCheckService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zuojiabin
 */
public class ShiroRealm extends AuthorizingRealm {
    private final VerificationCodeCheckService verificationCodeCheckService;

    @Autowired
    public ShiroRealm(VerificationCodeCheckService verificationCodeCheckService) {
        this.verificationCodeCheckService = verificationCodeCheckService;
        this.setCredentialsMatcher((token, info) -> new String((char[]) token.getCredentials()).equals(info.getCredentials()));
    }

    //权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(final PrincipalCollection principalCollection) {
        return null;
    }

    //验证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
        final String tel = (String) token.getPrincipal();
        final String correctCode = this.verificationCodeCheckService.getCorrectCode(tel);
        return new SimpleAuthenticationInfo(tel, correctCode, getName());
    }
}
