package com.company.birthday.service.impl;

import com.company.birthday.service.WhatsAppGreenApiService;
import com.greenapi.client.pkg.api.GreenApi;
import com.greenapi.client.pkg.models.request.OutgoingMessage;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppGreenApiServiceImpl implements WhatsAppGreenApiService {

    private final GreenApi greenApi;

    public WhatsAppGreenApiServiceImpl(GreenApi greenApi) {
        this.greenApi = greenApi;
    }

    public void sendMessage(String phone, String content) {
        var response = greenApi.sending.sendMessage(
                OutgoingMessage.builder()
                        .chatId(phone + "@c.us")
                        .message(content)
                        .build()
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Sent: " + response.getBody());
        } else {
            System.out.println("Fail: " + response.getStatusCode());
        }
    }

    @Override
    public void sendGroupMessage(String groupId, String content) {
        var response = greenApi.sending.sendMessage(
                OutgoingMessage.builder()
                        .chatId(groupId)
                        .message(content)
                        .build()
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Sent to group");
        } else {
            System.out.println("Fail");
        }
    }
}
