package com.dcampus.weblib.service;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.config.ResourceProperty;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.sys.entity.User;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.FilePathUtil;
import com.dcampus.weblib.web.action.ResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;

@EnableAsync
@Service
@Transactional(readOnly = false)
public class helpAsyncService {

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private LogService logService;

    @Autowired
    private  GroupService groupService;

    @Autowired
    private AsyncService asyncService;
    public  Future<Boolean> isCompleted=null;

    @Async
    public void uploadResource_v2(File file, GroupResource resourceBean, Boolean encrypt) throws GroupsException {
        if (resourceBean == null) {
            throw new GroupsException(ResourceProperty.getCannotUploadFileString());
        }
         Long id=0L;
//        resourceBean.setRate(1);
//        resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
//        resourceService.createResource(resourceBean);
        if (file != null) {
            String ext = resourceBean.getFileExt();
            boolean en = PropertyUtil.isIngoreEncrypt(ext);
            if (!en && encrypt != null && !encrypt.booleanValue()) {
                en = true;
            }
            String filePath = FilePathUtil.getFileFullPath(resourceBean);
            id=resourceBean.getId();
//            System.out.println("uploadResource_v2方法执行前，id:"+id);
            try {
                 isCompleted=asyncService.copyFileToServer(id, file, resourceBean, filePath, en);

//                 System.out.println("uploadResource_v2方法执行后");
                 // 记录日志
                Group groupBean = resourceBean.getGroup();
                logService.addUploadLog(groupBean.getId(), groupBean.getDisplayName(),
                        resourceBean.getId(), resourceBean.getOriginalName());
            } catch (Exception ex) {
                System.out.println("uploadResource_v2出现的异常是："+ex.toString());
                // 上传文件失败，删除记录
                resourceService.deleteResource(resourceBean.getId());
            }
        }
    }
    @Async
    public void uploadResource(File file, GroupResource resourceBean, Boolean encrypt) throws GroupsException {
        if (resourceBean == null) {
            throw new GroupsException(ResourceProperty.getCannotUploadFileString());
        }
        Long id=0L;
//        resourceBean.setRate(1);
//        resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
//        resourceService.createResource(resourceBean);
        if (file != null) {
            String ext = resourceBean.getFileExt();
            boolean en = PropertyUtil.isIngoreEncrypt(ext);
            if (!en && encrypt != null && !encrypt.booleanValue()) {
                en = true;
            }
            String filePath = FilePathUtil.getFileFullPath(resourceBean);
            id=resourceBean.getId();
//            System.out.println("uploadResource_v2方法执行前，id:"+id);
            try {
                isCompleted=asyncService.copyFileToServer(id, file, resourceBean, filePath, en);

//                 System.out.println("uploadResource_v2方法执行后");
                // 记录日志
                Group groupBean = resourceBean.getGroup();
                logService.addUploadLog(groupBean.getId(), groupBean.getDisplayName(),
                        resourceBean.getId(), resourceBean.getOriginalName());
                if(isCompleted.isDone()){
                    resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
                    resourceService.modifyResource(resourceBean);
//                    if(file.exists()) {
//                        file.delete();
//                    }
                }
            } catch (Exception ex) {
                // 上传文件失败，删除记录
                resourceService.deleteResource(resourceBean.getId());
            }
        }
    }


}
