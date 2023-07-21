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
package com.cskefu.cc.controller.admin.config;

import com.cskefu.cc.basic.Constants;
import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.basic.MainUtils;
import com.cskefu.cc.cache.RedisCommand;
import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.interceptor.UserExperiencePlanInterceptorHandler;
import com.cskefu.cc.model.Dict;
import com.cskefu.cc.model.Secret;
import com.cskefu.cc.model.SysDic;
import com.cskefu.cc.model.SystemConfig;
import com.cskefu.cc.persistence.repository.SecretRepository;
import com.cskefu.cc.persistence.repository.SystemConfigRepository;
import com.cskefu.cc.persistence.repository.SystemMessageRepository;
import com.cskefu.cc.persistence.repository.TemplateRepository;
import com.cskefu.cc.util.Menu;
import com.corundumstudio.socketio.SocketIOServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin/config")
public class SystemConfigController extends Handler {

    @Autowired
    private SocketIOServer server;

    @Autowired
    private SystemConfigRepository systemConfigRes;

    @Autowired
    private RedisCommand redisCommand;

    @Autowired
    private SystemMessageRepository systemMessageRes;

    @Autowired
    private SecretRepository secRes;

    @Autowired
    private TemplateRepository templateRes;

    @RequestMapping("/index")
    @Menu(type = "admin", subtype = "config", admin = true)
    public ModelAndView index(ModelMap map, HttpServletRequest request, @Valid String execute) throws SQLException {
        map.addAttribute("server", server);
        if (MainContext.hasModule(Constants.CSKEFU_MODULE_ENTIM)) {
            map.addAttribute(Constants.CSKEFU_MODULE_ENTIM, true);
        }
        if (request.getSession().getAttribute(Constants.CSKEFU_SYSTEM_INFOACQ) != null) {
            map.addAttribute(
                    Constants.CSKEFU_MODULE_ENTIM, request.getSession().getAttribute(Constants.CSKEFU_SYSTEM_INFOACQ));
        }
        map.addAttribute("server", server);
        map.addAttribute("imServerStatus", MainContext.getIMServerStatus());
        List<Secret> secretConfig = secRes.findAll();

        // check out secretConfig
        if (secretConfig != null && secretConfig.size() > 0) {
            map.addAttribute("secret", secretConfig.get(0));
        }

        List<SysDic> dicList = Dict.getInstance().getDic(Constants.CSKEFU_SYSTEM_DIC);
        SysDic callCenterDic = null, workOrderDic = null, smsDic = null;
        for (SysDic dic : dicList) {
            if (dic.getCode().equals(Constants.CSKEFU_SYSTEM_CALLCENTER)) {
                callCenterDic = dic;
            }
            if (dic.getCode().equals(Constants.CSKEFU_SYSTEM_WORKORDEREMAIL)) {
                workOrderDic = dic;
            }
            if (dic.getCode().equals(Constants.CSKEFU_SYSTEM_SMSEMAIL)) {
                smsDic = dic;
            }
        }
        if (callCenterDic != null) {
            map.addAttribute(
                    "templateList",
                    templateRes.findByTemplettype(callCenterDic.getId()));
        }
        if (workOrderDic != null) {
            map.addAttribute(
                    "workOrderList",
                    templateRes.findByTemplettype(workOrderDic.getId()));
        }
        if (smsDic != null) {
            map.addAttribute("smsList", templateRes.findByTemplettype(smsDic.getId()));
        }

        map.addAttribute(
                "sysMessageList", systemMessageRes.findByMsgtype(MainContext.SystemMessageType.EMAIL.toString()));

        if (StringUtils.isNotBlank(execute) && execute.equals("false")) {
            map.addAttribute("execute", execute);
        }
        if (StringUtils.isNotBlank(request.getParameter("msg"))) {
            map.addAttribute("msg", request.getParameter("msg"));
        }

        String userExpTelemetrySetting = redisCommand.get(UserExperiencePlanInterceptorHandler.FLAG_KEY);
        if (StringUtils.isEmpty(userExpTelemetrySetting) || StringUtils.equalsIgnoreCase(userExpTelemetrySetting, UserExperiencePlanInterceptorHandler.USER_EXP_PLAN_ON)) {
            map.addAttribute("userExpTelemetrySetting", true);
        } else {
            map.addAttribute("userExpTelemetrySetting", false);
        }

        return request(super.createView("/admin/config/index"));
    }

    @RequestMapping("/stopimserver")
    @Menu(type = "admin", subtype = "stopimserver", admin = true)
    public ModelAndView stopimserver(ModelMap map, HttpServletRequest request, @Valid String confirm) throws SQLException {
        boolean execute;
        if (execute = MainUtils.secConfirm(secRes, confirm)) {
            server.stop();
            MainContext.setIMServerStatus(false);
        }
        return request(super.createView("redirect:/admin/config/index.html?execute=" + execute));
    }

    @RequestMapping("/startentim")
    @Menu(type = "admin", subtype = "startentim", admin = true)
    public ModelAndView startentim(ModelMap map, HttpServletRequest request) throws SQLException {
        MainContext.enableModule(Constants.CSKEFU_MODULE_ENTIM);
        return request(super.createView("redirect:/admin/config/index.html"));
    }

    @RequestMapping("/stopentim")
    @Menu(type = "admin", subtype = "stopentim", admin = true)
    public ModelAndView stopentim(ModelMap map, HttpServletRequest request) throws SQLException {
        MainContext.removeModule(Constants.CSKEFU_MODULE_ENTIM);
        return request(super.createView("redirect:/admin/config/index.html"));
    }

    /**
     * 危险操作，请谨慎调用 ， WebLogic/WebSphere/Oracle等中间件服务器禁止调用
     *
     * @param map
     * @param request
     * @return
     * @throws SQLException
     */
    @RequestMapping("/stop")
    @Menu(type = "admin", subtype = "stop", admin = true)
    public ModelAndView stop(ModelMap map, HttpServletRequest request, @Valid String confirm) throws SQLException {
        boolean execute = false;
        if (execute = MainUtils.secConfirm(secRes, confirm)) {
            server.stop();
            MainContext.setIMServerStatus(false);
            System.exit(0);
        }
        return request(super.createView("redirect:/admin/config/index.html?execute=" + execute));
    }


    @RequestMapping("/save")
    @Menu(type = "admin", subtype = "save", admin = true)
    public ModelAndView save(
            ModelMap map, HttpServletRequest request,
            @Valid SystemConfig config,
            BindingResult result,
            @RequestParam(value = "keyfile", required = false) MultipartFile keyfile,
            @RequestParam(value = "loginlogo", required = false) MultipartFile loginlogo,
            @RequestParam(value = "consolelogo", required = false) MultipartFile consolelogo,
            @RequestParam(value = "favlogo", required = false) MultipartFile favlogo,
            @Valid Secret secret) throws SQLException, IOException, NoSuchAlgorithmException {
        SystemConfig systemConfig = systemConfigRes.findOne();
        String msg = "0";
        if (systemConfig == null) {
            config.setCreater(super.getUser(request).getId());
            config.setCreatetime(new Date());
            systemConfig = config;
        } else {
            MainUtils.copyProperties(config, systemConfig);
        }
        if (loginlogo != null && StringUtils.isNotBlank(
                loginlogo.getOriginalFilename()) && loginlogo.getOriginalFilename().lastIndexOf(".") > 0) {
            systemConfig.setLoginlogo(super.saveImageFileWithMultipart(loginlogo));
        }
        if (consolelogo != null && StringUtils.isNotBlank(
                consolelogo.getOriginalFilename()) && consolelogo.getOriginalFilename().lastIndexOf(".") > 0) {
            systemConfig.setConsolelogo(super.saveImageFileWithMultipart(consolelogo));
        }
        if (favlogo != null && StringUtils.isNotBlank(
                favlogo.getOriginalFilename()) && favlogo.getOriginalFilename().lastIndexOf(".") > 0) {
            systemConfig.setFavlogo(super.saveImageFileWithMultipart(favlogo));
        }

        if (secret != null && StringUtils.isNotBlank(secret.getPassword())) {
            List<Secret> secretConfig = secRes.findAll();
            String repassword = request.getParameter("repassword");
            if (StringUtils.isNotBlank(repassword) && repassword.equals(secret.getPassword())) {
                if (secretConfig != null && secretConfig.size() > 0) {
                    Secret tempSecret = secretConfig.get(0);
                    String oldpass = request.getParameter("oldpass");
                    if (StringUtils.isNotBlank(oldpass) && MainUtils.md5(oldpass).equals(tempSecret.getPassword())) {
                        tempSecret.setPassword(MainUtils.md5(secret.getPassword()));
                        msg = "1";
                        tempSecret.setEnable(true);
                        secRes.save(tempSecret);
                    } else {
                        msg = "3";
                    }
                } else {
                    secret.setCreater(super.getUser(request).getId());
                    secret.setCreatetime(new Date());
                    secret.setPassword(MainUtils.md5(secret.getPassword()));
                    secret.setEnable(true);
                    msg = "1";
                    secRes.save(secret);
                }
            } else {
                msg = "2";
            }
            map.addAttribute("msg", msg);
        }

        // 设置用户体验计划开关
        redisCommand.put(UserExperiencePlanInterceptorHandler.FLAG_KEY, config.getUserExpTelemetrySetting() ? UserExperiencePlanInterceptorHandler.USER_EXP_PLAN_ON : UserExperiencePlanInterceptorHandler.USER_EXP_PLAN_OFF);

        // 保存到数据库
        systemConfigRes.save(systemConfig);

        MainContext.getCache().putSystemById("systemConfig", systemConfig);
        map.addAttribute("imServerStatus", MainContext.getIMServerStatus());

        return request(super.createView("redirect:/admin/config/index.html?msg=" + msg));
    }
}