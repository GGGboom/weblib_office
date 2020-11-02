package com.dcampus.weblib.service;

import com.dcampus.common.generic.GenericDao;
import com.dcampus.common.util.Crypt;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.util.Log;
import com.dcampus.sys.entity.User;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupExtern;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.exception.GroupsException;
import com.dcampus.weblib.util.FileUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;

@EnableAsync
@Service
@Transactional(readOnly = false)
public class AsyncService {

    private static final int DEFAULT_BUFFER_SIZE = 1024*1024;//4M

    private static final int EOF = -1;

    private static final byte[] EncryptFlag = "20ENCRYPTbyWEBLIBsys15Yp".getBytes();

    public static final int EncryptLen = EncryptFlag.length*2;

    public static final int EncryptKeyLen = EncryptFlag.length;

    public static final byte[] KEY = new byte[]{1,0,1,1,1,0,1,0};

    private static Log logger = Log.getLog(FileUtil.class);
    public  static Map<Long,  List<Long>> solveMaps = new HashedMap();
    public  List<Long> i = new ArrayList<Long>();


    @Autowired
    @Lazy
    private  ResourceService resourceService;

    //根据id获取map的值

    /**
     * 保存文件到服务器
     *
     * @param file
     * @param uriPath
     * @throws GroupsException
     */
    @Async
    public  Future<Boolean>  copyFileToServer(Long id , File file, GroupResource resource, String uriPath,
                                              boolean ingoreEncrypt) throws GroupsException {
        long count = 0L;

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        Long inputSize=0L;
        try {
//            System.out.println("准备异步复制文件到服务器成功");
            inputStream = new FileInputStream(file);
            inputSize =Long.parseLong(resource.getDetailSize());
            i.add(count);
            i.add(inputSize);
            solveMaps.put(id,i);
//            System.out.println(solveMaps.get(id));
            File targetFile = new File(new URI(uriPath));
            outputStream = new FileOutputStream(targetFile, true);
            if (ingoreEncrypt) {
                int n= 0;
                Long cc=0L;
//                IOUtils.copy(inputStream, outputStream);
                for(boolean var5 = false; -1 != (n = inputStream.read(new byte[4096])); count += (long)n) {
                    outputStream.write(new byte[4096], 0, n);
                    //放入map()

//                    if(i!=null)
//                        i.clear();
////                    i.add(count);
                    cc+=n;
//                    i.add(cc);
//                    i.add(inputSize);
//                    if(cc==inputSize)//上传完成了
                    solveMaps.put(id,Arrays.asList(cc,inputSize));
//                    solveMaps.put(id,i);
                }
            } else {
//                System.out.println("aaaaaa");
                fileEncrypt(id,inputStream, outputStream,resource.getDetailSize());
            }
        } catch (Exception e) {
            i.add((long)-1);
            i.add(inputSize);
            solveMaps.put(id,i);
            e.printStackTrace();
            if (e instanceof FileNotFoundException)
                throw new GroupsException("找不到待复制文件" );
            else
                throw new GroupsException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
            resource.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
            resourceService.modifyResource(resource);
            if(file.exists()) {
                file.delete();
            }
            System.out.println("异步复制文件到服务器成功");

        }
        return new AsyncResult<Boolean>(true);
    }

    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }
    private static byte[] seed(byte[] keys) {
        int e = EncryptLen;
        int enIndex = 0;
        int index = 0;
        byte[] results = new byte[e];
        for(int x = 0 ; x < e ; x++) {
            if (x%2 == 1) {
                results[x] = EncryptFlag[enIndex++];
            } else {
                int ran = (int)(Math.random()*10)%2;
                results[x] = (byte)ran;//随机
                keys[index++] = results[x];
            }
        }
        return results;
    }
    public static void en(byte[] data, byte[] keys){
        en(data, keys, 0);
    }

    public static void en(byte[] data, byte[] keys,int byteStart){
        int keyIndex = byteStart ;
        for(int x = 0 ; x < data.length ; x++) {
            data[x] = (byte)(data[x] ^ keys[keyIndex]);
            if (++keyIndex == keys.length){
                keyIndex = 0;
            }
        }
    }

    @Async
    public  void fileEncrypt(Long id,InputStream input, OutputStream output,String detailSize) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        byte[] keys = new byte[EncryptKeyLen];
        byte[] seed = seed(keys);
        output.write(seed, 0, seed.length);
        byte[] k = byteMerger(keys, KEY);
        Long inputSize=Long.parseLong(detailSize);;
        Long count=0L;
        while (EOF != (n = input.read(buffer))) {
            en(buffer, k);
            output.write(buffer, 0, n);
            //放入map
//            if(i!=null)
//                i.clear();
//            count+=n;
//            i.add(count);
//            i.add(inputSize);
//            solveMaps.put(id,i);
            count+=n;
            System.out.println(count);
            solveMaps.put(id,Arrays.asList(count,inputSize));
            System.out.println(solveMaps.get(id));
        }

    }
}
