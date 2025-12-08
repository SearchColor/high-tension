package com.high.external.infrastructure.client.slcak;


import com.high.external.domain.service.SlackNotifier;
import com.high.external.infrastructure.exception.SlackApiCallErrorException;
import com.high.external.infrastructure.exception.SlackApiNotificationFailException;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SlackApiAdapter implements SlackNotifier {

    @Value("${slack.token}")
    private String slackToken;

    @Override
    public String notifyUser(String slackUserId, String message) {
        // userId는 U로 시작하는 Slack User ID
        try {
            // Slack 클라이언트 초기화
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(slackToken);

            // 메시지 전송 요청 객체 생성 (chat.postMessage)
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    // channel 필드에 유저 ID를 사용하면 DM으로 전송됨
                    .channel(slackUserId)
                    .text(message)
                    .build();

            ChatPostMessageResponse response = methods.chatPostMessage(request);
            if (response.isOk()) {
                System.out.println("✅ Slack DM 전송 성공. User ID: " + slackUserId);
                return message;
            } else {
                System.err.println("❌ Slack DM 전송 실패. Error: " + response.getError());
                throw new SlackApiNotificationFailException();
            }
        } catch (Exception e) {
            System.err.println("❌ Slack API 호출 중 오류 발생: " + e.getMessage());
            throw new SlackApiCallErrorException();
        }
    }
}