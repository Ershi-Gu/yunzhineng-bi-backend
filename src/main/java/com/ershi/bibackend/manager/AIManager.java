package com.ershi.bibackend.manager;


import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AIManager {

    @Resource
    private YuCongMingClient yuCongMingClient;


    /**
     * 请求 AI
     * @param message 请求的内容
     * @return {@link BaseResponse}<{@link DevChatResponse}>
     */
    public String doChat(long modelId, String message){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String result = response.getData().getContent();
        return result;
    }
}
