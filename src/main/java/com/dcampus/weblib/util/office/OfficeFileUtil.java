package com.dcampus.weblib.util.office;

import com.dcampus.common.config.OfficeProperty;
import com.dcampus.weblib.entity.office.FileType;

import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfficeFileUtil {

    /**
     * 这里把Document的可预览文件扩展名列出来
     */
    private static List<String> ExtsDocument = Arrays.asList
            (
                    ".doc", ".docx", ".docm",
                    ".dot", ".dotx", ".dotm",
                    ".odt", ".fodt", ".rtf", ".txt",
                    ".html", ".htm", ".mht",
                    ".pdf", ".djvu", ".fb2", ".epub", ".xps"
            );

    /**
     * 这里把Spreadsheet的可预览文件扩展名列出来
     */
    private static List<String> ExtsSpreadsheet = Arrays.asList
            (
                    ".xls", ".xlsx", ".xlsm",
                    ".xlt", ".xltx", ".xltm",
                    ".ods", ".fods", ".csv"
            );

    /**
     * 这里把Presentation的可预览文件扩展名列出来
     */
    private static List<String> ExtsPresentation = Arrays.asList
            (
                    ".pps", ".ppsx", ".ppsm",
                    ".ppt", ".pptx", ".pptm",
                    ".pot", ".potx", ".potm",
                    ".odp", ".fodp"
            );


    /**
     * 返回文件类型
     * 通过完整的文件名获取文件类型，这里的文件名是包含文件后缀名的
     * @param fileName
     * @return FileType
     */
    public static FileType GetFileType(String fileName)
    {
        String ext = GetFileExtension(fileName).toLowerCase();

        if (ExtsDocument.contains(ext))
            return FileType.Text;

        if (ExtsSpreadsheet.contains(ext))
            return FileType.Spreadsheet;

        if (ExtsPresentation.contains(ext))
            return FileType.Presentation;

        return FileType.Text;
    }

    /**
     * 返回文件名
     * 通过文件url获取文件名，这里的文件名包括文件后缀
     * @param url
     * @return
     */
    public static String GetFileName(String url)
    {
        if (url == null) return null;
        String tempstorage = OfficeProperty.getTempStorage();
        if (!tempstorage.isEmpty() && url.startsWith(tempstorage))
        {
            Map<String, String> params = GetUrlParams(url);
            return params == null ? null : params.get("filename");
        }

        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName;
    }

    /**
     * 通过url获取文件名，不包含文件后缀
     * @param url
     * @return
     */
    public static String GetFileNameWithoutExtension(String url)
    {
        String fileName = GetFileName(url);
        if (fileName == null) return null;
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileNameWithoutExt;
    }

    /**
     * 通过url获取文件扩展
     * @param url
     * @return
     */
    public static String GetFileExtension(String url)
    {
        String fileName = GetFileName(url);
        if (fileName == null) return null;
        String fileExt = fileName.substring(fileName.lastIndexOf("."));
        return fileExt.toLowerCase();
    }

    public static Map<String, String> GetUrlParams(String url)
    {
        try
        {
            String query = new URL(url).getQuery();
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<>();
            for (String param : params)
            {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            }
            return map;
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}
