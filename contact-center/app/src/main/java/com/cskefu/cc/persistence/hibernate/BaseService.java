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
package com.cskefu.cc.persistence.hibernate;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BaseService<T> {

    private SessionFactory hibernateFactory;

    @Autowired
    public BaseService(EntityManagerFactory factory) {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        this.hibernateFactory = factory.unwrap(SessionFactory.class);
    }


    /**
     * 批量更新
     *
     * @param ts
     */
    public void saveOrUpdateAll(final List<Object> ts) {
        Session session = hibernateFactory.openSession();
        try {
            for (final Object t : ts) {
                session.saveOrUpdate(t);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
    }

    public void saveOrUpdate(final Object t) {
        Session session = hibernateFactory.openSession();
        try {
            session.saveOrUpdate(t);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
    }

    public void save(final Object t) {
        Session session = hibernateFactory.openSession();
        try {
            session.save(t);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
    }

    /**
     * 批量删除
     *
     * @param objects
     */
    public void deleteAll(final List<Object> objects) {
        Session session = hibernateFactory.openSession();
        try {
            for (final Object t : objects) {
                session.delete(session.merge(t));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
    }

    public void delete(final Object object) {
        Session session = hibernateFactory.openSession();
        try {
            session.delete(session.merge(object));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<?> list(final String bean) {
        List<?> dataList = null;
        Session session = new Configuration()
                .configure()
                .buildSessionFactory()
                .openSession();
        try {
            dataList = new ArrayList<>();
            // TODO lecjy
            Class<?> clazz = Class.forName(bean);
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery(clazz);
            criteriaQuery.from(clazz);
            Query<?> query = session.createQuery(criteriaQuery);
            dataList = query.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.flush();
            session.close();
        }
        return dataList;
    }
}