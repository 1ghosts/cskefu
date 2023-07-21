/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2018-Jun. 2023 Chatopera Inc, <https://www.chatopera.com>, 
 * Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.cskefu.cc.plugins.messenger;

import com.cskefu.cc.basic.Constants;
import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.basic.MainUtils;
import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.model.FbMessenger;
import com.cskefu.cc.model.Organ;
import com.cskefu.cc.model.Channel;
import com.cskefu.cc.persistence.repository.FbMessengerRepository;
import com.cskefu.cc.persistence.repository.OrganRepository;
import com.cskefu.cc.persistence.repository.ChannelRepository;
import com.cskefu.cc.proxy.OrganProxy;
import com.cskefu.cc.util.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/messenger")
public class MessengerChannelController extends Handler {
    private final static Logger logger = LoggerFactory.getLogger(MessengerChannelController.class);

    @Autowired
    private FbMessengerRepository fbMessengerRepository;

    @Autowired
    private OrganRepository organRepository;

    @Autowired
    private OrganProxy organProxy;

    @Autowired
    private ChannelRepository channelRepository;

    private Map<String, Organ> getOwnOrgan(HttpServletRequest request) {
        return organProxy.findAllOrganByParent(super.getOrgan(request));
    }

    @RequestMapping("/index")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView index(ModelMap map, HttpServletRequest request) {
        Map<String, Organ> organs = getOwnOrgan(request);
        List<FbMessenger> fbMessengers = fbMessengerRepository.findByOrganIn(organs.keySet());
        Organ currentOrgan = super.getOrgan(request);
        map.addAttribute("fbMessengers", fbMessengers);
        map.addAttribute("organs", organs);
        map.addAttribute("organ", currentOrgan);
        return request(super.createView("/admin/channel/messenger/index"));
    }

    @RequestMapping("/add")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView add(ModelMap map, HttpServletRequest request) {
        Organ currentOrgan = super.getOrgan(request);
        map.addAttribute("organ", currentOrgan);
        return request(super.createView("/admin/channel/messenger/add"));
    }

    @RequestMapping("/save")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView save(ModelMap map, HttpServletRequest request, @Valid FbMessenger fbMessenger) {
        String msg = "save_ok";
        Organ currentOrgan = super.getOrgan(request);
        FbMessenger fbMessengerOne = fbMessengerRepository.findOneByPageId(fbMessenger.getPageId());
        if (fbMessengerOne != null) {
            msg = "save_no_PageId";
        } else {
            fbMessenger.setId(MainUtils.getUUID());
            fbMessenger.setOrgan(currentOrgan.getId());

            if (fbMessenger.getStatus() == null) {
                fbMessenger.setStatus("disabled");
            }
            fbMessenger.setCreatetime(new Date());
            fbMessenger.setUpdatetime(new Date());
            fbMessenger.setAiid(null);
            fbMessengerRepository.save(fbMessenger);

            Channel channel = new Channel();
            channel.setId(MainUtils.genID());
            channel.setCreatetime(new Date());
            channel.setName(fbMessenger.getName());
            channel.setOrgan(currentOrgan.getId());
            channel.setSnsid(fbMessenger.getPageId());
            channel.setType(MainContext.ChannelType.MESSENGER.toString());
            channelRepository.save(channel);
        }
        return request(super.createView("redirect:/admin/messenger/index.html?msg=" + msg));
    }

    @RequestMapping("/edit")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView edit(ModelMap map, HttpServletRequest request, @Valid String id) {
        FbMessenger fbMessenger = fbMessengerRepository.findById(id).orElse(null);

        Organ fbOrgan = organRepository.findById(fbMessenger.getOrgan()).orElse(null);
        map.addAttribute("organ", fbOrgan);
        map.addAttribute("fb", fbMessenger);

        return request(super.createView("/admin/channel/messenger/edit"));
    }

    @RequestMapping("/update")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView update(ModelMap map, HttpServletRequest request, @Valid FbMessenger fbMessenger) {
        String msg = "update_ok";
        FbMessenger oldMessenger = fbMessengerRepository.findById(fbMessenger.getId()).orElse(null);
        oldMessenger.setName(fbMessenger.getName());
        if (fbMessenger.getStatus() != null) {
            oldMessenger.setStatus(fbMessenger.getStatus());
        } else {
            oldMessenger.setStatus(Constants.MESSENGER_CHANNEL_DISABLED);
        }

        oldMessenger.setToken(fbMessenger.getToken());
        oldMessenger.setVerifyToken(fbMessenger.getVerifyToken());
        oldMessenger.setUpdatetime(new Date());

        fbMessengerRepository.save(oldMessenger);

        return request(super.createView("redirect:/admin/messenger/index.html?msg=" + msg));
    }

    @RequestMapping("/delete")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView delete(ModelMap map, HttpServletRequest request, @Valid String id) {
        String msg = "delete_ok";
        FbMessenger fbMessenger = fbMessengerRepository.findById(id).orElse(null);
        fbMessengerRepository.deleteById(id);

        channelRepository.findBySnsid(fbMessenger.getPageId()).ifPresent(snsAccount -> {
            channelRepository.delete(snsAccount);
        });

        return request(super.createView("redirect:/admin/messenger/index.html?msg=" + msg));
    }

    @RequestMapping("/setting")
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView setting(ModelMap map, HttpServletRequest request, @Valid String id) {
        FbMessenger fbMessenger = fbMessengerRepository.findById(id).orElse(null);
        Organ fbOrgan = organRepository.findById(fbMessenger.getOrgan()).orElse(null);

        map.mergeAttributes(fbMessenger.parseConfigMap());
        map.addAttribute("organ", fbOrgan);
        map.addAttribute("fb", fbMessenger);

        return request(super.createView("/admin/channel/messenger/setting"));
    }

    @RequestMapping(value = "/setting/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Menu(type = "admin", subtype = "messenger")
    public ModelAndView saveSetting(ModelMap map, HttpServletRequest request, @Valid String id, @RequestBody MultiValueMap<String, String> formData) {
        String msg = "update_ok";

        FbMessenger fbMessenger = fbMessengerRepository.findById(id).orElse(null);
        if (fbMessenger != null) {
            fbMessenger.setConfigMap(formData.toSingleValueMap());
            fbMessengerRepository.save(fbMessenger);
        }

        return request(super.createView("redirect:/admin/messenger/index.html?msg=" + msg));
    }

    @RequestMapping("/setStatus")
    @Menu(type = "admin", subtype = "messenger")
    @ResponseBody
    public String setStatus(ModelMap map, HttpServletRequest request, @Valid String id, @Valid String status) {
        FbMessenger fbMessenger = fbMessengerRepository.findById(id).orElse(null);
        fbMessenger.setStatus(status);
        fbMessengerRepository.save(fbMessenger);
        return "ok";
    }

}

