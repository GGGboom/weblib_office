package com.dcampus.weblib.web.action;

import com.dcampus.common.config.OfficeProperty;
import com.dcampus.common.util.Crypt;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.GroupResourceDao;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.office.OfficeFileModel;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.service.office.OfficeService;
import com.dcampus.weblib.service.ResourceService;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.office.DocumentManager;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping(value = "/office")
public class OfficeController {
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Permission permission;

    @Autowired
    GroupResourceDao resourceDao;

    @Autowired
    private OfficeService officeService;


    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/preview", produces = "application/json; charset=UTF-8")
    public String downloadResource(Long id, String mode, String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (id == null) {
            throw new GroupsException("资源id为空，无法查看");
        }
        //获取权限
        if (!permission.isReceivedResourceToMember(id, UserUtils.getCurrentMemberId())) {
            GroupResource resourceBean = resourceDao.getResourceById(id);
            if (!permission.hasGroupPerm(UserUtils.getCurrentMemberId(), resourceBean
                    .getGroup().getId(), IPermission.GroupPerm.DOWNLOAD_RESOURCE))
                throw PermissionsException.GroupException;
        }
        DocumentManager.init(request);
        GroupResource resourceBean = resourceService.getResourceById(id);
        String filename = resourceBean.getFilePath();
        String userName = resourceBean.getMemberName();
        String groupID = resourceBean.getGroupName();
        OfficeFileModel model = null;
        try {
            File downFile = resourceService.getDownloadResource(new Long[]{id});
            String targetPath = DocumentManager.FileRootPath(groupID);
            model = new OfficeFileModel(resourceBean);
            File targetFile = new File(targetPath + "/" + filename);
            DocumentManager.createMeta(filename, userName, groupID);
            Crypt.fileDecrypt(downFile, targetFile);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"" + e.toString() + "\"}";
        }
        model.changeType(mode, type);
        StringBuilder sb = new StringBuilder();
        sb.append("{" + "\"model\":" + model.toString() + "," + "\"apiUrl\":\"" + OfficeProperty.getUrlApi() + "\"}");
        System.out.println(model.toString());
        return sb.toString();
    }

    //    @RequiresUser
    @RequestMapping(value = "/track", produces = "application/json;charset=UTF-8")
    public void saveOfficeFile(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (id == null) {
            throw new GroupsException("资源id为空");
        }
        PrintWriter writer = response.getWriter();
        InputStream requestStream = request.getInputStream();
        JSONObject jsonObj = null;
        try {
            String body = officeService.getBody(requestStream);
            jsonObj = new JSONObject(body);
            System.out.println(jsonObj.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            writer.write("JSONParser.parse error:" + ex.getMessage());
            return;
        }
        try {
            GroupResource resourceBean = resourceService.getResourceById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (jsonObj.getInt("status") == 2) {
//            officeService.saveChanges(jsonObj, request, resourceBean);
//        }
        writer.write("{\"error\":0}");
    }
}
