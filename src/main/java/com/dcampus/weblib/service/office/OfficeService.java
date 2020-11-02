package com.dcampus.weblib.service.office;

import com.dcampus.weblib.dao.GroupResourceDao;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.service.ResourceService;
import com.dcampus.weblib.service.permission.impl.Permission;
import com.dcampus.weblib.util.office.DocumentManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.Scanner;

@Service
@Transactional(readOnly = false)
public class OfficeService {
    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Permission permission;

    @Autowired
    GroupResourceDao resourceDao;

    public String getBody(InputStream is) {
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

    public void downloadFile(String url, File file) throws Exception {
        if (url == null || url.isEmpty()) throw new Exception("argument url");
        URL uri = new URL(url);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) uri.openConnection();
        InputStream is = connection.getInputStream();
        if (is == null) throw new Exception("Stream is null");
        try (FileOutputStream os = new FileOutputStream(file)) {
            byte[] bytes = new byte[1024];
            int read;
            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
        }
        connection.disconnect();
    }

    public void saveChanges(JSONObject jsonObject, HttpServletRequest request, GroupResource resource) throws Exception {
        DocumentManager.init(request);
        String changesUri = (String) jsonObject.get("changesurl");
        String key = (String) jsonObject.get("key");

        String histDir = DocumentManager.StoragePath(resource.getFilePath(),resource.getGroupName());
        String versionDir = DocumentManager.VersionDir(histDir, DocumentManager.GetFileVersion(histDir) + 1);
        String history = (String) jsonObject.get("changeshistory");
        if (history == null) {
            history = ((JSONObject) jsonObject.get("history")).toString();
        }
        if (history != null && !history.isEmpty()) {
            FileWriter fw = new FileWriter(new File(versionDir + File.separator + "changes.json"));
            fw.write(history);
            fw.close();
        }
        downloadFile(changesUri, new File(versionDir + File.separator + "diff.zip"));

        FileWriter fw = new FileWriter(new File(versionDir + File.separator + "key.txt"));
        fw.write(key);
        fw.close();
    }
}
