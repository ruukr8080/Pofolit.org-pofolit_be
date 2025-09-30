package com.app.pofolit_be.security.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ForensicUtil {

    private static final Logger log = LoggerFactory.getLogger(ForensicUtil.class);

    private static final List<String> IP_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    private static final Map<String, String> OS_MAP = new LinkedHashMap<>();
    private static final Map<String, String> BROWSER_MAP = new LinkedHashMap<>();
    private static final String[] MOBILE_AGENTS = {
            "iphone", "ipod", "android", "windows ce", "blackberry", "symbian",
            "windows phone", "webos", "opera mini", "opera mobi", "polaris",
            "iemobile", "lgtelecom", "nokia", "sonyericsson", "lg", "samsung"
    };

    static {
        // TODO: whonix,tails 등 해킹에 많이 사용되는 os들을 하드코딩으로 넣거나
        //  이런거 지원해주는 라이브러리 찾아서 넣어야징.
        // OS Map (from most specific to least specific)
        OS_MAP.put("NT 6.0", "Windows Vista/Server 2008");
        OS_MAP.put("NT 5.2", "Windows Server 2003");
        OS_MAP.put("NT 5.1", "Windows XP");
        OS_MAP.put("NT 5.0", "Windows 2000");
        OS_MAP.put("NT", "Windows NT");
        OS_MAP.put("9x 4.90", "Windows Me");
        OS_MAP.put("98", "Windows 98");
        OS_MAP.put("95", "Windows 95");
        OS_MAP.put("Win16", "Windows 3.x");
        OS_MAP.put("Windows", "Windows");
        OS_MAP.put("Linux", "Linux");
        OS_MAP.put("Macintosh", "Macintosh");

        // Browser Map
        BROWSER_MAP.put("Trident", "MSIE");
        BROWSER_MAP.put("Chrome", "Chrome");
        BROWSER_MAP.put("Opera", "Opera");
    }

    public static String getIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if(StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                log.debug("IP found in header {}: {}", header, ip);
                return ip;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        log.debug("No IP in headers, falling back to getRemoteAddr: {}", remoteAddr);
        return remoteAddr;
    }

    public static String getBrowser(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");
        if(!StringUtils.hasText(agent)) {
            return "Unknown";
        }

        if(agent.contains("iPhone") && agent.contains("Mobile")) {
            return "iPhone";
        }
        if(agent.contains("Android") && agent.contains("Mobile")) {
            return "Android";
        }

        for (Map.Entry<String, String> entry : BROWSER_MAP.entrySet()) {
            if(agent.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Unknown";
    }

    public static String getOs(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");
        if(!StringUtils.hasText(agent)) {
            return "Unknown";
        }

        for (Map.Entry<String, String> entry : OS_MAP.entrySet()) {
            if(agent.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Unknown";
    }

    public static String getWebType(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent");
        if(!StringUtils.hasText(agent)) {
            return "PC"; // Default to PC if no agent
        }

        String lowerAgent = agent.toLowerCase();
        for (String mobileAgent : MOBILE_AGENTS) {
            if(lowerAgent.contains(mobileAgent)) {
                return "MOBILE";
            }
        }
        return "PC";
    }
}