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

package com.cskefu.cc.proxy;

import com.cskefu.cc.basic.Constants;
import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.model.*;
import com.cskefu.cc.persistence.interfaces.DataExchangeInterface;
import com.cskefu.cc.persistence.repository.*;
import com.cskefu.cc.util.mobile.MobileAddress;
import com.cskefu.cc.util.mobile.MobileNumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Component
public class AgentServiceProxy {
    private final static Logger logger = LoggerFactory.getLogger(AgentServiceProxy.class);

    @Autowired
    private AgentServiceRepository agentServiceRes;

    @Autowired
    private AgentUserContactsRepository agentUserContactsRes;

    @Autowired
    private ChannelRepository snsAccountRes;

    @Autowired
    private WeiXinUserRepository weiXinUserRes;

    @Autowired
    private PassportWebIMUserRepository onlineUserRes;

    @Autowired
    private PbxHostRepository pbxHostRes;

    @Autowired
    private StatusEventRepository statusEventRes;

    @Autowired
    private ServiceSummaryRepository serviceSummaryRes;

    @Autowired
    private TagRepository tagRes;

    @Autowired
    private TagRelationRepository tagRelationRes;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ContactsRepository contactsRes;

    @Autowired
    private ChatbotRepository chatbotRes;

    /**
     * 关联关系
     *
     * @param agentno
     * @param agentService
     * @param map
     */
    public void processRelaData(
            final String agentno,
            final AgentService agentService,
            final ModelMap map) {
        Sort defaultSort;
        defaultSort = Sort.by(Sort.Direction.DESC, "servicetime");
        map.addAttribute(
                "agentServiceList",
                agentServiceRes.findByUseridAndStatus(
                        agentService.getUserid(),
                        MainContext.AgentUserStatusEnum.END.toString(),
                        defaultSort
                )
        );

        if (StringUtils.isNotBlank(agentService.getAppid())) {
            map.addAttribute("snsAccount", snsAccountRes.findBySnsid(agentService.getAppid()).get());
        }
        agentUserContactsRes.findOneByUserid(
                agentService.getUserid()).ifPresent(p -> {
            if (MainContext.hasModule(Constants.CSKEFU_MODULE_CONTACTS) && StringUtils.isNotBlank(
                    p.getContactsid())) {
                contactsRes.findOneById(p.getContactsid()).ifPresent(k -> {
                    map.addAttribute("contacts", k);
                });
            }
            if (MainContext.hasModule(Constants.CSKEFU_MODULE_WORKORDERS) && StringUtils.isNotBlank(
                    p.getContactsid())) {
                DataExchangeInterface dataExchange = (DataExchangeInterface) MainContext.getContext().getBean(
                        "workorders");
                if (dataExchange != null) {
                    map.addAttribute(
                            "workOrdersList",
                            dataExchange.getListDataById(p.getContactsid(), agentno));
                }
                map.addAttribute("contactsid", p.getContactsid());
            }
        });
    }


    /**
     * 增加不同渠道的信息
     *
     * @param view
     * @param agentUser
     * @param agentService
     * @param logined      登录的用户
     */
    public void attacheChannelInfo(
            final ModelAndView view,
            final AgentUser agentUser,
            final AgentService agentService,
            final User logined) {
        if (MainContext.ChannelType.WEIXIN.toString().equals(agentUser.getChanneltype())) {
            List<PassportWechatUser> passportWechatUserList = weiXinUserRes.findByOpenid(
                    agentUser.getUserid());
            if (passportWechatUserList.size() > 0) {
                PassportWechatUser passportWechatUser = passportWechatUserList.get(0);
                view.addObject("weiXinUser", passportWechatUser);
            }
        } else if (MainContext.ChannelType.WEBIM.toString().equals(agentUser.getChanneltype())) {
            PassportWebIMUser passportWebIMUser = onlineUserRes.findById(agentUser.getUserid()).orElse(null);
            if (passportWebIMUser != null) {
                if (StringUtils.equals(
                        MainContext.OnlineUserStatusEnum.OFFLINE.toString(), passportWebIMUser.getStatus())) {
                    passportWebIMUser.setBetweentime(
                            (int) (passportWebIMUser.getUpdatetime().getTime() - passportWebIMUser.getLogintime().getTime()));
                } else {
                    passportWebIMUser.setBetweentime((int) (System.currentTimeMillis() - passportWebIMUser.getLogintime().getTime()));
                }
                view.addObject("onlineUser", passportWebIMUser);
            }
        } else if (MainContext.ChannelType.PHONE.toString().equals(agentUser.getChanneltype())) {
            if (agentService != null && StringUtils.isNotBlank(agentService.getOwner())) {
                StatusEvent statusEvent = statusEventRes.findById(agentService.getOwner()).orElse(null);
                if (statusEvent != null) {
                    if (StringUtils.isNotBlank(statusEvent.getHostid())) {
                         view.addObject("pbxHost", pbxHostRes.findById(statusEvent.getHostid()).orElse(null));
                    }
                    view.addObject("statusEvent", statusEvent);
                }
                MobileAddress ma = MobileNumberUtils.getAddress(agentUser.getPhone());
                view.addObject("mobileAddress", ma);
            }
        }
    }

    /**
     * 组装AgentUser的相关信息并封装在ModelView中
     *
     * @param view
     * @param map
     * @param agentUser
     * @param logined
     */
    public void bundleDialogRequiredDataInView(
            final ModelAndView view,
            final ModelMap map,
            final AgentUser agentUser,
            final User logined) {
        view.addObject("curagentuser", agentUser);

        Chatbot c = chatbotRes.findBySnsAccountIdentifier(agentUser.getAppid());
        if (c != null) {
            view.addObject("aisuggest", c.isAisuggest());
            view.addObject("ccaAisuggest", c.isAisuggest());
        }

        // 客服设置
        if (agentUser != null && StringUtils.isNotBlank(agentUser.getAppid())) {
            view.addObject("inviteData", OnlineUserProxy.consult(agentUser.getAppid()));
            // 服务小结
            if (StringUtils.isNotBlank(agentUser.getAgentserviceid())) {
                List<AgentServiceSummary> summarizes = serviceSummaryRes.findByAgentserviceid(
                        agentUser.getAgentserviceid());
                if (summarizes.size() > 0) {
                    view.addObject("summary", summarizes.get(0));
                }
            }

            // 对话消息
            view.addObject(
                    "agentUserMessageList",
                    chatMessageRepository.findByUsession(agentUser.getUserid(),
                            PageRequest.of(0, 20, Sort.Direction.DESC,
                                    "updatetime")));

            // 坐席服务记录
            AgentService agentService = null;
            if (StringUtils.isNotBlank(agentUser.getAgentserviceid())) {
                agentService = agentServiceRes.findById(agentUser.getAgentserviceid()).orElse(null);
                view.addObject("curAgentService", agentService);
                /**
                 * 获取关联数据
                 */
                if (agentService != null) {
                    processRelaData(logined.getId(), agentService, map);
                }
            }


            // 渠道信息
            attacheChannelInfo(view, agentUser, agentService, logined);

            // 标签，快捷回复等
            view.addObject("serviceCount", agentServiceRes
                    .countByUseridAndStatus(agentUser
                                    .getUserid(),
                            MainContext.AgentUserStatusEnum.END.toString()));
            view.addObject("tagRelationList", tagRelationRes.findByUserid(agentUser.getUserid()));
        }

        AgentService service = agentServiceRes.findById(agentUser.getAgentserviceid()).orElse(null);
        if (service != null) {
            view.addObject("tags", tagRes.findByTagtypeAndSkill(MainContext.ModelType.USER.toString(), service.getSkill()));
        }
    }
}
