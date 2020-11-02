package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;
import com.dcampus.sys.entity.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 资源柜，包括个人资源柜和公共资源柜
 * 由于之前是论坛的圈子实体，所以有一些与发帖和主题有关的字段
 * 不知道是否在service层中有调用，故未删除
 * 
 * 
 * group是数据库的关键字！！
 * 避免再出现关键字，将所有的数据库表名之前都加上weblib_
 * 
 * 
 * status继承BaseEntity中的status，与之前有一定差异，所以在这里重新写
 * 
 * DOCUMENTTYPE在GroupResource中定义了常量
 * 
 * @author patrick
 *
 */


@Entity
@Table(name = "weblib_group")
public class Group extends BaseEntity implements IBaseBean<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** 记录状态：普通状态*/
	public static final int STATUS_NORMAL = 1;
	/** 记录状态：推荐状态 */
	public static final int STATUS_RECOMMANDED = 2;
	/** 记录状态：关闭状态 */
	public static final int STATUS_CLOSE = 3;
	public static final int STATUS_AUDIT = 4;
	public static final int STATUS_UNKONW= 0;
    /**审核用户请求*/

    /**审核用户请求,不需要审核*/
	public static final int AUDITMEMBER_NOT_AUDIT = 1;
    /**审核用户请求,需要审核*/
	public static final int AUDITMEMBER_AUDIT = 2;
    /**审核用户请求,未知*/
	public static final int AUDITMEMBER_UNKNOWN = 0;
	
    /**审核帖子*/

	/**审核帖子,不需要审核*/
	public static final int AUDITPOST_NOT_AUDIT = 1;
	/**审核帖子,需要审核*/
	public static final int AUDITPOST_AUDIT = 2;
	/**审核用户请求,审核*/
	public static final int AUDITPOST_AUDIT_ALL = 3;
	/**审核帖子,未知*/
	public static final int AUDITPOST_UNKNOWN = 0;


	/** 资源柜用途，普通用途，就是普通的资源柜 **/
	public static final String USAGE_NORMAL = "normal";
	/** 资源柜用途，公开资源柜 **/
	public static final String USAGE_PUBLIC = "public";
	/** 资源柜用途，会员私有资源柜 **/
	public static final String USAGE_PRIVATE = "private";
	/** 资源柜用途，用以留言板 **/
	public static final String USAGE_GUESTBOOK = "guestbook";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/**资源柜所属分类目录*/
	@ManyToOne(optional = false)
	@JoinColumn(name = "category_id")
	private Category category;
	
	/** 资源柜显示名称 */
	@Column(name = "display_name")
	private String displayName;
	
	/**路径，包括了分类，用c分类id组成
	 * 例如/c486/
	 * */
	@Column(name = "path")
	private String path;
	
	/** 资源柜名字* */
	@Column(length = 100)
	private String name;

	/** 资源柜地址* */
	@Column(length = 100)
	private String addr;
	
	/** 资源柜头像 */
	@Column(name = "icon_id")
	private long groupIcon;
	
	/** 排列序号 **/
	@Column(name = "order_no")
	private double order;
	
	/** 资源柜描述* */
	@Lob
	@Basic(fetch = FetchType.LAZY)   
	@Type(type="text") 
	@Column(name = "description")
	private String desc;

	/** 资源柜创建者id* */
	@Column(name = "creator_id")
	private long creatorId;

	/** 资源柜创建者名字，外键（Member候选键name字段）* */
	@Column(name = "creator_name")
	private String creatorName;

	/** 资源柜创建时间* */
	@Column(name = "create_date")
	private Timestamp createDate;
	
	/** 柜子状态，该字段供数据库访问使用* */
	@Column(name="`status`")
	private int groupStatus;
	
	/** 资源柜类型，外键（GroupType主键id）* */
	@ManyToOne(optional = false)
	@JoinColumn(name="`type`")
	private GroupType groupType;

	/** 审核用户请求，该字段供数据库使用* */
	@Column(name="audit_member")
	private int memberAudit;


	/** 审核帖子,该字段供数据库使用* */
	@Column(name="audit_post")
	private int postAudit;


	/** 资源柜用途，用以数据库使用 **/
	@Column(name="`usage`")
	private String groupUsage;

	/** 资源柜主题总数* */
	@Column(name = "thread_count")
	private int threadCount;

	/** 资源柜帖子总数* */
	@Column(name = "post_count")
	private int postCount;

	/** 加入资源柜的用户总数* */
	@Column(name = "member_count")
	private int memberCount;

	/** 资源柜最后发帖时间* */
	@Column(name = "last_post_date")
	private Timestamp lastPostDate;

	/** 资源柜最后发帖人名字* */
	@Column(name ="last_post_member_name", length=200)
	private String lastPostMemberName;

	/** 资源柜最后修改时间* */
	@Column(name = "last_modified_date")
	private Timestamp lastModifiedDate;

	/** 资源柜最后修改人名字* */
	@Column(name ="last_modified_member_name",length=200)
	private String lastModifiedMemberName;

	/** 文件柜所在最顶层分类的ID* */
	@Column(name ="top_category_id",length=200)
	private Long topCategoryId;


	
	/** 资源柜扩展信息 **/
	@Transient
	private Map<String, String> externInfo;
	
//	/**文档类型*/
//	@Transient
//	private DocumentType documentType;


	public Long getTopCategoryId() {
		return topCategoryId;
	}

	public void setTopCategoryId(Long topCategoryId) {
		this.topCategoryId = topCategoryId;
	}

	/**文档类型，与数据库字段对接*/
	@Column(name="document_type")
	private int documentTypeValue;
	
	/**最大容量(KB)*/
	@Column(name = "total_file_size")
	private Long totalFileSize;

    /**当前可用容量(KB)*/
	@Column(name = "available_capacity")
	private Long availableCapacity;// kb

	/** paiban  **/
	@Column(name = "extend_field1")
	private String extendField1;
	
	/** 资源柜扩展信息2 **/
	@Column(name = "extend_field2")
	private String extendField2;
	
	/** 资源柜扩展信息3**/
	@Column(name = "extend_field3")
	private String extendField3;
	
	/** 资源柜扩展信息4 **/
	@Column(name = "extend_field4")
	private String extendField4;
	
	/** 资源柜扩展信息5 **/
	@Column(name = "extend_field5")
	private String extendField5;
	
	/**资源柜拥有者*/
	@Column(name = "owner")
	private String owner; 
	
	/**对应的应用id，0为不属于任何应用*/
	@Column(name = "application_id")
	private Long applicationId = 0L;

	/**已经使用容量(KB)*/
	@Column(name = "used_capacity")
	private Long usedCapacity;// kb

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public Long getUsedCapacity() {
		return usedCapacity;
	}

	public void setUsedCapacity(Long usedCapacity) {
		this.usedCapacity = usedCapacity;
	}


	/**该柜子对应的拓展信息集合*/
	@OneToMany(mappedBy="group" ,cascade=CascadeType.REMOVE, fetch=FetchType. LAZY)
    private Set<GroupExtern> groupExterns = new HashSet<GroupExtern>();
	
	/**该柜子对应的附属信息集合*/
	@OneToMany(mappedBy="group" ,cascade=CascadeType.REMOVE, fetch=FetchType. LAZY)
    private Set<GroupDecoration> groupDecorations = new HashSet<GroupDecoration>();
	
	/**该柜子对应的资源集合*/
	@OneToMany(mappedBy="agroup" ,cascade=CascadeType.REMOVE, fetch=FetchType. LAZY)
    private Set<GroupResource> groupResources = new HashSet<GroupResource>();

	public Long getAvailableCapacity() {
		return availableCapacity;
	}
	public void setAvailableCapacity(Long availableCapacity) {
		this.availableCapacity = availableCapacity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}


	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}


	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getPostCount() {
		return postCount;
	}

	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public Timestamp getLastPostDate() {
		return lastPostDate;
	}

	public void setLastPostDate(Timestamp lastPostDate) {
		this.lastPostDate = lastPostDate;
	}

	public String getLastPostMemberName() {
		return lastPostMemberName;
	}

	public void setLastPostMemberName(String lastPostMemberName) {
		this.lastPostMemberName = lastPostMemberName;
	}

	public Timestamp getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Timestamp lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getLastModifiedMemberName() {
		return lastModifiedMemberName;
	}

	public void setLastModifiedMemberName(String lastModifiedMemberName) {
		this.lastModifiedMemberName = lastModifiedMemberName;
	}

	/** ***供数据库用**************************************************************** */

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public int getMemberAudit() {
		return memberAudit;
	}

	public void setMemberAudit(int memberAudit) {
		this.memberAudit = memberAudit;

	}


	public GroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(GroupType groupType) {
		this.groupType = groupType;
	}

	public Map<String, String> getExternInfo() {
		return externInfo;
	}

	public void setExternInfo(Map<String, String> externInfo) {
		this.externInfo = externInfo;
	}

	public int getPostAudit() {
		return postAudit;
	}

	public void setPostAudit(int postAudit) {
		this.postAudit = postAudit;
	}



	public long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}

	public String getGroupUsage() {
		return groupUsage;
	}

	public void setGroupUsage(String groupUsage) {
		this.groupUsage = groupUsage;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public double getOrder() {
		return order;
	}

	public void setOrder(double order) {
		this.order = order;
	}


	public Long getGroupIcon() {
		return groupIcon;
	}

	public void setGroupIcon(long groupIcon) {
		this.groupIcon = groupIcon;
	}


	public int getDocumentTypeValue() {
		return documentTypeValue;
	}

	public void setDocumentTypeValue(int documentTypeValue) {
		this.documentTypeValue = documentTypeValue;
	}

	public Long getTotalFileSize() {
		return totalFileSize;
	}

	public void setTotalFileSize(Long totalFileSize) {
		this.totalFileSize = totalFileSize;
	}

	public String getExtendField1() {
		return extendField1;
	}

	public void setExtendField1(String extendField1) {
		this.extendField1 = extendField1;
	}

	public String getExtendField2() {
		return extendField2;
	}

	public void setExtendField2(String extendField2) {
		this.extendField2 = extendField2;
	}

	public String getExtendField3() {
		return extendField3;
	}

	public void setExtendField3(String extendField3) {
		this.extendField3 = extendField3;
	}

	public String getExtendField4() {
		return extendField4;
	}

	public void setExtendField4(String extendField4) {
		this.extendField4 = extendField4;
	}

	public String getExtendField5() {
		return extendField5;
	}

	public void setExtendField5(String extendField5) {
		this.extendField5 = extendField5;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	
	public static class ExternInfo {
		private String name;

		protected ExternInfo(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static ExternInfo TotalFileSize = new ExternInfo("TotalFileSize");

		public static ExternInfo SingleFileSize = new ExternInfo(
				"SingleFileSize");
	}
	
	public static final Comparator<Group> COMPARE_SORT_ORDER = new Comparator<Group>() {
        public int compare(Group r1, Group r2) {
        	double order1 = r1.getOrder();
        	double order2 = r2.getOrder();
            if (order1 == order2) {
            	return r1.getId() > r2.getId() ? 1 : -1; 
            }
            return (order1 < order2) ? -1 : 1;
        }
    };


	public Set<GroupExtern> getGroupExterns() {
		return groupExterns;
	}

	public void setGroupExterns(Set<GroupExtern> groupExterns) {
		this.groupExterns = groupExterns;
	}

	public Set<GroupDecoration> getGroupDecorations() {
		return groupDecorations;
	}

	public void setGroupDecorations(Set<GroupDecoration> groupDecorations) {
		this.groupDecorations = groupDecorations;
	}

	public Set<GroupResource> getGroupResources() {
		return groupResources;
	}

	public void setGroupResources(Set<GroupResource> groupResources) {
		this.groupResources = groupResources;
	}

	public int getGroupStatus() {
		return groupStatus;
	}

	public void setGroupStatus(int groupStatus) {
		this.groupStatus = groupStatus;
	}

	
	
	
	
    
}
