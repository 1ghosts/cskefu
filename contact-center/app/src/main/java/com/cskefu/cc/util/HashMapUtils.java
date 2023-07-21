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
package com.cskefu.cc.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HashMapUtils {


    public static <T> String concatKeys(final HashMap<String, T> map) {
        return concatKeys(map, ",");
    }

    /**
     * 将String Key连接起来
     *
     * @param map
     * @param <T>
     * @return
     */
    public static <T> String concatKeys(final HashMap<String, T> map, String separator) {
        String result = map.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(separator));
        return result;
    }
}
