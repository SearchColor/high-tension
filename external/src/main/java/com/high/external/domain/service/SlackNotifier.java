package com.high.external.domain.service;

public interface SlackNotifier {

    /**
     * 특정 사용자에게 메시지를 보냅니다.
     * @param slackUserId Slack User ID
     * @param message 전송할 메시지 내용
     */
    String notifyUser(String slackUserId, String message);

}