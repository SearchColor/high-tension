package com.high.cart.infrastructure.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 모든 API 요청을 가로채서 모니터링에 필요한 메타데이터(경로, 메서드, 처리 시간)를
 * MDC에 설정하고 최종 로그를 출력하는 인터셉터입니다.
 */
@Component
public class ApiMonitoringInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ApiMonitoringInterceptor.class);

    // 요청 시작 시간을 저장할 상수 (요청 속성 키)
    private static final String START_TIME_ATTRIBUTE = "startTimeMs";

    // 1. 요청 처리 직전에 실행됨
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 요청 경로 및 메서드 추출
        String path = request.getRequestURI();
        String method = request.getMethod();

        // MDC에 필드를 설정합니다. (이 필드는 최종 로그에 자동으로 포함됩니다.)
        MDC.put("http.method", method);
        MDC.put("url.path", path);
        MDC.put("log.type", "api_request"); // 이 로그가 API 요청 관련임을 식별하는 필드

        // 요청 시작 시간 기록
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        // true를 반환해야 핸들러(컨트롤러 메서드)로 요청이 계속 진행됩니다.
        return true;
    }

    // 2. 요청 처리 완료 후에 실행됨 (뷰 렌더링 포함)
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        try {
            long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            long duration = System.currentTimeMillis() - startTime;

            // 처리 시간을 MDC에 추가
            MDC.put("duration_ms", String.valueOf(duration));

            // HTTP 상태 코드 추가
            MDC.put("http.status_code", String.valueOf(response.getStatus()));

            // 최종 로그 출력 (이 로그 라인에 MDC의 모든 필드가 포함되어 JSON으로 출력됩니다.)
            log.info("API Request Processed.");

        } catch (Exception e) {
            // 예외 발생 시 로그 기록 및 정리
            log.error("Error during API monitoring logging: {}", e.getMessage(), e);
        } finally {
            // MDC 정리! 다음 요청에 이전 요청의 데이터가 오염되는 것을 방지하기 위해 필수!
            MDC.remove("http.method");
            MDC.remove("url.path");
            MDC.remove("duration_ms");
            MDC.remove("http.status_code");
            MDC.remove("log.type");
        }
    }
}