package com.dcampus.common.config;

import java.io.InputStream;
import java.util.Properties;

public class OfficeProperty {

    private static Properties properties;
    private static String storageFolder;
    private static String viewedDocs;
    private static String editedDocs;
    private static String convertDocs;
    private static String timeout;
    private static String urlConverter;
    private static String tempStorage;
    private static String urlApi;
    private static String urlPreloader;

    static {
        try{
            properties = new Properties();
            InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("office.properties");
            properties.load(stream);
            storageFolder = properties.getProperty("storage-folder");
            viewedDocs = properties.getProperty("files.docservice.viewed-docs");
            editedDocs = properties.getProperty("files.docservice.edited-docs");
            convertDocs = properties.getProperty("files.docservice.convert-docs");
            timeout = properties.getProperty("files.docservice.timeout");
            urlConverter = properties.getProperty("files.docservice.url.converter");
            tempStorage = properties.getProperty("files.docservice.url.tempstorage");
            urlApi = properties.getProperty("files.docservice.url.api");
            urlPreloader = properties.getProperty("files.docservice.url.preloader");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static String getStorageFolder() {
        return storageFolder;
    }

    public static String getViewedDocs() {
        return viewedDocs;
    }

    public static String getEditedDocs() {
        return editedDocs;
    }

    public static String getConvertDocs() {
        return convertDocs;
    }

    public static String getTimeout() {
        return timeout;
    }

    public static String getUrlConverter() {
        return urlConverter;
    }

    public static String getTempStorage() {
        return tempStorage;
    }

    public static String getUrlApi() {
        return urlApi;
    }

    public static String getUrlPreloader() {
        return urlPreloader;
    }
}
