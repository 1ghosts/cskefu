/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2019-2022 Chatopera Inc, All rights reserved. 
 * <https://www.chatopera.com>
 */
package com.cskefu.cc.proxy;

import com.cskefu.cc.basic.Constants;
import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.cache.Cache;
import com.cskefu.cc.exception.CSKefuException;
import com.cskefu.cc.model.AgentUser;
import com.cskefu.cc.model.Contacts;
import com.cskefu.cc.model.PassportWebIMUser;
import com.cskefu.cc.model.User;
import com.cskefu.cc.persistence.repository.ContactsRepository;
import com.cskefu.cc.persistence.repository.AgentUserRepository;
import com.cskefu.cc.persistence.repository.PassportWebIMUserRepository;
import com.cskefu.cc.persistence.repository.ChannelRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.*;

/**
 * 向联系人发送消息
 */
@Component
public class ContactsProxy {

    private final static Logger logger = LoggerFactory.getLogger(ContactsProxy.class);

    @Autowired
    private Cache cache;

    @Autowired
    private AgentUserRepository agentUserRes;

    @Autowired
    private ContactsRepository contactsRes;

    @Autowired
    private PassportWebIMUserRepository onlineUserRes;

    @Value("${web.upload-path}")
    private String path;

    @Autowired
    private ChannelRepository snsAccountRes;


    /**
     * 在传输SkypeId中有操作混入了非法字符
     *
     * @param dirty
     * @return
     */
    public String sanitizeSkypeId(final String dirty) {
        if (dirty != null) {
            return dirty.replace(":", "\\:");
        }
        return null;
    }


    /**
     * 根据联系人ID获得一个当前可以触达联系人的方式（们）
     * 检查时过滤掉和其它坐席聊天中的联系人
     *
     * @param logined   当前查询该信息的访客
     * @param contactid 目标联系人ID
     * @return
     */
    public List<MainContext.ChannelType> liveApproachChannelsByContactid(
            final User logined,
            final String contactid,
            final boolean isCheckSkype) throws CSKefuException {
//        logger.info("[liveApproachChannelsByContactid] contact id {}", contactid);

        List<MainContext.ChannelType> result = new ArrayList<>();

        Optional<Contacts> contactOpt = contactsRes.findOneById(contactid).filter(p -> !p.isDatastatus());

        if (contactOpt.isPresent()) {
            final Contacts contact = contactOpt.get();

            // 查看 WebIM 渠道
            agentUserRes.findOneByContactIdAndStatusNotAndChanneltype(
                            contact.getId(),
                            MainContext.AgentUserStatusEnum.END.toString(),
                            MainContext.ChannelType.WEBIM.toString())
                    .filter(p -> StringUtils.equals(p.getAgentno(), logined.getId()))
                    .ifPresent(p -> {
                        if (!cache.existBlackEntityByUserId(p.getUserid())) {
                            // 访客在线 WebIM，排队或服务中
                            result.add(MainContext.ChannelType.WEBIM);
                        } else {
                            // 该访客被拉黑
                        }
                    });

            // 查看 Skype 渠道
            if (isCheckSkype && StringUtils.isNotBlank(
                    contact.getSkypeid())) {
                // 查找匹配的OnlineUser
                Optional<PassportWebIMUser> opt = onlineUserRes.findOneByContactidAndChannel(
                        contactid,
                        Constants.CSKEFU_MODULE_SKYPE);
                if (opt.isPresent()) {
                    // 联系人存在访客信息
                    // 并且该访客没有被拉黑
                    if (!cache.existBlackEntityByUserId(
                            opt.get().getId())) {
                        Optional<AgentUser> agentUserOpt = cache.findOneAgentUserByUserId(
                                opt.get().getId());
                        if (agentUserOpt.isPresent()) {
                            AgentUser agentUser = agentUserOpt.get();
                            if ((StringUtils.equals(
                                    agentUser.getStatus(), MainContext.AgentUserStatusEnum.INSERVICE.toString())) &&
                                    (StringUtils.equals(agentUser.getAgentno(), logined.getId()))) {
                                // 该联系人的Skype账号被服务中
                                // TODO 此处可能是因为该联系的Skype对应的AgentUser没有被结束，长期被一个坐席占有
                                // 并不合理，后期需要加机制维护Skype的离线信息（1，Skype Agent查询;2, 加入最大空闲时间限制）
                                result.add(MainContext.ChannelType.SKYPE);
                            }
                        } else {
                            // 该联系人的Skype OnlineUser存在，而且未被其它坐席占用
                            // 并且该联系人没有被拉黑
                            result.add(MainContext.ChannelType.SKYPE);
                        }
                    }
                } else {
                    // 该联系人的Skype账号对应的OnlineUser不存在
                    // TODO 新建OnlineUser
                    OnlineUserProxy.createNewOnlineUserWithContactAndChannel(
                            contact, logined, Constants.CSKEFU_MODULE_SKYPE);
                    result.add(MainContext.ChannelType.SKYPE);
                }
            }
        } else {
            // can not find contact, may is deleted.
            throw new CSKefuException("Contact does not available.");
        }

//        logger.info("[liveApproachChannelsByContactid] get available list {}", StringUtils.join(result, "|"));

        return result;
    }

    /**
     * 批量查询联系人的可触达状态
     *
     * @param contacts
     * @param map
     * @param user
     */
    public void bindContactsApproachableData(final Page<Contacts> contacts, final ModelMap map, final User user) {
        Set<String> approachable = new HashSet<>();
        for (final Contacts c : contacts.getContent()) {
            try {
                if (liveApproachChannelsByContactid(user, c.getId(), isSkypeSetup()).size() > 0) {
                    approachable.add(c.getId());
                }
            } catch (CSKefuException e) {
                logger.warn("[bindContactsApproachableData] error", e);
            }
        }

        map.addAttribute("approachable", approachable);
    }

    /**
     * 检查Skype渠道是否被建立
     *
     * @return
     */
    public boolean isSkypeSetup() {
        if (MainContext.hasModule(Constants.CSKEFU_MODULE_SKYPE) && snsAccountRes.countByType(
                Constants.CSKEFU_MODULE_SKYPE) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 判断编辑是否变更
     *
     * @param newValue
     * @param oldValue
     * @return
     */
    public boolean determineChange(Contacts newValue, Contacts oldValue) {
        return (!newValue.getName().equals(oldValue.getName()) ||
                !newValue.getPhone().equals(oldValue.getPhone()) ||
                !newValue.getEmail().equals(oldValue.getEmail()) ||
                !newValue.getCity().equals(oldValue.getCity()) ||
                !newValue.getAddress().equals(oldValue.getAddress()) ||
                !newValue.getGender().equals(oldValue.getGender()) ||
                !newValue.getProvince().equals(oldValue.getProvince()) ||
                !newValue.getAddress().equals(oldValue.getAddress()) ||
                !newValue.getMemo().equals(oldValue.getMemo()) ||
                !(newValue.getCusbirthday().isEmpty() && StringUtils.isEmpty(oldValue.getCusbirthday())) ||
                (newValue.getCusbirthday().isEmpty() && StringUtils.isNotEmpty(oldValue.getCusbirthday())) ||
                !newValue.getMobileno().equals(oldValue.getMobileno()) ||
                !newValue.getSkypeid().equals(oldValue.getSkypeid()));
    }
}
