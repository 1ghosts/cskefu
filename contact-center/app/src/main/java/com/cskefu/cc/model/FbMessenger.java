/* 
 * Copyright (C) 2023 Beijing Huaxia Chunsong Technology Co., Ltd. 
 * <https://www.chatopera.com>, Licensed under the Chunsong Public 
 * License, Version 1.0  (the "License"), https://docs.cskefu.com/licenses/v1.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Copyright (C) 2019-Jun. 2023 Chatopera Inc, <https://www.chatopera.com>, 
 * Licensed under the Apache License, Version 2.0, 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package com.cskefu.cc.model;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "cs_fb_messenger")
@org.hibernate.annotations.Proxy(lazy = false)
public class FbMessenger implements Serializable {

    private String id;
    private String pageId;
    private String token;
    private String verifyToken;
    private String name;
    private String status;
    private String organ;
    private String aiid;
    private boolean aisuggest;
    private boolean ai;
    private Date createtime;
    private Date updatetime;
    private String config;

    @Id
    @Column(length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "assigned")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public String getAiid() {
        return aiid;
    }

    public void setAiid(String aiid) {
        this.aiid = aiid;
    }

    public boolean isAi() {
        return ai;
    }

    public void setAi(boolean ai) {
        this.ai = ai;
    }

    public boolean isAisuggest() {
        return aisuggest;
    }

    public void setAisuggest(boolean aisuggest) {
        this.aisuggest = aisuggest;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getConfig() {
        return config;
    }

    /**
     * Get config as map object
     *
     * @return
     */
    public Map<String, String> parseConfigMap() {
        Map<String, String> configMap = (Map<String, String>) JSONObject.parse(StringUtils.isNotBlank(getConfig()) ? getConfig() : "{}");
        return configMap;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public <K, V> void setConfigMap(Map<K, V> config) {
        String data = JSONObject.toJSONString(config);
        this.config = data;
    }

}
