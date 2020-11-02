package com.dcampus.common.config;

import com.dcampus.common.util.Log;
import com.dcampus.common.util.Properties;


public class ResourceProperty {

	private static Log log = Log.getLog(ResourceProperty.class);

	private static Properties properties;

	static {
		try {
			properties = new Properties(Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(
							"resource_forResource.properties"));
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	public static String getPrimaryKeyNotFoundString(String table, String name,
			Object value) {
		return String.format(properties.getProperty("PrimaryKeyNotFound"),
				new Object[] { table, name, value });
	}

	public static String getPrimaryKeyAlreadyExistsString(String table,
			String name, String value) {
		return String.format(properties.getProperty("PrimaryKeyAlreadyExists"),
				new Object[] { name, value, table });
	}

	public static String getAlternateKeyNotFoundString(String table,
			String name, Object value) {
		return String.format(properties.getProperty("AlternateKeyNotFound"),
				new Object[] { table, name, value });
	}

	public static String getAlternateKeyAlreadyExistsString(String table,
			String name, Object value) {
		return String.format(properties
				.getProperty("AlternateKeyAlreadyExists"), new Object[] { name,
				value, table });
	}
	public static String getDeleteFailByForeignKeyString() {
		return properties.getProperty("DeleteFailByForeignKey");
	}
	public static String getNoMemberProviderString() {
		return properties.getProperty("NoMemberProvider");
	}
	
	public static String getApplicationPermissionExceptionString() {
		return properties.getProperty("ApplicationPermissionException");
	}

	public static String getCategoryPermissionExceptionString() {
		return properties.getProperty("CategoryPermissionException");
	}

	public static String getForumPermissionExceptionString() {
		return properties.getProperty("ForumPermissionException");
	}

	public static String getGroupPermissionExceptionString() {
		return properties.getProperty("GroupPermissionException");
	}

	public static String getMemberPermissionExceptionString() {
		return properties.getProperty("MemberPermissionException");
	}

	public static String getPostPermissionExceptionString() {
		return properties.getProperty("PostPermissionException");
	}

	public static String getThreadPermissionExceptionString() {
		return properties.getProperty("ThreadPermissionException");
	}

	public static String getMessagePermissionExceptionString() {
		return properties.getProperty("MessagePermissionException");
	}

	public static String getDataCreationExceptionString() {
		return properties.getProperty("DataCreationException");
	}

	public static String getDataUpdateExceptionString() {
		return properties.getProperty("DataUpdateException");
	}

	public static String getDataNotFoundExceptionString() {
		return properties.getProperty("DataNotFoundException");
	}

	public static String getCannotDeleteDefaultAlbumString() {
		return properties.getProperty("CannotDeleteDefaultAlbum");
	}

	public static String getCategoryReachMaxDepthString() {
		return properties.getProperty("CategoryReachMaxDepth");
	}

	public static String getCannotDownloadFileString() {
		return properties.getProperty("CannotDownloadResource");
	}

	public static String getCannotUploadFileString() {
		return properties.getProperty("CannotUploadResource");
	}

	public static String getNoMemberContactWayString() {
		return properties.getProperty("NoMemberContactWay");
	}

	public static String getNotEnoughResourceRoomString() {
		return properties.getProperty("NotEnoughResourceRoom");
	}

	public static String getInvolidGroupNameString(String involidWord) {
		return String.format(properties.getProperty("InvolidGroupName"),
				involidWord);
	}

	public static String getInvolidGroupAddressString(String involidWord) {
		return String.format(properties.getProperty("InvolidGroupAddress"),
				involidWord);
	}
	
	public static String getInvolidFolderNameString(String involidWord) {
		return String.format(properties.getProperty("InvolidFolderName"),
				involidWord);
	}
	
	public static String getGroupAddrDescriptionString() {
		return properties.getProperty("GroupAddrDescription");
	}

	public static String getCreateTooManyGroupsString() {
		return properties.getProperty("CreateTooManyGroups");
	}

	public static String getInvolidResourceTypeString() {
		return properties.getProperty("InvolidResourceType");
	}

	public static String getNotGroupMemberString() {
		return properties.getProperty("NotGroupMember");
	}

	public static String getTooManyGroupManagersString() {
		return properties.getProperty("TooManyGroupManagers");
	}

	public static String getInvolidNameString() {
		return properties.getProperty("InvolidName");
	}

	public static String getCannotDeleteFileString() {
		return properties.getProperty("CannotDeleteResource");
	}

	public static String getCannotOperateToSelfString() {
		return properties.getProperty("CannotOperateToSelf");
	}

	public static String getReceiverInfoNotDetailedString() {
		return properties.getProperty("ReceiverInfoNotDetailed");
	}

	public static String getReceiverNotExistsString() {
		return properties.getProperty("ReceiverNotExists");
	}

	public static String getPostInfoNotIdenticalString() {
		return properties.getProperty("PostInfoNotIdentical");
	}

	public static String getThreadCannotReplyString() {
		return properties.getProperty("ThreadCannotReply");
	}

	public static String getNoLoginString() {
		return properties.getProperty("NoLogin");
	}

	public static String getNoCategoryServiceString() {
		return properties.getProperty("NoCategoryService");
	}

	public static String getNoMemberServiceString() {
		return properties.getProperty("NoMemberService");
	}

	public static String getNoGroupServiceString() {
		return properties.getProperty("NoGroupService");
	}

	public static String getNoForumServiceString() {
		return properties.getProperty("NoForumService");
	}

	public static String getNoThreadServiceString() {
		return properties.getProperty("NoThreadService");
	}

	public static String getNoPostServiceString() {
		return properties.getProperty("NoPostService");
	}

	public static String getNoImageServiceString() {
		return properties.getProperty("NoImageService");
	}

	public static String getNoPermissionServiceString() {
		return properties.getProperty("NoPermissionService");
	}

	public static String getNoWatchServiceString() {
		return properties.getProperty("NoWatchService");
	}

	public static String getNoMessageServiceString() {
		return properties.getProperty("NoMessageService");
	}

	public static String getNoAlbumServiceString() {
		return properties.getProperty("NoAlbumService");
	}

	public static String getNoBadWordServiceString() {
		return properties.getProperty("NoBadWordService");
	}

	public static String getNoInvitationServiceString() {
		return properties.getProperty("NoInvitationService");
	}

	public static String getInvalidMailAddrString() {
		return properties.getProperty("InvalidMailAddr");
	}

	public static String getInvitationPermissionExceptionString() {
		return properties.getProperty("InvitationPermissionException");
	}

	public static String getHasForbiddenBadWordString() {
		return properties.getProperty("HasForbiddenBadWord");
	}

	public static String getNotSuperAdminString() {
		return properties.getProperty("NotSuperAdmin");
	}

	public static String getBadWordPermissionExceptionString() {
		return properties.getProperty("BadWordPermissionException");
	}

	public static String getInBlackListString() {
		return properties.getProperty("InBlackList");
	}

	public static String getNotResourceDirectoryString() {
		return properties.getProperty("NotResourceDirectory");
	}

	public static String getMaxResourceDepthString() {
		return properties.getProperty("MaxResourceDepth");
	}

	public static String getFileSizeTooBigString() {
		return properties.getProperty("FileSizeTooBig");
	}

	public static String getNotAdminString() {
		return properties.getProperty("NotAdmin");
	}

	public static String getNotNormalGroupString() {
		return properties.getProperty("NotNormalGroup");
	}

	public static String getNotNormalCategoryString() {
		return properties.getProperty("NotNormalCategory");
	}

	public static String getCannotMoveResourceString() {
		return properties.getProperty("CannotMoveResource");
	}

	public static String getCannotMoveCategoryString() {
		return properties.getProperty("CannotMoveCategory");
	}

	public static String getCannotModifyResourceString() {
		return properties.getProperty("CannotModifyResource");
	}

	public static String getNoGlobalServiceString() {
		return properties.getProperty("NoGlobalService");
	}

	public static String getGlobalPermissionExceptionString() {
		return properties.getProperty("GlobalPermissionException");
	}

	public static String getRssPermissionExceptionString() {
		return properties.getProperty("RssPermissionException");
	}
	
	public static String getNotInSameGroupString() {
		return properties.getProperty("NotInSameGroup");
	}

	public static String getNotGoodThreadString() {
		return properties.getProperty("NotGoodThread");
	}

	public static String getNotGoodGroupString() {
		return properties.getProperty("NotGoodGroup");
	}

	public static String getCannotShareResourceDirString() {
		return properties.getProperty("CannotShareResourceDir");
	}

	public static String getNoRecordServiceString() {
		return properties.getProperty("NoRecordService");
	}

	public static String getNoRssServiceString() {
		return properties.getProperty("NoRssService");
	}
	
	public static String getResourceNotExistsString() {
		return properties.getProperty("ResourceNotExists");
	}

	public static String getCannotCreateResourceDirString() {
		return properties.getProperty("CannotCreateResourceDir");
	}

	public static String getGroupsExceptionString() {
		return properties.getProperty("GroupsException");
	}

	public static String getNoSuchAccountString() {
		return properties.getProperty("NoSuchAccount");
	}
	
	public static String getPasswordErrorExceptionString() {
		return properties.getProperty("PasswordErrorException");
	}

	public static String getAdminCannotDeleteString() {
		return properties.getProperty("AdminCannotDelete");
	}
	
	public static String getDuplicateAccount(){
		return properties.getProperty("DuplicateAccount");
	}
	
	public static String getCannotOperateOrg(){
		return properties.getProperty("CannotOperateOrg");
	}

	public static String getInvolidCharacters() {
		return properties.getProperty("InvolidCharacters").trim();
	}
	
	public static String getInvolidOperationWithDeletedAccount() {
		return properties.getProperty("InvolidOperationWithDeletedAccount");
	}
	
	public static String getDuplicateGroupName() {
		return properties.getProperty("DuplicateGroupName");
	}
	
	public static String getDuplicateFolderName() {
		return properties.getProperty("DuplicateFolderName");
	}
	
	public static String getParamsErrorException() {
		return properties.getProperty("ParamsErrorException");
	}
	
	public static String getCannotDeleteForumWithThread() {
		return properties.getProperty("CannotDeleteForumWithThread");
	}
	
	public static String getDuplicateShareResourceException() {
		return properties.getProperty("DuplicateShareResourceException");
	}
	
	public static String getNoLogServiceString(){
		return properties.getProperty("NoLogService");
	}

	public static String getNoBackupServiceString(){
		return properties.getProperty("NoBackupService");
	}

	public static String getMaintainSystemString(){
		return properties.getProperty("MaintainSystem");
	}
	
	public static String getOverUserMaxLimitException(String value){
		return String.format(properties.getProperty("OverUserMaxLimit"),
				new Object[] {value });
	}

	public static String getOverGroupSpaceMaxLimitException(String value){
		return String.format(properties.getProperty("OverGroupSpaceMaxLimit"),
				new Object[] {value});
	}
	public static void main(String[] args) {
		System.out.println(ResourceProperty.getInvolidCharacters().trim());
		
		String text = "AdminCannotDelete";

		String[] ss = text.split("=");
		System.out.println("public static String get" + ss[0] + "String(){");
		System.out.println("return properties.getProperty(\"" + ss[0] + "\");");
		System.out.println("}");
	}
}
