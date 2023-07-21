/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2019-2022 Chatopera Inc, <https://www.chatopera.com>, 
 * Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.cskefu.cc.acd;

import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.model.AgentService;
import com.cskefu.cc.model.AgentUser;
import com.cskefu.cc.persistence.repository.AgentServiceRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ACDChatbotService {
    private final static Logger logger = LoggerFactory.getLogger(ACDChatbotService.class);

    @Autowired
    private AgentServiceRepository agentServiceRes;

    /**
     * 为访客分配机器人客服， ACD策略，此处 AgentStatus 是建议 的 坐席，  如果启用了  历史服务坐席 优先策略， 则会默认检查历史坐席是否空闲，如果空闲，则分配，如果不空闲，则 分配当前建议的坐席
     *
     * @param agentUser
     * @return
     * @throws Exception
     */
    public AgentService processChatbotService(final String botName, final AgentUser agentUser) {
        AgentService agentService = new AgentService();    //放入缓存的对象
        Date now = new Date();
        if (StringUtils.isNotBlank(agentUser.getAgentserviceid())) {
            agentService = agentServiceRes.findById(agentUser.getAgentserviceid()).orElse(null);
            agentService.setEndtime(now);
            if (agentService.getServicetime() != null) {
                agentService.setSessiontimes(System.currentTimeMillis() - agentService.getServicetime().getTime());
            }
            agentService.setStatus(MainContext.AgentUserStatusEnum.END.toString());
        } else {
            agentService.setServicetime(now);
            agentService.setLogindate(now);
            agentService.setOwner(agentUser.getContextid());
            agentService.setSessionid(agentUser.getSessionid());
            agentService.setRegion(agentUser.getRegion());
            agentService.setUsername(agentUser.getUsername());
            agentService.setChanneltype(agentUser.getChanneltype());
            if (botName != null) {
                agentService.setAgentusername(botName);
            }

            if (StringUtils.isNotBlank(agentUser.getContextid())) {
                agentService.setContextid(agentUser.getContextid());
            } else {
                agentService.setContextid(agentUser.getSessionid());
            }

            agentService.setUserid(agentUser.getUserid());
            agentService.setAiid(agentUser.getAgentno());
            agentService.setAiservice(true);
            agentService.setStatus(MainContext.AgentUserStatusEnum.INSERVICE.toString());

            agentService.setAppid(agentUser.getAppid());
            agentService.setLeavemsg(false);
        }

        agentServiceRes.save(agentService);
        return agentService;
    }

}
