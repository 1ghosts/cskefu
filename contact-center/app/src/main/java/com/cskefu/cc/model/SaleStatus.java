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
package com.cskefu.cc.model;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Date;


/**
 * 状态表
 */
@Entity
@Table(name = "uk_sales_status")
@org.hibernate.annotations.Proxy(lazy = false)
public class SaleStatus implements java.io.Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */

    private String id;

    private String name;        //状态名
    private String code;    //状态代码
    private String cate;    //状态分类ID
    private String creater;
    private Date createtime;
    private Date updatetime;
    private String memo;    //备注

    private String activityid;    //所属的活动ID

    /**
     * @return the id
     */
    @Id
    @Column(length = 32)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public String getCate() {
        return cate;
    }


    public void setCate(String cate) {
        this.cate = cate;
    }

    public String getCreater() {
        return creater;
    }


    public void setCreater(String creater) {
        this.creater = creater;
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


    public String getMemo() {
        return memo;
    }


    public void setMemo(String memo) {
        this.memo = memo;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getActivityid() {
        return activityid;
    }


    public void setActivityid(String activityid) {
        this.activityid = activityid;
    }


}
