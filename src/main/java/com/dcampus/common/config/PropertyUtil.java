package com.dcampus.common.config;

import com.dcampus.common.util.Log;
import com.dcampus.common.util.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyUtil {

	private static Log log = Log.getLog(PropertyUtil.class);

	private static Properties properties;
	
	/**
	 * 系统级管理员路径
	 */
	public static final String ADMIN_PATH = "/admin";
	
	/**
	 * GrouperPermStrategy中用到
	 */
	public static long EVERYONE_TEAM_ID = 0L;
	

	/** 分类拥有子分类最大深度 * */
	private static int categoryMaxDepth;

	/** 圈子管理员最大数量 * */
	private static int groupManagersNum;

	/** 讨论区拥有子讨论区最大深度 * */
	private static int forumMaxLevel;

	/** 每层深度拥有讨论区的最大数量 * */
	private static int forumNumPerLevel;

	/** 圈子名字保留字 * */
	private static String groupNameKeyWord;

	/** 圈子地址保留字 * */
	private static String groupAddrKeyWord;

	/** 圈子默认图标 * */
	private static String groupDefaultIcon;

	/** 圈子默认logo * */
	private static String groupDefaultLogo;

	/** 圈子默认公告 * */
	private static String groupDefaultInform;

	/** 圈子资源存储根路径 * */
	private static String groupResourceRootPath;
	/** 用户个人头像根路径 * */
	private static String memberPicRootPath;
	
	private static String memberPicFolderPath;
	
	private static String customLogoRootPath;
	
	private static String customLogoFolderPath;

	private static String moduleLogoRootPath;

	private static String moduleLogoFolderPath;

	/**资源和数据库备份路径 **/
	private static String groupResourceBackupRootPath;
	/**数据库工具存放路径**/
	private static String databaseToolsPath;
	/**备份结束短消息**/
	private static String backupEndTopic;
	private static String backupEndBody;
	/**资源共享通知邮件标题**/
	private static String emailShareNoticeTitle;

/** 断点上传资源临时跟路径 */
	private static String mtuploadResourceRootPath;

	/**圈子图标资源存储根路径*/
	private static String groupIconRootPath;
	/**圈子图标资源存储文件夹*/
	private static String groupIconFolderPath;

	private static String groupThumbnailRootPath;
	private static String groupThumbnailFolderPath;
	/** 圈子资源最大空间 * */
	private static long groupResourceSize;

	/** 资源默认分页大小 * */
	private static int resourceDefaultPageSize;

	/** 圈子成员默认分页大小 * */
	private static int groupMemberDefaultPageSize;

	/** 圈子动态默认分页大小 * */
	private static int groupRecordsDefaultPageSize;

	/** 文章默认分页大小 * */
	private static int postDefaultPageSize;

	/** 主题默认分页大小 * */
	private static int threadDefaultPageSize;

	/** 默认讨论区名字 * */
	private static String defaultForumName;

	/** 图片存储根路径 * */
	private static String imageRootPath;

	/** 头像路径前缀 **/
	private static String iconPrefix;

	/** 关注的圈子 默认分页大小 * */
	private static int watchDefaultPageSize;

	/** 短消息最大数量 * */
	private static int messageMaxSize;

	/** 默认相册封面的imageId * */
	private static long defaultAlbumCoverImageId;

	/** 邀请函默认标题 * */
	private static String invitationTopic;

	/** 邀请函默认内容 * */
	private static String invitationBody;

	/** 邮箱后缀 * */
	private static String mailSuffix;

	/** 每人每天创建圈子的最大数量* */
	private static int maxGroupCreationPerDayPerMember;

	/** 是否需要审核圈子* */
	private static boolean auditGroup;

	/** 合法资源文件类型* */
	private static String volidResourceType;

	/** 合法图片文件类型* */
	private static String volidImageType;

	/** 获取加入圈子链接* */
	private static String joinGroupLink;

	/** 资源文件夹深度 **/
	private static int groupResourceDirDepth;

	/** 资源单文件大小限制 **/
	private static long groupResourceSingleFileSize;

	/** 超级管理员 **/
	private static String[] superAdmin;

	/** 分类默认分页 **/
	private static int categoryDefaultPageSize;

	/** 默认用户头像地址 **/
	private static String defaultIcon;

	/** 圈子默认分页 **/
	private static int groupDefaultPageSize;
	
	/** 查询默认分页 **/
	private static int SearchDefaultPageSize;

	/** 内置圈子类型 **/
	private static String[] buildinGroupType;
	
	private static String defaultGroupType;
	private static String personalGroupType;
	/** 邀请函有效使用次数 **/
	private static int invitationValidTimes;

	private static String exportAccountFileName;

	private static String[] defaultGroupCategory;
	/** 默认个人分类 */
	private static String defaultPersonGroupCategory;

	/** 默认组织名称 */
	private static String systemDefaultOrgName;

	private static String defaultAblumCategory;
	
	/**后台系统版本号**/
	private static String systemVersion;
	
	/**其他系统用户同步**/
	private static boolean synchronizeUser;
	private static String remoteSystem;
	
	private static String lmsUrl;
	private static String portalUrl;
	private static String ccnlUrl;
	private static String imsUrl;

	/** 是否使用ldap认证 */
	private static boolean ldapAuthenticate;
	private static boolean adAuthenticate;
	/** 默认项目管理员名称 */
	private static String systemDefaultManageTeamName;
	/** 文件夹关键字 */
	private static Map<String, String> folderKeyWordMap = new HashMap<String, String>();

	/** 默认发布系统信息圈子信息 */
	private static String defaultCmsGroup;

	/** 回收站名称 */
	private static String recyclerName;
	/** 本地机构名，供CNGI使用 */
	private static String local_institution;
	/**缩略图最大宽度*/
	private static int thumbnailMaxWith;
	/**缩略图最大高度*/
	private static int thumbnailMaxHeight;
	/**缩略图最大大小*/
	private static long thumbnailMaxLength;
	/**缩略图质量*/
	private static int thumbnailQuality;
	/**缩略比例*/
	private static double thumbnailProportion;
	/**缩略图类型*/
	private static String thumbnailContentType;
	/**缩略图最大宽度*/
	private static int thumbnailDefaultWidth;
	/**缩略图最大高度*/
	private static int thumbnailDefaultHeight;
	
	private static int thumbnailMinWidth;
	
	private static int thumbnailMinHeight;

	/**封面图宽度*/
	private static int coverWith;
	/**封面图高度*/
	private static int coverHeight;
	
	/**icon图宽度*/
	private static int iconWith;
	/**icon图高度*/
	private static int iconHeight;
	
	/**icon图宽度*/
	private static int padThumbnailWith;
	/**icon图高度*/
	private static int padThumbnailHeight;
	
	
	/**在线预览图片存储路径**/
	private static String previewImgSavePath;
	/** 最大用户数(Null表示无限制) * */
	private static Long userMaxLimit;
	/** 最大空间(GB)(Null表示无限制) * */
	private static Long totalGroupSpaceMaxLimit;
	/** 最大物理(GB)(Null表示无限制) * */
	private static Long totalStorageSpaceMaxLimit;
	/** 个人空间(	KB)* */
	private static long personalGroupSpaceLimit;
	
	private static long lmsGroupId;
	
	
	
	/** 是否有邮件大附件 */	
	private static boolean hasLargeAttach;
	
	/** 是否使用新接口返回json数据 */
	private static boolean newAPI;
	
	
	/** 邮件大附件文件夹 */
	private static String largeAttachFolderName = "邮件大附件";
	/** 邮件大附件文件夹 */
	private static String largeAttachFolderTitle = "邮件大附件";
	
	/** 忽略加密文件类型 */
	private static String ingoreEncryptFile = ".BMP|.GIF|.JPEG|.SVG|.PNG|.JPG|.AVI|.RMVB|.RM|.ASF|.DIVX|.MPG|.MPEG|.MPE|.WMV|.MP4|.MKV|.VOB|.MP3";
	
	static {
		try {
			properties = new Properties(Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(
							"groups.properties"));
			categoryMaxDepth = Integer.parseInt(properties.getProperty(
					"categoryMaxDepth", "2"));

			groupManagersNum = Integer.parseInt(properties.getProperty(
					"groupManagersNum", "5"));

			forumMaxLevel = Integer.parseInt(properties.getProperty(
					"forumMaxLevel", "1"));

			forumNumPerLevel = Integer.parseInt(properties.getProperty(
					"forumNumPerLevel", "1"));

			groupNameKeyWord = properties.getProperty("groupNameKeyWord", "");

			groupDefaultIcon = properties.getProperty("groupDefaultIcon");

			groupDefaultLogo = properties.getProperty("groupDefaultLogo");

			groupDefaultInform = properties.getProperty("groupDefaultInform");

			groupResourceRootPath = properties
					.getProperty("groupResourceRootPath");
			
			memberPicRootPath = properties.getProperty("memberPicRootPath");
			
			memberPicFolderPath = properties
					.getProperty("memberPicFolderPath");
			
			customLogoRootPath = properties.getProperty("customLogoRootPath");
			
			customLogoFolderPath = properties
					.getProperty("customLogoFolderPath");
			moduleLogoRootPath = properties.getProperty("moduleLogoRootPath");

			moduleLogoFolderPath = properties
					.getProperty("moduleLogoFolderPath");

			groupResourceBackupRootPath = properties
			        .getProperty("groupResourceBackupRootPath");

			databaseToolsPath = properties.getProperty("databaseToolsPath");

			backupEndTopic = properties.getProperty("backupEndTopic");
			backupEndBody = properties.getProperty("backupEndBody");

			emailShareNoticeTitle = properties.getProperty("emailShareNoticeTitle");

			mtuploadResourceRootPath = properties
					.getProperty("mtuploadResourceRootPath");
			groupIconRootPath = properties.getProperty("groupIconRootPath");
			groupIconFolderPath = properties.getProperty("groupIconFolderPath");
			groupThumbnailRootPath = properties.getProperty("groupThumbnailRootPath");
			groupThumbnailFolderPath = properties.getProperty("groupThumbnailFolderPath");
			groupResourceSize = Long.parseLong(properties
					.getProperty("groupResourceSize"));

			resourceDefaultPageSize = Integer.parseInt(properties
					.getProperty("resourceDefaultPageSize"));

			groupMemberDefaultPageSize = Integer.parseInt(properties
					.getProperty("groupMemberDefaultPageSize"));

			groupRecordsDefaultPageSize = Integer.parseInt(properties
					.getProperty("groupRecordsDefaultPageSize"));

			postDefaultPageSize = Integer.parseInt(properties
					.getProperty("postDefaultPageSize"));

			threadDefaultPageSize = Integer.parseInt(properties
					.getProperty("threadDefaultPageSize"));

			defaultForumName = properties.getProperty("defaultForumName");

			groupAddrKeyWord = properties.getProperty("groupAddrKeyWord");

			imageRootPath = properties.getProperty("imageRootPath");

			watchDefaultPageSize = Integer.parseInt(properties
					.getProperty("watchDefaultPageSize"));

			messageMaxSize = Integer.parseInt(properties
					.getProperty("messageMaxSize"));

			defaultAlbumCoverImageId = Long.parseLong(properties
					.getProperty("defaultAlbumCoverImageId"));

			invitationTopic = properties.getProperty("invitationTopic");

			invitationBody = properties.getProperty("invitationBody");

			mailSuffix = properties.getProperty("mailSuffix");

			maxGroupCreationPerDayPerMember = Integer.parseInt(properties
					.getProperty("maxGroupCreationPerDayPerMember"));

			auditGroup = Boolean.parseBoolean(properties
					.getProperty("auditGroup"));

			volidResourceType = properties.getProperty("volidResourceType");

			volidImageType = properties.getProperty("volidImageType");

			joinGroupLink = properties.getProperty("joinGroupLink");

			groupResourceDirDepth = Integer.parseInt(properties
					.getProperty("groupResourceDirDepth"));

			groupResourceSingleFileSize = Long.parseLong(properties
					.getProperty("groupResourceSingleFileSize"));

			iconPrefix = properties.getProperty("iconPrefix");

			superAdmin = properties.getProperty("superAdmin.account.name")
					.split(";");

			categoryDefaultPageSize = Integer.parseInt(properties
					.getProperty("categoryDefaultPageSize"));

			defaultIcon = properties.getProperty("defaultIcon");

			groupDefaultPageSize = Integer.parseInt(properties
					.getProperty("groupDefaultPageSize"));
			
			SearchDefaultPageSize = Integer.parseInt(properties
					.getProperty("SearchDefaultPageSize"));

			buildinGroupType = properties.getProperty("buildinGroupType", "")
					.split(";");
			defaultGroupType = properties.getProperty("defaultGroupType");
			
			personalGroupType = properties.getProperty("personalGroupType");
			invitationValidTimes = Integer.parseInt(properties
					.getProperty("invitationValidTimes"));

			exportAccountFileName = properties
					.getProperty("exportAccountFileName");

			defaultGroupCategory = properties.getProperty(
					"DefaultGroupCategory").split(";");
			String[] defaultGroupCategorys = defaultGroupCategory[0].split(":");
			defaultPersonGroupCategory = defaultGroupCategorys[0];
			systemDefaultOrgName = properties
					.getProperty("SystemDefaultOrgName");

			systemDefaultManageTeamName = properties
					.getProperty("SystemDefaultManageTeamName");

			defaultCmsGroup = properties.getProperty("DefaultCmsGroup");

			ldapAuthenticate = Boolean.parseBoolean(properties
					.getProperty("LdapAuthenticate"));
			if (properties.getProperty("AdAuthenticate") != null) {
				adAuthenticate = Boolean.parseBoolean(properties
						.getProperty("AdAuthenticate"));
			}
			local_institution = properties.getProperty("Local_institution");
			
			defaultAblumCategory = properties.getProperty("DefaultAblumCategory");
			
			systemVersion = properties.getProperty("SystemVersion");
			
			if (properties.getProperty("largeAttachFolderName") != null)
				largeAttachFolderName = properties.getProperty("largeAttachFolderName");
			if (properties.getProperty("largeAttachFolderTitle") != null)
				largeAttachFolderTitle = properties.getProperty("largeAttachFolderTitle");
			
			String[] folderKeyWords = properties.getProperty("folderKeyWord")
					.split(";");
			for (String word : folderKeyWords) {
				String[] words = word.split(":");
				folderKeyWordMap.put(words[0], words[1]);
			}

			recyclerName = properties.getProperty("recyclerName");

			thumbnailContentType = properties.getProperty("thumbnailContentType");

			thumbnailMaxWith = Integer.parseInt(properties.getProperty("thumbnailMaxWith"));

			thumbnailMaxHeight = Integer.parseInt(properties.getProperty("thumbnailMaxHeight"));

			thumbnailMaxLength = Long.parseLong(properties.getProperty("thumbnailMaxLength"));

			thumbnailQuality = Integer.parseInt(properties.getProperty("thumbnailQuality"));

			thumbnailProportion = Double.parseDouble(properties.getProperty("thumbnailProportion"));

			thumbnailDefaultWidth = Integer.parseInt(properties.getProperty("thumbnailDefaultWidth"));

			thumbnailDefaultHeight = Integer.parseInt(properties.getProperty("thumbnailDefaultHeight"));
			
			thumbnailMinWidth = Integer.parseInt(properties.getProperty("thumbnailMinWidth"));

			thumbnailMinHeight = Integer.parseInt(properties.getProperty("thumbnailMinHeight"));

			
			previewImgSavePath = properties.getProperty("previewImgSavePath");
			
			iconWith = Integer.parseInt(properties.getProperty("iconWidth"));
			
			iconHeight = Integer.parseInt(properties.getProperty("iconHeight"));
			
			padThumbnailWith = Integer.parseInt(properties.getProperty("padThumbnailWidth"));
			
			padThumbnailHeight = Integer.parseInt(properties.getProperty("padThumbnailHeight"));
			
			coverWith = Integer.parseInt(properties.getProperty("coverWidth"));
			
			coverHeight = Integer.parseInt(properties.getProperty("coverHeight"));
			
			if (properties.getProperty("userMaxLimit") != null &&
					properties.getProperty("userMaxLimit").trim().length() > 0) {
				userMaxLimit = Long.parseLong(properties.getProperty("userMaxLimit"));
			}
			if (properties.getProperty("totalGroupSpaceMaxLimit") != null &&
					properties.getProperty("totalGroupSpaceMaxLimit").trim().length() > 0) {
				totalGroupSpaceMaxLimit = Long.parseLong(properties.getProperty("totalGroupSpaceMaxLimit"));
			}

			if (properties.getProperty("totalStorageSpaceMaxLimit") != null &&
					properties.getProperty("totalStorageSpaceMaxLimit").trim().length() > 0) {
				totalStorageSpaceMaxLimit = Long.parseLong(properties.getProperty("totalStorageSpaceMaxLimit"));
			}


			personalGroupSpaceLimit = Long.parseLong(properties.getProperty("personalGroupSpaceLimit"));
			if (properties.getProperty("lmsGroupId") != null) {
				lmsGroupId = Long.parseLong(properties
						.getProperty("lmsGroupId"));
			}
			
			if (properties.getProperty("hasLargeAttach") != null) {
				hasLargeAttach = Boolean.parseBoolean(properties
						.getProperty("hasLargeAttach"));
			}
			
			if (properties.getProperty("ingoreEncryptFile") != null) {
				ingoreEncryptFile = properties.getProperty("ingoreEncryptFile");
			}
			
			newAPI = properties.getPropertyBoolean("newAPI", false);
			
			synchronizeUser = properties.getPropertyBoolean("synchronizeUser", false);
			remoteSystem = properties.getProperty("remoteSystem");
			lmsUrl = properties.getProperty("lmsUrl","");
			portalUrl = properties.getProperty("portalUrl","");
			ccnlUrl = properties.getProperty("ccnlUrl","");
			imsUrl = properties.getProperty("imsUrl","");
		} catch (Exception e) {
			log.error(e, e);
			System.out.println("!!!!*********");
			e.printStackTrace();
		}
	}

	public static int getCategoryMaxDepth() {
		if (categoryMaxDepth <= 0)
			throw new IllegalStateException(
					"bad configuration for categoryMaxDepth");
		return categoryMaxDepth;
	}

	public static int getGroupManagersNum() {
		if (groupManagersNum <= 0)
			throw new IllegalStateException(
					"bad configuration for groupManagersNum");
		return groupManagersNum;
	}

	public static int getForumMaxLevel() {
		if (forumMaxLevel <= 0)
			throw new IllegalStateException(
					"bad configuration for forumMaxLevel");
		return forumMaxLevel;
	}

	public static int getForumNumPerLevel() {
		if (forumNumPerLevel <= 0)
			throw new IllegalStateException(
					"bad configuration for forumNumPerLevel");
		return forumNumPerLevel;
	}

	public static String[] getGroupNameKeyWord() {
		if (groupNameKeyWord == null || groupNameKeyWord.length() == 0)
			return new String[0];

		List<String> keyWordList = new ArrayList<String>();

		String[] keyWords = groupNameKeyWord.split(";");
		for (String word : keyWords) {
			if (word.length() != 0)
				keyWordList.add(word);
		}

		return keyWordList.toArray(new String[keyWordList.size()]);
	}

	public static String[] getGroupAddrKeyWord() {
		if (groupAddrKeyWord == null || groupAddrKeyWord.length() == 0)
			return new String[0];

		List<String> keyWordList = new ArrayList<String>();

		String[] keyWords = groupAddrKeyWord.split(";");
		for (String word : keyWords) {
			if (word.length() != 0)
				keyWordList.add(word);
		}

		return keyWordList.toArray(new String[keyWordList.size()]);
	}

	public static String getGroupDefaultIcon() {
		if (groupDefaultIcon == null || groupDefaultIcon.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupDefaultIcon");
		return groupDefaultIcon;
	}

	public static String getGroupDefaultLogo() {
		if (groupDefaultLogo == null)
			groupDefaultLogo = "";
		return groupDefaultLogo;
	}

	public static String getGroupDefaultInform() {
		if (groupDefaultInform == null || groupDefaultInform.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupDefaultInform");
		return groupDefaultInform;
	}

	public static String getGroupResourceRootPath() {
		if (groupResourceRootPath == null
				|| groupResourceRootPath.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupResourceRootPath");

		return groupResourceRootPath;
	}
	
	public static String getMemberPicRootPath(){
		if(memberPicRootPath==null||memberPicRootPath.length()==0)
			throw new IllegalStateException(
					"bad configuration for memberPicRootPath");
		return memberPicRootPath;
	}
	
	public static String getMemberPicFolderPath(){
		if(memberPicFolderPath==null||memberPicFolderPath.length()==0)
			throw new IllegalStateException(
					"bad configuration for memberPicFolderPath");
		return memberPicFolderPath;
	}

	public static String getCustomLogoRootPath(){
		if(customLogoRootPath==null||customLogoRootPath.length()==0)
			throw new IllegalStateException(
					"bad configuration for customLogoRootPath");
		return customLogoRootPath;
	}

	
	public static String getCustomLogoFolderPath(){
		if(customLogoFolderPath==null||customLogoFolderPath.length()==0)
			throw new IllegalStateException(
					"bad configuration for customLogoFolderPath");
		return customLogoFolderPath;
	}


	public static String getModuleLogoFolderPath(){
		if(moduleLogoFolderPath==null||moduleLogoFolderPath.length()==0)
			throw new IllegalStateException(
					"bad configuration for moduleLogoFolderPath");
		return moduleLogoFolderPath;
	}


	public static String getModuleLogoRootPath(){
		if(moduleLogoRootPath==null||moduleLogoRootPath.length()==0)
			throw new IllegalStateException(
					"bad configuration for moduleLogoRootPath");
		return moduleLogoRootPath;
	}
	

	public static String getGroupIconRootPath() {
		if (groupIconRootPath == null
				|| groupIconRootPath.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupIconRootPath");

		return groupIconRootPath;
	}
	public static String getGroupIconFolderPath() {
		if (groupIconFolderPath == null
				|| groupIconFolderPath.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupIconFolderPath");

		return groupIconFolderPath;
	}

	public static String getGroupResourceBackupRootPath(){
		if(groupResourceBackupRootPath == null
				|| groupResourceBackupRootPath.length() == 0){
			throw new IllegalStateException("bad configuration for groupResourceBackupRootPath");
		}
		return groupResourceBackupRootPath;
	}

	public static String getDatabaseToolsPath(){
		if(databaseToolsPath == null
				|| databaseToolsPath.length() == 0){
			throw new IllegalStateException("bad configuration for databaseToolsPath");
		}
		return databaseToolsPath;
	}

	public static String getBackupEndTopic(){
		if(backupEndTopic == null || backupEndTopic.length() == 0)
			throw new IllegalStateException("bad configuration for backupEndTopic");
		return backupEndTopic;
	}

	public static String getBackupEndBody(){
		if(backupEndBody == null || backupEndBody.length() == 0)
			throw new IllegalStateException("bad configuration for backupEndBody");
		return backupEndBody;
	}

	public static String getEmailShareNoticeTitle(){
		if(emailShareNoticeTitle == null || emailShareNoticeTitle.length() == 0)
			throw new IllegalStateException("bad configuration for emailShareNoticeTitle");
		return emailShareNoticeTitle;
	}

	public static long getGroupResourceSize() {
		if (groupResourceSize == 0)
			throw new IllegalStateException(
					"bad configuration for groupResourceSize");
		return groupResourceSize;
	}

	public static int getResourceDefaultPageSize() {
		return resourceDefaultPageSize;
	}

	public static int getGroupMemberDefaultPageSize() {
		return groupMemberDefaultPageSize;
	}

	public static int getGroupRecordsDefaultPageSize() {
		return groupRecordsDefaultPageSize;
	}

	public static int getPostDefaultPageSize() {
		return postDefaultPageSize;
	}

	public static int getThreadDefaultPageSize() {
		return threadDefaultPageSize;
	}

	public static String getDefaultForumName() {
		if (defaultForumName == null)
			return "默认讨论区";
		return defaultForumName;
	}

	public static String getImageRootPath() {
		if (imageRootPath == null || imageRootPath.length() == 0)
			throw new IllegalStateException(
					"bad configuration for imageRootPath");
		return imageRootPath;
	}

	public static int getWatchDefaultPageSize() {
		return watchDefaultPageSize;
	}

	public static int getMessageMaxSize() {
		return messageMaxSize;
	}

	public static long getDefaultAlbumCoverImageId() {
		return defaultAlbumCoverImageId;
	}

	public static String getInvitationTopic() {
		if (invitationTopic == null)
			invitationTopic = "诚邀您加入我的圈子";
		return invitationTopic;
	}

	public static String getInvitationBody() {
		if (invitationBody == null)
			invitationBody = "一起来我的圈子疯狂吧！（http://192.168.78.15:8085/%s/）";
		return invitationBody;
	}

	public static String getMailSuffix() {
		if (mailSuffix == null)
			mailSuffix = "@scut.edu.cn";
		return mailSuffix;
	}

	public static int getMaxGroupCreationPerDayPerMember() {
		return maxGroupCreationPerDayPerMember;
	}

	public static boolean isAuditGroup() {
		return auditGroup;
	}

	public static String[] getVolidResourceType() {
		return volidResourceType.split(";");
	}

	public static String[] getVolidImageType() {
		return volidImageType.split(";");
	}

	public static String getJoinGroupLink() {
		return joinGroupLink;
	}

	public static int getGroupResourceDirDepth() {
		return groupResourceDirDepth;
	}

	public static long getGroupResourceSingleFileSize() {
		return groupResourceSingleFileSize;
	}

	public static String getIconPrefix() {
		return iconPrefix;
	}

	public static String[] getSuperAdmin() {
		return superAdmin;
	}

	public static int getCategoryDefaultPageSize() {
		return categoryDefaultPageSize;
	}

	public static String getDefaultIcon() {
		return defaultIcon;
	}

	public static int getGroupDefaultPageSize() {
		return groupDefaultPageSize;
	}

	public static String[] getBuildinGroupType() {
		List<String> list = new ArrayList<String>();
		for (String s : buildinGroupType) {
			if (s.trim().length() == 0)
				continue;
			list.add(s);
		}

		if (list.size() == 0)
			return new String[] { "普通" };
		return list.toArray(new String[list.size()]);
	}
	
	public static String getDefaultGroupType() {
		return defaultGroupType;
	}
	
	public static String getPersonalGroupType() {
		return personalGroupType;
	}

	public static int getInvitationValidTimes() {
		if (invitationValidTimes <= 0)
			invitationValidTimes = 20;
		return invitationValidTimes;
	}

	public static String getExportAccountFileName() {
		return exportAccountFileName;
	}

	public static String[] getDefaultGroupCategory() {
		if (defaultGroupCategory.equals("") || defaultGroupCategory == null) {
			return new String[0];
		}
		return defaultGroupCategory;
	}

	public static String getSystemDefaultOrgName() {
		return systemDefaultOrgName;
	}

	public static String getSystemDefaultManageTeamName() {
		return systemDefaultManageTeamName;
	}

	public static String getDefaultPersonGroupCategory() {
		return defaultPersonGroupCategory;
	}

	public static Map<String, String> getFolderKeyWordMap() {
		return folderKeyWordMap;
	}

	public static String getRecyclerName() {
		return recyclerName;
	}

	public static String getDefaultCmsGroup() {
		return defaultCmsGroup;
	}

	public static boolean getLdapAuthenticate() {
		return ldapAuthenticate;
	}

	public static String getLocal_institution() {
		return local_institution;
	}

	public static int getThumbnailMaxHeight() {
		return thumbnailMaxHeight;
	}

	public static long getThumbnailMaxLength() {
		return thumbnailMaxLength;
	}

	public static int getThumbnailMaxWith() {
		return thumbnailMaxWith;
	}
	
	public static int getThumbnailDefaultWidth() {
		return thumbnailDefaultWidth;
	}
	
	public static int getThumbnailDefaultHeight() {
		return thumbnailDefaultHeight;
	}
	
	public static int getThumbnailMinWidth() {
		return thumbnailMinWidth;
	}
	
	public static int getThumbnailMinHeight() {
		return thumbnailMinHeight;
	}

	public static double getThumbnailProportion() {
		return thumbnailProportion;
	}

	public static int getThumbnailQuality() {
		return thumbnailQuality;
	}

	public static String getThumbnailContentType() {
		return thumbnailContentType;
	}

	public static String getMtuploadResourceRootPath() {
		return mtuploadResourceRootPath;
	}

	public static String getPreviewImgSavePath(){
		return previewImgSavePath;
	}

	public static String getDefaultAblumCategory() {
		return defaultAblumCategory;
	}
	
	public static String getSystemVersion() {
		return systemVersion;
	}

	public static int getIconHeight() {
		return iconHeight;
	}

	public static int getIconWith() {
		return iconWith;
	}

	public static int getPadThumbnailHeight() {
		return padThumbnailHeight;
	}

	public static int getPadThumbnailWith() {
		return padThumbnailWith;
	}

	public static int getCoverHeight() {
		return coverHeight;
	}

	public static int getCoverWith() {
		return coverWith;
	}

	public static Long getTotalGroupSpaceMaxLimit() {
		return totalGroupSpaceMaxLimit;
	}

	public static Long getTotalStorageSpaceMaxLimit() {
		return totalStorageSpaceMaxLimit;
	}


	public static Long getUserMaxLimit() {
		return userMaxLimit;
	}

	public static long getPersonalGroupSpaceLimit() {
		return personalGroupSpaceLimit;
	}

	public static boolean isAdAuthenticate() {
		return adAuthenticate;
	}
	public static long getLmsGroupId() {
		return lmsGroupId;
	}
	public static String getGroupThumbnailRootPath() {
		if (groupThumbnailRootPath == null
				|| groupThumbnailRootPath.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupThumbnailRootPath");
		return groupThumbnailRootPath;
	}
	public static String getGroupThumbnailFolderPath() {
		if (groupThumbnailFolderPath == null
				|| groupThumbnailFolderPath.length() == 0)
			throw new IllegalStateException(
					"bad configuration for groupThumbnailFolderPath");
		return groupThumbnailFolderPath;
	}

	public static String getLargeAttachFolderName() {
		return largeAttachFolderName;
	}

	public static String getLargeAttachFolderTitle() {
		return largeAttachFolderTitle;
	}
	public static boolean isIngoreEncrypt(String ext){
		if (ext == null || ext.length() == 0) {
			return false;
		}
		if (ingoreEncryptFile.indexOf(ext.toUpperCase()) != -1) {
			return true;
		}
		return false;
	}
	
	public static boolean getHasLargeAttach() {
		return hasLargeAttach;
	}
	public static boolean getNewAPI(){
		return newAPI;		
	}
	public static boolean getSynchronizeUser(){
		return synchronizeUser;
	}
	public static Map<String ,String> getRemoteSystem(){
		if(remoteSystem==null||remoteSystem.length()==0){
			return null;
		}
		Map<String,String> map = new HashMap<String, String>();
		String[] systems = remoteSystem.split(",");
		for(String sys:systems){
			try{
				String _name = sys.split("\\|")[0];
				String _url = sys.split("\\|")[1];
				map.put(_name, _url);
			}catch(Exception e){
				System.out.println("remoteSystem配置有误");
			}
		}
		return map;
	}
	
	public static String getLmsUrl(){
		return lmsUrl;
	}
	public static String getPortalUrl(){
		return portalUrl;
	}
	public static String getCcnlUrl(){
		return ccnlUrl;
	}
	public static String getImsUrl(){
		return imsUrl;
	}

	public static boolean getEnableRoleModule() {
		
		return properties.getPropertyBoolean("enableRoleModule", false);
	}
	
	public static String getCasLoginUrl(){
		return properties.getProperty("casLoginUrl", "");

	}
	
	public static boolean getEnableDomainModule(){
		return properties.getPropertyBoolean("enableDomainModule", false);

	}

	public static int getSearchDefaultPageSize() {
		// TODO Auto-generated method stub
		return SearchDefaultPageSize;
	}

	public static String getRootDomainName() {
		// TODO Auto-generated method stub
		return properties.getProperty("rootDomainName", "根域");
	}
	
	public static String getDomainResourceRootPath() {
		String domainResourceRootPath = properties.getProperty("domainResourceRootPath", null);
		if (domainResourceRootPath == null)
			throw new IllegalStateException(
					"bad configuration for domainResourceRootPath");

		return domainResourceRootPath;
	}

	public static void setTotalGroupSpaceMaxLimit(Long totalCapacity){
		totalGroupSpaceMaxLimit = totalCapacity;
	}

}
