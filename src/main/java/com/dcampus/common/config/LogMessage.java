package com.dcampus.common.config;

import java.util.Locale;
import java.util.ResourceBundle;

public class LogMessage {
	private static ResourceBundle bundle;
	static{
		bundle = ResourceBundle.getBundle("logMessage", Locale.getDefault());
	}

	private static String formatDesc(String desc, Object...params){
		return java.text.MessageFormat.format(desc, params);
	}
	private static String getDesc(String key){
		return bundle.getString(key);
	}
	public static String getGroupClose(String groupName){
		return formatDesc(getDesc("groupClose"),groupName);
	}
	public static String getGroupOpen(String groupName){
		return formatDesc(getDesc("groupOpen"),groupName);
	}
	public static String getGroupAdd(String groupName){
		return formatDesc(getDesc("groupAdd"),groupName);
	}
	public static String getGroupNameMod(String groupName){
		return formatDesc(getDesc("groupNameMod"),groupName);
	}
	public static String getGroupMove(String source,String groupName,String target){
		return formatDesc(getDesc("groupMove"),source,groupName,target);
	}
	public static String getResourceDirAdd(String groupName){
		return formatDesc(getDesc("resourceDirAdd"),groupName);
	}
	public static String getResourceDirMod(String groupName,String resourceName){
		return formatDesc(getDesc("resourceDirMod"),groupName,resourceName);
	}
	public static String getResourceStateMod(String groupName,String resourceName){
		return formatDesc(getDesc("resourceStateMod"),groupName,resourceName);
	}
	public static String getResourceExport(String resourceName){
		return formatDesc(getDesc("resourceExport"),resourceName);
	}
	public static String getResourceExportToWord(String resourceName){
		return formatDesc(getDesc("resourceExportToWord"),resourceName);
	}
	public static String getResourceMod(String groupName,String resourceName){
		return formatDesc(getDesc("resourceMod"),groupName,resourceName);
	}
	public static String getResourceMove(String sGroupName,String resourceName,String tGroupName){
		return formatDesc(getDesc("resourceMove"),sGroupName,resourceName,tGroupName);
	}
	public static String getResourceDelete(String groupName,String resourceName){
		return formatDesc(getDesc("resourceDelete"),groupName,resourceName);
	}
	public static String getResourceDirDelete(String groupName,String resourceName){
		return formatDesc(getDesc("resourceDirDelete"),groupName,resourceName);
	}
	public static String getGroupManagerAdd(String groupName,String memberName){
		return formatDesc(getDesc("groupManagerAdd"),groupName,memberName);
	}
	public static String getGroupManagerDelete(String groupName,String memberName){
		return formatDesc(getDesc("groupManagerDelete"),groupName,memberName);
	}
	public static String getGroupInformMod(String groupName){
		return formatDesc(getDesc("groupInformMod"),groupName);
	}
	public static String getGroupTagMod(String groupName){
		return formatDesc(getDesc("groupTagMod"),groupName);
	}
	public static String getGroupSizeMod(String groupName){
		return formatDesc(getDesc("groupSizeMod"),groupName);
	}
	public static String getGroupFileSizeMod(String groupName){
		return formatDesc(getDesc("groupFileSizeMod"),groupName);
	}
	public static String getGroupRecommend(String groupName){
		return formatDesc(getDesc("groupRecommend"),groupName);
	}
	public static String getGroupCancelRecommend(String groupName){
		return formatDesc(getDesc("groupCancelRecommend"),groupName);
	}
	public static String getGroupTypeAdd(String typeName){
		return formatDesc(getDesc("groupTypeAdd"),typeName);
	}
	public static String getGroupTypeDelete(String typeName){
		return formatDesc(getDesc("groupTypeDelete"),typeName);
	}
	public static String getGroupTypeMod(String typeName){
		return formatDesc(getDesc("groupTypeMod"),typeName);
	}
	public static String getGroupByTypeMod(String groupName,String typeName){
		return formatDesc(getDesc("groupByTypeMod"),groupName,typeName);
	}
	public static String getResourceCopy(String sGroupName,String resourceName,String tGroupName){
		return formatDesc(getDesc("resourceCopy"),sGroupName,resourceName,tGroupName);
	}
	public static String getGroupDelete(String groupName){
		return formatDesc(getDesc("groupDelete"),groupName);
	}
	public static String getGroupSequenceMod(String groupName){
		return formatDesc(getDesc("groupSequenceMod"),groupName);
	}
	public static String getGroupUsageMod(String groupName){
		return formatDesc(getDesc("groupUsageMod"),groupName);
	}
	public static String getResourceImport(String groupName){
		return formatDesc(getDesc("resourceImport"),groupName);
	}
	public static String getAlbumAdd(String albumName){
		return formatDesc(getDesc("albumAdd"),albumName);
	}
	public static String getAlbumDelete(String albumName){
		return formatDesc(getDesc("albumDelete"),albumName);
	}
	public static String getAlbumCoverMod(String albumName){
		return formatDesc(getDesc("albumCoverMod"),albumName);
	}
	public static String getAlbumNameMod(String albumName){
		return formatDesc(getDesc("albumNameMod"),albumName);
	}
	public static String getBadWordAdd(String badWord){
		return formatDesc(getDesc("badWordAdd"),badWord);
	}
	public static String getBadWordDelete(String badWord){
		return formatDesc(getDesc("badWordDelete"),badWord);
	}
	public static String getBadWordMod(String sBadWord,String sReplace,String tBadWord,String tReplace){
		return formatDesc(getDesc("badWordMod"),sBadWord,sReplace,tBadWord,tReplace);
	}
	public static String getCategoryClose(String categoryName){
		return formatDesc(getDesc("categoryClose"),categoryName);
	}
	public static String getCategoryOpen(String categoryName){
		return formatDesc(getDesc("categoryOpen"),categoryName);
	}
	public static String getCategoryAdd(String categoryName){
		return formatDesc(getDesc("categoryAdd"),categoryName);
	}
	public static String getCategoryDelete(String categoryName){
		return formatDesc(getDesc("categoryDelete"),categoryName);
	}
	public static String getCategoryMove(String categoryName,String tCategoryName){
		return formatDesc(getDesc("categoryMove"),categoryName,tCategoryName);
	}
	public static String getCategoryNameDescMod(String categoryName){
		return formatDesc(getDesc("categoryNameDescMod"),categoryName);
	}
	public static String getCategoryNameMod(String sCategoryName,String tCategoryName){
		return formatDesc(getDesc("categoryNameMod"),sCategoryName,tCategoryName);
	}
	public static String getCategorySequenceMod(String categoryName){
		return formatDesc(getDesc("categorySequenceMod"),categoryName);
	}
	public static String getForumAdd(String forumName){
		return formatDesc(getDesc("forumAdd"),forumName);
	}
	public static String getForumMod(String forumName){
		return formatDesc(getDesc("forumMod"),forumName);
	}
	public static String getForumDelete(String forumName){
		return formatDesc(getDesc("forumDelete"),forumName);
	}
	public static String getFriendAdd(String friendName){
		return formatDesc(getDesc("friendAdd"),friendName);
	}
	public static String getFriendDelete(String friendName){
		return formatDesc(getDesc("friendDelete"),friendName);
	}
	public static String getSiteClose(String siteName,String reason){
		return formatDesc(getDesc("siteClose"),siteName,reason);
	}
	public static String getSiteOpen(String siteName){
		return formatDesc(getDesc("siteOpen"),siteName);
	}
	public static String getSmtpSet(){
		return getDesc("smtpSet");
	}
	public static String getDomainSet(String domain){
		return formatDesc(getDesc("domainSet"),domain);
	}
	public static String getSiteNameSet(String siteName){
		return formatDesc(getDesc("siteNameSet"),siteName);
	}
	public static String getGroupAuditSet(boolean auditGroup){
		String yesOrNo = "否";
		if(auditGroup){
			yesOrNo = "是";
		}
		return formatDesc(getDesc("groupAuditSet"),yesOrNo);
	}
	public static String getGroupCrossCopySet(boolean crossCopy,boolean crossMove){
		String yesOrNo1 = "否";
		String yesOrNo2 = "否";
		if(crossCopy){
			yesOrNo1 = "是";
		}
		if(crossMove){
			yesOrNo2 = "是";
		}
		return formatDesc(getDesc("groupCrossCopySet"),yesOrNo1,yesOrNo2);
	}
	public static String getImageDelete(String imageName){
		return formatDesc(getDesc("imageDelete"),imageName);
	}
	public static String getMailSend(String mailAddr,String groupName){
		return formatDesc(getDesc("mailSend"),mailAddr,groupName);
	}
	public static String getMessageSend(String memberName,String groupName){
		return formatDesc(getDesc("messageSend"),memberName,groupName);
	}
	public static String getFail(){
		return getDesc("fail");
	}
	public static String getSuccess(){
		return getDesc("success");
	}
	public static String getMemberAdd(String memberName){
		return formatDesc(getDesc("memberAdd"),memberName);
	}
	public static String getMemberApplyToJoin(String groupName){
		return formatDesc(getDesc("memberApplyToJoin"),groupName);
	}
	public static String getManagerAdd(String managerName){
		return formatDesc(getDesc("managerAdd"),managerName);
	}
	public static String getManagerDelete(String managerName){
		return formatDesc(getDesc("managerDelete"),managerName);
	}
	public static String getMemberAddToTeam(String memberName,String teamName){
		return formatDesc(getDesc("memberAddToTeam"),memberName,teamName);
	}
	public static String getTeamDelete(String teamName){
		return formatDesc(getDesc("teamDelete"),teamName);
	}
	public static String getMemberDeleteFromTeam(String teamName,String memberName){
		return formatDesc(getDesc("memberDeleteFromTeam"),teamName,memberName);
	}
	public static String getTeamCopy(String teamName,String tTeamName){
		return formatDesc(getDesc("teamCopy"),teamName,tTeamName);
	}
	public static String getTeamMove(String teamName,String tTeamName){
		return formatDesc(getDesc("teamMove"),teamName,tTeamName);
	}
	public static String getTeamNameMod(String teamName,String tTeamName){
		return formatDesc(getDesc("teamNameMod"),teamName,tTeamName);
	}
	public static String getTeamImport(){
		return getDesc("teamImport");
	}
	public static String getAccountAdd(String account){
		return formatDesc(getDesc("accountAdd"),account);
	}
	public static String getAccountCancel(String account){
		return formatDesc(getDesc("accountCancel"),account);
	}
	public static String getPasswordMod(String account){
		return formatDesc(getDesc("passwordMod"),account);
	}
	public static String getAccountExport(){
		return getDesc("accountExport");
	}
	public static String getAccountImport(){
		return getDesc("accountImport");
	}
	public static String getAccountMod(String account){
		return formatDesc(getDesc("accountMod"),account);
	}
	public static String getMemberDelete(String memberName){
		return formatDesc(getDesc("memberDelete"),memberName);
	}
	public static String getAccountDelete(String account){
		return formatDesc(getDesc("accountDelete"),account);
	}
	public static String getAccountActivate(String account){
		return formatDesc(getDesc("accountActivate"),account);
	}
	public static String getPostAdd(String postTopic){
		return formatDesc(getDesc("postAdd"),postTopic);
	}
	public static String getPostMod(String postTopic){
		return formatDesc(getDesc("postMod"),postTopic);
	}
	public static String getPostDelete(String postTopic){
		return formatDesc(getDesc("postDelete"),postTopic);
	}
	public static String getPostAuditPass(String postTopic){
		return formatDesc(getDesc("postAuditPass"),postTopic);
	}
	public static String getRssAdd(String rssTitle){
		return formatDesc(getDesc("rssAdd"),rssTitle);
	}
	public static String getRssDelete(String rssTitle){
		return formatDesc(getDesc("rssDelete"),rssTitle);
	}
	public static String getRssMod(String rssTitle){
		return formatDesc(getDesc("rssMod"),rssTitle);
	}
	public static String getThreadAdd(String threadTopic){
		return formatDesc(getDesc("threadAdd"),threadTopic);
	}
	public static String getThreadPriorityMod(String threadTopic){
		return formatDesc(getDesc("threadPriorityMod"),threadTopic);
	}
	public static String getThreadDelete(String threadTopic){
		return formatDesc(getDesc("threadDelete"),threadTopic);
	}
	public static String getThreadRestore(String threadTopic){
		return formatDesc(getDesc("threadRestore"),threadTopic);
	}
	public static String getThreadClose(String threadTopic){
		return formatDesc(getDesc("threadClose"),threadTopic);
	}
	public static String getThreadOpen(String threadTopic){
		return formatDesc(getDesc("threadOpen"),threadTopic);
	}
	public static String getThreadShield(String threadTopic){
		return formatDesc(getDesc("threadShield"),threadTopic);
	}
	public static String getThreadCancelShield(String threadTopic){
		return formatDesc(getDesc("threadCancelShield"),threadTopic);
	}
	public static String getThreadMove(String threadTopic,String forumName){
		return formatDesc(getDesc("threadMove"),threadTopic,forumName);
	}
	public static String getThreadMod(String threadTopic){
		return formatDesc(getDesc("threadMod"),threadTopic);
	}
	public static String getWatchAdd(String type){
		return formatDesc(getDesc("watchAdd"),type);
	}
	public static String getWatchDelete(String type){
		return formatDesc(getDesc("watchDelete"),type);
	}
	public static String getBackupDelete(String day){
		return formatDesc(getDesc("backupDelete"),day);
	}
	public static String getBackupAdd(String day){
		return formatDesc(getDesc("backupAdd"),day);
	}
	public static String getRestoreAdd(String day){
		return formatDesc(getDesc("restoreAdd"),day);
	}
	public static String getResourcePreview(String groupName,String resourceName){
		return formatDesc(getDesc("resourcePreview"),groupName ,resourceName);
	}
}