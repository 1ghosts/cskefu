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
import com.cskefu.cc.basic.MainUtils;
import com.cskefu.cc.cache.Cache;
import com.cskefu.cc.model.AgentReport;
import com.cskefu.cc.model.AgentStatus;
import com.cskefu.cc.model.Organ;
import com.cskefu.cc.model.WorkMonitor;
import com.cskefu.cc.persistence.repository.AgentServiceRepository;
import com.cskefu.cc.persistence.repository.AgentUserRepository;
import com.cskefu.cc.persistence.repository.WorkMonitorRepository;
import com.cskefu.cc.proxy.OrganProxy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class ACDWorkMonitor {
    private final static Logger logger = LoggerFactory.getLogger(ACDWorkMonitor.class);

    @Autowired
    private WorkMonitorRepository workMonitorRes;

    @Autowired
    private Cache cache;

    @Autowired
    private OrganProxy organProxy;

    @Autowired
    private AgentServiceRepository agentServiceRes;

    @Autowired
    private AgentUserRepository agentUserRes;

    /**
     * 获得 当前服务状态
     *
     * @return
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public AgentReport getAgentReport() {
        return getAgentReport(null);
    }

    /**
     * 获得一个技能组的坐席状态
     *
     * @param organ
     * @return
     */
    public AgentReport getAgentReport(String organ) {
        /**
         * 统计当前在线的坐席数量
         */
        AgentReport report = new AgentReport();

        Map<String, AgentStatus> readys = cache.getAgentStatusReady();

        int readyNum = 0;
        int busyNum = 0;

        for (Map.Entry<String, AgentStatus> entry : readys.entrySet()) {
            if (organ == null) {
                readyNum++;
                if (entry.getValue().isBusy()) {
                    busyNum++;
                }
                continue;
            }

            if (entry.getValue().getSkills() != null &&
                    entry.getValue().getSkills().containsKey(organ)) {
                readyNum++;
                if (entry.getValue().isBusy()) {
                    busyNum++;
                }

            }
        }
        report.setAgents(readyNum);
        report.setBusy(busyNum);

        /**
         * 统计当前服务中的用户数量
         */

        if (organ != null) {
            Organ currentOrgan = new Organ();
            currentOrgan.setId(organ);
            Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

            report.setUsers(agentServiceRes.countByStatusAndAgentskillIn(MainContext.AgentUserStatusEnum.INSERVICE.toString(), organs.keySet()));
            report.setInquene(agentUserRes.countByStatusAndSkillIn(MainContext.AgentUserStatusEnum.INQUENE.toString(), organs.keySet()));
        } else {
            // 服务中
            report.setUsers(cache.getInservAgentUsersSize());
            // 等待中
            report.setInquene(cache.getInqueAgentUsersSize());
        }

        // DEBUG
        logger.info(
                "[getAgentReport] organ {}, agents {}, busy {}, users {}, inqueue {}", organ,
                report.getAgents(), report.getBusy(), report.getUsers(), report.getInquene()
        );
        return report;
    }

    /**
     * @param agent    坐席
     * @param userid   用户ID
     * @param status   工作状态，也就是上一个状态
     * @param current  下一个工作状态
     * @param worktype 类型 ： 语音OR 文本
     * @param lasttime
     */
    public void recordAgentStatus(
            String agent,
            String username,
            String extno,
            boolean admin,
            String userid,
            String status,
            String current,
            String worktype,
            Date lasttime
    ) {
        WorkMonitor workMonitor = new WorkMonitor();
        if (StringUtils.isNotBlank(agent) && StringUtils.isNotBlank(status)) {
            workMonitor.setAgent(agent);
            workMonitor.setAgentno(agent);
            workMonitor.setStatus(status);
            workMonitor.setAdmin(admin);
            workMonitor.setUsername(username);
            workMonitor.setExtno(extno);
            workMonitor.setWorktype(worktype);
            if (lasttime != null) {
                workMonitor.setDuration((int) (System.currentTimeMillis() - lasttime.getTime()) / 1000);
            }
            if (status.equals(MainContext.AgentStatusEnum.BUSY.toString())) {
                workMonitor.setBusy(true);
            }
            if (status.equals(MainContext.AgentStatusEnum.READY.toString())) {
                int count = workMonitorRes.countByAgentAndDatestrAndStatus(
                        agent, MainUtils.simpleDateFormat.format(new Date()),
                        MainContext.AgentStatusEnum.READY.toString()
                );
                if (count == 0) {
                    workMonitor.setFirsttime(true);
                }
            }
            if (current.equals(MainContext.AgentStatusEnum.NOTREADY.toString())) {
                List<WorkMonitor> workMonitorList = workMonitorRes.findByAgentAndDatestrAndFirsttime(agent, MainUtils.simpleDateFormat.format(new Date()), true);
                if (workMonitorList.size() > 0) {
                    WorkMonitor firstWorkMonitor = workMonitorList.get(0);
                    if (firstWorkMonitor.getFirsttimes() == 0) {
                        firstWorkMonitor.setFirsttimes(
                                (int) (System.currentTimeMillis() - firstWorkMonitor.getCreatetime().getTime()));
                        workMonitorRes.save(firstWorkMonitor);
                    }
                }
            }
            workMonitor.setCreatetime(new Date());
            workMonitor.setDatestr(MainUtils.simpleDateFormat.format(new Date()));

            workMonitor.setName(agent);
            workMonitor.setUserid(userid);

            workMonitorRes.save(workMonitor);
        }
    }

}
