/*
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2018- Jun. 2023 Chatopera Inc, <https://www.chatopera.com>,  Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 * Copyright (C) 2017 优客服-多渠道客服系统,  Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.cskefu.cc.controller.api;

import com.cskefu.cc.acd.ACDAgentService;
import com.cskefu.cc.acd.ACDWorkMonitor;
import com.cskefu.cc.cache.Cache;
import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.model.AgentStatus;
import com.cskefu.cc.model.User;
import com.cskefu.cc.persistence.repository.AgentStatusRepository;
import com.cskefu.cc.proxy.AgentStatusProxy;
import com.cskefu.cc.util.Menu;
import com.cskefu.cc.util.RestResult;
import com.cskefu.cc.util.RestResultType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Date;

/**
 * ACD服务
 * 获取队列统计信息
 */
@RestController
@RequestMapping("/api/servicequene")
public class ApiServiceQueneController extends Handler {

    @Autowired
    private AgentStatusProxy agentStatusProxy;

    @Autowired
    private ACDWorkMonitor acdWorkMonitor;

    @Autowired
    private AgentStatusRepository agentStatusRes;

    @Autowired
    private ACDAgentService acdAgentService;

    @Autowired
    private Cache cache;

    /**
     * 获取队列统计信息，包含当前队列服务中的访客数，排队人数，坐席数
     *
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @Menu(type = "apps", subtype = "user", access = true)
    public ResponseEntity<RestResult> list(HttpServletRequest request) {
        return new ResponseEntity<>(
                new RestResult(RestResultType.OK, acdWorkMonitor.getAgentReport()),
                HttpStatus.OK);
    }

    /**
     * 坐席状态操作，就绪、未就绪、忙
     *
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @Menu(type = "apps", subtype = "user", access = true)
    public ResponseEntity<RestResult> agentStatus(
            HttpServletRequest request,
            @Valid String status) {
        User logined = super.getUser(request);
        AgentStatus agentStatus = null;
        if (StringUtils.isNotBlank(status) && status.equals(MainContext.AgentStatusEnum.READY.toString())) {

            agentStatus = agentStatusRes.findOneByAgentno(logined.getId()).orElseGet(() -> {
                AgentStatus p = new AgentStatus();
                p.setUserid(logined.getId());
                p.setUsername(logined.getUname());
                p.setAgentno(logined.getId());
                p.setLogindate(new Date());

//                SessionConfig sessionConfig = acdPolicyService.initSessionConfig();
                p.setUpdatetime(new Date());
//                p.setMaxusers(sessionConfig.getMaxuser());
                return p;
            });

            /**
             * 设置技能组
             */
            agentStatus.setSkills(logined.getSkills());

            /**
             * 更新当前用户状态
             */
            agentStatus.setUsers(cache.getInservAgentUsersSizeByAgentno(
                    agentStatus.getAgentno()));
            agentStatus.setStatus(MainContext.AgentStatusEnum.READY.toString());
            agentStatusRes.save(agentStatus);

            acdWorkMonitor.recordAgentStatus(
                    agentStatus.getAgentno(), agentStatus.getUsername(), agentStatus.getAgentno(),
                    logined.isAdmin(), agentStatus.getAgentno(),
                    MainContext.AgentStatusEnum.OFFLINE.toString(), MainContext.AgentStatusEnum.READY.toString(),
                    MainContext.AgentWorkType.MEIDIACHAT.toString(), null);
            acdAgentService.assignVisitors(agentStatus.getAgentno());
        } else if (StringUtils.isNotBlank(status)) {
            if (status.equals(MainContext.AgentStatusEnum.NOTREADY.toString())) {
                agentStatusRes.findOneByAgentno(
                        logined.getId()).ifPresent(p -> {
                    acdWorkMonitor.recordAgentStatus(
                            p.getAgentno(), p.getUsername(), p.getAgentno(),
                            logined.isAdmin(),
                            p.getAgentno(),
                            p.isBusy() ? MainContext.AgentStatusEnum.BUSY.toString() : MainContext.AgentStatusEnum.READY.toString(),
                            MainContext.AgentStatusEnum.NOTREADY.toString(),
                            MainContext.AgentWorkType.MEIDIACHAT.toString(), p.getUpdatetime());
                    agentStatusRes.delete(p);
                });
            } else if (StringUtils.isNotBlank(status) && status.equals(MainContext.AgentStatusEnum.BUSY.toString())) {
                agentStatusRes.findOneByAgentno(
                        logined.getId()).ifPresent(p -> {
                    p.setBusy(true);
                    acdWorkMonitor.recordAgentStatus(
                            p.getAgentno(), p.getUsername(), p.getAgentno(),
                            logined.isAdmin(), p.getAgentno(),
                            MainContext.AgentStatusEnum.READY.toString(), MainContext.AgentStatusEnum.BUSY.toString(),
                            MainContext.AgentWorkType.MEIDIACHAT.toString(),
                            p.getUpdatetime());
                    p.setUpdatetime(new Date());
                    agentStatusRes.save(p);
                });
            } else if (StringUtils.isNotBlank(status) && status.equals(
                    MainContext.AgentStatusEnum.NOTBUSY.toString())) {
                agentStatusRes.findOneByAgentno(
                        logined.getId()).ifPresent(p -> {
                    p.setBusy(false);
                    acdWorkMonitor.recordAgentStatus(
                            p.getAgentno(), p.getUsername(), p.getAgentno(),
                            logined.isAdmin(), p.getAgentno(),
                            MainContext.AgentStatusEnum.BUSY.toString(), MainContext.AgentStatusEnum.READY.toString(),
                            MainContext.AgentWorkType.MEIDIACHAT.toString(),
                            p.getUpdatetime());

                    p.setUpdatetime(new Date());
                    agentStatusRes.save(p);
                });
                acdAgentService.assignVisitors(agentStatus.getAgentno());
            }
            agentStatusProxy.broadcastAgentsStatus("agent", "api", super.getUser(request).getId());
        }
        return new ResponseEntity<>(new RestResult(RestResultType.OK, agentStatus), HttpStatus.OK);
    }
}