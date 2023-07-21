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

package com.cskefu.cc.controller.api;

import com.cskefu.cc.controller.Handler;
import com.cskefu.cc.util.restapi.RestUtils;
import com.cskefu.cc.exception.CSKefuRestException;
import com.cskefu.cc.model.Tag;
import com.cskefu.cc.persistence.repository.TagRepository;
import com.cskefu.cc.util.Menu;
import com.cskefu.cc.util.json.GsonTools;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 标签
 * 管理标签
 */
@RestController
@RequestMapping("/api/repo/tags")
public class ApiTagsController extends Handler {
    private static final Logger logger = LoggerFactory.getLogger(ApiTagsController.class);

    @Autowired
    private TagRepository tagRes;

    /**
     * 获取标签
     *
     * @param j
     * @param request
     * @return
     */
    private JsonObject fetch(final JsonObject j, final HttpServletRequest request) {
        JsonObject resp = new JsonObject();
        String tagType = null;

        if (j.has("tagtype"))
            tagType = j.get("tagtype").getAsString();

        Page<Tag> records = tagRes.findByTagtype(tagType,
                PageRequest.of(super.getP(request), super.getPs(request), Sort.Direction.DESC, "createtime"));

        JsonArray ja = new JsonArray();

        for (Tag t : records) {
            JsonObject o = new JsonObject();
            o.addProperty("id", t.getId());
            o.addProperty("name", t.getTag());
            o.addProperty("type", t.getTagtype());
            o.addProperty("icon", t.getIcon());
            o.addProperty("color", t.getColor());
            ja.add(o);
        }

        resp.add("data", ja);
        resp.addProperty("size", records.getSize()); // 每页条数
        resp.addProperty("number", records.getNumber()); // 当前页
        resp.addProperty("totalPage", records.getTotalPages()); // 所有页
        resp.addProperty("totalElements", records.getTotalElements()); // 所有检索结果数量
        resp.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_SUCC);

        return resp;
    }


    /**
     * 联系人标签
     *
     * @param request
     * @param body
     * @return
     * @throws CSKefuRestException
     * @throws GsonTools.JsonObjectExtensionConflictException
     */
    @RequestMapping(method = RequestMethod.POST)
    @Menu(type = "apps", subtype = "tags", access = true)
    public ResponseEntity<String> operations(HttpServletRequest request, @RequestBody final String body) throws CSKefuRestException, GsonTools.JsonObjectExtensionConflictException {
        final JsonObject j = (new JsonParser()).parse(body).getAsJsonObject();
        logger.info("[contact tags] operations payload {}", j.toString());
        JsonObject json = new JsonObject();
        HttpHeaders headers = RestUtils.header();
        j.addProperty("creater", super.getUser(request).getId());

        if (!j.has("ops")) {
            json.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_FAIL_1);
            json.addProperty(RestUtils.RESP_KEY_ERROR, "不合法的请求参数。");
        } else {
            switch (StringUtils.lowerCase(j.get("ops").getAsString())) {
                case "fetch":
                    json = fetch(j, request);
                    break;
                default:
                    json.addProperty(RestUtils.RESP_KEY_RC, RestUtils.RESP_RC_FAIL_3);
                    json.addProperty(RestUtils.RESP_KEY_ERROR, "不支持的操作。");
                    break;
            }
        }
        return new ResponseEntity<>(json.toString(), headers, HttpStatus.OK);
    }

}
