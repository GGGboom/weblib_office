package com.dcampus.weblib.service;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
public class IconService {

    @Autowired
    private GenericDao genericDao;
    @Autowired
    private GroupDao groupDao;

    public GroupIcon getIconByGroupId(Long groupId){
        Group group=groupDao.getGroupById(groupId);
        return  genericDao.get(GroupIcon.class,group.getGroupIcon());
    }
}
