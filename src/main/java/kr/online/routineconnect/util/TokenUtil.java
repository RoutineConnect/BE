package kr.online.routineconnect.util;

import org.springframework.util.StringUtils;

public class TokenUtil {

    public static final String TYPE = "Bearer";
    public static final String TOKEN = "Authorization";

    public static String resolveToken(String token) {
        if (StringUtils.hasText(token) && StringUtils.startsWithIgnoreCase(token, TYPE)) {
            return token.substring(TYPE.length() + 1);
        }

        return null;
    }
}
