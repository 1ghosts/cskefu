/**
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright 2018-Jun. 2023 Chatopera Inc. <https://www.chatopera.com>. All rights reserved.
 */
package com.cskefu.cc.controller.api;

import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.util.restapi.RestUtils;
import com.cskefu.cc.model.InviteRecord;
import com.cskefu.cc.model.PassportWebIMUser;
import com.cskefu.cc.persistence.repository.InviteRecordRepository;
import com.cskefu.cc.persistence.repository.PassportWebIMUserRepository;
import com.cskefu.cc.proxy.OnlineUserProxy;
import com.cskefu.cc.util.Menu;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/apps")
public class ApiAppsController extends Handler {
    private final static Logger logger = LoggerFactory.getLogger(ApiAppsController.class);

    @Autowired
    private PassportWebIMUserRepository onlineUserRes;

    @Autowired
    private InviteRecordRepository inviteRecordRes;

    @RequestMapping(method = RequestMethod.POST)
    @Menu(type = "apps", subtype = "apps", access = true)
    public ResponseEntity<String> operations(HttpServletRequest request, @RequestBody final String body, @Valid String q) {
        logger.info("[operations] body {}, q {}", body, q);
        final JsonObject j = StringUtils.isBlank(body) ? (new JsonObject()) : (new JsonParser()).parse(
                body).getAsJsonObject();

        JsonObject json = new JsonObject();
        HttpHeaders headers = RestUtils.header();

        if (!j.has("ops")) {
            json.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_FAIL_1);
            json.addProperty(RestUtils.RESP_KEY_ERROR, "不合法的请求参数。");
        } else {
            switch (StringUtils.lowerCase(j.get("ops").getAsString())) {
                case "invite":
                    json = invite(request, j);
                    break;
                default:
                    json.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_FAIL_2);
                    json.addProperty(RestUtils.RESP_KEY_ERROR, "不合法的操作。");
            }
        }

        return new ResponseEntity<>(json.toString(), headers, HttpStatus.OK);

    }

    /**
     * 邀请访客加入会话
     *
     * @param request
     * @param j
     * @return
     */
    private JsonObject invite(final HttpServletRequest request, final JsonObject j) {
        JsonObject resp = new JsonObject();
        final String agentno = super.getUser(request).getId();

        final String userid = j.get("userid").getAsString();

        logger.info("[invite] agentno {} invite onlineUser {}", agentno, userid);
        PassportWebIMUser passportWebIMUser = OnlineUserProxy.onlineuser(userid);

        if (passportWebIMUser != null) {
            logger.info("[invite] userid {}, agentno {}", userid, agentno);
            passportWebIMUser.setInvitestatus(MainContext.OnlineUserInviteStatus.INVITE.toString());
            passportWebIMUser.setInvitetimes(passportWebIMUser.getInvitetimes() + 1);
            onlineUserRes.save(passportWebIMUser);

            InviteRecord record = new InviteRecord();
            record.setAgentno(super.getUser(request).getId());
            // 对于OnlineUser, 其userId与id是相同的
            record.setUserid(passportWebIMUser.getUserid());
            record.setAppid(passportWebIMUser.getAppid());
            inviteRecordRes.save(record);
            logger.info("[invite] new invite record {} of onlineUser id {} saved.", record.getId(), passportWebIMUser.getId());

            try {
                OnlineUserProxy.sendWebIMClients(passportWebIMUser.getUserid(), "invite:" + agentno);
                resp.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_SUCC);
            } catch (Exception e) {
                logger.error("[invite] error", e);
                resp.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_FAIL_3);
                resp.addProperty(RestUtils.RESP_KEY_ERROR, "online user is offline.");
            }
        } else {
            // 找不到的情况不可能发生，因为坐席看到的Onlineuser信息是从数据库查找到的
            logger.info("[invite] can not find onlineUser {} in database.", userid);
            resp.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_FAIL_4);
            resp.addProperty(RestUtils.RESP_KEY_ERROR, "online user is invalid, not found in db or cache.");
        }

        return resp;
    }
}
