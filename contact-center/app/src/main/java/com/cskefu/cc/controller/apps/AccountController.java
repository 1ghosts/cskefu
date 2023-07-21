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

package com.cskefu.cc.controller.apps;

import com.cskefu.cc.basic.MainUtils;
import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.exception.CSKefuException;
import com.cskefu.cc.model.*;
import com.cskefu.cc.persistence.repository.ContactsRepository;
import com.cskefu.cc.persistence.repository.AccountRepository;
import com.cskefu.cc.persistence.repository.MetadataRepository;
import com.cskefu.cc.persistence.repository.PropertiesEventRepository;
import com.cskefu.cc.persistence.repository.ReporterRepository;
import com.cskefu.cc.proxy.OrganProxy;
import com.cskefu.cc.util.Menu;
import com.cskefu.cc.util.PinYinTools;
import com.cskefu.cc.util.PropertiesEventUtil;
import com.cskefu.cc.util.dsdata.DSData;
import com.cskefu.cc.util.dsdata.DSDataEvent;
import com.cskefu.cc.util.dsdata.ExcelImportProecess;
import com.cskefu.cc.util.dsdata.export.ExcelExporterProcess;
import com.cskefu.cc.util.dsdata.process.AccountProcess;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 客户管理
 */
@Controller
@RequestMapping("/apps/customer")
public class AccountController extends Handler {
    private final static Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountRepository accountRes;

    @Autowired
    private ContactsRepository contactsRes;

    @Autowired
    private ReporterRepository reporterRes;

    @Autowired
    private MetadataRepository metadataRes;

    @Autowired
    private PropertiesEventRepository propertiesEventRes;

    @Autowired
    private OrganProxy organProxy;

    @Value("${web.upload-path}")
    private String path;

    @RequestMapping("/index")
    @Menu(type = "customer", subtype = "index")
    public ModelAndView index(ModelMap map,
                              HttpServletRequest request,
                              final @Valid String q,
                              final @Valid String ekind,
                              final @Valid String msg) throws CSKefuException {
        logger.info("[index] query {}, ekind {}, msg {}", q, ekind, msg);
        final User logined = super.getUser(request);
        final Organ currentOrgan = super.getOrgan(request);

        map.put("msg", msg);
        if (!super.preCheckPermissions(request)) {
            return request(super.createView("/apps/customer/index"));
        }

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }
        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        map.addAttribute("currentOrgan", currentOrgan);

        map.addAttribute("entCustomerList", accountRes.findByOrganInAndSharesAllAndDatastatusFalse(
                super.getMyCurrentAffiliatesFlat(logined),
                PageRequest.of(super.getP(request), super.getPs(request))));

        return request(super.createView("/apps/customer/index"));
    }

    @RequestMapping("/today")
    @Menu(type = "customer", subtype = "today")
    public ModelAndView today(ModelMap map, HttpServletRequest request, @Valid String q, @Valid String ekind) throws CSKefuException {
        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (!super.preCheckPermissions(request)) {
            return request(super.createView("/apps/customer/index"));
        }

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }
        map.addAttribute("entCustomerList", accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(),
                super.getUser(request).getId(),
                false,
                PageRequest.of(super.getP(request), super.getPs(request))));

        return request(super.createView("/apps/customer/index"));
    }

    @RequestMapping("/week")
    @Menu(type = "customer", subtype = "week")
    public ModelAndView week(ModelMap map, HttpServletRequest request, @Valid String q, @Valid String ekind) throws CSKefuException {

        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (!super.preCheckPermissions(request)) {
            return request(super.createView("/apps/customer/index"));
        }

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        map.addAttribute("entCustomerList", accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(), super.getUser(request).getId(),
                false,
                PageRequest.of(super.getP(request), super.getPs(request))));

        return request(super.createView("/apps/customer/index"));
    }

    @RequestMapping("/enterprise")
    @Menu(type = "customer", subtype = "enterprise")
    public ModelAndView enterprise(ModelMap map, HttpServletRequest request, @Valid String q, @Valid String ekind) throws CSKefuException {

        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (!super.preCheckPermissions(request)) {
            return request(super.createView("/apps/customer/index"));
        }

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }
        map.addAttribute("entCustomerList", accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(),
                super.getUser(request).getId(),
                false,
                PageRequest.of(super.getP(request), super.getPs(request))));
        return request(super.createView("/apps/customer/index"));
    }

    @RequestMapping("/personal")
    @Menu(type = "customer", subtype = "personal")
    public ModelAndView personal(ModelMap map, HttpServletRequest request, @Valid String q, @Valid String ekind) throws CSKefuException {

        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (!super.preCheckPermissions(request)) {
            return request(super.createView("/apps/customer/index"));
        }

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }
        map.addAttribute("entCustomerList", accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(),
                super.getUser(request).getId(),
                false,
                PageRequest.of(super.getP(request), super.getPs(request))));
        return request(super.createView("/apps/customer/index"));
    }

    @RequestMapping("/creater")
    @Menu(type = "customer", subtype = "creater")
    public ModelAndView creater(ModelMap map, HttpServletRequest request, @Valid String q, @Valid String ekind) throws CSKefuException {

        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (!super.preCheckPermissions(request)) {
            return request(super.createView("/apps/customer/index"));
        }

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }

        map.addAttribute("entCustomerList", accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(), super.getUser(request).getId(), false, PageRequest.of(super.getP(request), super.getPs(request))));
        return request(super.createView("/apps/customer/index"));
    }

    @RequestMapping("/add")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView add(ModelMap map, HttpServletRequest request, @Valid String ekind) {
        map.addAttribute("ekind", ekind);
        return request(super.createView("/apps/customer/add"));
    }

    @RequestMapping("/save")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView save(HttpServletRequest request,
                             @Valid CustomerGroupForm customerGroupForm) {
        String msg = "";
        msg = "new_entcustomer_success";
        customerGroupForm.getEntcustomer().setCreater(super.getUser(request).getId());

        final User logined = super.getUser(request);
        Organ currentOrgan = super.getOrgan(request);

//    	customerGroupForm.getEntcustomer().setEtype(MainContext.CustomerTypeEnum.ENTERPRISE.toString());
        customerGroupForm.getEntcustomer().setPinyin(PinYinTools.getInstance().getFirstPinYin(customerGroupForm.getEntcustomer().getName()));
        if (currentOrgan != null && StringUtils.isBlank(customerGroupForm.getEntcustomer().getOrgan())) {
            customerGroupForm.getEntcustomer().setOrgan(currentOrgan.getId());
            customerGroupForm.getContacts().setOrgan(currentOrgan.getId());
        }

        accountRes.save(customerGroupForm.getEntcustomer());
        if (customerGroupForm.getContacts() != null && StringUtils.isNotBlank(customerGroupForm.getContacts().getName())) {
            customerGroupForm.getContacts().setEntcusid(customerGroupForm.getEntcustomer().getId());
            customerGroupForm.getContacts().setCreater(logined.getId());
            customerGroupForm.getContacts().setPinyin(PinYinTools.getInstance().getFirstPinYin(customerGroupForm.getContacts().getName()));
            if (StringUtils.isBlank(customerGroupForm.getContacts().getCusbirthday())) {
                customerGroupForm.getContacts().setCusbirthday(null);
            }

            contactsRes.save(customerGroupForm.getContacts());
        }
        return request(super.createView("redirect:/apps/customer/index.html?ekind=" + customerGroupForm.getEntcustomer().getEkind() + "&msg=" + msg));
    }

    @RequestMapping("/delete")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView delete(HttpServletRequest request, @Valid Account account, @Valid String p, @Valid String ekind) {
        if (account != null) {
            account = accountRes.findById(account.getId()).orElse(null);
            account.setDatastatus(true);                            //客户和联系人都是 逻辑删除
            accountRes.save(account);
        }
        return request(super.createView("redirect:/apps/customer/index.html?p=" + p + "&ekind=" + ekind));
    }

    @RequestMapping("/edit")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView edit(ModelMap map, HttpServletRequest request, @Valid String id, @Valid String ekind) {
        map.addAttribute("entCustomer", accountRes.findById(id).orElse(null));
        map.addAttribute("ekindId", ekind);
        return request(super.createView("/apps/customer/edit"));
    }

    @RequestMapping("/update")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView update(HttpServletRequest request, @Valid CustomerGroupForm customerGroupForm, @Valid String ekindId) {
        final User logined = super.getUser(request);
        Account customer = accountRes.findById(customerGroupForm.getEntcustomer().getId()).orElse(null);
        String msg = "";

        List<PropertiesEvent> events = PropertiesEventUtil.processPropertiesModify(request, customerGroupForm.getEntcustomer(), customer, "id", "creater", "createtime", "updatetime");    //记录 数据变更 历史
        if (events.size() > 0) {
            msg = "edit_entcustomer_success";
            String modifyid = MainUtils.getUUID();
            Date modifytime = new Date();
            for (PropertiesEvent event : events) {
                event.setDataid(customerGroupForm.getEntcustomer().getId());
                event.setCreater(super.getUser(request).getId());
                event.setModifyid(modifyid);
                event.setCreatetime(modifytime);
                propertiesEventRes.save(event);
            }
        }
        customerGroupForm.getEntcustomer().setOrgan(customer.getOrgan());
        customerGroupForm.getEntcustomer().setCreater(customer.getCreater());
        customerGroupForm.getEntcustomer().setCreatetime(customer.getCreatetime());
        customerGroupForm.getEntcustomer().setPinyin(PinYinTools.getInstance().getFirstPinYin(customerGroupForm.getEntcustomer().getName()));
        accountRes.save(customerGroupForm.getEntcustomer());

        return request(super.createView("redirect:/apps/customer/index.html?ekind=" + ekindId + "&msg=" + msg));
    }

    @RequestMapping("/imp")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView imp(ModelMap map, HttpServletRequest request, @Valid String ekind) {
        map.addAttribute("ekind", ekind);
        return request(super.createView("/apps/customer/imp"));
    }

    @RequestMapping("/impsave")
    @Menu(type = "customer", subtype = "customer")
    public ModelAndView impsave(ModelMap map, HttpServletRequest request, @RequestParam(value = "cusfile", required = false) MultipartFile cusfile, @Valid String ekind) throws IOException {
        DSDataEvent event = new DSDataEvent();
        String fileName = "customer/" + MainUtils.getUUID() + cusfile.getOriginalFilename().substring(cusfile.getOriginalFilename().lastIndexOf("."));
        File excelFile = new File(path, fileName);
        if (!excelFile.getParentFile().exists()) {
            excelFile.getParentFile().mkdirs();
        }

        Organ currentOrgan = super.getOrgan(request);
        String organId = currentOrgan != null ? currentOrgan.getId() : null;

        MetadataTable table = metadataRes.findByTablename("uk_entcustomer");
        if (table != null) {
            FileUtils.writeByteArrayToFile(new File(path, fileName), cusfile.getBytes());
            event.setDSData(new DSData(table, excelFile, cusfile.getContentType(), super.getUser(request)));
            event.getDSData().setClazz(Account.class);
            event.getDSData().setProcess(new AccountProcess(accountRes));
	    	/*if(StringUtils.isNotBlank(ekind)){
	    		exchange.getValues().put("ekind", ekind) ;
	    	}*/
            event.getValues().put("creater", super.getUser(request).getId());
            event.getValues().put("organ", organId);
            event.getValues().put("shares", "all");
            reporterRes.save(event.getDSData().getReport());
            new ExcelImportProecess(event).process();        //启动导入任务
        }

        return request(super.createView("redirect:/apps/customer/index.html"));
    }

    @RequestMapping("/expids")
    @Menu(type = "customer", subtype = "customer")
    public void expids(ModelMap map, HttpServletRequest request, HttpServletResponse response, @Valid String[] ids) throws IOException {
        if (ids != null && ids.length > 0) {
            Iterable<Account> entCustomerList = accountRes.findAllById(Arrays.asList(ids));
            MetadataTable table = metadataRes.findByTablename("uk_entcustomer");
            List<Map<String, Object>> values = new ArrayList<>();
            for (Account customer : entCustomerList) {
                values.add(MainUtils.transBean2Map(customer));
            }

            response.setHeader("content-disposition", "attachment;filename=CSKeFu-EntCustomer-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xls");

            ExcelExporterProcess excelProcess = new ExcelExporterProcess(values, table, response.getOutputStream());
            excelProcess.process();
        }

        return;
    }

    @RequestMapping("/expall")
    @Menu(type = "customer", subtype = "customer")
    public void expall(ModelMap map, HttpServletRequest request, HttpServletResponse response, @Valid String ekind) throws IOException, CSKefuException {
        if (!super.preCheckPermissions(request)) {
            // #TODO 提示没有部门
            return;
        }

        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        Iterable<Account> entCustomerList = accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(), super.getUser(request).getId(), false, PageRequest.of(super.getP(request), super.getPs(request)));

        MetadataTable table = metadataRes.findByTablename("uk_entcustomer");
        List<Map<String, Object>> values = new ArrayList<>();
        for (Account customer : entCustomerList) {
            values.add(MainUtils.transBean2Map(customer));
        }

        response.setHeader("content-disposition", "attachment;filename=CSKeFu-EntCustomer-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xls");

        ExcelExporterProcess excelProcess = new ExcelExporterProcess(values, table, response.getOutputStream());
        excelProcess.process();
        return;
    }

    @RequestMapping("/expsearch")
    @Menu(type = "customer", subtype = "customer")
    public void expall(ModelMap map, HttpServletRequest request, HttpServletResponse response, @Valid String q, @Valid String ekind) throws IOException, CSKefuException {
        if (!super.preCheckPermissions(request)) {
            // #TODO 提示没有部门
            return;
        }

        Organ currentOrgan = super.getOrgan(request);
        Map<String, Organ> organs = organProxy.findAllOrganByParent(currentOrgan);

        if (StringUtils.isNotBlank(q)) {
            map.put("q", q);
        }

        if (StringUtils.isNotBlank(ekind)) {
            map.put("ekind", ekind);
        }

        Iterable<Account> entCustomerList = accountRes.findByCreaterAndSharesAndDatastatus(super.getUser(request).getId(),
                super.getUser(request).getId(),

                false,
                PageRequest.of(super.getP(request), super.getPs(request)));
        MetadataTable table = metadataRes.findByTablename("uk_entcustomer");
        List<Map<String, Object>> values = new ArrayList<>();
        for (Account customer : entCustomerList) {
            values.add(MainUtils.transBean2Map(customer));
        }

        response.setHeader("content-disposition", "attachment;filename=CSKeFu-EntCustomer-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xls");

        ExcelExporterProcess excelProcess = new ExcelExporterProcess(values, table, response.getOutputStream());
        excelProcess.process();

        return;
    }
}
