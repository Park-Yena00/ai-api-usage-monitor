package com.eevee.proxyservice.security;

/**
 * Gateway 신뢰 헤더에서 수집한 사용자 맥락. {@code subject}는 JWT {@code sub}(로그인 식별자, 일반적으로 이메일),
 * {@code platformUserId}는 Identity JWT {@code userId} 클레임(플랫폼 DB PK 문자열)에 대응한다.
 */
public record UserContext(
        String subject,
        String platformUserId,
        String role,
        String organizationId,
        String teamId,
        String correlationId
) {
    /**
     * API Key 조회·사용량 이벤트에 쓰는 키. 플랫폼 사용자 ID가 있으면 우선, 없으면 {@code subject}.
     */
    public String userKey() {
        if (platformUserId != null && !platformUserId.isBlank()) {
            return platformUserId;
        }
        return subject;
    }
}
