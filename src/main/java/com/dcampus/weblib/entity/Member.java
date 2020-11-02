package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


/**
 * member表示用户管理中用到的各种实体,该字段为接入grouper之后加入
 * grouper中的用户组、组织映射到圈子中作为圈子的member存在
 * 根据type分为三类
 * floder,team,person 分别表示组织，组和组中的人
 * 
 * 
 * 暂时还未加入资源共享等属性
 * @author patrick
 *
 */


@Entity
@Table(name = "weblib_member")
public class Member extends BaseEntity implements Comparator<Member>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**member的类型，用户组织*/
	public static final String MEMBER_TYPE_FOLDER = "folder";
	/**member的类型，用户组*/
	public static final String MEMBER_TYPE_TEAM = "team";
	/**member的类型，个人*/
	public static final String MEMBER_TYPE_PERSON = "person";
	/**member的类型，自建分享组*/
	public static final String MEMBER_TYPE_SHARETEAM = "shareteam";
	
	/** 记录状态：普通状态*/
	public static final int STATUS_NORMAL = 1;
	/** 记录状态：注销状态 */
	public static final int STATUS_EXPIRED = 2;
	
	
	/** member的id **/
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	/** member名字 **/
	private String name;

	/** member签名 **/
	private String signature;

	/** member头像路径 **/
	private String icon;

	/** member的用户名 **/
	private String account;

	/** member使用的email **/
	private String email;
	
	/** 状态，供数据库使用 **/
	@Column(name = "`status`")
	private int memberStatus;
	
	
	/** member类型，可能为 用户、用户组、组织。该字段为接入grouper之后加入，grouper中的用户组、组织映射到圈子中作为圈子的马甲存在 **/
	@Column(name="type")
	private String memberType;

	
	/** 排序 **/
	@Column
	private Double priority = 100d;
	
	/**所在组织id*/
	@Column(name = "folder_id")
	private Long folderId = 0L;
	
	/** 是否是叶子节点 **/
	@Transient
	private Boolean isLeaf;
	
	@Column(name = "modify_from")
	private String modifyFrom;
	

	
	/**该用户对应的分享外链集合*/
	@OneToMany(mappedBy = "register",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
	private Set<ResourceCode> ResourceCodes = new HashSet<ResourceCode>();
	
	/**该用户对应的分享提供集合*/
	@OneToMany(mappedBy = "provider",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
	private Set<GroupResourceShare> provideResources = new HashSet<GroupResourceShare>();
	
	/**该用户对应的分享接收集合*/
	@OneToMany(mappedBy="recipient",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
	private Set<GroupResourceReceive> receiveResources = new HashSet<GroupResourceReceive>();

//	/**该用户对应的分享接收集合*/
//	@OneToMany(mappedBy="member",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
//	private Set<MemberRole> memberRoles = new HashSet<MemberRole>();
//
//	public Set<MemberRole> getMemberRoles() {
//		return memberRoles;
//	}
//
//	public void setMemberRoles(Set<MemberRole> memberRoles) {
//		this.memberRoles = memberRoles;
//	}
//	/**该用户对应的分享接收集合*/
//	@OneToMany(mappedBy="manager",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
//	private Set<DomainManager> domainManagers = new HashSet<DomainManager>();
//
//	public Set<DomainManager> getDomainManagers() {
//		return domainManagers;
//	}
//
//	public void setDomainManagers(Set<DomainManager> domainManagers) {
//		this.domainManagers = domainManagers;
//	}
//
//	/**该用户对应的分享接收集合*/
//	@OneToMany(mappedBy="creator",cascade=CascadeType.REMOVE,fetch=FetchType.LAZY)
//	private Set<DomainFolder> domainFolder = new HashSet<DomainFolder>();
//
//	public Set<DomainFolder> getDomainFolder() {
//		return domainFolder;
//	}
//
//	public void setDomainFolder(Set<DomainFolder> domainFolder) {
//		this.domainFolder = domainFolder;
//	}

	/**该用户对用户用户组的收藏集合*/
	@OneToMany(mappedBy = "collector", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<Collection> collectionsOfCollector = new HashSet<Collection>();

	/**包含该用户的收藏集合*/
	@OneToMany(mappedBy = "collection", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<Collection> collectionsWhichContainsThis = new HashSet<Collection>();
	
	@OneToOne(mappedBy = "shareTeam", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private ShareTeamInfo info;

	/**该用户创建的分享组信息集合*/
	@OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<ShareTeamInfo> shareTeamsOfThisCreator = new HashSet<ShareTeamInfo>();
	
	/**该分享组包含的成员集合*/
	@OneToMany(mappedBy = "shareTeam", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<ShareTeamMember> membersOfThisShareTeam = new HashSet<ShareTeamMember>();

	/**该用户所在的分享组集合*/
	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private Set<ShareTeamMember> shareTeamsContainsThisMember = new HashSet<ShareTeamMember>();
	
	public Member() {
	}
	public Member(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getFolderId() {
		return folderId;
	}
	public void setFolderId(Long folderId) {
		this.folderId = folderId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public String getMemberType() {
		return memberType;
	}

	public void setMemberType(String memberType) {
		this.memberType = memberType;

	}
	public Double getPriority() {
		return priority;
	}
	public void setPriority(Double priority) {
		this.priority = priority;
	}
	
	public Boolean getIsLeaf() {
		return isLeaf;
	}
	public void setIsLeaf(Boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	
	@Override
	public int compare(Member o1, Member o2) {
		double d1 = o1.getPriority() == null ? 0 : o1.getPriority().doubleValue();
		double d2 = o2.getPriority() == null ? 0 : o2.getPriority().doubleValue();
		if (d1 > d2) {
			return 1;
		} else if (d1 < d2){
			return -1;
		} else {
			Long id1 = o1.getId() == null ? 0 : o1.getId().longValue();
			Long id2 = o2.getId() == null ? 0 : o2.getId().longValue();
			if (id1 == id2) {
				return 0;
			}
			return id1 > id2 ? 1 : -1; 
		}
	}
	
	/**优先级比较*/
	public static void main(String[] args) {
		
		Member m1 = new Member(1L);
		m1.setPriority(1d);
		
		Member m2 = new Member(2L);
		m2.setPriority(2d);
		
		Member m3 = new Member(3L);
		m3.setPriority(3d);
		
		Member[] ms = new Member[]{m1,m2,m3};
		Arrays.sort(ms, new Member());
		
		for (Member m : ms) {
			System.out.println(m.getPriority()+"  "+m.getId());
		}
		
	}
	public Set<GroupResourceShare> getProvideResources() {
		return provideResources;
	}
	public void setProvideResources(Set<GroupResourceShare> provideResources) {
		this.provideResources = provideResources;
	}
	public Set<GroupResourceReceive> getReceiveResources() {
		return receiveResources;
	}
	public void setReceiveResources(Set<GroupResourceReceive> receiveResources) {
		this.receiveResources = receiveResources;
	}
	public Set<ResourceCode> getResourceCodes() {
		return ResourceCodes;
	}
	public void setResourceCodes(Set<ResourceCode> resourceCodes) {
		ResourceCodes = resourceCodes;
	}

	public int getMemberStatus() {
		return memberStatus;
	}
	public void setMemberStatus(int memberStatus) {
		this.memberStatus = memberStatus;
	}
	public Set<Collection> getCollectionsOfCollector() {
		return collectionsOfCollector;
	}
	public void setCollectionsOfCollector(Set<Collection> collectionsOfCollector) {
		this.collectionsOfCollector = collectionsOfCollector;
	}
	public Set<Collection> getCollectionsWhichContainsThis() {
		return collectionsWhichContainsThis;
	}
	public void setCollectionsWhichContainsThis(
			Set<Collection> collectionsWhichContainsThis) {
		this.collectionsWhichContainsThis = collectionsWhichContainsThis;
	}
	public ShareTeamInfo getInfo() {
		return info;
	}
	public void setInfo(ShareTeamInfo info) {
		this.info = info;
	}
	public Set<ShareTeamInfo> getShareTeamsOfThisCreator() {
		return shareTeamsOfThisCreator;
	}
	public void setShareTeamsOfThisCreator(
			Set<ShareTeamInfo> shareTeamsOfThisCreator) {
		this.shareTeamsOfThisCreator = shareTeamsOfThisCreator;
	}
	public Set<ShareTeamMember> getMembersOfThisShareTeam() {
		return membersOfThisShareTeam;
	}
	public void setMembersOfThisShareTeam(
			Set<ShareTeamMember> membersOfThisShareTeam) {
		this.membersOfThisShareTeam = membersOfThisShareTeam;
	}
	public Set<ShareTeamMember> getShareTeamsContainsThisMember() {
		return shareTeamsContainsThisMember;
	}
	public void setShareTeamsContainsThisMember(
			Set<ShareTeamMember> shareTeamsContainsThisMember) {
		this.shareTeamsContainsThisMember = shareTeamsContainsThisMember;
	}
	public String getModifyFrom() {
		return modifyFrom;
	}
	public void setModifyFrom(String modifyFrom) {
		this.modifyFrom = modifyFrom;
	}
	
}
