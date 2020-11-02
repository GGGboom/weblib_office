package com.dcampus.weblib.web.action;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dcampus.weblib.dao.GroupDao;
import com.dcampus.weblib.exception.PermissionsException;
import com.dcampus.weblib.service.*;
import com.dcampus.weblib.service.permission.IPermission;
import com.dcampus.weblib.util.*;
import net.sf.json.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.dcampus.common.config.PropertyUtil;
import com.dcampus.common.paging.AndSearchTerm;
import com.dcampus.common.paging.AscSortItem;
import com.dcampus.common.paging.DescSortItem;
import com.dcampus.common.paging.OrSearchTerm;
import com.dcampus.common.paging.PageNavigater;
import com.dcampus.common.paging.PageTerm;
import com.dcampus.common.paging.SearchItem;
import com.dcampus.common.paging.SearchTerm;
import com.dcampus.common.paging.SortTerm;
import com.dcampus.common.util.HTML;
import com.dcampus.common.util.JS;
import com.dcampus.common.web.BaseController;
import com.dcampus.sys.entity.User;
import com.dcampus.sys.service.UserService;
import com.dcampus.sys.util.UserUtils;
import com.dcampus.weblib.entity.Category;
import com.dcampus.weblib.entity.Group;
import com.dcampus.weblib.entity.GroupResource;
import com.dcampus.weblib.entity.GroupResourceReceive;
import com.dcampus.weblib.entity.GroupResourceShare;
import com.dcampus.weblib.entity.Member;
import com.dcampus.weblib.entity.ShareResponse;
import com.dcampus.weblib.entity.ShareWrap;
import com.dcampus.weblib.entity.keys.ResourceSearchItemKey;
import com.dcampus.weblib.entity.keys.ResourceSortItemKey;
import com.dcampus.weblib.exception.GroupsException;

@Controller
@RequestMapping(value = "/group")
public class ResourceController extends BaseController {

    private static final Logger logger = Logger.getLogger(ResourceController.class);
    private JsonUtil jsonUtil = new JsonUtil();
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private PermissionService permService;
    @Autowired
    private ResourceService resourceService;
    @Autowired
    private GrouperService grouperService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private LogService logService;


    @Autowired
    private GroupDao groupDao;

    @Autowired
    private helpAsyncService helpAsyncService;

    @Autowired
    private AsyncService asyncService;

    /**
     * 创建资源文件夹
     *
     * @param groupId       柜子id
     * @param parentId      父目录id
     * @param name          文件夹名字
     * @param applicationId
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/createDir", "/createDir_v2"}, produces = "application/json; charset=UTF-8")
    public String createDir(Long groupId, Long parentId, String name, String applicationId) {
        StringBuffer buffer = new StringBuffer();

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        if (parentId < 0) {
            parentId = 0L;
        }
        if (name == null || name == "") {
            name = "新建文件夹";
        }
        Group group = groupService.getGroupById(groupId);
        Member creator = grouperService.getMemberById(memberId.longValue());
        if (group == null || group.getId() <= 0) {
            throw new GroupsException("柜子不存在！");
        }

        GroupResource bean = new GroupResource();
        bean.setGroup(group);
        bean.setName(name);
        bean.setParentId(parentId);
        if (applicationId != null) {
            bean.setApplicationId(applicationId);
        }
        bean.setCreatorId(memberId);
        bean.setDocumentTypeValue(GroupResource.DOCUMENT_TYPE_UNKNOWN);
        bean.setResourceType(GroupResource.RESOURCE_TYPE_DIRECTORY);
        resourceService.createResourceDir(bean, false);

        buffer.append("{");
        buffer.append("\"id\":").append(bean.getId()).append(",");
        buffer.append("\"groupId\":").append(group.getId()).append(",");
        buffer.append("\"owner\":").append(bean.getCreatorId() == memberId.longValue()).append(",");

        String n = bean.getName();
        if (bean.getFilePreName() != null && n != null
                && !n.startsWith(bean.getFilePreName())) {
            n = bean.getOriginalName();
        }
        String displayName = JS.quote(HTML.escape(n));
        buffer.append("\"displayName\":\"").append(displayName).append("\",");
        String desc = JS.quote(HTML.escape(bean.getDesc()));
        buffer.append("\"desc\":\"").append(desc == null ? "" : desc).append("\",");
        buffer.append("\"checkCode\":\"").append(bean.getCheckCode() == null ? "" : bean.getCheckCode()).append("\",");
        buffer.append("\"priority\":").append((int) bean.getPriority()).append(",");
        buffer.append("\"documentType\":").append(bean.getDocumentTypeValue()).append(",");
        if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
            String linkPath = JS.quote(HTML.escape(bean.getLinkPath() == null ? "" : bean.getLinkPath()));
            buffer.append("\"linkPath\":\"").append(linkPath).append("\",");
        }
        buffer.append("\"paiban\":\"").append(bean.getPaiban() == null ? "" : bean.getPaiban()).append("\",");
        buffer.append("\"size\":").append(bean.getSize()).append(",");
        buffer.append("\"contentType\":\"").append(bean.getContentType()).append("\",");
        String creationDate = bean.getCreateDate().toString();
        buffer.append("\"creationDate\":\"")
                .append(creationDate.substring(0, creationDate.length() - 2))
                .append("\",");
        buffer.append("\"type\":").append(bean.getResourceType()).append(",");
        buffer.append("\"leaf\":").append(true);
        buffer.append("}");
        return buffer.toString();
    }

    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/createDirByPath", produces = "application/json; charset=UTF-8")
    public String createDirByPath(String name, String path, String applicationId) throws Exception {
        Long memberId = UserUtils.getCurrentMemberId();
        Group agroup = null;
        if (name == null)
            throw new GroupsException("找不到要上传的文件柜");
        Category personCategory = categoryService.getCategoriesByName(
                PropertyUtil.getDefaultPersonGroupCategory()).get(0);
        agroup = groupService.getGroupByDisplyName(personCategory.getId(), name);
        if (agroup == null) {
            throw new GroupsException("找不到该用户的文件柜");
        }
        long parentId = 0L;
        // 若传进来的不是parentId是path的话，则查找该路径所在的文件夹的id
        if (path == null) {
            throw new GroupsException("路径不能为空");
        }
        long temp = 0;
        String[] path_temp = path.split("/");
        for (int i = 1; i < path_temp.length; i++) {
            GroupResource temp_resource = resourceService.getResource(agroup.getId(), path_temp[i], parentId, GroupResource.RESOURCE_TYPE_DIRECTORY);
            if (temp_resource == null) {
                GroupResource bean = new GroupResource();
                bean.setGroup(agroup);
                bean.setName(path_temp[i]);
                bean.setParentId(parentId);
                if (applicationId != null) {
                    bean.setApplicationId(applicationId);
                }
                bean.setCreatorId(memberId);
                bean.setDocumentTypeValue(GroupResource.DOCUMENT_TYPE_UNKNOWN);
                bean.setResourceType(GroupResource.RESOURCE_TYPE_DIRECTORY);
                temp = resourceService.createResourceDir(bean, false);
                parentId = temp;
            } else {
                parentId = temp_resource.getId();
            }
        }

        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }


    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/getResources", "/getResources_v2", "/getSimpleResources"}, produces = "application/json; charset=UTF-8")
    public String getResources(Long parentId, String type, Integer start, Integer limit, Boolean back, String orderBy) throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        if (back == null) {
            back = false;
        }
        if (parentId == null || parentId == 0) {
            throw new GroupsException("parentId不能为空");
        }
        start = start == null ? 0 : start.intValue();
        limit = limit == null ? Integer.MAX_VALUE : limit.intValue();
        type = type == null ? "all" : type;

        List<GroupResource> resourceBeans = new ArrayList<GroupResource>();
        long totalCount = 0;

        // 获取路径
        List<GroupResource> list = new ArrayList<GroupResource>();
        if (parentId > 0) {
            long pid = parentId;

            while (true) {
                GroupResource bean = resourceService.getResourceById(pid);
                list.add(bean);
                pid = bean.getParentId();
                if (pid <= 0)
                    break;

            }

            // 如果是向上回溯，需要去掉第一个元素，因为parentId的parentId才是当前目录
            if (back && list.size() > 0)
                list.remove(0);
        }

        // 获取groupId = -parentId的第一层资源列表
        long groupId = 0;
        if (parentId <= 0) {
            groupId = -parentId;
            resourceBeans = resourceService.getResourcesByGroup(groupId, start, limit, true, orderBy);
            totalCount = resourceService.getTopResourcesAmountByGroup(groupId);

        }

        // 回溯到上一层资源列表
        if (parentId > 0 && back) {
            GroupResource bean = resourceService.getResourceById(parentId);
            groupId = bean.getGroup().getId();

            parentId = bean.getParentId();
            if (parentId <= 0) {
                // parentId设置成-groupId
                parentId = -bean.getParentId();

                resourceBeans = resourceService.getResourcesByGroup(groupId, start, limit, true, orderBy);
                totalCount = resourceService.getTopResourcesAmountByGroup(groupId);

            } else {
                resourceBeans = resourceService.getResourcesByParent(parentId, null, start, limit, orderBy);
                totalCount = resourceService.getResourcesAmountByParent(parentId);
            }
        }

        // 进入下一层资源列表
        if (parentId > 0 && !back) {
            GroupResource bean = resourceService.getResourceById(parentId);
            groupId = bean.getGroup().getId();
            resourceBeans = resourceService.getResourcesByParent(parentId, null, start, limit, orderBy);
            totalCount = resourceService.getResourcesAmountByParent(parentId);
        }

        GroupResource recycler = resourceService.getRecycler(groupId);

        // ////////////////////////////////////////////////////////////////////////////////////////

        // 是否有下级节点 20141201_fzd/kjhe新的获取方法
        List<GroupResource> resourceChildBeans_temp = resourceService.getAllChildBeans(resourceBeans);
        boolean[] leaf = new boolean[resourceBeans.size()];
        boolean[] hasChildFolder = new boolean[resourceBeans.size()];
        if (resourceChildBeans_temp != null) {
            GroupResource[] resourceChildBeans = (GroupResource[]) resourceChildBeans_temp
                    .toArray(new GroupResource[resourceChildBeans_temp
                            .size()]);

            Set<Long> leafSet = new HashSet<Long>();
            Set<Long> childFolderSet = new HashSet<Long>();

            for (GroupResource beans : resourceChildBeans) {

                if (!leafSet.contains(beans.getParentId())) {
                    leafSet.add(beans.getParentId());
                }
                if (beans.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY
                        && !childFolderSet.contains(beans.getParentId())) {
                    childFolderSet.add(beans.getParentId());
                }

            }

            for (int i = 0; i < leaf.length; ++i) {
                if (leafSet.contains(resourceBeans.get(i).getId())) {
                    leaf[i] = false;
                } else {
                    leaf[i] = true;
                }
                if (childFolderSet.contains(resourceBeans.get(i).getId())) {
                    hasChildFolder[i] = true;
                } else {
                    hasChildFolder[i] = false;
                }
            }
        }

        // 获取圈子信息
        Group groupBean = groupService.getGroupById(groupId);
        List<Category> clist = null;
        if (groupBean.getCategory() != null && groupBean.getCategory().getId() > 0) {
            clist = categoryService.tracedCategoryList(groupBean.getCategory().getId());
        }
        boolean isGroupManager = permService.isGroupManager(memberId, groupId);

        /////拼接返回json

        StringBuffer buffer = new StringBuffer();
        if ("tree".equalsIgnoreCase(type)) {
            buffer.append("[");

            boolean flag = false;
            int index = 0;
            for (GroupResource bean : resourceBeans) {
                if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                    buffer.append("{");
                    buffer.append("\"id\":").append(bean.getId()).append(",");
                    buffer.append("\"text\":\"").append(JS.quote(HTML.escape(bean.getName()))).append("\",");
                    buffer.append("\"hasChildren\":").append(hasChildFolder[index]).append(",");
                    buffer.append("\"hasChildFolder\":").append(hasChildFolder[index++]);
                    buffer.append("},");
                    flag = true;
                }
            }
            if (flag) {
                buffer.setLength(buffer.length() - 1);
            }
            buffer.append("]");
            return buffer.toString();
        }
        /////type 不是tree返回所有的数据
        StringBuffer cpath = new StringBuffer();
        StringBuffer categoryPath = new StringBuffer();
        if (clist != null && clist.size() > 0) {
            for (int i = clist.size() - 1; i >= 0; i--) {
                Category c = clist.get(i);
                cpath.append(JS.quote(HTML.escape(c.getDisplayName())))
                        .append(HTML.escape(" > "));
                categoryPath.append("{\"id\":").append(c.getId()).append(",\"displayName\":\"").append(c.getDisplayName()).append("\"},");
            }
            cpath.setLength(cpath.length() - 1);
            categoryPath.setLength(categoryPath.length() - 1);
        }
        Map<String, String> keywords = PropertyUtil.getFolderKeyWordMap();
        buffer.append("{");
        String pathDisplayName = groupBean.getDisplayName();
        pathDisplayName = JS.quote(HTML.escape(pathDisplayName == null ? "" : pathDisplayName));
        //提取面包屑
        buffer.append("\"path\":[");
        buffer.append("{\"id\":-").append(groupBean.getId())
                .append(", \"name\":\"").append(groupBean.getName())
//		.append("\", \"displayName\":\"").append(pathDisplayName).append("\"}");
                .append("\", \"displayName\":").append(JSONUtils.valueToString(pathDisplayName)).append("}");

        for (int i = list.size() - 1; i >= 0; --i) {
            buffer.append(",");
            buffer.append("{\"id\":").append(list.get(i).getId()).append(",");
            String displayName = list.get(i).getName();
            if (keywords.containsKey(displayName)
                    && list.get(i).getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                displayName = keywords.get(displayName);
            }
            displayName = JS.quote(HTML.escape(displayName == null ? "" : displayName));
            buffer.append("\"displayName\":").append(JSONUtils.valueToString(displayName));
            buffer.append("}");
        }
        buffer.append("],");
        buffer.append("\"categorys\":\"").append(cpath.toString()).append("\",");
        buffer.append("\"categoryPath\":[").append(categoryPath.toString())
                .append("],");
        buffer.append("\"isGroupManager\":").append(isGroupManager).append(",");
        buffer.append("\"parentId\":").append(parentId).append(",");
        buffer.append("\"recyclerId\":").append(recycler.getId()).append(",");
        int resSize = 0;
        int privateRes = 0;
        int privateFileRes = 0;
        buffer.append("\"resources\":[");
        for (int i = 0; i < resourceBeans.size(); ++i) {
            GroupResource resourceBean = resourceBeans.get(i);
            if (resourceBean.getFinishSign() == null
                    || !resourceBean.getFinishSign().equals(
                    GroupResource.UPLOAD_UNFINISH)) {
                if (resourceBean.isPrivateFile()
                        && ((Boolean) isGroupManager == null || !isGroupManager)) {
                    if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)
                        ++privateRes;
                    if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE)
                        ++privateFileRes;
                    continue;
                }
                buffer.append("{");
                buffer.append("\"id\":").append(resourceBean.getId()).append(",");
                buffer.append("\"groupId\":").append(resourceBean.getGroup().getId()).append(",");
                buffer.append("\"groupName\":\"").append(resourceBean.getGroupName()).append("\",");
                buffer.append("\"memberId\":").append(resourceBean.getCreatorId()).append(",");
                buffer.append("\"memberName\":\"").append(resourceBean.getMemberName()).append("\",");
                buffer.append("\"owner\":").append(resourceBean.getCreatorId() == memberId).append(",");
                String n = resourceBean.getName();
                if (resourceBean.getFilePreName() != null && n != null
                        && !n.startsWith(resourceBean.getFilePreName())) {
                    n = resourceBean.getOriginalName();
                }
                String name = JS.quote(HTML.escape(n));
                buffer.append("\"name\":").append(JSONUtils.valueToString(name)).append(",");
                String displayName = name;
                if (keywords.containsKey(name)
                        && resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                    displayName = keywords.get(name);
                    displayName = JS.quote(HTML.escape(displayName == null ? "" : displayName));
                }
                buffer.append("\"displayName\":").append(JSONUtils.valueToString(displayName)).append(",");
                String desc = JS.quote(HTML.escape(resourceBean.getDesc()));
                buffer.append("\"desc\":\"").append(desc == null ? "" : desc).append("\",");
                buffer.append("\"checkCode\":\"").append(resourceBean.getCheckCode() == null ? "" : resourceBean.getCheckCode()).append("\",");
                buffer.append("\"priority\":").append((int) resourceBean.getPriority()).append(",");
                buffer.append("\"documentType\":").append(resourceBean.getDocumentTypeValue()).append(",");
                if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
                    String linkPath = JS.quote(HTML.escape(resourceBean.getLinkPath() == null ? "" : resourceBean.getLinkPath()));
                    buffer.append("\"linkPath\":\"").append(linkPath).append("\",");
                }
                buffer.append("\"paiban\":\"").append(resourceBean.getPaiban() == null ? "" : resourceBean.getPaiban()).append("\",");
                String remark = JS.quote(HTML.escape(resourceBean
                        .getRemark() == null ? "" : resourceBean
                        .getRemark()));
                buffer.append("\"remark\":\"").append(remark).append("\",");
                buffer.append("\"filePath\":\"")
                        .append(resourceBean.getFilePath()).append("\",");
                buffer.append("\"icon\":\"")
                        .append(resourceBean.getIcon() == null ? ""
                                : resourceBean.getIcon()).append("\",");
                buffer.append("\"size\":").append(resourceBean.getSize()).append(",");
                buffer.append("\"rate\":").append(resourceBean.getRate())
                        .append(",");
                buffer.append("\"contentType\":\"").append(resourceBean.getContentType()).append("\",");
                String creationDate = resourceBean.getCreateDate().toString();
                buffer.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
                Timestamp publishDate = resourceBean.getPublishDate();
                buffer.append("\"publishDate\":\"")
                        .append(publishDate == null ? "" : publishDate.toString().substring(0,
                                creationDate.length() - 2)).append("\"");
                buffer.append(",\"publishTimestamp\":\"")
                        .append(publishDate == null ? "" : publishDate.getTime()).append("\"");
                buffer.append(",\"parentId\":")
                        .append(resourceBean.getParentId()).append(",");
                buffer.append("\"inner\":")
                        .append(resourceBean.isInnerDefaultFolder() ? "true"
                                : "false").append(",");
                buffer.append("\"type\":").append(resourceBean.getResourceType()).append(",");
                buffer.append("\"leaf\":").append(leaf[i]);
                buffer.append("}");
                ///////////////////////////////////////
                buffer.append(",");
                resSize++;
            } else {
                totalCount = totalCount - 1;
            }
        }
        if (resSize > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("],");
        //统计文件夹数和文件数
        int dirAmount = 0;
        int fileAmout = 0;
        for (GroupResource resourceBean : resourceBeans) {
            if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)
                ++dirAmount;
            if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE)
                ++fileAmout;
        }
        buffer.append("\"dirAmount\":").append(dirAmount - privateRes).append(",");
        buffer.append("\"totalCount\":").append(totalCount).append(",");
        buffer.append("\"fileAmount\":").append(fileAmout - privateFileRes);
        buffer.append("}");
        return buffer.toString();
    }

    private List<GroupResource> _uploadResource(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
            , String[] publishDate, Integer width, Integer height, Integer quality) throws Exception {
//    private Future<Boolean>  _uploadResource(String id, Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
//              ,String[] publishDate, Integer width, Integer height, Integer quality) throws Exception {
//        Future<Boolean> booleanFuture=null;

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member member = grouperService.getMemberById(memberId);
        Group group = null;
        if (groupId == null || groupId == 0) {
            if (name == null) {
//				throw new GroupsException("找不到要上传的文件柜,groupId:"+groupId);
                throw new GroupsException("groupId为null或者0，请传入有效的groupId参数");
            }
            Category personCategory = categoryService.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory());
            group = groupService.getGroupByDisplyName(personCategory.getId(), name);
        } else {
            group = groupService.getGroupById(groupId.longValue());
        }
        if (group == null) {
            throw new GroupsException("找不到该用户的文件柜");
        }
        parentId = parentId == null ? 0 : parentId;
        // 若传进来的不是parentId是path的话，则查找该路径所在的文件夹的id
        if (parentId == 0 && path != null) {
            String[] path_temp = path.split("/");
            for (int i = 1; i < path_temp.length; i++) {
                GroupResource temp = resourceService.getResource(group.getId(), path_temp[i], parentId,
                        GroupResource.RESOURCE_TYPE_DIRECTORY);
                if (temp == null) {
                    throw new GroupsException("找不到该上传的路径");
                }
                parentId = temp.getId();
            }
        }
        List<GroupResource> uploadResources = new ArrayList<GroupResource>();
        // ////////////////////////////////////////////////////////////////////////

//		//上传文件
//		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//		MultipartFile multipartFile =  null;
//		multipartFile = multipartRequest.getFile("filedata");
//		if (multipartFile == null) {
//			multipartFile = multipartRequest.getFile("Filedata");
//		}
//		File file = null;
//		try {
//			file = File.createTempFile("tmp", null);
//			multipartFile.transferTo(file);
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}


        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if (multipartResolver.isMultipart(request)) {
            //将request变成多部分request
            Timestamp now = new Timestamp(System.currentTimeMillis());
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //获取multiRequest 中所有的文件名
            Iterator<String> iter = multiRequest.getFileNames();
            if (iter == null || !iter.hasNext()) {
                throw new GroupsException("资源文件列表为空列表");
            }
            //判断上传的位置是否是个人柜子
            Boolean isPersonalGroup = false;
            String personGroupName = "" + memberId;
            Group pg = groupService.getGroupByName(personGroupName);
            if (pg != null && pg.getId().longValue() == group.getId()) {
                isPersonalGroup = true;
            }

            //一次遍历所有文件
            List<MultipartFile> multipartFile = multiRequest.getFiles("filedata");
            if (multipartFile == null || multipartFile.size() <= 0) {
                multipartFile = multiRequest.getFiles("Filedata");
            }
            if (multipartFile == null || multipartFile.size() <= 0) {
                throw new GroupsException("资源文件列表为空列表");
            }
            //判断上传图片时自带缩略图
            List<MultipartFile> thumbFile = multiRequest.getFiles("thumbnail");
            if (thumbFile == null || thumbFile.size() <= 0) {
                thumbFile = multiRequest.getFiles("Thumbnail");
            }
            List<MultipartFile> thumDefault = multiRequest.getFiles("thumDefault");
            if (thumDefault == null || thumDefault.size() <= 0) {
                thumDefault = multiRequest.getFiles("ThumDefault");
            }
            long sumsize = 0L;
            for (int i = 0; i < multipartFile.size(); i++) {
                String filedataFileName = multipartFile.get(i).getOriginalFilename();
                String filedataContentType = multipartFile.get(i).getContentType();
                long fileSize = multipartFile.get(i).getSize();
                File file = null;
                try {
                    file = File.createTempFile("tmp", null);
                    multipartFile.get(i).transferTo(file);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (file != null) {
                    String fileName = null;
                    try {
                        fileName = URLDecoder.decode(filedataFileName, "UTF-8");
                    } catch (Exception ex) {
                        fileName = filedataFileName;
                    }
                    String contentType = filedataContentType;
                    GroupResource resourceBean = new GroupResource();
                    resourceBean.setContentType(contentType);
                    resourceBean.setCreateDate(now);
                    resourceBean.setDesc(desc);
                    resourceBean.setGroup(group);
                    resourceBean.setGroupName(group.getName());
                    resourceBean.setCreatorId(memberId);
                    resourceBean.setMemberName(member.getName());
                    resourceBean.setName(fileName);

                    //上传个人柜子资源并指定关联到域
                    if (isPersonalGroup && domainTags != null && domainTags.length > i) {
                        resourceBean.setDomainTag(domainTags[i]);//设置关联
                    }

                    if (publishDate != null && i < publishDate.length) {
                        String pd = publishDate[i];
                        if (pd == null || "".equals(pd.trim())) {
                            resourceBean.setPublishDate(null);
                        } else {
                            Timestamp timestamp = new Timestamp(Long.valueOf(pd));
                            resourceBean.setPublishDate(timestamp);
                        }
                    }

                    // 上传未完成
                    resourceBean.setFinishSign(GroupResource.UPLOAD_UNFINISH);
                    resourceBean.setDetailSize(Long.toString(fileSize));
                    long len = fileSize / 1024;
                    if (len * 1024 < fileSize) {
                        len++;
                    }
                    resourceBean.setSize(len);
                    resourceBean.setParentId(parentId < 0 ? 0 : parentId);
                    // ==========更新path==========
                    String path_temp = null;
                    if (parentId <= 0) {
                        path_temp = "/";
                    } else {
                        GroupResource temp = resourceService.getResourceById(parentId);
                        if (temp.getPath() == null || temp.getPath().equals("")) {
                            // rebuilt resource path
                            path_temp = resourceService.rebuiltPath(temp.getId());
                        } else {
                            path_temp = temp.getPath() + parentId + "/";
                        }
                    }
                    resourceBean.setPath(path_temp);
                    // ==========更新path==========
                    resourceBean.setResourceType(GroupResource.RESOURCE_TYPE_FILE);
                    resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
                    int documentTypeValue = documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue();
                    resourceBean.setDocumentTypeValue(documentTypeValue);
                    resourceService.uploadResource(file, resourceBean);
//					Long id = System.currentTimeMillis();
//					booleanFuture = helpAsyncService.uploadResource_v2(id, file, resourceBean, null);

                    uploadResources.add(resourceBean);
                    //删除multipartFile转换成File的临时文件
//					if(file.exists()){
//						file.delete();
//					}

                    if (thumbFile != null && thumbFile.size() > 0 && thumbFile.get(i) != null) {
                        File thumb = null;
                        try {
                            thumb = File.createTempFile("tmpthum", null);
                            thumbFile.get(i).transferTo(thumb);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // 上传时附带缩略图(该缩略图用作浏览器预览)
                        System.out.println("upload thumbnail");
                        if (width == null || width == 0)
                            width = PropertyUtil.getThumbnailDefaultWidth();
                        if (height == null || height == 0)
                            height = PropertyUtil.getThumbnailDefaultHeight();
                        if (quality == null || quality == 0)
                            quality = PropertyUtil.getThumbnailQuality();
                        resourceService.uploadPreviewThumbnail(resourceBean,
                                thumb, width, height, quality, false);
                        //删除multipartFile转换成File的临时文件
                        if (thumb.exists()) {
                            thumb.delete();
                        }
                    }


                    if (thumDefault != null && thumDefault.size() > 0 && thumDefault.get(i) != null) {
                        File thumdefault = null;
                        try {
                            thumdefault = File.createTempFile("tmpthumdefault", null);
                            thumDefault.get(i).transferTo(thumdefault);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // 上传时附带缩略图(该缩略图用作浏览器预览)
                        System.out.println("upload thumbnail");
                        if (width == null || width == 0)
                            width = PropertyUtil.getThumbnailDefaultWidth();
                        if (height == null || height == 0)
                            height = PropertyUtil.getThumbnailDefaultHeight();
                        if (quality == null || quality == 0)
                            quality = PropertyUtil.getThumbnailQuality();
                        resourceService.uploadThumbnail(resourceBean,
                                thumdefault, width, height, quality);
                        //删除multipartFile转换成File的临时文件
                        if (thumdefault.exists()) {
                            thumdefault.delete();
                        }
                    }
                    sumsize += len;
                }

            }
            //编辑柜子容量by mi
            long groupavai = group.getAvailableCapacity();
            group.setAvailableCapacity(groupavai - sumsize);
            group.setUsedCapacity(group.getTotalFileSize() - group.getAvailableCapacity());
            groupDao.saveOrUpdateGroup(group);
        }
        return uploadResources;
//        return booleanFuture;
    }

    private Long _uploadResource_v3(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
            , String[] publishDate, Integer width, Integer height, Integer quality) throws Exception {
//    private Future<Boolean>  __uploadResource(String id, Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
//              ,String[] publishDate, Integer width, Integer height, Integer quality) throws Exception {
        Long id = 0L;

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member member = grouperService.getMemberById(memberId);
        Group group = null;
        if (groupId == null || groupId == 0) {
            if (name == null) {
//				throw new GroupsException("找不到要上传的文件柜,groupId:"+groupId);
                throw new GroupsException("groupId为null或者0，请传入有效的groupId参数");
            }
            Category personCategory = categoryService.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory());
            group = groupService.getGroupByDisplyName(personCategory.getId(), name);
        } else {
            group = groupService.getGroupById(groupId.longValue());
        }
        if (group == null) {
            throw new GroupsException("找不到该用户的文件柜");
        }
        parentId = parentId == null ? 0 : parentId;
        // 若传进来的不是parentId是path的话，则查找该路径所在的文件夹的id
        if (parentId == 0 && path != null) {
            String[] path_temp = path.split("/");
            for (int i = 1; i < path_temp.length; i++) {
                GroupResource temp = resourceService.getResource(group.getId(), path_temp[i], parentId,
                        GroupResource.RESOURCE_TYPE_DIRECTORY);
                if (temp == null) {
                    throw new GroupsException("找不到该上传的路径");
                }
                parentId = temp.getId();
            }
        }
        List<GroupResource> uploadResources = new ArrayList<GroupResource>();
        // ////////////////////////////////////////////////////////////////////////

//		//上传文件
//		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//		MultipartFile multipartFile =  null;
//		multipartFile = multipartRequest.getFile("filedata");
//		if (multipartFile == null) {
//			multipartFile = multipartRequest.getFile("Filedata");
//		}
//		File file = null;
//		try {
//			file = File.createTempFile("tmp", null);
//			multipartFile.transferTo(file);
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}


        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if (multipartResolver.isMultipart(request)) {
            //将request变成多部分request
            Timestamp now = new Timestamp(System.currentTimeMillis());
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //获取multiRequest 中所有的文件名
            Iterator<String> iter = multiRequest.getFileNames();
            if (iter == null || !iter.hasNext()) {
                throw new GroupsException("资源文件列表为空列表");
            }
            //判断上传的位置是否是个人柜子
            Boolean isPersonalGroup = false;
            String personGroupName = "" + memberId;
            Group pg = groupService.getGroupByName(personGroupName);
            if (pg != null && pg.getId().longValue() == group.getId()) {
                isPersonalGroup = true;
            }

            //一次遍历所有文件
            List<MultipartFile> multipartFile = multiRequest.getFiles("filedata");
            if (multipartFile == null || multipartFile.size() <= 0) {
                multipartFile = multiRequest.getFiles("Filedata");
            }
            if (multipartFile == null || multipartFile.size() <= 0) {
                throw new GroupsException("资源文件列表为空列表");
            }
            //判断上传图片时自带缩略图
            List<MultipartFile> thumbFile = multiRequest.getFiles("thumbnail");
            if (thumbFile == null || thumbFile.size() <= 0) {
                thumbFile = multiRequest.getFiles("Thumbnail");
            }
            List<MultipartFile> thumDefault = multiRequest.getFiles("thumDefault");
            if (thumDefault == null || thumDefault.size() <= 0) {
                thumDefault = multiRequest.getFiles("ThumDefault");
            }
            long sumsize = 0L;
            for (int i = 0; i < multipartFile.size(); i++) {
                String filedataFileName = multipartFile.get(i).getOriginalFilename();
                String filedataContentType = multipartFile.get(i).getContentType();
                long fileSize = multipartFile.get(i).getSize();
                File file = null;
                try {
                    file = File.createTempFile("tmp", null);
                    multipartFile.get(i).transferTo(file);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (file != null) {
                    String fileName = null;
                    try {
                        fileName = URLDecoder.decode(filedataFileName, "UTF-8");
                    } catch (Exception ex) {
                        fileName = filedataFileName;
                    }
                    String contentType = filedataContentType;
                    GroupResource resourceBean = new GroupResource();
                    resourceBean.setContentType(contentType);
                    resourceBean.setCreateDate(now);
                    resourceBean.setDesc(desc);
                    resourceBean.setGroup(group);
                    resourceBean.setGroupName(group.getName());
                    resourceBean.setCreatorId(memberId);
                    resourceBean.setMemberName(member.getName());
                    resourceBean.setName(fileName);

                    //上传个人柜子资源并指定关联到域
                    if (isPersonalGroup && domainTags != null && domainTags.length > i) {
                        resourceBean.setDomainTag(domainTags[i]);//设置关联
                    }

                    if (publishDate != null && i < publishDate.length) {
                        String pd = publishDate[i];
                        if (pd == null || "".equals(pd.trim())) {
                            resourceBean.setPublishDate(null);
                        } else {
                            Timestamp timestamp = new Timestamp(Long.valueOf(pd));
                            resourceBean.setPublishDate(timestamp);
                        }
                    }

                    // 上传未完成
                    resourceBean.setFinishSign(GroupResource.UPLOAD_UNFINISH);
                    resourceBean.setDetailSize(Long.toString(fileSize));
                    long len = fileSize / 1024;
                    if (len * 1024 < fileSize) {
                        len++;
                    }
                    resourceBean.setSize(len);
                    resourceBean.setParentId(parentId < 0 ? 0 : parentId);
                    // ==========更新path==========
                    String path_temp = null;
                    if (parentId <= 0) {
                        path_temp = "/";
                    } else {
                        GroupResource temp = resourceService.getResourceById(parentId);
                        if (temp.getPath() == null || temp.getPath().equals("")) {
                            // rebuilt resource path
                            path_temp = resourceService.rebuiltPath(temp.getId());
                        } else {
                            path_temp = temp.getPath() + parentId + "/";
                        }
                    }
                    resourceBean.setPath(path_temp);
                    // ==========更新path==========
                    resourceBean.setResourceType(GroupResource.RESOURCE_TYPE_FILE);
//                    resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
                    resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_DELETE);
                    int documentTypeValue = documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue();
                    resourceBean.setDocumentTypeValue(documentTypeValue);
//        			resourceService.uploadResource(file, resourceBean);
//					Long id = System.currentTimeMillis();
                    resourceBean.setRate(1);
                    resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
                    resourceService.createResource(resourceBean);
                    id = resourceBean.getId();
                    System.out.println("上传之前--------------------");
                    helpAsyncService.uploadResource_v2(file, resourceBean, null);
                    uploadResources.add(resourceBean);
                    System.out.println("上传之后********************异步操作已经执行");
                    //删除multipartFile转换成File的临时文件
//					if(file.exists()){
//						file.delete();
//					}

                    if (thumbFile != null && thumbFile.size() > 0 && thumbFile.get(i) != null) {
                        File thumb = null;
                        try {
                            thumb = File.createTempFile("tmpthum", null);
                            thumbFile.get(i).transferTo(thumb);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // 上传时附带缩略图(该缩略图用作浏览器预览)
                        System.out.println("upload thumbnail");
                        if (width == null || width == 0)
                            width = PropertyUtil.getThumbnailDefaultWidth();
                        if (height == null || height == 0)
                            height = PropertyUtil.getThumbnailDefaultHeight();
                        if (quality == null || quality == 0)
                            quality = PropertyUtil.getThumbnailQuality();
                        resourceService.uploadPreviewThumbnail(resourceBean,
                                thumb, width, height, quality, false);
                        //删除multipartFile转换成File的临时文件
                        if (thumb.exists()) {
                            thumb.delete();
                        }
                    }


                    if (thumDefault != null && thumDefault.size() > 0 && thumDefault.get(i) != null) {
                        File thumdefault = null;
                        try {
                            thumdefault = File.createTempFile("tmpthumdefault", null);
                            thumDefault.get(i).transferTo(thumdefault);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // 上传时附带缩略图(该缩略图用作浏览器预览)
                        System.out.println("upload thumbnail");
                        if (width == null || width == 0)
                            width = PropertyUtil.getThumbnailDefaultWidth();
                        if (height == null || height == 0)
                            height = PropertyUtil.getThumbnailDefaultHeight();
                        if (quality == null || quality == 0)
                            quality = PropertyUtil.getThumbnailQuality();
                        resourceService.uploadThumbnail(resourceBean,
                                thumdefault, width, height, quality);
                        //删除multipartFile转换成File的临时文件
                        if (thumdefault.exists()) {
                            thumdefault.delete();
                        }
                    }
                    sumsize += len;
                }

            }
            //编辑柜子容量by mi
            long groupavai = group.getAvailableCapacity();
            group.setAvailableCapacity(groupavai - sumsize);
            group.setUsedCapacity(group.getTotalFileSize() - group.getAvailableCapacity());
            groupDao.saveOrUpdateGroup(group);
        }
        return id;
//        return booleanFuture;
    }

    //接口
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/getProgress", produces = "application/json; charset=UTF-8")
    public String getProgress(Long id) {
//		List<Long> solveList = asyncService.getSolveList(id);
        List<Long> solveList = asyncService.solveMaps.get(id);
        System.out.println("getProgress:" + asyncService.solveMaps.get(id));
        Long current = 0L;
        Long allCount = 0L;
        if (solveList != null) {
            current = solveList.get(0);
            allCount = solveList.get(1);

//			Long current=solveList.get(0);
//			Long allCount=solveList.get(1);
            if (current == -1)
                throw new GroupsException("上传过程存在错误");
            else
                return "{\"total\":" + allCount + ",\"progress\":" + current + "}";
        } else
            throw new GroupsException("列表为空");

    }


    /**
     * 上传资源,未完善，不包括自带缩略图
     *
     * @param groupId      柜子id
     * @param parentId     父目录id
     * @param name         上传个人资源柜子的名字（与账号同名），与groupId二选一
     * @param path         根据路径上传，与parentId二选一
     * @param documentType 文件类型，可选
     * @param desc         描述，可选
     * @param request
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/uploadResource", produces = "application/json; charset=UTF-8")
    public String uploadResource(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
            , String[] publishDate) throws Exception {
        this._uploadResource(groupId, parentId, name, path, documentType, desc, request, domainTags, publishDate, null, null, null);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value = "/uploadResource", produces = "application/json; charset=UTF-8")
//	public String uploadResource_v3(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
//			,String[] publishDate) throws Exception {
//		//生成随机数
//		Long id=0L;
//
//		id=this._uploadResource_v3(groupId, parentId, name, path, documentType, desc, request, domainTags, publishDate, null, null, null);
//		System.out.println("前端返回uploadResource接口"+id);
//		System.out.println("前端返回执行成功");
//		return "{\"asyncId\":"+id+"}";
//	}

    /**
     * uwp上传接口
     *
     * @param groupId
     * @param parentId
     * @param name
     * @param path
     * @param documentType
     * @param desc
     * @param request
     * @param domainTags
     * @param publishDate
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/uploadResourceWithId", produces = "application/json; charset=UTF-8")
    public String uploadResourceWithId(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
            , String[] publishDate) throws Exception {
        //modified by mi
        List<GroupResource> uploads = this._uploadResource(groupId, parentId, name, path, documentType, desc, request, domainTags, publishDate, null, null, null);
        List<Long> uploadIds = new ArrayList<Long>();
        for (GroupResource r : uploads) {
            uploadIds.add(r.getId());
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"totalCount\":").append(uploadIds.size()).append(",");
        buffer.append("\"ids\":[");
        for (Long id : uploadIds) {
            buffer.append("{");
            buffer.append("\"id\":").append(id);
            buffer.append("},");
        }
        if (uploadIds.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    /**
     * 上传圈子资源 (包括文件路径、用户名字)
     *
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/uploadResourceWithPath", produces = "application/json; charset=UTF-8")
    public String uploadResourceWithPath(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request) throws Exception {
        Long memberId = UserUtils.getCurrentMemberId();
        Member member = grouperService.getMemberById(memberId);
        //上传文件
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = null;
        multipartFile = multipartRequest.getFile("filedata");
        if (multipartFile == null) {
            multipartFile = multipartRequest.getFile("Filedata");
        }

        if (multipartFile == null)
            throw new GroupsException("资源文件列表为空列表");
        File file = null;
        try {
            file = File.createTempFile("tmp", null);
            multipartFile.transferTo(file);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Timestamp now = new Timestamp(System.currentTimeMillis());

        List<Long> uploadIds = new ArrayList<Long>();
        String filedataFileName = multipartFile.getOriginalFilename();
        String filedataContentType = multipartFile.getContentType();
        long fileSize = multipartFile.getSize();
        String fileName = null;
        try {
            fileName = URLDecoder.decode(filedataFileName, "UTF-8");
        } catch (Exception ex) {
            fileName = filedataFileName;
        }
        Group group = groupService.getGroupById(groupId.longValue());
        String contentType = filedataContentType;
        GroupResource resourceBean = new GroupResource();
        resourceBean.setContentType(contentType);
        resourceBean.setCreateDate(now);
        resourceBean.setDesc(desc);
        resourceBean.setGroup(group);
        resourceBean.setGroupName(group.getName());
        resourceBean.setCreatorId(memberId);
        resourceBean.setMemberName(member.getName());
        resourceBean.setName(fileName);
        resourceBean.setDetailSize(Long.toString(fileSize));
        long len = fileSize / 1024;
        if (len * 1024 < fileSize) {
            len++;
        }
        resourceBean.setSize(len);
        resourceBean.setParentId(parentId < 0 ? 0 : parentId);
        resourceBean.setResourceType(GroupResource.RESOURCE_TYPE_FILE);
        resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
        int documentTypeValue = documentType == null ? GroupResource.DOCUMENT_TYPE_UNKNOWN : documentType.intValue();
        resourceBean.setDocumentTypeValue(documentTypeValue);
        resourceService.uploadResource(file, resourceBean);


        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 上传资源,未完善，不包括自带缩略图
     *
     * @param groupId      柜子id
     * @param parentId     父目录id
     * @param name         上传个人资源柜子的名字（与账号同名），与groupId二选一
     * @param path         根据路径上传，与parentId二选一
     * @param documentType 文件类型，可选
     * @param desc         描述，可选
     * @param request
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/uploadResource_v2", produces = "application/json; charset=UTF-8")
    public String uploadResource_v2(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
            , String[] publishDate) throws Exception {
        List<GroupResource> resources = this._uploadResource(groupId, parentId, name, path, documentType, desc, request, domainTags, publishDate, null, null, null);
        // this.uploadResourceWithPath();
        List<Long> ids = new ArrayList<Long>();
        for (GroupResource r : resources) {
            ids.add(r.getId());
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"totalCount\":").append(ids.size()).append(",");
        buffer.append("\"ids\":[");
        for (Long id : ids) {
            buffer.append("{");
            buffer.append("\"id\":").append(id);
            buffer.append("},");
        }
        if (ids.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/uploadResourceReturnId", produces = "application/json; charset=UTF-8")
    public String uploadResourceReturnId(Long groupId, Long parentId, String name, String path, Integer documentType, String desc, HttpServletRequest request, Long[] domainTags
            , String[] publishDate) throws Exception {
        List<GroupResource> ids = this._uploadResource(groupId, parentId, name, path, documentType, desc, request, domainTags, publishDate, null, null, null);

        StringBuffer buffer = new StringBuffer();
        int i = 1;
        buffer.append("{");
        buffer.append("\"total\":").append(ids.size()).append(", \"file\":");
        buffer.append("[");
        for (GroupResource id : ids) {
            String ext = id.getFileExt();
            if (ext != null && ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            String name00 = id.getName();
            if (name00 != null && name00.startsWith(".")) {
                name00 = name00.substring(1);
            }
            buffer.append("{");
            buffer.append("\"no\":\"").append(i++).append("\",");
            buffer.append("\"filename\":\"").append(name00).append("\",");
            buffer.append("\"ext\":\"").append(ext).append("\",");
            buffer.append("\"id\":").append(id.getId()).append("},");
        }
        if (ids.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]");
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * 获取缩略图
     *
     * @param id      资源id
     * @param width   宽
     * @param height  高
     * @param quality 质量
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/getThumbnail", "/getThumbnail_v2"}, produces = "application/json; charset=UTF-8")
    public String getThumbnail(Long[] id, Integer width, Integer height, Integer quality) throws Exception {
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id为空，无法查看");
        }
        if (width == null || width == 0)
            width = PropertyUtil.getThumbnailDefaultWidth();
        if (height == null || height == 0)
            height = PropertyUtil.getThumbnailDefaultHeight();
        if (quality == null || quality == 0)
            quality = PropertyUtil.getThumbnailQuality();
        String[] thumbUrl = new String[id.length];
        for (int i = 0; i < id.length; ++i) {
            try {
                thumbUrl[i] = PropertyUtil.getGroupThumbnailFolderPath()
                        + resourceService.getThumbnail(id[i], width, height, quality);
            } catch (Exception e) {
                logger.error(e);
                System.out.println(e.toString());
            }
        }

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < thumbUrl.length; i++) {
            try {
                thumbUrl[i] = thumbUrl[i].replaceAll("\\\\", "/");
            } catch (Exception e) {

            }

        }

        buffer.append("[{\"id\":").append(id[0]).append(",");
        buffer.append("\"thumbUrl\":\"").append(thumbUrl[0]).append("\"}");

        for (int i = 1; i < thumbUrl.length; i++) {
            buffer.append(",{");
            buffer.append("\"id\":").append(id[i]).append(",");
            buffer.append("\"thumbUrl\":\"").append(thumbUrl[i]).append("\"}");
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * 获取缩略图
     *
     * @param id      资源id
     * @param width   宽
     * @param height  高
     * @param quality 质量
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/getThumbnail_V2", produces = "application/json; charset=UTF-8")
    public String getThumbnail_V2(Long[] id, Integer width, Integer height, Integer quality) throws Exception {
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id为空，无法查看");
        }
        if (width == 0)
            width = PropertyUtil.getThumbnailDefaultWidth();
        if (height == 0)
            height = PropertyUtil.getThumbnailDefaultHeight();
        if (quality == 0)
            quality = PropertyUtil.getThumbnailQuality();
        String[] thumbUrl = new String[id.length];
        for (int i = 0; i < id.length; ++i) {
            try {
                thumbUrl[i] = PropertyUtil.getGroupThumbnailFolderPath()
                        + resourceService.getThumbnail(id[i], width, height, quality);
            } catch (Exception e) {
                logger.error(e);
                System.out.println(e.toString());
            }
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("[{");
        buffer.append("\"thumbUrl\":\"").append(thumbUrl[0]).append("\"}");
        for (int i = 1; i < thumbUrl.length; i++) {
            buffer.append(",{");
            buffer.append("\"thumbUrl\":\"").append(thumbUrl[i]).append("\"}");
        }
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * 下载圈子资源
     *
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/downloadResource", "/downloadResource_v2"}, produces = "application/json; charset=UTF-8")
    public String downloadResource(Long[] id, Long[] Id, Integer isInline, Long rangeByteStart, Long rangeByteEnd, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (id == null || id.length == 0) {
            if (Id == null || Id.length == 0) {
                throw new GroupsException("资源id为空，无法下载资源");
            } else {
                id = Id;
            }

        }
        isInline = isInline == null ? 0 : isInline;
        GroupResource resourceBean = resourceService.getResourceById(id[0]);
        String filename = resourceBean.getName();
        if (id.length > 1 || (id.length == 1 && resourceService.getResourceById(id[0]).getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY)) {

            if (id.length > 1) {
                if (resourceBean.getParentId() > 0) {
                    filename = resourceService.getResourceById(resourceBean.getParentId()).getName() + ".zip";
                } else {
                    Group group = resourceBean.getGroup();
                    String groupName = group.getDisplayName();
                    if (groupName == null || groupName.length() == 0) {
                        groupName = group.getName();
                    }
                    filename = groupName + ".zip";
                }
            } else {
                filename = resourceBean.getName() + ".zip";
            }
            filename = filename.replaceAll(" ", "");

            response.setContentType("application/octet-stream; charset=utf-8");

            String toFileName = FileNameEncoder.encode(filename, request.getHeader("User-agent"));

            if (isInline == 1) {
                response.setHeader("Content-Disposition", "inline; " + toFileName);
            } else {
                response.setHeader("Content-Disposition", "attachment; " + toFileName);
            }

            OutputStream outputStream = null;
            try {
                outputStream = response.getOutputStream();
                resourceService.downloadResource(id, outputStream);
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e) {
                Throwable te = e.getCause();
                if (!(te instanceof SocketException))
                    if (e instanceof IOException)
                        throw (IOException) e;
                    else
                        throw new IOException(e.getLocalizedMessage());
            } finally {
                if (outputStream != null)
                    outputStream.close();
            }
        } else {
            File downFile = resourceService.getDownloadResource(id);
            if (downFile != null && downFile.exists()) {
                filename = filename.replaceAll(" ", "");
                if (rangeByteStart != null && rangeByteEnd != null) {
                    FileDownloadUtil.outputWithRange(downFile, filename, rangeByteStart.longValue(),
                            rangeByteEnd.longValue(), request, response,
                            isInline);
                } else {
                    FileDownloadUtil.output(downFile, filename, request,
                            response, isInline);
                }
            } else {
                throw new GroupsException("无法下载资源");
            }
        }
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 该接口仅供LMS打包下载学生作业使用，功能逻辑未必适合其他使用场景
     *
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/zipDownloadResource", produces = "application/json; charset=UTF-8")
    public String zipDownloadResource(String[] homeworks, String homeworkName, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if (homeworks == null || homeworks.length == 0)
            throw new GroupsException("参数错误");
        String[] newFileName = new String[homeworks.length];
        Long[] id = new Long[homeworks.length];
        for (int i = 0; i < homeworks.length; i++) {
            String[] map = homeworks[i].trim().split(":");
            id[i] = Long.parseLong(map[0].trim());
            newFileName[i] = map[1].trim();
        }
        if (id == null || id.length == 0)
            throw new GroupsException("资源id为空，无法下载资源");

        if (homeworkName == null) {
            GroupResource resourceBean = resourceService.getResourceById(id[0]);
            if (resourceBean.getParentId() > 0) {
                homeworkName = resourceService.getResourceById(
                        resourceBean.getParentId()).getName()
                        + ".zip";
            } else {
                Group group = resourceBean.getGroup();
                String groupName = group.getDisplayName();
                if (groupName == null || groupName.length() == 0) {
                    groupName = group.getName();
                }
                homeworkName = groupName + ".zip";
            }
        } else if (!homeworkName.endsWith("\\.zip")) {
            homeworkName = homeworkName + ".zip";
        }
        homeworkName.replaceAll(" ", "_");

        response.setContentType("application/octet-stream; charset=utf-8");
        String toFileName = FileEncoder.encode(homeworkName, request.getHeader("User-agent"));
        response.setHeader("Content-Disposition", "attachment; "
                + toFileName);
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            if (newFileName != null && newFileName.length > 0) {
                //改名(LMS 导出学生作业时，该文件名为学生名)
                resourceService.downloadResourceReplaceName(id, outputStream, newFileName);
            } else {//不改名
                resourceService.downloadResource(id, outputStream);
            }

            if (outputStream != null)
                outputStream.close();
        } catch (Exception e) {
            Throwable te = e.getCause();
            if (!(te instanceof SocketException))
                if (e instanceof IOException)
                    throw (IOException) e;
                else
                    throw new IOException(e.getLocalizedMessage());
        } finally {
            if (outputStream != null)
                outputStream.close();
        }

        return null;
    }


    /**
     * 从回收站还原
     *
     * @param id       还原资源id数组
     * @param parentId 恢复到的父目录
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/restoreResource", produces = "application/json; charset=UTF-8")
    public String restoreResource(Long[] id, Long parentId) {
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id不能为空");
        }
        if (parentId == null || parentId < 0) {
            parentId = 0L;
        }
        for (long _id : id) {
            GroupResource res = resourceService.getResourceById(_id);
            resourceService.moveResource(_id, parentId, res.getGroup().getId(), false);
        }

        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 彻底删除
     *
     * @param id 资源id数组
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/deleteResource", "/deleteResource_v2"}, produces = "application/json; charset=UTF-8")
    public String deleteResource(Long[] id, Long[] Id) {
        if (id == null || id.length == 0) {
            if (Id == null || Id.length == 0) {
                throw new GroupsException("资源id不能为空");
            } else {
                id = Id;
            }
        }
        for (long _id : id) {
            GroupResource bean = resourceService.getResourceById(_id);
            if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                resourceService.deleteResourceDir(_id);
            }
            if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE
                    || bean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
                resourceService.deleteResource(_id);
            }
        }
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 移动资源
     *
     * @param parentId 目的父亲文件夹id
     * @param id       待移动资源id数组
     * @param groupId  目的柜子id
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/moveResource", "/moveResource_v2"}, produces = "application/json; charset=UTF-8")
    public String moveResource(Long parentId, Long[] id, Long groupId) {
        if (parentId < 0) {
            parentId = 0L;
        }
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id不能为空");
        }
        if (groupId == null || groupId <= 0) {
            throw new GroupsException("柜子id不能为空");
        }
        for (long _id : id) {
            resourceService.moveResource(_id, parentId, groupId, null);
        }
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 复制资源到指定位置
     *
     * @param parentId 目的父亲文件夹id
     * @param id       待复制资源id数组
     * @param groupId  目的柜子id
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/copyResource", "/copyResource_v2"}, produces = "application/json; charset=UTF-8")
    public String copyResource(Long parentId, Long[] id, Long groupId) {
        if (parentId < 0) {
            parentId = 0L;
        }
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id不能为空");
        }
        if (groupId == null || groupId <= 0) {
            throw new GroupsException("柜子id不能为空");
        }
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        for (long _id : id) {
            resourceService.copyResource(_id, groupId, parentId, memberId);
        }
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 修改资源文件夹名字和描述
     *
     * @param id
     * @param name
     * @param desc
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/modifyResource", "/modifyResource_v2"}, produces = "application/json; charset=UTF-8")
    public String modifyResource(Long[] id, String name, String desc, String[] publishDate) {
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id不能为空");
        }
        if (name == null) {
            throw new GroupsException("资源名字不能为空");
        }
        for (int i = 0; i < id.length; i++) {
            resourceService.modifyResource(id[i], name, desc);
            if (publishDate != null && i < publishDate.length) {
                String pd = publishDate[i];
                Timestamp timestamp;
                if (pd == null || "".equals(pd.trim())) {
                    timestamp = null;
                } else {
                    timestamp = new Timestamp(Long.valueOf(pd));
                }
                resourceService.modifyResourcePublishDate(id[i], timestamp);
            }
        }
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value = {"/modifyResource_v2",}, produces = "application/json; charset=UTF-8")
//	public String modifyResource_v2(Long[] id, String name, String desc, String[] publishDate) {
//		if (id == null || id.length == 0) {
//			throw new GroupsException("资源id不能为空");
//		}
//		if (name == null ) {
//			throw new GroupsException("资源名字不能为空");
//		}
//		for (int i=0;i<id.length;i++) {
//			resourceService.modifyResource(id[i], name, desc);
//			if(publishDate!=null&&i<publishDate.length) {
//				String pd = publishDate[i];
//				Timestamp timestamp;
//				if(pd==null||"".equals(pd.trim())) {
//					timestamp = null;
//				}else {
//					timestamp = new Timestamp(Long.valueOf(pd));
//				}
//				resourceService.modifyResourcePublishDate(id[i], timestamp);
//			}
//		}
//		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
//	}


    /**
     * 修改顺序号优先级
     *
     * @param id
     * @param orders
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/modifyResourceOrder", produces = "application/json; charset=UTF-8")
    public String modifyResourceOrder(Long[] id, Double[] orders) {
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id不能为空");
        }
        if (orders == null || orders.length == 0) {
            throw new GroupsException("资源优先级顺序号不能为空");
        }
        int ii = 0;
        for (long _id : id) {
            resourceService.modifyResourceOrder(_id, orders[ii++]);
        }
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 新建链接
     *
     * @param groupId  柜子id
     * @param parentId 父资源id
     * @param name     链接名字
     * @param url      链接url
     * @return
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/createLinkDir", produces = "application/json; charset=UTF-8")
    public String createLinkDir(Long groupId, Long parentId, String name, String url) {
        if (groupId == null || groupId <= 0) {
            throw new GroupsException("柜子id不能为空");
        }
        if (name == null || url == null) {
            throw new GroupsException("链接名字和url 不能为空");
        }
        if (parentId < 0) {
            parentId = 0L;
        }
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Group group = groupService.getGroupById(groupId);
        if (group == null || group.getId() <= 0) {
            throw new GroupsException("柜子不存在");
        }

        GroupResource bean = new GroupResource();
        bean.setGroup(group);
        bean.setName(name);
        bean.setParentId(parentId);
        bean.setCreatorId(memberId);
        bean.setDocumentTypeValue(GroupResource.DOCUMENT_TYPE_UNKNOWN);
        bean.setLinkPath(url);
        bean.setResourceType(GroupResource.RESOURCE_TYPE_LINK);
        resourceService.createResourceDir(bean, false);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\", \"id\": \"000\"}";
    }

    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/searchResources", produces = "application/json; charset=UTF-8")
    public String searchResources(String query, Long groupId, String memberName, Long categoryId, Long upCreationDate, Long downCreationDate, String owner, Integer start, Integer limit) throws Exception {

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member creator = grouperService.getMemberById(memberId);
        if (categoryId == null || categoryId < 0) {
            categoryId = 0L;
        }
        SearchTerm searchTerm = new AndSearchTerm();
        SortTerm sortTerm = new SortTerm();
        PageTerm pt = new PageTerm();
        pt.setBeginIndex(start);
        pt.setPageSize(limit);

        searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Status,
                SearchItem.Comparison.EQ, GroupResource.RESOURCE_STATUS_NORMAL));

        // 获得非回收站资源
        searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Name,
                SearchItem.Comparison.NE, PropertyUtil.getRecyclerName()));
        if (query != null && query.trim().length() != 0) {
            searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Name,
                    SearchItem.Comparison.LK, query));
        }

        if (memberName != null && memberName.trim().length() != 0) {
            searchTerm.add(new SearchItem<String>(
                    ResourceSearchItemKey.MemberName, SearchItem.Comparison.LK,
                    memberName.trim()));
        }
        if ("myself".equals(owner)) {
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.CreatorId,
                    SearchItem.Comparison.EQ, memberId));
        } else if ("other".equals(owner)) {
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.CreatorId,
                    SearchItem.Comparison.NE, memberId));
        }
        // creationDate
        if (downCreationDate != null && downCreationDate > 0) {
            Timestamp ts = new Timestamp(downCreationDate);
            searchTerm.add(new SearchItem<Timestamp>(
                    ResourceSearchItemKey.CreationDate,
                    SearchItem.Comparison.GE, ts));
        }
        if (upCreationDate != null && upCreationDate > 0) {
            Timestamp ts = new Timestamp(upCreationDate);
            searchTerm.add(new SearchItem<Timestamp>(
                    ResourceSearchItemKey.CreationDate,
                    SearchItem.Comparison.LE, ts));
        }
        boolean isEmpty = false;
        if (groupId != null && groupId > 0) {
            Group group = groupService.getGroupById(groupId);
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.GroupId,
                    SearchItem.Comparison.EQ, group.getId()));
        } else {
            // 全库或根据分类搜索
            List<Group> beans = groupService.getMyVisualGroups(categoryId, memberId);
            if (beans.isEmpty()) {
                isEmpty = true;
            }
            if (beans.size() > 1) {
                SearchTerm orTerm = new OrSearchTerm();
                for (Group groupBean : beans) {
                    orTerm.add(new SearchItem<Long>(
                            ResourceSearchItemKey.GroupId,
                            SearchItem.Comparison.EQ, groupBean.getId()));

                }
                searchTerm.add(orTerm);
            } else if (beans.size() == 1) {
                searchTerm.add(new SearchItem<Long>(
                        ResourceSearchItemKey.GroupId,
                        SearchItem.Comparison.EQ, beans.get(0).getId()));
            }
        }

        sortTerm.add(new AscSortItem(ResourceSortItemKey.Type));
        sortTerm.add(new DescSortItem(ResourceSortItemKey.CreationDate));

        long amount = 0;

        GroupResource[] list = null;
        if (!isEmpty) {
            PageNavigater<GroupResource> pn = resourceService.searchResource(searchTerm, sortTerm, pt);
            amount = pn.getItemsCount();
            System.out.println(amount);
            list = pn.getContent();
        }
        if (list == null) {
            list = new GroupResource[0];
        }

        Map<String, String> keywords = PropertyUtil.getFolderKeyWordMap();
        StringBuffer sb = new StringBuffer();
        sb.append("{\"totalCount\":").append(amount).append(",");
        sb.append("\"resources\":[");
        for (GroupResource bean : list) {
            sb.append("{");
            sb.append("\"id\":").append(bean.getId()).append(",");
            sb.append("\"groupId\":").append(bean.getGroup().getId()).append(",");
            sb.append("\"groupName\":\"").append(bean.getGroup().getDisplayName()).append("\",");
            String displayName = bean.getName();
            if (keywords.containsKey(displayName) && bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                displayName = keywords.get(displayName);
            }
            displayName = JS.quote(HTML.escape(displayName == null ? "" : displayName));
            sb.append("\"displayName\":\"").append(displayName).append("\",");
            if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                sb.append("\"type\":\"folder\",");
            } else if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
                sb.append("\"type\":\"link\",");
                String linkPath = JS.quote(HTML.escape(bean.getLinkPath() == null ? "" : bean.getLinkPath()));
                sb.append("\"linkPath\":\"").append(linkPath).append("\",");
            } else {
                sb.append("\"type\":\"file\",");
            }
            sb.append("\"desc\":\"").append(
                    JS.quote(HTML.escape(bean.getDesc())))
                    .append("\",");
            sb.append("\"size\":").append(bean.getSize()).append(",");
            String creationDate = bean.getCreateDate().toString();
            sb.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
            sb.append("\"owner\":").append(bean.getCreatorId() == memberId.longValue());
            sb.append("},");
        }
        if (list.length > 0)
            sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 增加每个资源对应的柜子信息、柜子内路径信息、柜子所在分类路径信息
     *
     * @param query
     * @param groupId
     * @param memberName
     * @param categoryId
     * @param upCreationDate
     * @param downCreationDate
     * @param owner
     * @param start
     * @param limit
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/searchResources_v2", produces = "application/json; charset=UTF-8")
    public String searchResources_v2(String query, Long groupId, String memberName, Long categoryId, Long upCreationDate, Long downCreationDate, String owner, Integer start, Integer limit) throws Exception {

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member creator = grouperService.getMemberById(memberId);
        if (categoryId == null || categoryId < 0) {
            categoryId = 0L;
        }
        SearchTerm searchTerm = new AndSearchTerm();
        SortTerm sortTerm = new SortTerm();
        PageTerm pt = new PageTerm();
        pt.setBeginIndex(start == null ? 0 : start);
        pt.setPageSize(limit == null ? 0 : Integer.MAX_VALUE);

        searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Status,
                SearchItem.Comparison.EQ, GroupResource.RESOURCE_STATUS_NORMAL));

        // 获得非回收站资源
        searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Name,
                SearchItem.Comparison.NE, PropertyUtil.getRecyclerName()));
        if (query != null && query.trim().length() != 0) {
            searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Name,
                    SearchItem.Comparison.LK, query));
        }

        if (memberName != null && memberName.trim().length() != 0) {
            searchTerm.add(new SearchItem<String>(
                    ResourceSearchItemKey.MemberName, SearchItem.Comparison.LK,
                    memberName.trim()));
        }
        if ("myself".equals(owner)) {
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.CreatorId,
                    SearchItem.Comparison.EQ, memberId));
        } else if ("other".equals(owner)) {
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.CreatorId,
                    SearchItem.Comparison.NE, memberId));
        }
        // creationDate
        if (downCreationDate != null && downCreationDate > 0) {
            Timestamp ts = new Timestamp(downCreationDate);
            searchTerm.add(new SearchItem<Timestamp>(
                    ResourceSearchItemKey.CreationDate,
                    SearchItem.Comparison.GE, ts));
        }
        if (upCreationDate != null && upCreationDate > 0) {
            Timestamp ts = new Timestamp(upCreationDate);
            searchTerm.add(new SearchItem<Timestamp>(
                    ResourceSearchItemKey.CreationDate,
                    SearchItem.Comparison.LE, ts));
        }
        boolean isEmpty = false;
        if (groupId != null && groupId > 0) {
            Group group = groupService.getGroupById(groupId);
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.GroupId,
                    SearchItem.Comparison.EQ, group.getId()));
        } else {
            // 全库或根据分类搜索
            List<Group> beans = groupService.getMyVisualGroups(categoryId, memberId);
            if (beans.isEmpty()) {
                isEmpty = true;
            }
            if (beans.size() > 1) {
                SearchTerm orTerm = new OrSearchTerm();
                for (Group groupBean : beans) {
                    orTerm.add(new SearchItem<Long>(
                            ResourceSearchItemKey.GroupId,
                            SearchItem.Comparison.EQ, groupBean.getId()));

                }
                searchTerm.add(orTerm);
            } else if (beans.size() == 1) {
                searchTerm.add(new SearchItem<Long>(
                        ResourceSearchItemKey.GroupId,
                        SearchItem.Comparison.EQ, beans.get(0).getId()));
            }
        }

        sortTerm.add(new AscSortItem(ResourceSortItemKey.Type));
        sortTerm.add(new DescSortItem(ResourceSortItemKey.CreationDate));

        long amount = 0;

        GroupResource[] list = null;
        if (!isEmpty) {
            PageNavigater<GroupResource> pn = resourceService.searchResource(searchTerm, sortTerm, pt);
            amount = pn.getItemsCount();
            list = pn.getContent();
        }
        if (list == null) {
            list = new GroupResource[0];
        }

        //获得每个资源对应的柜子信息、柜子内路径信息、柜子所在分类路径信息
        List<Group> groups = new ArrayList<Group>();
        List<List<Category>> clist = new ArrayList<List<Category>>();
        List<List<GroupResource>> dirList = new ArrayList<List<GroupResource>>();
        for (GroupResource res : list) {
            try {
                Group group = res.getGroup();
                groups.add(group);
                //获得所在分类信息
                List<Category> temp = new ArrayList<Category>();
                if (group.getCategory() != null) {
                    temp = categoryService.tracedCategoryList(group.getCategory().getId());
                }
                clist.add(temp);
                //获取柜子内文件夹路径信息
                List<GroupResource> dirPath = new ArrayList<GroupResource>();
                long pid = res.getParentId();
                if (pid > 0) {
                    while (true) {
                        GroupResource bean = resourceService.getResourceById(pid);
                        dirPath.add(bean);
                        pid = bean.getParentId();
                        if (pid <= 0) {
                            break;
                        }
                    }
                }
                dirList.add(dirPath);
            } catch (Exception e) {
                groups.add(null);
            }
        }

        Map<String, String> keywords = PropertyUtil.getFolderKeyWordMap();
        StringBuffer sb = new StringBuffer();
        sb.append("{\"totalCount\":").append(amount).append(",");
        sb.append("\"resources\":[");
        int index = 0;
        for (GroupResource bean : list) {
            sb.append("{");
            sb.append("\"id\":").append(bean.getId()).append(",");
            sb.append("\"groupId\":").append(bean.getGroup().getId()).append(",");
            sb.append("\"groupName\":\"").append(bean.getGroup().getDisplayName()).append("\",");
            String displayName = bean.getName();
            if (keywords.containsKey(displayName) && bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                displayName = keywords.get(displayName);
            }
            displayName = JS.quote(HTML.escape(displayName == null ? "" : displayName));
            sb.append("\"displayName\":\"").append(displayName).append("\",");

            //提取文件柜内部面包屑
            List<GroupResource> curDirPath = dirList.get(index);
            Group groupBean = groups.get(index);
            String pathDisplayName = groupBean.getDisplayName();
            pathDisplayName = JS.quote(HTML.escape(pathDisplayName == null ? ""
                    : pathDisplayName));
            sb.append("\"path\":[");
            sb.append("{\"id\":-").append(groupBean.getId())
                    .append(", \"name\":\"").append(groupBean.getName())
                    .append(groupBean.getId()).append("\", \"displayName\":\"")
                    .append(pathDisplayName).append("\"}");
            for (int i = curDirPath.size() - 1; i >= 0; --i) {
                sb.append(",{");
                sb.append("\"id\":").append(curDirPath.get(i).getId())
                        .append(",");
                sb.append("\"name\":\"")
                        .append(JS.quote(HTML.escape(curDirPath.get(i).getName())))
                        .append("\",");
                String dispName = curDirPath.get(i).getName();
                if (keywords.containsKey(dispName)
                        && curDirPath.get(i).getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                    dispName = keywords.get(dispName);
                }
                dispName = JS.quote(HTML.escape(displayName == null ? ""
                        : dispName));
                sb.append("\"displayName\":\"").append(dispName)
                        .append("\"");

                sb.append("}");
            }
            sb.append("],");
            //提取分类路径
            StringBuffer cpath = new StringBuffer();
            StringBuffer categoryPath = new StringBuffer();
            List<Category> curCateList = clist.get(index);
            if (curCateList != null && curCateList.size() > 0) {
                for (int i = curCateList.size() - 1; i >= 0; i--) {
                    Category c = curCateList.get(i);
                    cpath.append(JS.quote(c.getDisplayName()))
                            .append(">");
                    categoryPath.append("{\"id\":").append(c.getId()).append(",\"displayName\":\"").append(c.getDisplayName()).append("\"},");
                }
                cpath.setLength(cpath.length() - 1);
                categoryPath.setLength(categoryPath.length() - 1);
            }
            sb.append("\"categorys\":\"").append(cpath.toString())
                    .append("\",");
            sb.append("\"categoryPath\":[").append(categoryPath.toString())
                    .append("],");
            if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                sb.append("\"type\":\"folder\",");
            } else if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
                sb.append("\"type\":\"link\",");
                String linkPath = JS.quote(HTML.escape(bean.getLinkPath() == null ? "" : bean.getLinkPath()));
                sb.append("\"linkPath\":\"").append(linkPath).append("\",");
            } else {
                sb.append("\"type\":\"file\",");
            }
            sb.append("\"desc\":\"").append(
                    JS.quote(HTML.escape(bean.getDesc())))
                    .append("\",");
            sb.append("\"size\":").append(bean.getSize()).append(",");
            String creationDate = bean.getCreateDate().toString();
            sb.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
            sb.append("\"owner\":").append(bean.getCreatorId() == memberId.longValue());
            sb.append("},");
            index++;
        }
        if (list.length > 0)
            sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 支持多个query字段，是或关系
     *
     * @param query
     * @param groupId
     * @param memberName
     * @param categoryId
     * @param upCreationDate
     * @param downCreationDate
     * @param owner
     * @param start
     * @param limit
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/searchResources_v3", produces = "application/json; charset=UTF-8")
    public String searchResources_v3(String[] query, Long groupId, String memberName, Long categoryId, Long upCreationDate, Long downCreationDate, String owner, Integer start, Integer limit) throws Exception {

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member creator = grouperService.getMemberById(memberId);
        if (categoryId == null || categoryId < 0) {
            categoryId = 0L;
        }
        SearchTerm searchTerm = new AndSearchTerm();
        SortTerm sortTerm = new SortTerm();
        PageTerm pt = new PageTerm();
        pt.setBeginIndex(start);
        pt.setPageSize(limit);

        searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Status,
                SearchItem.Comparison.EQ, GroupResource.RESOURCE_STATUS_NORMAL));

        // 获得非回收站资源
        searchTerm.add(new SearchItem<String>(ResourceSearchItemKey.Name,
                SearchItem.Comparison.NE, PropertyUtil.getRecyclerName()));
        if (query != null && query.length > 0) {
            SearchTerm searchTerm1 = new OrSearchTerm();
            for (String q : query) {
                searchTerm1.add(new SearchItem<String>(ResourceSearchItemKey.Name,
                        SearchItem.Comparison.LK, q));
            }
            searchTerm.add(searchTerm1);
        }

        if (memberName != null && memberName.trim().length() != 0) {
            searchTerm.add(new SearchItem<String>(
                    ResourceSearchItemKey.MemberName, SearchItem.Comparison.LK,
                    memberName.trim()));
        }
        if ("myself".equals(owner)) {
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.CreatorId,
                    SearchItem.Comparison.EQ, memberId));
        } else if ("other".equals(owner)) {
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.CreatorId,
                    SearchItem.Comparison.NE, memberId));
        }
        // creationDate
        if (downCreationDate != null && downCreationDate > 0) {
            Timestamp ts = new Timestamp(downCreationDate);
            searchTerm.add(new SearchItem<Timestamp>(
                    ResourceSearchItemKey.CreationDate,
                    SearchItem.Comparison.GE, ts));
        }
        if (upCreationDate != null && upCreationDate > 0) {
            Timestamp ts = new Timestamp(upCreationDate);
            searchTerm.add(new SearchItem<Timestamp>(
                    ResourceSearchItemKey.CreationDate,
                    SearchItem.Comparison.LE, ts));
        }
        boolean isEmpty = false;
        if (groupId != null && groupId > 0) {
            Group group = groupService.getGroupById(groupId);
            searchTerm.add(new SearchItem<Long>(ResourceSearchItemKey.GroupId,
                    SearchItem.Comparison.EQ, group.getId()));
        } else {
            // 全库或根据分类搜索
            List<Group> beans = groupService.getMyVisualGroups(categoryId, memberId);
            if (beans.isEmpty()) {
                isEmpty = true;
            }
            if (beans.size() > 1) {
                SearchTerm orTerm = new OrSearchTerm();
                for (Group groupBean : beans) {
                    orTerm.add(new SearchItem<Long>(
                            ResourceSearchItemKey.GroupId,
                            SearchItem.Comparison.EQ, groupBean.getId()));

                }
                searchTerm.add(orTerm);
            } else if (beans.size() == 1) {
                searchTerm.add(new SearchItem<Long>(
                        ResourceSearchItemKey.GroupId,
                        SearchItem.Comparison.EQ, beans.get(0).getId()));
            }
        }

        sortTerm.add(new AscSortItem(ResourceSortItemKey.Type));
        sortTerm.add(new DescSortItem(ResourceSortItemKey.CreationDate));

        long amount = 0;

        GroupResource[] list = null;
        if (!isEmpty) {
            PageNavigater<GroupResource> pn = resourceService.searchResource(searchTerm, sortTerm, pt);
            amount = pn.getItemsCount();
            System.out.println(amount);
            list = pn.getContent();
        }
        if (list == null) {
            list = new GroupResource[0];
        }

        Map<String, String> keywords = PropertyUtil.getFolderKeyWordMap();
        StringBuffer sb = new StringBuffer();
        sb.append("{\"totalCount\":").append(amount).append(",");
        sb.append("\"resources\":[");
        for (GroupResource bean : list) {
            sb.append("{");
            sb.append("\"id\":").append(bean.getId()).append(",");
            sb.append("\"parentId\":").append(bean.getParentId()).append(",");
            sb.append("\"groupId\":").append(bean.getGroup().getId()).append(",");
            sb.append("\"groupName\":\"").append(bean.getGroup().getDisplayName()).append("\",");
            String displayName = bean.getName();
            if (keywords.containsKey(displayName) && bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                displayName = keywords.get(displayName);
            }
            displayName = JS.quote(HTML.escape(displayName == null ? "" : displayName));
            sb.append("\"displayName\":\"").append(displayName).append("\",");
            if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
                sb.append("\"type\":\"folder\",");
            } else if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
                sb.append("\"type\":\"link\",");
                String linkPath = JS.quote(HTML.escape(bean.getLinkPath() == null ? "" : bean.getLinkPath()));
                sb.append("\"linkPath\":\"").append(linkPath).append("\",");
            } else {
                sb.append("\"type\":\"file\",");
            }
            sb.append("\"desc\":\"").append(
                    JS.quote(HTML.escape(bean.getDesc())))
                    .append("\",");
            sb.append("\"size\":").append(bean.getSize()).append(",");
            String creationDate = bean.getCreateDate().toString();
            sb.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
            sb.append("\"owner\":").append(bean.getCreatorId() == memberId.longValue());
            sb.append("},");
        }
        if (list.length > 0)
            sb.setLength(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    /**
     * 查看预览图
     *
     * @param id
     * @param response
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/viewThumbnail", produces = "application/json; charset=UTF-8")
    public String viewThumbnail(Long[] id, HttpServletResponse response) throws Exception {
        if (id == null || id.length == 0) {
            throw new GroupsException("资源id为空，无法查看");
        }
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            resourceService.viewThumbnail(id[0], outputStream);
            if (outputStream != null)
                outputStream.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            if (outputStream != null)
                outputStream.close();
        }
        return null;
    }

    /**
     * 共享资源，即将资源共享给指定member
     *
     * @param memberName  分号分隔的多个person的memberName
     * @param memberIds   分号分割的多个team的memberId
     * @param resourceIds 分号分隔的多个资源resourceId
     * @param desc        描述
     * @return
     * @throws Exception
     */
//	@RequiresUser
//	@ResponseBody
//	@RequestMapping(value = {"/shareResourceToMember_v3","/shareResourceToMember_v2","/shareResourceToMember"}, produces = "application/json; charset=UTF-8")
//	public String shareResourceToMember(Long groupId, String memberName, String memberIds,String resourceIds,String desc, String resourceId) throws Exception{
//		Subject currentUser = SecurityUtils.getSubject();
//		Session session = currentUser.getSession();
//		CurrentUserWrap currentUserWrap = (CurrentUserWrap)session.getAttribute("currentUserWrap");
//		Long loginMemberId = currentUserWrap.getMemberId();
//		String loginMemberName = currentUserWrap.getMemberName();
//		Map map = null;
//		try {
//			map = this.getSharedMemberId(memberName, memberIds);
//		} catch (GroupsException e) {
//			throw new GroupsException(e);
//		}
//
//		LinkedHashMap<Long,String> members = (LinkedHashMap<Long,String>) map.get("individualAndTeamMember");
//		LinkedHashSet<String> emails = (LinkedHashSet<String>)map.get("individualAndTeamMemberEmail");
//		String recipient = (String) map.get("recipient");
//		List<String> emailList = (List<String>) map.get("emailList");
//		if (resourceIds == null || resourceIds.equals("")) {
//			if (resourceId == null || resourceId.equals("")) {
//				throw new GroupsException("参数错误，共享文件列表为空");
//			}
//			resourceIds =resourceId;
//		}
//		Member provider = grouperService.getMemberById(loginMemberId);
//		Timestamp createDate = new Timestamp(System.currentTimeMillis());
//		//构造ShareWrap
//		ShareWrap shareWrap = new ShareWrap();
//		shareWrap.setMessage(desc);
//		shareWrap.setRecipients(recipient);
//		shareWrap.setResourceIds(resourceIds.trim());
//		shareWrap.setCreateDate(createDate);
//		shareWrap.setProviderId(loginMemberId);
//		//计算多个资源总大小
//		String[] tempResourceIds = resourceIds.trim().split(";");
//		ArrayList<Long> resourceIdList = new ArrayList<Long>();
//		for(int i=0;i<tempResourceIds.length;i++){
//			try {
//				resourceIdList.add(Long.parseLong(tempResourceIds[i].trim()));
//			} catch (Exception e) {
//				//
//			}
//		}
//		long size = resourceService.getResourcesSizeSum(resourceIdList.toArray(new Long[0]));
//		shareWrap.setSize(size);
//		//resourceService.createShareWrap(shareWrap);
//		for(long rid:resourceIdList){
//			GroupResource r = resourceService.getResourceById(rid);
//			GroupResourceShare share = new GroupResourceShare();
//			share.setResource(r);
//			share.setProvider(provider);
//			share.setRemark(desc);
//			share.setRecipient(recipient);
//			share.setShareWrap(shareWrap);
//			share.setCreateDate(createDate);
//
//			resourceService.createShareAndSplitReceive(groupId, shareWrap,share,members);
//
//
//		}
//		//同一次分享多个资源，合并发送邮件
//		//TODO
//		String title = PropertyUtil.getEmailShareNoticeTitle();
//		File file = new ClassPathResource("emailShareNotice.properties").getFile();
//		String contentTemplate = FileUtils.readFileToString(file, "UTF-8");
//		String sender = loginMemberName;
//		String content = contentTemplate.replaceAll("\\$\\{sender\\}", sender);
//		StringBuffer resourceName = new StringBuffer("");
//		for(Long rid:resourceIdList){
//			resourceName.append(resourceService.getResourceById(rid).getName()+"、");
//		}
//		if(resourceIdList.size()>0){
//			resourceName.setLength(resourceName.length()-1);
//		}
//		content = content.replaceAll("\\$\\{resourceName\\}", resourceName.toString());
//		String[] emailArr=emails.toArray(new String[emails.size()]);
//
//		try {
//			MailSender.sendMail(emailArr, title, content);
//		} catch (Exception ex) {
//		}
//		return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
//	}
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/shareResourceToMember_v3", "/shareResourceToMember_v2", "/shareResourceToMember"}, produces = "application/json; charset=UTF-8")
    public String shareResourceToMember(Long groupId, String memberName, String memberIds, String resourceIds, String desc, String resourceId) throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long loginMemberId = currentUserWrap.getMemberId();
        String loginMemberName = currentUserWrap.getMemberName();
        Map map = null;
        try {
            map = this.getSharedMemberId(memberName, memberIds);
        } catch (GroupsException e) {
            throw new GroupsException(e);
        }

        LinkedHashMap<Long, String> members = (LinkedHashMap<Long, String>) map.get("individualAndTeamMember");
        LinkedHashSet<String> emails = (LinkedHashSet<String>) map.get("individualAndTeamMemberEmail");
        String recipient = (String) map.get("recipient");
        List<String> emailList = (List<String>) map.get("emailList");
        if (resourceIds == null || resourceIds.equals("")) {
            if (resourceId == null || resourceId.equals("")) {
                throw new GroupsException("参数错误，共享文件列表为空");
            }
            resourceIds = resourceId;
        }
        Member provider = grouperService.getMemberById(loginMemberId);
        Timestamp createDate = new Timestamp(System.currentTimeMillis());
        //构造ShareWrap
        ShareWrap shareWrap = new ShareWrap();
        shareWrap.setMessage(desc);
        shareWrap.setRecipients(recipient);
        shareWrap.setResourceIds(resourceIds.trim());
        shareWrap.setCreateDate(createDate);
        shareWrap.setProviderId(loginMemberId);
        //计算多个资源总大小
        String[] tempResourceIds = resourceIds.trim().split(";");
        ArrayList<Long> resourceIdList = new ArrayList<Long>();
        for (int i = 0; i < tempResourceIds.length; i++) {
            try {
                resourceIdList.add(Long.parseLong(tempResourceIds[i].trim()));
            } catch (Exception e) {
                //
            }
        }
        long size = resourceService.getResourcesSizeSum(resourceIdList.toArray(new Long[0]));
        shareWrap.setSize(size);
        //resourceService.createShareWrap(shareWrap);
        for (long rid : resourceIdList) {
            GroupResource r = resourceService.getResourceById(rid);
            GroupResourceShare share = new GroupResourceShare();
            share.setResource(r);
            share.setProvider(provider);
            share.setRemark(desc);
            share.setRecipient(recipient);
            share.setShareWrap(shareWrap);
            share.setCreateDate(createDate);

            resourceService.createShareAndSplitReceive(groupId, shareWrap, share, members);


        }
        //同一次分享多个资源，合并发送邮件
        //TODO
        String title = PropertyUtil.getEmailShareNoticeTitle();
        File file = new ClassPathResource("emailShareNotice.properties").getFile();
        String contentTemplate = FileUtils.readFileToString(file, "UTF-8");
        String sender = loginMemberName;
        String content = contentTemplate.replaceAll("\\$\\{sender\\}", sender);
        StringBuffer resourceName = new StringBuffer("");
        for (Long rid : resourceIdList) {
            resourceName.append(resourceService.getResourceById(rid).getName() + "、");
        }
        if (resourceIdList.size() > 0) {
            resourceName.setLength(resourceName.length() - 1);
        }
        content = content.replaceAll("\\$\\{resourceName\\}", resourceName.toString());
        String[] emailArr = emails.toArray(new String[emails.size()]);

//		try {
//			MailSender.sendMail(emailArr, title, content);
//		} catch (Exception ex) {
//		}
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 获得被分享人姓名邮件等信息
     *
     * @param memberName 分号分隔的多个person的memberName
     * @param memberIds  分号分割的多个team的memberId
     * @return
     * @throws Exception
     */
    private Map getSharedMemberId(String memberName, String memberIds) throws Exception {
        Map map = new HashMap();
        StringBuffer buf = new StringBuffer();
        StringBuffer error = new StringBuffer();

        List<Long> idList = new ArrayList<Long>();
        List<String> namesList = new ArrayList<String>();
        List<String> emailList = new ArrayList<String>();
        Map<Long, String> individualAndTeamMember = new LinkedHashMap<Long, String>();
        Set<String> individualAndTeamMemberEmail = new LinkedHashSet<String>();
        if (memberName != null && memberName.trim().length() > 0) {
            String[] names = memberName.trim().split(";");
            for (String name : names) {
                Member member = grouperService.getMemberByNameAndAccount(name, name);
                if (member == null || member.getId() <= 0) {
                    error.append(name).append(";");
                } else {
                    idList.add(member.getId());
                    individualAndTeamMember.put(member.getId(), name);
                    buf.append(name).append(";");
                    namesList.add(name);
                    User user = userService.getUserByAccount(member.getAccount());
                    if (user.getEmail() != null
                            && user.getEmail().trim().length() > 0) {
                        emailList.add(user.getEmail());
                        individualAndTeamMemberEmail.add(user.getEmail());
                    }
                }
            }
        }
        if (error.length() > 0) {
            error.deleteCharAt(error.length() - 1);
            throw new GroupsException("用户：" + error.toString() + "不存在。");
        }
        if (memberIds != null && memberIds != ""
                && memberIds.trim().length() > 0) {
            String[] temp = null;
            if (memberIds.indexOf(";") != -1) {
                temp = memberIds.trim().split(";");
            } else {
                temp = memberIds.trim().split(",");
            }
            for (String mid : temp) {
                long id = Long.parseLong(mid.trim());
                Member bean = grouperService.getTeam(id);
                if (bean == null) {
                    throw new GroupsException("ID为" + id + "的用户组不存在。");
                }
                buf.append(bean.getSignature()).append(";");
                idList.add(id);
                namesList.add(bean.getSignature());
                Member[] members = grouperService.getMembersInTeam(id);
                if (members != null && members.length > 0) {
                    for (Member member : members) {
                        User user = userService.getUserByAccount(member.getAccount());
                        individualAndTeamMember.put(member.getId(), member.getAccount());
                        if (user.getEmail() != null
                                && user.getEmail().trim().length() > 0) {
                            emailList.add(user.getEmail());
                            individualAndTeamMemberEmail.add(user.getEmail());
                        }
                    }
                }
            }
        }
        if (idList.isEmpty()) {
            throw new GroupsException("没有收件人参数。");
        }
        buf.deleteCharAt(buf.length() - 1);
        long[] ids = new long[idList.size()];
        int ii = 0;
        for (Long id : idList) {
            ids[ii++] = id;
        }
        map.put("ids", ids);
        map.put("namesList", namesList);
        map.put("recipient", buf.toString());
        map.put("emailList", emailList);
        //以下数据用户shareResourceToMember_v3
        map.put("individualAndTeamMember", individualAndTeamMember);
        map.put("individualAndTeamMemberEmail", individualAndTeamMemberEmail);

        return map;
    }

    /**
     * 获得我的接收数量
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getMyNewReceiveCount", produces = "application/json; charset=UTF-8")
    public String getMyNewReceiveCount() {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        long count = 0;
        if (currentUserWrap != null) {
            Long memberId = currentUserWrap.getMemberId();
            count = resourceService.getMyNewReceiveCount(memberId);
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"count\":").append(count);
        buffer.append("}");
        return buffer.toString();
    }

    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/getMyReceiveResources", "/getMyReceiveResources_v2"}, produces = "application/json; charset=UTF-8")
    public String getMyReceiveResources(Long memberId, Integer start, Integer limit) throws Exception {
        if (memberId == null || memberId < 0) {
            memberId = 0L;
        }
        start = start == null ? 0 : start;
        limit = limit == null ? Integer.MAX_VALUE : limit;
        long totalCount = 0;

        GroupResourceReceive[] list = resourceService.getMyReceiveResource(memberId, start, limit);
        totalCount = list.length;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"totalCount\":").append(totalCount).append(",");
        buffer.append("\"resources\":[");
        if (list != null) {
            for (int i = 0; i < totalCount; ++i) {
                GroupResourceReceive bean = list[i];
                GroupResourceShare share = bean.getShare();
                GroupResource resourceBean = bean.getResource();
                buffer.append("{");
                buffer.append("\"id\":").append(bean.getId()).append(",");
                buffer.append("\"resourceId\":").append(resourceBean.getId()).append(",");
                buffer.append("\"shareId\":").append(share.getId()).append(",");
                buffer.append("\"remark\":\"").append(JS.quote(HTML.escape(share.getRemark()))).append("\",");
                buffer.append("\"provider\":\"").append(share.getProvider().getName()).append("\",");
                String n = resourceBean.getName();
                if (resourceBean.getFilePreName() != null && n != null && !n.startsWith(resourceBean.getFilePreName())) {
                    n = resourceBean.getOriginalName();
                }
                String name = JS.quote(HTML.escape(n));
                buffer.append("\"name\":\"").append(name).append("\",");
                String displayName = name;
                buffer.append("\"displayName\":\"").append(displayName).append("\",");
                if (resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK) {
                    String linkPath = JS.quote(HTML.escape(resourceBean
                            .getLinkPath() == null ? "" : resourceBean.getLinkPath()));
                    buffer.append("\"linkPath\":\"").append(linkPath).append("\",");
                }
                buffer.append("\"filePath\":\"").append(
                        resourceBean.getFilePath()).append("\",");
                buffer.append("\"size\":").append(resourceBean.getSize()).append(",");
                String creationDate = resourceBean.getCreateDate().toString();
                buffer.append("\"creationDate\":\"").append(
                        creationDate.substring(0, creationDate.length() - 2))
                        .append("\",");
                buffer.append("\"type\":").append(resourceBean.getResourceType()).append(",");
                buffer.append("\"shareDate\":\"").append(df.format(share.getCreateDate())).append("\"");
                buffer.append("},");
            }
        }

        if (totalCount > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    /**
     * 新版查看接收接口
     *
     * @param memberId 接收所有者，当前登录用户可以不传
     * @param start
     * @param limit
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/getMyReceive", produces = "application/json; charset=UTF-8")
    public String getMyReceive(Long memberId, Integer start, Integer limit) throws Exception {
        if (memberId == null || memberId <= 0) {
            Subject currentUser = SecurityUtils.getSubject();
            Session session = currentUser.getSession();
            CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
            memberId = currentUserWrap.getMemberId();
        }
        start = start == null ? 0 : start;
        limit = limit == null ? Integer.MAX_VALUE : limit;
        int totalCount = 0;
        int newCount = 0;
        List<Object[]> result = resourceService.getMyReceive(memberId, start, limit);
        ArrayList<Object[]> shareRecords = new ArrayList<Object[]>();
        HashSet<Long> uniqueWrap = new HashSet<Long>();
        HashMap<Long, ArrayList<GroupResourceReceive>> shareReceiveMap = new HashMap<Long, ArrayList<GroupResourceReceive>>();
        for (int i = 0; i < result.size(); i++) {
            Object[] curResult = result.get(i);
            GroupResourceReceive r = (GroupResourceReceive) curResult[0];
            GroupResourceShare s = (GroupResourceShare) curResult[1];
            if (s.getShareWrap() != null) {
                //存在wrap的shareBean
                Object[] thisRecord = new Object[6];
                ShareWrap shareWrap = s.getShareWrap();//(ShareWrap) curResult[5];
                if (!uniqueWrap.contains(shareWrap.getId())) {//过滤重复warp
                    uniqueWrap.add(shareWrap.getId());
                    long providerId = shareWrap.getProviderId();
                    Member provider = grouperService.getMemberById(providerId);
                    thisRecord[0] = provider;
                    String remark = shareWrap.getMessage();
                    thisRecord[1] = remark;
                    Timestamp shareDate = shareWrap.getCreateDate();
                    thisRecord[2] = shareDate;
                    Long shareId = shareWrap.getId();
                    thisRecord[3] = shareId;
                    Integer shareType = s.getShareType();//(Integer) curResult[6]==null?1:(Integer) curResult[6];
                    thisRecord[4] = shareType;
                    List<ShareResponse> response = resourceService.getShareResponseByShareWrapAndResponder(shareId, memberId);
                    ShareResponse uniqueResponse = null;
                    if (!response.isEmpty()) {
                        uniqueResponse = response.get(0);
                    }
                    thisRecord[5] = uniqueResponse;
                    shareRecords.add(thisRecord);
                }
                if (!shareReceiveMap.containsKey(shareWrap.getId())) {
                    shareReceiveMap.put(shareWrap.getId(), new ArrayList<GroupResourceReceive>());
                }
                ArrayList<GroupResourceReceive> resourceList = shareReceiveMap.get(shareWrap.getId());
                resourceList.add(r);
            } else {
                //对于不存在warp的旧分享数据
                Object[] thisRecord = new Object[6];
                Long shareId = s.getId();//(Long) curResult[0];
                if (!uniqueWrap.contains(shareId)) {//过滤重复warp
                    uniqueWrap.add(shareId);
                    Member provider = s.getProvider();//(Member) curResult[3];
                    thisRecord[0] = provider;
                    String remark = s.getRemark();//(String) curResult[2];
                    thisRecord[1] = remark;
                    Timestamp shareDate = (Timestamp) s.getCreateDate();//(Timestamp) curResult[1];
                    thisRecord[2] = shareDate;

                    thisRecord[3] = shareId;
                    Integer shareType = s.getShareType();//(Integer) curResult[6]==null?1:(Integer) curResult[6];
                    thisRecord[4] = shareType;
                    Long shareWrapId = shareId, responderId = 0L;//0表示本人
                    ShareResponse uniqueResponse = null;
                    thisRecord[5] = uniqueResponse;
                    shareRecords.add(thisRecord);
                }
                if (!shareReceiveMap.containsKey(shareId)) {
                    shareReceiveMap.put(shareId, new ArrayList<GroupResourceReceive>());
                }
                ArrayList<GroupResourceReceive> resourceList = shareReceiveMap.get(shareId);
                resourceList.add(r);
            }

        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"shareRecords\":[");
        for (int i = 0; i < shareRecords.size(); ++i) {
            Object[] shareRecord = shareRecords.get(i);

            buffer.append("{");
            buffer.append("\"provider\":").append("\"" + ((Member) shareRecord[0]).getName() + "\"").append(",");
            //buffer.append("\"remark\":").append("\""+(String)shareRecord[1]+"\"").append(",");
            buffer.append("\"remark\":").append("\"" + JS.quote(HTML.escape((String) shareRecord[1])) + "\"").append(",");
            buffer.append("\"shareDate\":\"").append((df.format((Date) shareRecord[2]))).append("\",");
            buffer.append("\"shareTimestamp\":").append(((Date) shareRecord[2]).getTime()).append(",");
            buffer.append("\"shareId\":").append((Long) shareRecord[3]).append(",");
            if ((ShareResponse) shareRecord[5] != null) {
                ShareResponse resp = (ShareResponse) shareRecord[5];
                String content = resp.getContent();
                if (content != null && !"".equals(content)) {
                    buffer.append("\"response\":\"").append(JS.quote(HTML.escape(content))).append("\",");
                }
            }

            buffer.append("\"resources\":[");
            ArrayList<GroupResourceReceive> rList = shareReceiveMap.get((Long) shareRecord[3]);
            if (rList != null && rList.size() > 0) {
                for (GroupResourceReceive rc : rList) {
                    buffer.append("{");
                    buffer.append("\"id\":").append(rc.getId()).append(",");
                    buffer.append("\"resourceId\":").append(rc.getResource().getId()).append(",");
                    buffer.append("\"displayName\":\"").append(rc.getResource().getName()).append("\",");
                    buffer.append("\"size\":").append(rc.getResource().getSize()).append(",");
                    String status = rc.getStatus() == null ? "0" : rc.getStatus().toString();
                    buffer.append("\"status\":\"").append(status).append("\",");
                    buffer.append("\"type\":").append(rc.getResource().getResourceType());
                    buffer.append("},");
                }
                if (rList.size() > 0) {
                    buffer.setLength(buffer.length() - 1);
                }
            }
            buffer.append("]");
            buffer.append("},");

        }
        if (shareRecords.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }


    /**
     * 新版查看我的分享接口
     *
     * @param start
     * @param limit
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/getMyShared", produces = "application/json; charset=UTF-8")
    public String getMyShared(Integer start, Integer limit) throws Exception {
        start = start == null ? 0 : start;
        limit = limit == null ? Integer.MAX_VALUE : limit;
        List<Object[]> result = resourceService.getMyShared(start, limit);

        ArrayList<Object[]> shareRecord = new ArrayList<Object[]>();
        HashSet<Long> uniqueFilter = new HashSet<Long>();
        HashMap<Long, ArrayList<GroupResource>> shareResourceMap = new HashMap<Long, ArrayList<GroupResource>>();
        HashMap<Long, ArrayList<GroupResource>> shareWrapResourceMap = new HashMap<Long, ArrayList<GroupResource>>();
        for (int i = 0; i < result.size(); i++) {
            GroupResourceShare share = (GroupResourceShare) result.get(i)[0];
            ShareWrap shareWrap = (ShareWrap) result.get(i)[1];

            Object[] o = new Object[7];
            if (share.getShareWrap() == null) {
                //旧分享数据
                String recipients = share.getRecipient();
                String[] memberAndTeam = divide(recipients);

                o[0] = memberAndTeam[0];//share.getRecipient();//接收人
                o[1] = share.getRemark();//分享留言
                o[2] = share.getCreateDate();//分享时间
                o[3] = share.getId();//分享id
                o[4] = true;//是否旧版分享
                o[5] = share.getResource().getSize();//总大小
                o[6] = memberAndTeam[1];//接收组
                shareRecord.add(o);
                if (shareResourceMap.containsKey(share.getId())) {
                    ArrayList<GroupResource> temp = shareResourceMap.get(share.getId());
                    temp.add(share.getResource());

                } else {
                    ArrayList<GroupResource> temp = new ArrayList<GroupResource>();
                    temp.add(share.getResource());
                    shareResourceMap.put(share.getId(), temp);
                }

            } else {
                //对于新分享
                String recipients = shareWrap.getRecipients();
                String[] memberAndTeam = divide(recipients);

                o[0] = memberAndTeam[0];//shareWrap.getRecipients();//接收人
                o[1] = shareWrap.getMessage();//分享留言
                o[2] = shareWrap.getCreateDate();//分享时间
                o[3] = shareWrap.getId();//分享id
                o[4] = false;//是否新版分享
                o[5] = shareWrap.getSize();//总大小
                o[6] = memberAndTeam[1];//接收组
                if (!uniqueFilter.contains(shareWrap.getId())) {
                    shareRecord.add(o);
                    uniqueFilter.add(shareWrap.getId());
                }

                if (shareWrapResourceMap.containsKey(shareWrap.getId())) {
                    ArrayList<GroupResource> temp = shareWrapResourceMap.get(shareWrap.getId());
                    temp.add(share.getResource());
                } else {
                    ArrayList<GroupResource> temp = new ArrayList<GroupResource>();
                    temp.add(share.getResource());
                    shareWrapResourceMap.put(shareWrap.getId(), temp);
                }
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"shareRecord\":[");
        for (int i = 0; i < shareRecord.size(); ++i) {
            Object[] record = shareRecord.get(i);
            String recipients = (String) record[0];//接收人
            String remark = (String) record[1];//分享留言
            Date date = (Date) record[2];//分享时间
            Long shareId = (Long) record[3];//分享id
            boolean isOld = (Boolean) record[4];//是否新版分享
            long size = (Long) record[5];//总大小
            String groupReceivers = (String) record[6];//接收组

            buffer.append("{");
            buffer.append("\"recipients\":\"").append(recipients).append("\",");
            buffer.append("\"groupReceivers\":\"").append(groupReceivers).append("\",");
            buffer.append("\"remark\":").append("\"" + JS.quote(HTML.escape(remark)) + "\"").append(",");
            buffer.append("\"shareDate\":\"").append(df.format(date)).append("\",");
            buffer.append("\"shareTimestamp\":").append(date.getTime()).append(",");
            buffer.append("\"shareId\":").append(shareId).append(",");
            buffer.append("\"resources\":[");

            ArrayList<GroupResource> resourceList = null;
            if (!isOld) {
                resourceList = shareWrapResourceMap.get(shareId);
            } else {
                resourceList = shareResourceMap.get(shareId);
            }
            for (int j = 0; j < resourceList.size(); j++) {
                GroupResource r = resourceList.get(j);
                buffer.append("{");
                buffer.append("\"resourceId\":").append(r.getId()).append(",");
                buffer.append("\"displayName\":\"").append(r.getName()).append("\",");
                buffer.append("\"type\":").append(r.getResourceType()).append(",");
                buffer.append("\"size\":").append(r.getSize()).append("");
                buffer.append("},");
            }
            if (resourceList.size() > 0) {
                buffer.setLength(buffer.length() - 1);
            }

            buffer.append("]");
            buffer.append("},");
        }
        if (shareRecord.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    private String[] divide(String recipients) {
        String[] result = new String[2];
        if (recipients != null) {
            String[] temp = recipients.split(";");
            StringBuffer memberBuf = new StringBuffer();
            StringBuffer teamBuf = new StringBuffer();
            for (String mem : temp) {
                Member member = grouperService.getMemberByNameAndAccount(mem, mem);
                if (member != null) {
                    memberBuf.append(mem + ";");
                } else {
                    teamBuf.append(mem + ";");
                }
            }
            if (memberBuf.length() > 0 && memberBuf.charAt(memberBuf.length() - 1) == ';') {
                memberBuf.deleteCharAt(memberBuf.length() - 1);
            }
            if (teamBuf.length() > 0 && teamBuf.charAt(teamBuf.length() - 1) == ';') {
                teamBuf.deleteCharAt(teamBuf.length() - 1);
            }
            result[0] = memberBuf.toString();
            result[1] = teamBuf.toString();
        }
        return result;

    }

    /**
     * 根据接收id将接收设为已读
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "/markReceived", produces = "application/json; charset=UTF-8")
    public String markReceived(Long[] id) throws Exception {
        if (id == null || id.length <= 0) {
            throw new GroupsException("参数错误，id列表为空");
        }
        resourceService.markReceived(id);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 获取我的分享
     *
     * @param start
     * @param limit
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/getMySharedResources", "/getMySharedResources_v2"}, produces = "application/json; charset=UTF-8")
    public String getMySharedResources(Integer start, Integer limit) throws Exception {
        start = start == null ? 0 : start;
        limit = limit == null ? Integer.MAX_VALUE : limit;
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        long totalCount = 0;
        List<GroupResourceShare> list = resourceService.getSharedResourceByProvider(memberId, start, limit);
        totalCount = list.size();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"totalCount\":").append(totalCount).append(",");
        buffer.append("\"resources\":[");
        for (int i = 0; i < totalCount; ++i) {
            GroupResourceShare bean = list.get(i);
            GroupResource resourceBean = bean.getResource();
            buffer.append("{");
            buffer.append("\"id\":").append(bean.getId()).append(",");
            buffer.append("\"resourceId\":").append(resourceBean.getId()).append(",");
            buffer.append("\"memberName\":\"").append(JS.quote(HTML.escape(bean.getRecipient()))).append("\",");
            String n = resourceBean.getName();
            if (resourceBean.getFilePreName() != null && n != null && !n.startsWith(resourceBean.getFilePreName())) {
                n = resourceBean.getOriginalName();
            }
            String name = JS.quote(HTML.escape(n));
            buffer.append("\"name\":\"").append(name).append("\",");
            String displayName = name;
            buffer.append("\"displayName\":\"").append(displayName).append("\",");
            buffer.append("\"filePath\":\"").append(
                    resourceBean.getFilePath()).append("\",");
            buffer.append("\"size\":").append(resourceBean.getSize()).append(",");
            String creationDate = resourceBean.getCreateDate().toString();
            buffer.append("\"creationDate\":\"").append(
                    creationDate.substring(0, creationDate.length() - 2))
                    .append("\",");
            buffer.append("\"type\":").append(resourceBean.getResourceType()).append(",");
            buffer.append("\"shareType\":").append(bean.getShareType() == null ? GroupResourceShare.INNER_SHARE : bean.getShareType().intValue()).append(",");
            String validTimes = "";
            String expiredDate = "永久";
            if (bean.getResourceCode() != null) {
                validTimes = "" + bean.getResourceCode().getValidTimes();
                Date ed = bean.getResourceCode().getExpiredDate();
                if (ed != null) {
                    expiredDate = df.format(ed);
                }
            }
            buffer.append("\"validTimes\":\"").append(validTimes).append("\",");
            buffer.append("\"expiredDate\":\"").append(expiredDate).append("\",");
            buffer.append("\"shareDate\":\"").append(df.format(bean.getCreateDate())).append("\"");
            buffer.append("},");
        }
        if (totalCount > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    /**
     * 根据id删除未上传完成资源
     * 包括数据库记录和物理文件
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "deleteChunckResource", produces = "application/json; charset=UTF-8")
    public String deleteChunckResource(Long resourceId) throws Exception {
        if (resourceId != null && resourceId > 0) {
            GroupResource bean = resourceService.getResourceById(resourceId);
            if (bean.getResourceType() == GroupResource.RESOURCE_TYPE_LINK || bean.getResourceType() == GroupResource.RESOURCE_TYPE_FILE) {
                resourceService.deleteChunkResource(resourceId);
            } else {
                throw new GroupsException("用户帐户名或者密码为空");
            }
        } else
            throw new GroupsException("参数不对！");
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 获取当前用户的所有未上传完成的资源记录
     *
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "getMyChunckResources", produces = "application/json; charset=UTF-8")
    public String getMyChunckResources() throws Exception {
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        List<GroupResource> ids = resourceService.getAllChunckResources(memberId);
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"total\":").append(ids.size()).append(", \"file\":");
        buffer.append("[");
        for (GroupResource id : ids) {
            String name = id.getOriginalName();
            if (name != null && name.startsWith(".")) {
                name = name.substring(1);
            }
            buffer.append("{");
            buffer.append("\"resourceId\":\"").append(id.getId()).append("\",");
            buffer.append("\"groupId\":\"").append(id.getGroup().getId()).append("\",");
            buffer.append("\"parentId\":\"").append(id.getParentId()).append("\",");
            buffer.append("\"filename\":\"").append(name).append("\",");
            buffer.append("\"lastModified\":\"").append(id.getFileLastModified()).append("\",");
            buffer.append("\"no\":\"").append(id.getReserveField1()).append("\"},");
        }
        if (ids.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]");
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * 上传柜子资源
     *
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "uploadChunkResource", produces = "application/json; charset=UTF-8")
    public String uploadChunkResource(HttpServletRequest request) throws Exception {

        String contentRange = request.getHeader("Content-Range");
        //上传资源的数据库id
        List<GroupResource> resources = new ArrayList<GroupResource>();
        if (contentRange != null && contentRange != "") {
            resources = this._chunkUpload(request);

        } else {
            Long groupId = ServletRequestUtils.getLongParameter(request, "groupId", 0L);
            Long parentId = ServletRequestUtils.getLongParameter(request, "parentId", 0L);
            resources = this._uploadResource(groupId, parentId, null, null, null, null, request, null, null, null, null, null);
        }
        StringBuffer buffer = new StringBuffer();
        int i = 1;
        buffer.append("{");
        buffer.append("\"files\":").append("[");
        for (GroupResource resource : resources) {
            String ext = resource.getFileExt();
            if (ext != null && ext.startsWith(".")) {
                ext = ext.substring(1);
            }
            String name = resource.getName();
            if (name != null && name.startsWith(".")) {
                name = name.substring(1);
            }
            buffer.append("{");
            buffer.append("\"no\":\"").append(resource.getReserveField1()).append("\",");
            buffer.append("\"filename\":\"").append(name).append("\",");
            buffer.append("\"ext\":\"").append(ext).append("\",");
            buffer.append("\"groupId\":\"").append(resource.getGroup().getId()).append("\",");
            buffer.append("\"parentId\":\"").append(resource.getParentId()).append("\",");
            buffer.append("\"resourceId\":\"").append(resource.getId()).append("\"},");
        }
        if (resources.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]");
        buffer.append("}");
        return buffer.toString();
    }

    //modify by mi
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "uploadChunkResource_v2", produces = "application/json; charset=UTF-8")
    public String uploadChunkResource_v2(HttpServletRequest request) throws Exception {

        String contentRange = request.getHeader("Content-Range");
        //上传资源的数据库id
        List<GroupResource> resources = new ArrayList<GroupResource>();
        Long id = 0L;
        if (contentRange != null && contentRange != "") {
            id = this._chunkUpload_v2(request);
        } else {
            Long groupId = ServletRequestUtils.getLongParameter(request, "groupId", 0L);
            Long parentId = ServletRequestUtils.getLongParameter(request, "parentId", 0L);
//			resources = this._uploadResource(groupId, parentId,null, null, null, null, request,null, null, null,null ,null);
            id = this._uploadResource_v3(groupId, parentId, null, null, null, null, request, null, null, null, null, null);
            System.out.println("前端返回uploadResource接口" + id);
            System.out.println("前端返回执行成功");
        }
        return "{\"asyncId\":" + id + "}";
    }

    /**
     * 分块上传
     *
     * @return
     * @throws Exception
     */
    private List<GroupResource> _chunkUpload(HttpServletRequest request) throws Exception {
        String contentRange = request.getHeader("Content-Range");
        String contentDiposition = request.getHeader("Content-Disposition");
        Long groupId = ServletRequestUtils.getLongParameter(request, "groupId", 0L);
        Long parentId = ServletRequestUtils.getLongParameter(request, "parentId", 0L);
        Long resourceId = ServletRequestUtils.getLongParameter(request, "resourceId", 0L);
        Long lastModified = ServletRequestUtils.getLongParameter(request, "lastModified", 0L);
        String name = ServletRequestUtils.getStringParameter(request, "name", null);
        String path = ServletRequestUtils.getStringParameter(request, "path", null);

        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member creator = grouperService.getMemberById(memberId);

        Group group = null;
        if (groupId == 0) {
            if (name == null) throw new GroupsException("找不到要上传的文件柜");
            Category personCategory = categoryService.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory());
            group = groupService.getGroupByDisplyName(personCategory.getId(), name);
        }
        group = groupService.getGroupById(groupId.longValue());
        if (group == null) {
            throw new GroupsException("找不到要上传的文件柜");
        }

        // 若传进来的不是parentId是path的话，则查找该路径所在的文件夹的id
        if (parentId == 0 && path != null) {
            String[] path_temp = path.split("/");
            for (int i = 1; i < path_temp.length; i++) {
                GroupResource temp = resourceService.getResource(groupId, path_temp[i], parentId,
                        GroupResource.RESOURCE_TYPE_DIRECTORY);
                if (temp == null) {
                    throw new GroupsException("找不到该上传的路径");
                }
                parentId = temp.getId();
            }
        }
        //////////////////////////////////////////////////////////////////////////
        List<GroupResource> uploadIds = new ArrayList<GroupResource>();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if (multipartResolver.isMultipart(request)) {
            //将request变成多部分request
            Timestamp now = new Timestamp(System.currentTimeMillis());
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //获取multiRequest 中所有的文件名
            Iterator<String> iter = multiRequest.getFileNames();
            if (iter == null || !iter.hasNext()) {
                throw new GroupsException("资源文件列表为空列表");
            }

            //一次遍历所有文件
            List<MultipartFile> multipartFile = multiRequest.getFiles("filedata");
            if (multipartFile == null || multipartFile.size() <= 0) {
                multipartFile = multiRequest.getFiles("Filedata");
            }
            if (multipartFile == null || multipartFile.size() <= 0) {
                throw new GroupsException("资源文件列表为空列表");
            }
            long sumsize = 0L;
            for (int i = 0; i < multipartFile.size(); i++) {

                //一次遍历所有文件
                String filedataFileName = multipartFile.get(i).getOriginalFilename();
                String contentType = multipartFile.get(i).getContentType();
                long fileSize = multipartFile.get(i).getSize();
                File file = null;
                try {
                    file = File.createTempFile("tmp", null);
                    multipartFile.get(i).transferTo(file);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String fileName = null;
                if (file != null) {
                    try {
                        fileName = URLDecoder.decode(filedataFileName, "UTF-8");
                    } catch (Exception ex) {
                        fileName = filedataFileName;
                    }
                }
                //判断是否是第一块分片，是第一块分片就创建数据库记录并且上传否则上传后更新
                long[] rangeFull = FileUtil.splitContentRange(contentRange);
                long startByte = rangeFull[0];
                long endByte = rangeFull[1];
                long total = rangeFull[2];
                long len = total / 1024;
                if (len * 1024 < total) {
                    len++;
                }

                if (startByte == 0L) {
                    //是第一块就创建记录
                    GroupResource resourceBean = new GroupResource();
                    resourceBean.setContentType(contentType);
                    resourceBean.setCreateDate(now);
                    resourceBean.setGroup(group);
                    resourceBean.setGroupName(group.getName());
                    resourceBean.setCreatorId(memberId);
                    resourceBean.setMemberName(creator.getName());
                    resourceBean.setName(fileName);
                    // 上传未完成
                    resourceBean.setFinishSign(GroupResource.UPLOAD_UNFINISH);
                    resourceBean.setDetailSize(Long.toString(total));
//    				long len = total / 1024;
//					if (len*1024 < total) {
//						len++;
//					}
                    resourceBean.setSize(len);
                    resourceBean.setParentId(parentId < 0 ? 0 : parentId);
                    if (lastModified != 0L) {
                        Date fileLastModified = new Date(lastModified);
                        resourceBean.setFileLastModified(fileLastModified);
                    }


                    // ==========更新path==========
                    String path_temp = null;
                    if (parentId <= 0) {
                        path_temp = "/";
                    } else {
                        GroupResource temp = resourceService.getResourceById(parentId);
                        if (temp.getPath() == null || temp.getPath().equals("")) {
                            // rebuilt resource path
                            path_temp = resourceService.rebuiltPath(temp.getId());
                        } else {
                            path_temp = temp.getPath() + parentId + "/";
                        }
                    }
                    resourceBean.setPath(path_temp);
                    // ==========更新path==========
                    resourceBean.setResourceType(GroupResource.RESOURCE_TYPE_FILE);
                    resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
                    //只是记录日志并写入数据库，并未上传
                    resourceService.createResource(resourceBean);
                    String uriPath = FilePathUtil.getFileTempPath(resourceBean);

                    try {
                        //上传文件
                        String blockIndex = FileUtil.createStrBlockIndex(0);
                        FileUtil.copyFileToServer(file, uriPath + blockIndex + "part.tmp", true);
                        uploadIds.add(resourceBean);
                    } catch (Exception ex) {
                        //上传文件失败，删除记录
                        resourceService.deleteResource(resourceBean.getId());
                    }
                    resourceBean.setReserveField1("1");
                    resourceService.modifyResource(resourceBean);

                } else if (endByte + 1 == total) {
                    //是最后一块就融合文件
                    GroupResource resourceBean = resourceService.getResourceById(resourceId);
                    String uriPath = FilePathUtil.getFileTempPath(resourceBean);
                    try {
                        //上传文件
                        String blockIndex = FileUtil.createStrBlockIndex(Integer.parseInt(resourceBean.getReserveField1()));
                        FileUtil.copyFileToServer(file, uriPath + blockIndex + "part.tmp", true);
                        uploadIds.add(resourceBean);
                    } catch (Exception e) {
                        //上传文件失败，并不删除数据库记录
                        e.printStackTrace();
                    }
                    resourceBean.setReserveField1(String.valueOf(Integer.parseInt(resourceBean.getReserveField1()) + 1));
                    resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
                    resourceBean.setRate(1);
                    if (lastModified != 0L) {
                        Date fileLastModified = new Date(lastModified);
                        resourceBean.setFileLastModified(fileLastModified);
                    }
                    resourceService.modifyResource(resourceBean);

                    //创建缩略图
                    resourceService.createThumbnail_new(resourceBean, PropertyUtil
                            .getThumbnailDefaultWidth(), PropertyUtil
                            .getThumbnailDefaultHeight(), PropertyUtil
                            .getThumbnailQuality());

                    //融合临时文件，成功后删除临时文件
                    String assembleFilePath = FilePathUtil.getFileFullPath(resourceBean);
                    URI assembleURIPath = new URI(assembleFilePath);
                    URI assembleDirURIPath = new URI(uriPath);
                    String targetFilePath = assembleURIPath.getPath();
                    String tempDirPath = assembleDirURIPath.getPath();
                    FileUtil.assembleFiles(tempDirPath, targetFilePath);
                    //modify by mi 上传成功就增加文件的大小
                    sumsize += len;
                    logService.addDownloadLog(group.getId(), group.getDisplayName(), resourceBean.getId(), resourceBean.getName());
                } else {
                    //中间块只上传更新数据库记录
                    GroupResource resourceBean = resourceService.getResourceById(resourceId);
                    String uriPath = FilePathUtil.getFileTempPath(resourceBean);
                    try {
                        //上传文件
                        String blockIndex = FileUtil.createStrBlockIndex(Integer.parseInt(resourceBean.getReserveField1()));
                        FileUtil.copyFileToServer(file, uriPath + blockIndex + "part.tmp", true);
                        uploadIds.add(resourceBean);
                    } catch (Exception e) {
                        //上传文件失败，并不删除数据库记录
                        e.printStackTrace();
                    }
                    resourceBean.setReserveField1(String.valueOf(Integer.parseInt(resourceBean.getReserveField1()) + 1));
                    if (lastModified != 0L) {
                        Date fileLastModified = new Date(lastModified);
                        resourceBean.setFileLastModified(fileLastModified);
                    }
                    resourceService.modifyResource(resourceBean);

                }
                //删除multipartFile转换成File的临时文件
                if (file.exists()) {
                    file.delete();
                }
            }
            //修改柜子的可用容量 by mi
            System.out.println("分片上传前柜子的可用容量：" + group.getAvailableCapacity());
            group.setAvailableCapacity(group.getAvailableCapacity() - sumsize);
            group.setUsedCapacity(group.getTotalFileSize() - group.getAvailableCapacity());
            groupDao.saveOrUpdateGroup(group);
            System.out.println("分片上传后柜子的可用容量：" + group.getAvailableCapacity());
        }
        return uploadIds;
    }

    private Long _chunkUpload_v2(HttpServletRequest request) throws Exception {
        String contentRange = request.getHeader("Content-Range");
        String contentDiposition = request.getHeader("Content-Disposition");
        Long groupId = ServletRequestUtils.getLongParameter(request, "groupId", 0L);
        Long parentId = ServletRequestUtils.getLongParameter(request, "parentId", 0L);
        Long resourceId = ServletRequestUtils.getLongParameter(request, "resourceId", 0L);
        Long lastModified = ServletRequestUtils.getLongParameter(request, "lastModified", 0L);
        String name = ServletRequestUtils.getStringParameter(request, "name", null);
        String path = ServletRequestUtils.getStringParameter(request, "path", null);

        Long id = 0L;
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession();
        CurrentUserWrap currentUserWrap = (CurrentUserWrap) session.getAttribute("currentUserWrap");
        Long memberId = currentUserWrap.getMemberId();
        Member creator = grouperService.getMemberById(memberId);
        Future<Boolean> isCompleted = null;

        Group group = null;
        if (groupId == 0) {
            if (name == null) throw new GroupsException("找不到要上传的文件柜");
            Category personCategory = categoryService.getCategoryByName(PropertyUtil.getDefaultPersonGroupCategory());
            group = groupService.getGroupByDisplyName(personCategory.getId(), name);
        }
        group = groupService.getGroupById(groupId.longValue());
        if (group == null) {
            throw new GroupsException("找不到要上传的文件柜");
        }

        // 若传进来的不是parentId是path的话，则查找该路径所在的文件夹的id
        if (parentId == 0 && path != null) {
            String[] path_temp = path.split("/");
            for (int i = 1; i < path_temp.length; i++) {
                GroupResource temp = resourceService.getResource(groupId, path_temp[i], parentId,
                        GroupResource.RESOURCE_TYPE_DIRECTORY);
                if (temp == null) {
                    throw new GroupsException("找不到该上传的路径");
                }
                parentId = temp.getId();
            }
        }
        //////////////////////////////////////////////////////////////////////////
        List<GroupResource> uploadIds = new ArrayList<GroupResource>();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if (multipartResolver.isMultipart(request)) {
            //将request变成多部分request
            Timestamp now = new Timestamp(System.currentTimeMillis());
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //获取multiRequest 中所有的文件名
            Iterator<String> iter = multiRequest.getFileNames();
            if (iter == null || !iter.hasNext()) {
                throw new GroupsException("资源文件列表为空列表");
            }

            //一次遍历所有文件
            List<MultipartFile> multipartFile = multiRequest.getFiles("filedata");
            if (multipartFile == null || multipartFile.size() <= 0) {
                multipartFile = multiRequest.getFiles("Filedata");
            }
            if (multipartFile == null || multipartFile.size() <= 0) {
                throw new GroupsException("资源文件列表为空列表");
            }
            long sumsize = 0L;
            for (int i = 0; i < multipartFile.size(); i++) {

                //一次遍历所有文件
                String filedataFileName = multipartFile.get(i).getOriginalFilename();
                String contentType = multipartFile.get(i).getContentType();
                long fileSize = multipartFile.get(i).getSize();
                File file = null;
                try {
                    file = File.createTempFile("tmp", null);
                    multipartFile.get(i).transferTo(file);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String fileName = null;
                if (file != null) {
                    try {
                        fileName = URLDecoder.decode(filedataFileName, "UTF-8");
                    } catch (Exception ex) {
                        fileName = filedataFileName;
                    }
                }
                //判断是否是第一块分片，是第一块分片就创建数据库记录并且上传否则上传后更新
                long[] rangeFull = FileUtil.splitContentRange(contentRange);
                long startByte = rangeFull[0];
                long endByte = rangeFull[1];
                long total = rangeFull[2];
                long len = total / 1024;
                if (len * 1024 < total) {
                    len++;
                }

                if (startByte == 0L) {
                    //是第一块就创建记录
                    GroupResource resourceBean = new GroupResource();
                    resourceBean.setContentType(contentType);
                    resourceBean.setCreateDate(now);
                    resourceBean.setGroup(group);
                    resourceBean.setGroupName(group.getName());
                    resourceBean.setCreatorId(memberId);
                    resourceBean.setMemberName(creator.getName());
                    resourceBean.setName(fileName);
                    // 上传未完成
                    resourceBean.setFinishSign(GroupResource.UPLOAD_UNFINISH);
                    resourceBean.setDetailSize(Long.toString(total));
//    				long len = total / 1024;
//					if (len*1024 < total) {
//						len++;
//					}
                    resourceBean.setSize(len);
                    resourceBean.setParentId(parentId < 0 ? 0 : parentId);
                    if (lastModified != 0L) {
                        Date fileLastModified = new Date(lastModified);
                        resourceBean.setFileLastModified(fileLastModified);
                    }


                    // ==========更新path==========
                    String path_temp = null;
                    if (parentId <= 0) {
                        path_temp = "/";
                    } else {
                        GroupResource temp = resourceService.getResourceById(parentId);
                        if (temp.getPath() == null || temp.getPath().equals("")) {
                            // rebuilt resource path
                            path_temp = resourceService.rebuiltPath(temp.getId());
                        } else {
                            path_temp = temp.getPath() + parentId + "/";
                        }
                    }
                    resourceBean.setPath(path_temp);
                    // ==========更新path==========
                    resourceBean.setResourceType(GroupResource.RESOURCE_TYPE_FILE);
                    resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_DELETE);
//					resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
                    //只是记录日志并写入数据库，并未上传
                    resourceService.createResource(resourceBean);
                    String uriPath = FilePathUtil.getFileTempPath(resourceBean);

                    try {
                        //上传文件
                        id = resourceBean.getId();
                        String blockIndex = FileUtil.createStrBlockIndex(0);
                        String detailSize = resourceBean.getDetailSize();
                        asyncService.copyFileToServer(resourceBean.getId(), file, resourceBean, uriPath + blockIndex + "part.tmp", true);
//						FileUtil.copyFileToServer(file, uriPath + blockIndex + "part.tmp", true);
                        uploadIds.add(resourceBean);
                    } catch (Exception ex) {
                        //上传文件失败，删除记录
                        resourceService.deleteResource(resourceBean.getId());
                    }
                    resourceBean.setReserveField1("1");
                    resourceService.modifyResource(resourceBean);

                } else if (endByte + 1 == total) {
                    //是最后一块就融合文件
                    GroupResource resourceBean = resourceService.getResourceById(resourceId);
                    String uriPath = FilePathUtil.getFileTempPath(resourceBean);
                    try {
                        //上传文件
                        String blockIndex = FileUtil.createStrBlockIndex(Integer.parseInt(resourceBean.getReserveField1()));
                        isCompleted = asyncService.copyFileToServer(resourceBean.getId(), file, resourceBean, uriPath + blockIndex + "part.tmp", true);
                        if (isCompleted.isDone()) {
                            resourceBean.setResourceStatus(GroupResource.RESOURCE_STATUS_NORMAL);
                            resourceService.modifyResource(resourceBean);
                        }
//    			        FileUtil.copyFileToServer(file, uriPath + blockIndex + "part.tmp", true);
                        uploadIds.add(resourceBean);
                    } catch (Exception e) {
                        //上传文件失败，并不删除数据库记录
                        e.printStackTrace();
                    }
                    resourceBean.setReserveField1(String.valueOf(Integer.parseInt(resourceBean.getReserveField1()) + 1));
                    resourceBean.setFinishSign(GroupResource.UPLOAD_FINISHED);
                    resourceBean.setRate(1);
                    if (lastModified != 0L) {
                        Date fileLastModified = new Date(lastModified);
                        resourceBean.setFileLastModified(fileLastModified);
                    }
                    resourceService.modifyResource(resourceBean);

                    //创建缩略图
                    resourceService.createThumbnail_new(resourceBean, PropertyUtil
                            .getThumbnailDefaultWidth(), PropertyUtil
                            .getThumbnailDefaultHeight(), PropertyUtil
                            .getThumbnailQuality());

                    //融合临时文件，成功后删除临时文件
                    String assembleFilePath = FilePathUtil.getFileFullPath(resourceBean);
                    URI assembleURIPath = new URI(assembleFilePath);
                    URI assembleDirURIPath = new URI(uriPath);
                    String targetFilePath = assembleURIPath.getPath();
                    String tempDirPath = assembleDirURIPath.getPath();
                    FileUtil.assembleFiles(tempDirPath, targetFilePath);
                    //modify by mi 上传成功就增加文件的大小
                    sumsize += len;
                    logService.addDownloadLog(group.getId(), group.getDisplayName(), resourceBean.getId(), resourceBean.getName());
                } else {
                    //中间块只上传更新数据库记录
                    GroupResource resourceBean = resourceService.getResourceById(resourceId);
                    String uriPath = FilePathUtil.getFileTempPath(resourceBean);
                    try {
                        //上传文件
                        String blockIndex = FileUtil.createStrBlockIndex(Integer.parseInt(resourceBean.getReserveField1()));
                        FileUtil.copyFileToServer(file, uriPath + blockIndex + "part.tmp", true);
                        uploadIds.add(resourceBean);
                    } catch (Exception e) {
                        //上传文件失败，并不删除数据库记录
                        e.printStackTrace();
                    }
                    resourceBean.setReserveField1(String.valueOf(Integer.parseInt(resourceBean.getReserveField1()) + 1));
                    if (lastModified != 0L) {
                        Date fileLastModified = new Date(lastModified);
                        resourceBean.setFileLastModified(fileLastModified);
                    }
                    resourceService.modifyResource(resourceBean);

                }
                //删除multipartFile转换成File的临时文件
                if (file.exists()) {
                    file.delete();
                }
            }
            //修改柜子的可用容量 by mi
            System.out.println("分片上传前柜子的可用容量：" + group.getAvailableCapacity());
            group.setAvailableCapacity(group.getAvailableCapacity() - sumsize);
            group.setUsedCapacity(group.getTotalFileSize() - group.getAvailableCapacity());
            groupDao.saveOrUpdateGroup(group);
            System.out.println("分片上传后柜子的可用容量：" + group.getAvailableCapacity());
        }
        return id;
    }

    /**
     * 回复分享
     *
     * @param shareId      分享id
     * @param replyContent 回复内容
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "replyShare", produces = "application/json; charset=UTF-8")
    public String replyShare(Long shareId, String replyContent) throws Exception {
        if (shareId == null || shareId <= 0) {
            throw new GroupsException("参数错误");
        }
        Long memberId = UserUtils.getCurrentMemberId();

        resourceService.replyShare(shareId, memberId, replyContent);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 获取我的分享细节
     *
     * @param shareId
     * @param isOld
     * @param onlyResponse
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "getMySharedDetails", produces = "application/json; charset=UTF-8")
    public String getMySharedDetails(Long shareId, Boolean isOld, Boolean onlyResponse) throws Exception {
        if (shareId == null || shareId <= 0) {
            throw new GroupsException("参数错误");
        }
        isOld = isOld == null ? false : isOld;
        onlyResponse = onlyResponse == null ? false : onlyResponse;
        List<Object[]> result = resourceService.getMySharedDetials(shareId, isOld, onlyResponse);
        if (result.isEmpty()) {
            throw new GroupsException("找不到相应的分享");
        }
        ArrayList<ShareResponse> responseList = new ArrayList<ShareResponse>();
        List<GroupResource> resourceList = new ArrayList<GroupResource>();
        String recipients = "";
        String remark = "";
        Date shareDate = null;
        Long shareIdd = 0L;
        Long size = 0L;
        if (!isOld) {
            if (!onlyResponse) {
                ShareWrap shareWrap = (ShareWrap) result.get(0)[0];
                recipients = shareWrap.getRecipients();
                remark = shareWrap.getMessage();
                shareDate = shareWrap.getCreateDate();
                shareIdd = shareWrap.getId();
                size = shareWrap.getSize();
                String[] resources = shareWrap.getResourceIds().trim().split(";");
                Long[] ids = new Long[resources.length];
                for (int i = 0; i < resources.length; i++) {
                    ids[i] = Long.valueOf(resources[i].trim());
                }
                resourceList = resourceService.getResourcesByIds(ids);
            }
            for (int i = 0; i < result.size(); i++) {
                ShareResponse response = (ShareResponse) result.get(i)[1];
                if (response != null) {
                    responseList.add(response);
                }
            }
        } else {
            if (!onlyResponse) {
                GroupResourceShare share = (GroupResourceShare) result.get(0)[0];
                recipients = share.getRecipient();
                remark = share.getRemark();
                shareDate = share.getCreateDate();
                shareIdd = share.getId();
                size = share.getResource().getSize();
                resourceList.add(share.getResource());
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        if (!onlyResponse) {
            buffer.append("\"recipients\":\"").append(recipients).append("\",");
            //buffer.append("\"remark\":\"").append(remark).append("\",");
            buffer.append("\"remark\":").append("\"" + JS.quote(HTML.escape(remark)) + "\"").append(",");
            buffer.append("\"shareDate\":\"").append(df.format(shareDate)).append("\",");
            buffer.append("\"shareTimestamp\":").append(shareDate.getTime()).append(",");
            buffer.append("\"shareId\":").append(shareIdd).append(",");
            buffer.append("\"isOld\":").append(isOld).append(",");
            buffer.append("\"size\":").append(size).append(",");

            buffer.append("\"resources\":[");

            for (int j = 0; j < resourceList.size(); j++) {
                GroupResource r = resourceList.get(j);
                buffer.append("{");
                buffer.append("\"resourceId\":").append(r.getId()).append(",");
                buffer.append("\"displayName\":\"").append(r.getName()).append("\",");
                buffer.append("\"size\":\"").append(r.getSize()).append("\"");
                buffer.append("},");
            }
            if (resourceList.size() > 0) {
                buffer.setLength(buffer.length() - 1);
            }

            buffer.append("],");
        }

        buffer.append("\"responses\":[");

        for (int j = 0; j < responseList.size(); j++) {
            ShareResponse r = responseList.get(j);
            buffer.append("{");
            buffer.append("\"responseId\":").append(r.getId()).append(",");
            buffer.append("\"content\":\"").append(JS.quote(HTML.escape(r.getContent()))).append("\",");
            buffer.append("\"memberId\":").append(r.getResponder().getId()).append(",");
            buffer.append("\"memberName\":\"").append(r.getResponder().getAccount()).append("\",");
            buffer.append("\"responseDate\":\"").append(df.format(r.getResponseDate())).append("\",");
            buffer.append("\"responseTimestamp\":").append(r.getResponseDate().getTime());
            buffer.append("},");
        }
        if (responseList.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }

        buffer.append("]");
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * 删除分享记录
     *
     * @param id shareId
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"deleteSharedResource", "/deleteSharedResource_v2"}, produces = "application/json; charset=UTF-8")
    public String deleteSharedResource(Long[] id) throws Exception {
        if (id == null || id.length == 0)
            throw new GroupsException("资源id为空，无法获取资源");
        resourceService.revokeShareResourceToMember(id);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 删除分享记录以及相应的response，receieve,shareWrap
     *
     * @param id shareWrapId
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = "deleteSharedResource_v3", produces = "application/json; charset=UTF-8")
    public String deleteSharedResource_v3(Long[] id) throws Exception {
        if (id == null || id.length == 0)
            throw new GroupsException("资源id为空，无法获取资源");
        resourceService.revokeShareResource(id);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 删除接收资源
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"deleteReceivedResource", "deleteReceivedResource_v2"}, produces = "application/json; charset=UTF-8")
    public String deleteReceivedResource(Long[] id) throws Exception {
        if (id == null || id.length == 0)
            throw new GroupsException("参数错误");
        resourceService.deleteResourceReceiveBean(id);
        return "{\"type\":\"success\",\"code\":\"200\", \"detail\": \"ok\",\"success\":true}";
    }

    /**
     * 根据id获取资源信息
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"getResourceInfo"}, produces = "application/json; charset=UTF-8")
    public String getResourceInfo(Long resourceId) throws Exception {

        GroupResource resourceBean = resourceService.getResourceById(resourceId);

        Map<String, String> keywords = PropertyUtil.getFolderKeyWordMap();
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"id\":").append(resourceBean.getId()).append(",");
        buffer.append("\"groupName\":\"").append(resourceBean.getGroupName()).append("\",");
        buffer.append("\"memberId\":").append(resourceBean.getCreatorId()).append(",");
        buffer.append("\"memberName\":\"").append(resourceBean.getMemberName()).append("\",");
        String name = JS.quote(HTML.escape(resourceBean.getName()));
        buffer.append("\"name\":\"").append(name).append("\",");
        String displayName = name;
        if (keywords.containsKey(name) && resourceBean.getResourceType() == GroupResource.RESOURCE_TYPE_DIRECTORY) {
            displayName = keywords.get(name);
            displayName = JS.quote(HTML.escape(displayName == null ? "" : displayName));
        }
        buffer.append("\"displayName\":\"").append(displayName).append("\",");
        String desc = JS.quote(HTML.escape(resourceBean.getDesc()));
        buffer.append("\"desc\":\"").append(desc == null ? "" : desc).append("\",");
        buffer.append("\"filePath\":\"").append(resourceBean.getFilePath()).append("\",");
        buffer.append("\"size\":").append(resourceBean.getSize()).append(",");
        buffer.append("\"contentType\":\"").append(resourceBean.getContentType()).append("\",");
        String creationDate = resourceBean.getCreateDate().toString();
        buffer.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2))
                .append("\",");
        buffer.append("\"type\":").append(
                resourceBean.getResourceType());
        buffer.append("}");
        return buffer.toString();
    }

    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"getSharedChildResources"}, produces = "application/json; charset=UTF-8")
    public String getSharedChildResources(Long shareId, Long parentId, Integer start, Integer limit) throws Exception {
        if (parentId == null || shareId == null) {
            throw new GroupsException("参数为空！");
        }
        start = start == null ? 0 : start.intValue();
        limit = limit == null ? Integer.MAX_VALUE : limit.intValue();
        List<GroupResource> resourceBeans = null;
        long totalCount = 0;

        // 进入下一层资源列表
        GroupResourceShare share = resourceService.getResourceShareBean(shareId);
        resourceBeans = resourceService.getResourcesByParent(parentId, GroupResource.RESOURCE_STATUS_NORMAL, start, limit, null);
        totalCount = resourceService.getResourcesAmountByParent(parentId);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        //提取面包屑
        buffer.append("\"parentId\":").append(parentId).append(",");
        buffer.append("\"totalCount\":").append(totalCount).append(",");
        buffer.append("\"resources\":[");
        for (int i = 0; i < resourceBeans.size(); ++i) {
            GroupResource resourceBean = resourceBeans.get(i);
            buffer.append("{");
            buffer.append("\"resourceId\":").append(resourceBean.getId()).append(",");
            buffer.append("\"shareId\":").append(share.getId()).append(",");
            buffer.append("\"provider\":\"").append(share.getProvider().getName()).append("\",");
            buffer.append("\"parentId\":").append(resourceBean.getParentId()).append(",");
            buffer.append("\"type\":").append(resourceBean.getResourceType()).append(",");
            String name = JS.quote(HTML.escape(resourceBean.getName()));
            buffer.append("\"name\":\"").append(name).append("\",");
            String displayName = name;
            buffer.append("\"displayName\":\"").append(displayName).append("\",");
            buffer.append("\"filePath\":\"").append(resourceBean.getFilePath()).append("\",");
            buffer.append("\"size\":").append(resourceBean.getSize()).append(",");
            String creationDate = resourceBean.getCreateDate().toString();
            buffer.append("\"creationDate\":\"").append(creationDate.substring(0, creationDate.length() - 2)).append("\",");
            buffer.append("\"shareDate\":\"").append(df.format(share.getCreateDate())).append("\"");
            buffer.append("},");
        }
        if (resourceBeans.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"getSharedChildResources_v2"}, produces = "application/json; charset=UTF-8")
    public String getSharedChildResources_v2(Long parentId, Integer start, Integer limit) throws Exception {
        if (parentId == null) {
            throw new GroupsException("参数为空！");
        }
        start = start == null ? 0 : start.intValue();
        limit = limit == null ? Integer.MAX_VALUE : limit.intValue();
        List<GroupResource> resourceBeans = null;

        // 进入下一层资源列表
        resourceBeans = resourceService.getResourcesByParent(parentId, GroupResource.RESOURCE_STATUS_NORMAL, start, limit, null);
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        buffer.append("\"resources\":[");
        for (int i = 0; i < resourceBeans.size(); ++i) {
            GroupResource resourceBean = resourceBeans.get(i);
            buffer.append("{");
            buffer.append("\"id\":").append(-1).append(",");
            buffer.append("\"resourceId\":").append(resourceBean.getId()).append(",");
            buffer.append("\"type\":").append(resourceBean.getResourceType()).append(",");
            String name = JS.quote(HTML.escape(resourceBean.getName()));
            String displayName = name;
            buffer.append("\"displayName\":\"").append(displayName).append("\",");
            buffer.append("\"size\":").append(resourceBean.getSize());
            buffer.append("},");
        }
        if (resourceBeans.size() > 0) {
            buffer.setLength(buffer.length() - 1);
        }
        buffer.append("]}");
        return buffer.toString();
    }

    /**
     * 根据resourceId返回该资源是所在父资源直接getResource的第几页
     *
     * @param parentId   父资源id
     * @param resourceId 被查找资源id
     * @param pageSize   分页大小
     * @return
     * @throws Exception
     */
    @RequiresUser
    @ResponseBody
    @RequestMapping(value = {"/locateResource"}, produces = "application/json; charset=UTF-8")
    public String locateResource(Long parentId, Long resourceId, Integer pageSize) throws Exception {
        if (parentId == null || parentId == 0) {
            throw new GroupsException("parentId不能为空");
        }
        if (resourceId == null || resourceId == 0) {
            throw new GroupsException("resourceId不能为空");
        }
        Integer start = 0;
        Integer limit = pageSize == null ? Integer.MAX_VALUE : pageSize.intValue();
        List<GroupResource> resourceBeans = new ArrayList<GroupResource>();
        int pageNow = 0;
        Boolean findFlag = false;
        long totalCount = 0;

        if (parentId <= 0) {
            long groupId = -parentId;
            totalCount = resourceService.getTopResourcesAmountByGroup(groupId);
            while (!findFlag && pageNow <= totalCount / pageSize) {
                resourceBeans = resourceService.getResourcesByGroup(groupId, start, limit, true, null);
                for (GroupResource resource : resourceBeans) {
                    if (resourceId.longValue() == resource.getId()) {
                        findFlag = true;
                        break;
                    }
                }
                start += pageSize;
                pageNow++;
            }

        } else {
            totalCount = resourceService.getResourcesAmountByParent(parentId);
            while (!findFlag && pageNow <= totalCount / pageSize) {
                resourceBeans = resourceService.getResourcesByParent(parentId, null, start, limit, null);
                for (GroupResource resource : resourceBeans) {
                    if (resourceId.longValue() == resource.getId()) {
                        findFlag = true;
                        break;
                    }
                }
                start += pageSize;
                pageNow++;
            }
        }
        JSONObject json = new JSONObject();
        if (findFlag) {
            json.put("found", "true");
            json.put("pageNow", pageNow);
            json.put("pageSize", limit);
        } else {
            json.put("found", "false");
            json.put("pageNow", 0);
            json.put("pageSize", limit);
        }
        return json.toString();
    }


}
