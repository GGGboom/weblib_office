package com.dcampus.common.util;

import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.LinkedList;

public class JPAUtil {
    public JPAUtil() {
    }

    public static Query getQuery(EntityManager em, String hql, SearchTerm searchTerm, SortTerm sortTerm, PageTerm pageTerm) {
        LinkedList<Object> parameterList = new LinkedList();
        Query query = em.createQuery(GenericHqlUtil.getHql(hql, searchTerm, sortTerm, parameterList));

        for(int i = 0; i < parameterList.size()/2; ++i) {
            query.setParameter(i + 1, parameterList.get(i));
        }

        return query.setFirstResult(pageTerm.getBeginIndex()).setMaxResults(pageTerm.getPageSize());
    }

    public static Query getQuery(EntityManager em, String hql, SearchTerm searchTerm, SortTerm sortTerm) {
        LinkedList<Object> parameterList = new LinkedList();
        Query query = em.createQuery(GenericHqlUtil.getHql(hql, searchTerm, sortTerm, parameterList));

        for(int i = 0; i < parameterList.size()/2; ++i) {
            query.setParameter(i + 1, parameterList.get(i));
        }

        return query;
    }

    public static Query getQuery(EntityManager em, String hql, SearchTerm searchTerm) {
        LinkedList<Object> parameterList = new LinkedList();
        Query query = em.createQuery(GenericHqlUtil.getHql(hql, searchTerm, parameterList));

        for(int i = 0; i < parameterList.size()/2; ++i) {
            query.setParameter(i + 1, parameterList.get(i));
        }

        return query;
    }
}

