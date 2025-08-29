package com.app.pofolit_be.security.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

   public static void addTokensToCookie(HttpServletResponse response, String accessToken, String refreshToken) {
      Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
      accessTokenCookie.setHttpOnly(true);
      accessTokenCookie.setPath("/");
      accessTokenCookie.setMaxAge(60 * 60);
      accessTokenCookie.setSecure(true);
      accessTokenCookie.setAttribute("SameSite", "Strict");
      response.addCookie(accessTokenCookie);

      Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
      refreshTokenCookie.setHttpOnly(true);
      refreshTokenCookie.setPath("/");
      refreshTokenCookie.setMaxAge(14 * 24 * 60 * 60);
      refreshTokenCookie.setSecure(true);
      refreshTokenCookie.setAttribute("SameSite", "Strict");
      response.addCookie(refreshTokenCookie);
   }

   public static void expireCookie(HttpServletResponse response, String cookieName) {
      Cookie cookie = new Cookie(cookieName, null);
      cookie.setMaxAge(0);
      cookie.setPath("/");
      response.addCookie(cookie);
   }
}