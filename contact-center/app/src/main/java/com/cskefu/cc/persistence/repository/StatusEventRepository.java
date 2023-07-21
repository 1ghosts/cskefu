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
package com.cskefu.cc.persistence.repository;

import com.cskefu.cc.model.StatusEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface StatusEventRepository extends JpaRepository<StatusEvent, String> {

    StatusEvent findByIdOrBridgeid(String id, String bridgeid);

    Page<StatusEvent> findByAni(String ani, Pageable page);

    Page<StatusEvent> findByNameid(String nameid, Pageable page);

    Page<StatusEvent> findByDataid(String dataid, Pageable page);

    Page<StatusEvent> findAll(Pageable page);

    Page<StatusEvent> findByServicestatus(String servicestatus, Pageable page);

    Page<StatusEvent> findByMisscall(boolean misscall, Pageable page);

    Page<StatusEvent> findByRecord(boolean record, Pageable page);

    Page<StatusEvent> findByCalled(String voicemail, Pageable page);

    Page<StatusEvent> findAll(Specification<StatusEvent> spec, Pageable pageable);  //分页按条件查询


    /**
     * 坐席报表
     *
     * @param channel
     * @param fromdate
     * @param enddate
     * @param organ
     * @return
     */
    @Query(nativeQuery = true, value = 
            "select " +
                    "  agent, " +
                    "  direction, " +
                    "  count(IF(DIALPLAN is not null, 1, null)) dialplan, " +
                    "  count(*) total, " +
                    "  sum(duration) seconds, " +
                    "  count(IF(DURATION = 0, 1, null)) fails, " +
                    "  count(IF(DURATION >= 60, 1, null)) gt60," +
                    "  max(duration) maxduration, " +
                    "  avg(duration) avgduration, " +
                    "  agentname " +
                    "from uk_callcenter_event " +
                    "where " +
                    "  status = '已挂机' " +
                    "  and datestr >= ?2" +
                    "  and datestr < ?3" +
                    "  and voicechannel = ?1" +
                    "  and (?4 is null or organid = ?4) " +
                    "  and agent is not null " +
                    "group by" +
                    "  agent," +
                    "  direction")
    List<Object[]>
    queryCalloutHangupAuditGroupByAgentAndDirection(String channel,
                                                    String fromdate,
                                                    String enddate,
                                                    String organ);

    /**
     * 外呼计划通话记录接通记录查询
     *
     * @param fromdate
     * @param enddate
     * @param organid
     * @param agentid
     * @param called
     * @param page
     * @return
     */
    @Query("select s from StatusEvent s where (:fromdate is null or s.createtime >= :fromdate) " +
            "and (:enddate is null or s.createtime < :enddate) " +
            "and (:organid is null or s.organid = :organid) " +
            "and (:agent is null or s.agent = :agent) " +
            "and (:called is null or s.called = :called) " +
            "and (:dialplan is null or s.dialplan = :dialplan) " +
            "and s.direction = :direction " +
            "and s.status = :status " +
            "and s.duration > 0 ")
    Page<StatusEvent> queryCalloutDialplanSuccRecords(@Param("fromdate") Date fromdate,
                                                      @Param("enddate") Date enddate,
                                                      @Param("organid") String organid,
                                                      @Param("agent") String agentid,
                                                      @Param("called") String called,
                                                      @Param("direction") String direction,
                                                      @Param("status") String status,
                                                      @Param("dialplan") String dialplan,
                                                      Pageable page);

    @Query("select s " +
            "from StatusEvent s " +
            "where " +
            "  s.agent = :agent and " +
            "  s.siptrunk = :siptrunk and " +
            "  s.status = :status " +
            "order by s.createtime DESC")
    StatusEvent findByAgentAndSiptrunkAndStatus(@Param("agent") String agent, @Param("siptrunk") String siptrunk, @Param("status") String status);


    /**
     * 外呼日报
     *
     * @param datestr
     * @param channel
     * @param direction
     * @return
     */
    @Query(nativeQuery = true, value = "select dialplan, " +
            "datestr, " +
            "count(*) as total, " +
            "count(case duration when 0 then 1 else null end) fails, " +
            "sum(duration) as seconds " +
            "from uk_callcenter_event " +
            "where " +
            "DIRECTION = ?3 " +
            "and status = '已挂机' " +
            "and datestr = ?1 " +
            "and voicechannel = ?2 " +
            "group by dialplan")
    List<Object[]> queryCallOutHangupAggsGroupByDialplanByDatestrAndChannelAndDirection(String datestr,
                                                                                        String channel,
                                                                                        String direction);

    int countByAgent(String agent);

    int countByAniOrCalled(String ani, String called);

    int countByAni(String ani);

    int countByCalled(String called);

}
