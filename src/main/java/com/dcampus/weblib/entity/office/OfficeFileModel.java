package com.dcampus.weblib.entity.office;

import com.dcampus.weblib.util.office.DocumentManager;
import com.dcampus.weblib.util.office.OfficeFileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * 该类是only office的文件配置类，用于配置打开文件时的基本属性
 */
public class OfficeFileModel {
    public String type = "desktop";
    public String mode = "edit";
    public String documentType;
    public Document document;
    public EditorConfig editorConfig;
    public String token;

    public OfficeFileModel(String fileName, String lang, String uid, String uname, String groupID) {
        if (fileName == null) fileName = "";
        fileName = fileName.trim();

        documentType = OfficeFileUtil.GetFileType(fileName).toString().toLowerCase();

        document = new Document();
        document.title = fileName;
        document.url = DocumentManager.getFileUrl(fileName, groupID);
//        document.url = "http://211.66.87.5:9000/app_data/127.0.1.1/new.docx";
        document.fileType = OfficeFileUtil.GetFileExtension(fileName).replace(".", "");
        document.key = DocumentManager.GenerateRevisionId(DocumentManager.CurUserHostAddress(null) + "/" + fileName + "/" + Long.toString(new File(DocumentManager.StoragePath(fileName, null)).lastModified()));

        editorConfig = new EditorConfig();
//        editorConfig.callbackUrl = DocumentManager.getLocalCallback(fileName);
        editorConfig.callbackUrl = "";
        editorConfig.lang = lang == null ? editorConfig.lang : lang;

        if (uid != null) editorConfig.user.id = uid;
        if (uname != null) editorConfig.user.name = uname;

        editorConfig.customization.goback.url = "";


        changeType(mode, type);
    }


    public void changeType(String _mode, String _type) {
        if (_mode != null) mode = _mode;
        if (_type != null) type = _type;

        Boolean canEdit = DocumentManager.GetEditedExts().contains(OfficeFileUtil.GetFileExtension(document.title));

        editorConfig.mode = canEdit && !mode.equals("view") ? "edit" : "view";

        document.permissions = new Permissions(mode, type, canEdit);

        if (type.equals("embedded")) InitDesktop();
    }

    public void InitDesktop() {
        editorConfig.InitDesktop(document.url);
    }

    //    public String[] GetHistory()
//    {
//        JSONParser parser = new JSONParser();
//        String histDir = DocumentManager.HistoryDir(DocumentManager.StoragePath(document.title, null));
//        if (DocumentManager.GetFileVersion(histDir) > 0) {
//            Integer curVer = DocumentManager.GetFileVersion(histDir);
//
//            Set<Object> hist = new HashSet<Object>();
//            Map<String, Object> histData = new HashMap<String, Object>();
//
//            for (Integer i = 0; i <= curVer; i++) {
//                Map<String, Object> obj = new HashMap<String, Object>();
//                Map<String, Object> dataObj = new HashMap<String, Object>();
//                String verDir = DocumentManager.VersionDir(histDir, i + 1);
//
//                try {
//                    String key = null;
//
//                    key = i == curVer ? document.key : readFileToEnd(new File(verDir + File.separator + "key.txt"));
//
//                    obj.put("key", key);
//                    obj.put("version", i);
//
//                    if (i == 0) {
//                        String createdInfo = readFileToEnd(new File(histDir + File.separator + "createdInfo.json"));
//                        JSONObject json = (JSONObject) parser.parse(createdInfo);
//
//                        obj.put("created", json.get("created"));
//                        Map<String, Object> user = new HashMap<String, Object>();
//                        user.put("id", json.get("id"));
//                        user.put("name", json.get("name"));
//                        obj.put("user", user);
//                    }
//
//                    dataObj.put("key", key);
//                    dataObj.put("url", i == curVer ? document.url : DocumentManager.GetPathUri(verDir + File.separator + "prev" + FileUtility.GetFileExtension(document.title)));
//                    dataObj.put("version", i);
//
//                    if (i > 0) {
//                        JSONObject changes = (JSONObject) parser.parse(readFileToEnd(new File(DocumentManager.VersionDir(histDir, i) + File.separator + "changes.json")));
//                        JSONObject change = (JSONObject) ((JSONArray) changes.get("changes")).get(0);
//
//                        obj.put("changes", changes.get("changes"));
//                        obj.put("serverVersion", changes.get("serverVersion"));
//                        obj.put("created", change.get("created"));
//                        obj.put("user", change.get("user"));
//
//                        Map<String, Object> prev = (Map<String, Object>) histData.get(Integer.toString(i - 1));
//                        Map<String, Object> prevInfo = new HashMap<String, Object>();
//                        prevInfo.put("key", prev.get("key"));
//                        prevInfo.put("url", prev.get("url"));
//                        dataObj.put("previous", prevInfo);
//                        dataObj.put("changesUrl", DocumentManager.GetPathUri(DocumentManager.VersionDir(histDir, i) + File.separator + "diff.zip"));
//                    }
//
//                    hist.add(obj);
//                    histData.put(Integer.toString(i), dataObj);
//
//                } catch (Exception ex) { }
//            }
//
//            Map<String, Object> histObj = new HashMap<String, Object>();
//            histObj.put("currentVersion", curVer);
//            histObj.put("history", hist);
//
//            Gson gson = new Gson();
//            return new String[] { gson.toJson(histObj), gson.toJson(histData) };
//        }
//        return new String[] { "", "" };
//    }
    private String readFileToEnd(File file) {
        String output = "";
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                Scanner scanner = new Scanner(is);
                scanner.useDelimiter("\\A");
                while (scanner.hasNext()) {
                    output += scanner.next();
                }
                scanner.close();
            }
        } catch (Exception e) {
        }
        return output;
    }

    /**
     * Document类允许更改与文档相关的所有参数（标题、url、文件类型等）.
     * title:所要查看或编辑的文档的文件名，在下载文档时，该文件名也将用作文件名。
     * url:所要查看或编辑的文档的绝对URL。
     * fileType:为查看或编辑的源文件定义文件类型。
     * key:用于服务识别文档的唯一文档标识符。
     */
    public class Document {
        public String title;
        public String url;
        public String fileType;
        public String key;
        public Permissions permissions;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"title\":\"" + title + "\",");
            sb.append("\"url\":\"" + url + "\",");
            sb.append("\"fileType\":\"" + fileType + "\",");
            sb.append("\"key\":\"" + key + "\",");
            sb.append("\"permissions\":" + permissions.toString() + "");
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * Permissions类配置文档的相关权限
     * comment：是否可以对文档进行注释
     * download：文档是否可以下载或仅在线查看或编辑
     * edit：是否可以编辑或仅查看文档
     * fillForms：是否可以填写表格
     * modifyFilter：定义过滤器是否可以全局（true）应用到影响所有其他用户，或局部（false）应用，即仅适用于当前用户
     * modifyContentControl：定义是否可以更改内容控件设置
     * review：定义是否可以查看文档
     */
    public class Permissions {
        public Boolean comment;
        public Boolean download;
        public Boolean edit;
        public Boolean fillForms;
        public Boolean modifyFilter;
        public Boolean modifyContentControl;
        public Boolean review;

        public Permissions(String mode, String type, Boolean canEdit) {
            comment = !mode.equals("view") && !mode.equals("fillForms") && !mode.equals("embedded") && !mode.equals("blockcontent");
            download = true;
            edit = canEdit && (mode.equals("edit") || mode.equals("filter") || mode.equals("blockcontent"));
            fillForms = !mode.equals("view") && !mode.equals("comment") && !mode.equals("embedded") && !mode.equals("blockcontent");
            modifyFilter = !mode.equals("filter");
            modifyContentControl = !mode.equals("blockcontent");
            review = mode.equals("edit") || mode.equals("review");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"comment\":" + comment + ",");
            sb.append("\"download\":" + download + ",");
            sb.append("\"edit\":" + edit + ",");
            sb.append("\"fillForms\":" + fillForms + ",");
            sb.append("\"modifyFilter\":" + modifyFilter + ",");
            sb.append("\"modifyContentControl\":" + modifyContentControl + ",");
            sb.append("\"review\":" + review);
            sb.append("}");
            return sb.toString();
        }
    }

    /**
     * EditorConfig类允许更改与编辑器界面有关的参数：打开模式（查看器或编辑器），界面语言，其他按钮等）。
     * mode:定义编辑器的打开模式，默认：edit
     * callbackUrl:指定文档存储服务的绝对URL，该配置用于文件保存
     * lang:编辑器界面语言
     * user:当前正在查看或编辑文档的用户
     * Customization:本部分允许自定义编辑器界面
     */
    public class EditorConfig {
        public String mode = "edit";
        public String callbackUrl;
        public String lang = "zh-CN";
        public User user;
        public Customization customization;
        public Embedded embedded;

        public EditorConfig() {
            user = new User();
            customization = new Customization();
            embedded = new Embedded();
        }

        public void InitDesktop(String url) {
            embedded = new Embedded();
            embedded.saveUrl = url;
            embedded.embedUrl = url;
            embedded.shareUrl = url;
            embedded.toolbarDocked = "top";
        }

        public class User {
            public String id = "uid-1";
            public String name = "John Smith";

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"id\":\"" + id + "\",");
                sb.append("\"name\":\"" + name + "\"");
                sb.append("}");
                return sb.toString();
            }
        }

        /**
         * Customization类允许自定义编辑器界面
         */
        public class Customization {
            public Goback goback;

            public Customization() {
                goback = new Goback();
            }

            public class Goback {
                public String url;
            }
        }

        /**
         * Embedded类仅适用于嵌入式文档类型，它允许更改定义嵌入式模式下按钮行为的设置。
         * saveUrl:定义允许将文档保存到用户个人计算机上的绝对URL。
         * embedUrl:定义文档的绝对URL，以作为嵌入到网页中的文档的源文件
         * shareUrl:定义允许其他用户共享此文档的绝对URL。
         * toolbarDocked:定义嵌入式查看器工具栏的位置，可以在顶部或底部。
         */
        public class Embedded {
            public String saveUrl;
            public String embedUrl;
            public String shareUrl;
            public String toolbarDocked;

            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"saveUrl\":" + (saveUrl == null ? "\"\"" : saveUrl) + ",");
                sb.append("\"embedUrl\":" + (embedUrl == null ? "\"\"" : embedUrl) + ",");
                sb.append("\"shareUrl\":" + (shareUrl == null ? "\"\"" : shareUrl) + ",");
                sb.append("\"toolbarDocked\":" + (toolbarDocked == null ? "\"\"" : toolbarDocked));
                sb.append("}");
                return sb.toString();
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"mode\":\"" + mode + "\",");
            sb.append("\"callbackUrl\":\"" + callbackUrl + "\",");
            sb.append("\"lang\":\"" + lang + "\",");
            sb.append("\"user\":" + user.toString() + ",");
            sb.append("\"customization\":" + "\"\"" + ",");
            sb.append("\"embedded\":" + (embedded == null ? "\"\"" : embedded));
            sb.append("}");
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"document\":" + document.toString() + ",");
        sb.append("\"documentType\":\"" + documentType + "\",");
        sb.append("\"editorConfig\":" + editorConfig.toString() + ",");
        sb.append("\"mode\":\"" + mode + "\",");
        sb.append("\"type\":\"" + type + "\"");
        sb.append("}");
        return sb.toString();

    }
}
