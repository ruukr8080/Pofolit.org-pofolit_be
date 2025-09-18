package com.app.pofolit_be.security.service;

import com.app.pofolit_be.security.authentication.AuthenticatedUser;
import com.app.pofolit_be.user.dto.SignDto;
import com.app.pofolit_be.user.mapper.OAuth2AttributeMapper;
import com.app.pofolit_be.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService extends OidcUserService implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final AuthService authService;
    private final OAuth2AttributeMapper oAuth2AttributeMapper;
    @Value("${uri.auth.base}")
    String target;

    @Override
    public OidcUser loadUser(OidcUserRequest oidcUserRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(oidcUserRequest);
        String providerId = oidcUser.getIdToken().getSubject();

        SignDto userDto = oAuth2AttributeMapper.getOAuth2UserAttribute(
                oidcUserRequest.getClientRegistration().getRegistrationId(),
                oidcUser.getAttributes()
        );
        userService.saveOrUpdateUser(userDto);
        log.info("로그인[{}]", oidcUser.getEmail());

        return new AuthenticatedUser(oidcUserRequest);
        //        return oidcUser;
        // Name: [101682669708335475369],
        // Granted Authorities: [
        // [OIDC_USER,
        // SCOPE_https://www.googleapis.com/auth/userinfo.email,
        // SCOPE_https://www.googleapis.com/auth/userinfo.profile,
        // SCOPE_openid]],
        // User Attributes: [{
        // at_hash=0GOA-3aoipsZsNXCXdcx1g,
        // sub=101682669708335475369,
        // email_verified=true,
        // iss=https://accounts.google.com,
        // given_name=Goo,
        // nonce=pHjmBz6vxXqtkZLs-a0-g2Llnk01DxZLwtizc0BkvnI,
        // picture=https://lh3.googleusercontent.com/a/ACg8ocIEaYkzoMmTyZhJNsvEjzZqmr5s1mCCeeIlZVcppBH-W48Eoyi-=s96-c,
        // aud=[700219984120-4ivvho2pfl26h5ql92sm67hejionj62q.apps.googleusercontent.com],
        // azp=700219984120-4ivvho2pfl26h5ql92sm67hejionj62q.apps.googleusercontent.com,
        // name=Goo, exp=2025-09-18T09:19:10Z, iat=2025-09-18T08:19:10Z, email=fourfirst1@gmail.com}]
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        AuthenticatedUser authoidcUserRequest = (AuthenticatedUser) authentication.getPrincipal();
        //Name: [104820571850105706781], Granted Authorities: [[OIDC_USER, SCOPE_https://www.googleapis.com/auth/userinfo.email, SCOPE_https://www.googleapis.com/auth/userinfo.profile, SCOPE_openid]], User Attributes: [{at_hash=SpLmSfGtm8pauSTLB7iwpg, sub=104820571850105706781, email_verified=true, iss=https://accounts.google.com, given_name=07, nonce=JVR44fVkCkkp_G7DiNHwAQbFv_zkB-Hx8BypayEH7DA, picture=https://lh3.googleusercontent.com/a/ACg8ocLdFBI3jxH98b2eAQb8eHGJWwTl1M3W4BBwAYwg-TH24ZmeJQ=s96-c, aud=[700219984120-4ivvho2pfl26h5ql92sm67hejionj62q.apps.googleusercontent.com], azp=700219984120-4ivvho2pfl26h5ql92sm67hejionj62q.apps.googleusercontent.com, name=07 31, exp=2025-09-18T08:59:33Z, family_name=31, iat=2025-09-18T07:59:33Z, email=junie250731@gmail.com}]

        // TODO: idToken만 들어올 수 있으니 pre토큰이랑 pair토큰으로 나누는게 좋아보임.
        // pre토큰은
        Map<String, ResponseCookie> cookies;
        if (authoidcUserRequest.isOidcLogin()) {
            // 구글 로그인: ID Token만 있음
            cookies = authService.issuePairTokens(authoidcUserRequest);
        } else {
            // JWT 인증 등 DB User 기반
            cookies = authService.generateAllTokens(authUser);
        }

        /
        Map<String, ResponseCookie> cookies = authService.issuePairTokens(authoidcUserRequest);
//        Map<String, ResponseCookie> cookies = authService.issueAllTokens(authoidcUserRequest);
        cookies.values().forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString()));

        response.sendRedirect(target);
    }
}
