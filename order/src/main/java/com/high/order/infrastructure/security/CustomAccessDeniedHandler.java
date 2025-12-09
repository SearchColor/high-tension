package com.high.order.infrastructure.security;

//시큐리티 필터 예외 처리
//@Slf4j
//@Component
//public class CustomAccessDeniedHandler implements AccessDeniedHandler {
//
//    // ObjectMapper를 직접 생성 (Bean 주입 시 순환참조 발생 가능)
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void handle(
//        HttpServletRequest request,
//        HttpServletResponse response,
//        AccessDeniedException accessDeniedException
//    ) throws IOException, ServletException {
//
//        log.error("Access denied error: {}", accessDeniedException.getMessage());
//
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setCharacterEncoding("UTF-8");
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("success", false);
//        errorResponse.put("code", 1007);
//        errorResponse.put("message", "접근 권한이 없습니다.");
//        errorResponse.put("path", request.getRequestURI());
//
//        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
//    }
//}