/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2019-Jun. 2023 Chatopera Inc, <https://www.chatopera.com>, 
 * Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.cskefu.cc.plugins.chatbot;

import com.cskefu.cc.basic.plugins.AbstractPluginConfigurer;
import com.cskefu.cc.basic.plugins.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 定义Plugin存在
 */
@Configuration(value = "chatbotPluginConfig")
public class ChatbotPluginConfigurer extends AbstractPluginConfigurer {
    private final static Logger logger = LoggerFactory.getLogger(ChatbotPluginConfigurer.class);
    private final static String pluginName = "智能机器人";
    private final static String pluginId = "chatbot";

    @Autowired
    private PluginRegistry pluginRegistry;

    @Override
    @PostConstruct
    public void setup() {
        pluginRegistry.addPlugin(this);
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    /**
     * 获取消息服务的Bean的名字
     * 当该方法存在时，加载到消息处理的调用栈 PeerSyncIM 中
     *
     * @return
     */
    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public String getIOEventHandler() {
        return ChatbotEventHandler.class.getName();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        Map<String, String> env = new HashMap<>();
        env.put(ChatbotConstants.BOT_PROVIDER, "https://bot.chatopera.com");
        env.put(ChatbotConstants.THRESHOLD_FAQ_BEST_REPLY, "0.9");
        env.put(ChatbotConstants.THRESHOLD_FAQ_SUGG_REPLY, "0.3");
        return env;
    }

    @Override
    public boolean isModule() {
        return true;
    }
}