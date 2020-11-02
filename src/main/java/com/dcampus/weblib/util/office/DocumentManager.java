package com.dcampus.weblib.util.office;

import com.dcampus.common.config.OfficeProperty;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DocumentManager {
    private static HttpServletRequest request;

    /**
     * 初始化
     *
     * @param req
     */
    public static void init(HttpServletRequest req) {
        request = req;
    }


    public static List<String> GetFileExts() {
        List<String> res = new ArrayList<>();

        res.addAll(GetViewedExts());
        res.addAll(GetEditedExts());
        res.addAll(GetConvertExts());

        return res;
    }

    public static List<String> GetViewedExts() {
        String exts = OfficeProperty.getViewedDocs();
        return Arrays.asList(exts.split("\\|"));
    }

    public static List<String> GetEditedExts() {
        String exts = OfficeProperty.getEditedDocs();
        return Arrays.asList(exts.split("\\|"));
    }

    public static List<String> GetConvertExts() {
        String exts = OfficeProperty.getConvertDocs();
        return Arrays.asList(exts.split("\\|"));
    }


    /**
     * 根据文件名和groupID获取文件url
     *
     * @param filename
     * @param groupID
     * @return
     */
    public static String getFileUrl(String filename, String groupID) {
        String serverPath = getServerUrl();
        String storagePath = OfficeProperty.getStorageFolder();
        String path = serverPath + "/" + storagePath + "/" + groupID + "/" + filename;
        return path;
    }

    /**
     * 获取当前用户的ip地址
     *
     * @param userAddress
     * @return
     */
    public static String CurUserHostAddress(String userAddress) {
        if (userAddress == null) {
            try {
                userAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
                userAddress = "";
                ex.printStackTrace();
            }
        }
        return userAddress.replaceAll("[^0-9a-zA-Z.=]", "_");
    }

    public static String getServerUrl() {
        return request.getScheme() + "://" + CurUserHostAddress(null) + ":" + request.getServerPort() + request.getContextPath();
    }

    public static String getCallback(Long id) {
        String serverPath = getServerUrl();
        try {
            String query = "?id=" + URLEncoder.encode(String.valueOf(id), java.nio.charset.StandardCharsets.UTF_8.toString());
            return serverPath + "/office/track" + query;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String FileRootPath(String groupID) {
        String fileRootPath = null;
        try {
            fileRootPath = String.valueOf(Thread.currentThread().getContextClassLoader().getResource("").toURI());
            String directory = fileRootPath + "/static/" + OfficeProperty.getStorageFolder() + "/" + groupID + "/";
            File file = new File(new URI(directory));
            if (!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String StoragePath(String filename, String groupID) {
        String filePath = FileRootPath(groupID);
        return filePath + "/" + filename;
    }

    public static String HistoryDir(String storagePath) {
        return storagePath + "hist";
    }

    public static Integer GetFileVersion(String historyPath)
    {
        File dir = new File(historyPath);

        if (!dir.exists()) return 0;

        File[] dirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        return dirs.length;
    }

    public static String VersionDir(String histPath, Integer version)
    {
        return histPath + File.separator + Integer.toString(version);
    }

    public static String GenerateRevisionId(String expectedKey) {
        if (expectedKey.length() > 20)
            expectedKey = Integer.toString(expectedKey.hashCode());

        String key = expectedKey.replace("[^0-9-.a-zA-Z_=]", "_");

        return key.substring(0, Math.min(key.length(), 20));
    }

    /**
     * 用于创建office文件的版本控制
     *
     * @param fileName
     * @param uid
     * @param groupID
     * @throws Exception
     */
    public static void createMeta(String fileName, String uid, String groupID) throws Exception {
        String histDir = FileRootPath(groupID) + "/" + fileName + "-hist";
        File file = new File(histDir);
        if (!file.exists()) {
            file.mkdir();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("create", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        jsonObject.put("uid", (uid == null || uid.isEmpty()) ? "uid-1" : uid);
        jsonObject.put("groupID", (groupID == null || groupID.isEmpty()) ? "default-user" : groupID);
        File meta = new File(histDir + "/" + "createdInfo.json");
        try (FileWriter writer = new FileWriter(meta)) {
            jsonObject.write(writer);
        }
    }
}
