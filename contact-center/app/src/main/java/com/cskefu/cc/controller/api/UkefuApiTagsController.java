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
package com.cskefu.cc.controller.api;

import com.cskefu.cc.basic.MainContext;
import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.persistence.repository.TagRepository;
import com.cskefu.cc.util.Menu;
import com.cskefu.cc.util.RestResult;
import com.cskefu.cc.util.RestResultType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 标签功能
 * 获取分类标签
 */
@RestController
@RequestMapping("/api/tags")
public class UkefuApiTagsController extends Handler {

    @Autowired
    private TagRepository tagRes;

    /**
     * 按照分类获取标签列表
     * 按照分类获取标签列表，Type 参数类型来自于 枚举，可选值目前有三个 ： user  workorders summary
     *
     * @param request
     * @param type    类型
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @Menu(type = "apps", subtype = "tags", access = true)
    public ResponseEntity<RestResult> list(HttpServletRequest request, @Valid String type) {
        return new ResponseEntity<>(new RestResult(RestResultType.OK, tagRes.findByTagtype(!StringUtils.isBlank(type) ? type : MainContext.ModelType.USER.toString())), HttpStatus.OK);
    }
}