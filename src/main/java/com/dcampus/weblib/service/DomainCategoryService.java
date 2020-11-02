package com.dcampus.weblib.service;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.entity.DomainCategory;
import com.dcampus.weblib.exception.GroupsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@Service
@Transactional(readOnly = false)
public class DomainCategoryService {

    @Autowired
    GenericDao genericDao;

    public List<DomainCategory>  getDcByCategory(Long categoryId){
        String domaincategoryId="from "+DomainCategory.class.getName() +" as dc where dc.category.id=?1";
        Query qdc = genericDao.createQuery(domaincategoryId, new Object[] { categoryId });
        List<DomainCategory>  dcs=qdc.getResultList();
        return dcs;

    }

}
