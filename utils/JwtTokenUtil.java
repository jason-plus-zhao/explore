package com.qg.airubbish.utils;

import com.qg.airubbish.common.exception.CustomException;
import com.qg.airubbish.constant.Enum.ResultCode;
import com.qg.airubbish.domain.Audience;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * ========================
 * Created with IntelliJ IDEA.
 * User：pyy
 * Date：2019/7/17 17:24
 * Version: v1.0
 * ========================
 */
public class JwtTokenUtil {

    private static Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    public static final String clientId="09867897y86y78";
    public static final String base64Secret="MDkjZiY2Q05vggbjhghfvzMjYyN2I0yhY";
    public static final String name="api";
    //设置过期时间为50分钟
    public static final int expiresSecond=12*60*60*1000;

    public static final String AUTH_HEADER_KEY = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 解析jwt
     * @param jsonWebToken
     * @param base64Security
     * @return
     */
    public static Claims parseJWT(String jsonWebToken, String base64Security) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
                    .parseClaimsJws(jsonWebToken).getBody();
            return claims;
        } catch (ExpiredJwtException  eje) {
            log.error("===== Token过期 =====", eje);
            throw new CustomException(ResultCode.PERMISSION_TOKEN_EXPIRED);

        } catch (Exception e){
            log.error("===== token解析异常 =====", e);
            throw new CustomException(ResultCode.PERMISSION_TOKEN_INVALID);
        }
    }

    /**
     * 构建jwt
     * @param userId
     * @param communityId
     * @param role
     * @param userName
     * @param audience
     * @return
     */
    public static String createJWT(String userId, String communityId, String role, String userName, Audience audience) {
        try {
            // 使用HS256加密算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);

            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Secret);
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

            JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                    // 可以将基本不重要的对象信息放到claims
                    .claim("role", role)
                    .claim("userId", userId)
                    .claim("communityId",communityId)
                    .setSubject(userName)           // 代表这个JWT的主体，即它的所有人
                    .setIssuer(clientId)              // 代表这个JWT的签发主体；
                    .setIssuedAt(new Date())        // 是一个时间戳，代表这个JWT的签发时间；
                    .setAudience(name)          // 代表这个JWT的接收对象；
                    .signWith(signatureAlgorithm, signingKey);
            //添加Token过期时间
            int TTLMillis = expiresSecond;
            if (TTLMillis >= 0) {
                long expMillis = nowMillis + TTLMillis;
                Date exp = new Date(expMillis);
                // 是一个时间戳，代表这个JWT的过期时间；
                // 是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的
                builder.setExpiration(exp)
                        .setNotBefore(now);
            }

            //生成JWT
            return builder.compact();
        } catch (Exception e) {
            log.error("签名失败", e);
            throw new CustomException(ResultCode.PERMISSION_SIGNATURE_ERROR);
        }
    }

    public static String createJWTofWeChat(String openId, String userId, String userName, String role, String isHost ) {
        try {
            // 使用HS256加密算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);

            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(base64Secret);
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

            JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                    // 可以将基本不重要的对象信息放到claims
                    .claim("openId", openId)
                    .claim("userId", userId)
                    .claim("userName",userName)
                    .claim("role", role)
                    .claim("isHost",isHost)
                    // 代表这个JWT的主体，即它的所有人
                    .setSubject(userName)
                    // 代表这个JWT的签发主体；
                    .setIssuer(clientId)
                    // 是一个时间戳，代表这个JWT的签发时间；
                    .setIssuedAt(new Date())
                    // 代表这个JWT的接收对象；
                    .setAudience(name)
                    .signWith(signatureAlgorithm, signingKey);
            //添加Token过期时间
            int TTLMillis = expiresSecond;
            if (TTLMillis >= 0) {
                long expMillis = nowMillis + TTLMillis;
                Date exp = new Date(expMillis);
                // 是一个时间戳，代表这个JWT的过期时间；
                // 是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的
                builder.setExpiration(exp)
                        .setNotBefore(now);
            }

            //生成JWT
            return builder.compact();
        } catch (Exception e) {
            log.error("签名失败", e);
            throw new CustomException(ResultCode.PERMISSION_SIGNATURE_ERROR);
        }
    }


    /**
     * 从token中获取用户名
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUserName(String token, String base64Security){
        return parseJWT(token, base64Security).getSubject();
    }

    /**
     * 从token中获取用户ID
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUserId(String token, String base64Security){
        String userId =(String) parseJWT(token, base64Security).get("userId");
        log.info("获取的JWT中userId："+userId);
        return userId;
    }

    public static String getIsHost(String token, String base64Security){
        String isHost =(String) parseJWT(token, base64Security).get("isHost");
        log.info("获取的JWT中isHost："+isHost);
        return isHost;
    }

    /**
     * 从token中获取用户的社区编号communityId
     * @param token
     * @param base64Security
     * @return
     */
    public static String getCommunityId(String token, String base64Security){
        String communityId =(String) parseJWT(token, base64Security).get("communityId");
        log.info("获取的JWT中communityId："+communityId);
        return communityId;
        //return new String(Base64.decodeBase64(userId.getBytes()));
    }

    /**
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static String getOpenId(String token, String base64Security){
        String openId =(String) parseJWT(token, base64Security).get("openId");
        log.info("获取的JWT中openId："+openId);
        return openId;
    }

    /**
     * 从token中获取用户ROLE角色名
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUserRole(String token, String base64Security){
        String role = (String) parseJWT(token, base64Security).get("role");
        return role;
    }

    /**
     * 是否已过期
     * @param token
     * @param base64Security
     * @return
     */
    public static boolean isExpiration(String token, String base64Security) {
        return parseJWT(token, base64Security).getExpiration().before(new Date());
    }
}

