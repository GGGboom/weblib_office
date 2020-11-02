package com.dcampus.weblib.web.action;

import com.dcampus.common.config.OfficeProperty;
import com.dcampus.common.util.Crypt;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.dao.GroupResourceDao;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.office.OfficeFileModel;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.exception.PermissionsException;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

@Controller
@RequestMapping(value = "/office")
public class OfficeController {
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Permission permission;

    @Autowired
    GroupResourceDao resourceDao;


    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/preview", produces = "application/json; charset=UTF-8")
    public String downloadResource(Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        OfficeFileModel model = null;
        try {
            File downFile = resourceService.getDownloadResource(new Long[]{id});
            String targetPath = DocumentManager.FileRootPath(resourceBean.getGroupName());
            model = new OfficeFileModel(filename, null, "20", resourceBean.getMemberName(), resourceBean.getGroupName());
            File targetFile = new File(targetPath + "/" + filename);
            DocumentManager.createMeta(filename, resourceBean.getMemberName(), resourceBean.getGroupName());
            Crypt.fileDecrypt(downFile, targetFile);
        } catch (Exception e) {
            System.out.println(e.toString());
            return "{\"type\":\"error\",\"code\":\"300\", \"detail\": \"" + e.toString() + "\"}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{" + "\"model\":" + model.toString() + ",");
        sb.append("\"documentServerUrl\":\"" + OfficeProperty.getUrlApi() + "\"" + "}");
        System.out.println(model.toString());
        return sb.toString();
    }

    @RequiresUser
    @RequestMapping(value = "/save", produces = "application/json;charset=UTF-8")
    public void saveOfficeFile(Long id, HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
        if (id == null) {
            throw new GroupsException("资源id为空");
        }

        PrintWriter writer = response.getWriter();

        InputStream requestStream = request.getInputStream();

        String body = getBody(requestStream);

        GroupResource resourceBean = resourceService.getResourceById(id);

        JSONObject parse = new JSONObject();

        JSONObject jsonObj = parse.getJSONObject(body);

        String targetPath = DocumentManager.FileRootPath(resourceBean.getGroupName());

        String fileName = resourceBean.getName();

        if (jsonObj.getInt("status") == 2) {

            String downloadUri = jsonObj.getString("url");

            URL url = new URL(downloadUri);

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();

            InputStream stream = connection.getInputStream();

            File savedFile = new File(new URI(targetPath + fileName));

            try (FileOutputStream out = new FileOutputStream(savedFile)) {
                int read;
                final byte[] bytes = new byte[1024];
                while ((read = stream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                out.flush();
            }

            connection.disconnect();
        }
        writer.write("{\"error\":0}");
    }

    private String getBody(InputStream is) {
        Scanner scanner = null;
        Scanner scannerUseDelimiter = null;
        try {
            scanner = new Scanner(is);
            scannerUseDelimiter = scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } finally {
            scannerUseDelimiter.close();
            scanner.close();
        }
    }


}
