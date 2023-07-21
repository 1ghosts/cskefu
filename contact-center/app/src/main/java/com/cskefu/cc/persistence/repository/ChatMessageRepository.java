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

import com.cskefu.cc.socketio.message.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    Page<ChatMessage> findByUsession(String usession, Pageable page);

    @Query(nativeQuery = true, value = "select *  from (select * from uk_chat_message where  usession = :usession and createtime <= :createtime order by createtime desc limit 0,10 )t3" +
            " UNION " +
            "select *  from (select * from uk_chat_message where  usession = :usession and  createtime > :createtime order by createtime  limit 0,9999)t4 ORDER BY createtime")
    List<ChatMessage> findByCreatetime(@Param("usession") String usession, @Param("createtime") Date createtime);

    @Query("select u from ChatMessage u where u.usession = ?1 and u.message like %?2% and u.islabel = true")
    Page<ChatMessage> findByislabel(String usession, String message, Pageable page);

    @Query("select u from ChatMessage u where u.usession = ?1 and u.message like %?2%")
    Page<ChatMessage> findByUsessionAndMessageContaining(String usession, String message, Pageable page);

    @Query(nativeQuery = true, value = "select * from(select * from uk_chat_message where  usession = ?1 order by createtime desc limit ?2,20 ) c order by createtime asc")
    List<ChatMessage> findByusession(String usession, Integer current);

    int countByUsessionAndCreatetimeGreaterThanEqual(String usession, Date createtime);

    Page<ChatMessage> findByContextid(String contextid, Pageable page);

    Page<ChatMessage> findByContextidAndCreatetimeLessThan(String contextid, Date createtime, Pageable page);

    Page<ChatMessage> findByAgentserviceid(String agentserviceid, Pageable page);

    Page<ChatMessage> findByContextidAndUserid(String contextid, String userid, Pageable page);

    Page<ChatMessage> findByContextidAndUseridAndCreatetimeLessThan(String contextid, String userid, Date createtime, Pageable page);
}
