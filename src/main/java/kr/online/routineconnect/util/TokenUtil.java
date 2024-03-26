package kr.online.routineconnect.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class TokenUtil {

    public static final String TYPE = "Bearer";
    private static final String TOKEN = "Authorization";

    public static String resolveToken(HttpServletRequest request) {
        var token = request.getHeader(TOKEN);

        if (StringUtils.hasText(token) && token.startsWith(TYPE)) {
            return token.substring(TYPE.length() + 1);
        }

        return null;
    }
}
