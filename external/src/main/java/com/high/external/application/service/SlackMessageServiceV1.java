package com.high.external.application.service;


import com.high.external.domain.service.SlackNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlackMessageServiceV1 {

    private final SlackNotifier slackNotifier;
    private final SlackRecordServiceV1 slackRecordServiceV1;

    public String slackMessageSend(String targetSlackUserId, String message) {

        String response;
        try {
            response = slackNotifier.notifyUser(targetSlackUserId, message);
            slackRecordServiceV1.createSlackRecord(targetSlackUserId,message);
        } catch (Exception e) {
            throw new RuntimeException("Slack 알림 전송 및 기록 처리 중 오류 발생", e);
        }
        return response;
    }
}
