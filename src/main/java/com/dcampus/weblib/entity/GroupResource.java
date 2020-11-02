package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;


/**
 * 柜子资源类
 * 包括资源的类型，状态等基本属性，也有保留字段供以后扩展
 * 
 * 
 * 资源所在柜子和资源的创建人都建立多对一关联了，
 * 
 * 父资源也建立的多对一关联，
 * 但是仍然保留了parentId作为瞬时态变量，
 * 因为里面有一些函数会用到
 * 
 * 
 * 新增category属性,方便搜索分类下的资源
 * 
 * 
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_group_resource")
public class GroupResource extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**资源类型，文件夹***/
	public static final int RESOURCE_TYPE_DIRECTORY = 1;
	/**资源类型，文件***/
	public static final int RESOURCE_TYPE_FILE = 2;
	/**资源类型，链接***/
	public static final int RESOURCE_TYPE_LINK = 3;
	/**资源类型，未知***/
	public static final int RESOURCE_TYPE_UNKNOWN = 0;
	
	/**资源状态，正常使用**/
	public static final String RESOURCE_STATUS_NORMAL = "normal";
	/**资源状态，已删除**/
	public static final String RESOURCE_STATUS_DELETE = "delete";
	
	/**文件类型,会议文章**/
	public static final int DOCUMENT_TYPE_MEETINGARTICLE = 1;
	/**文件类型,期刊文章**/
	public static final int DOCUMENT_TYPE_JOURNALSARTICLE = 2;
	/**文件类型,专著**/
	public static final int DOCUMENT_TYPE_MONOGRAPHS = 3;
	/**文件类型,奖项**/
	public static final int DOCUMENT_TYPE_AWARDS = 4;
	/**文件类型,专利**/
	public static final int DOCUMENT_TYPE_PATENTS = 5;
	/**文件类型,论文**/
	public static final int DOCUMENT_TYPE_PAPER = 6;
	/**文件类型,项目**/
	public static final int DOCUMENT_TYPE_PROJECT = 7;
	/**文件类型,老师**/
	public static final int DOCUMENT_TYPE_TEACHER = 8;
	/**文件类型,学生**/
	public static final int DOCUMENT_TYPE_STUDENT = 9;
	/**文件类型,设备**/
	public static final int DOCUMENT_TYPE_EQUIPMENT =10;
	/**文件类型,照片**/
	public static final int DOCUMENT_TYPE_PHOTO = 11;
	/**文件类型,相册**/
	public static final int DOCUMENT_TYPE_ALBUM = 12;
	/**文件类型,装置**/
	public static final int DOCUMENT_TYPE_DEVICE =13;
	/**文件类型,未知**/
	public static final int DOCUMENT_TYPE_UNKNOWN = 0;
	
	/**上传状态，已完成**/
	public static final String UPLOAD_FINISHED = "1";
	
	/**上传状态，未完成**/
	public static final String UPLOAD_UNFINISH = "0";
	
	/**保留区1**/
	public static final String PROP_RESERVE_FIELD_1 = "reserveField1";
	
	/**保留区2**/
	public static final String PROP_RESERVE_FIELD_2 = "reserveField2";
	
	/**保留区双精度**/
	public static final String PROP_RESERVE_DOUBLE_FIELD_1 = "reserveDoubleField1";
	
	/**邮件附件文件夹**/
	public static final String EMAIL_ATTACH_FOLDER = "#";
	
	/** 资源项id **/
	@Id  
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** 柜子id，外键（Group主键id） **/
	@ManyToOne(optional = false)
	@JoinColumn(name = "group_id")
	private Group agroup;

	/** 柜子名字，外键（Group候选键name字段） **/
	@Column(name = "group_name", length=200)
	private String groupName;

	/** 资源创建者id，外键（MemberBean主键id） **/
	@Column(name = "member_id")
	private long creatorId;

	/** 资源创建者名字，外键（Member候选键name字段） **/
	@Column(name = "member_name",length=200)
	private String memberName;

	/** 资源的存储路径 **/
	@Column(name = "file_path",length=200)
	private String filePath;

	/** 资源名字 **/
	@Column(length=200)
	private String name;
	
	/** 资源原始名称 **/
	@Column(name = "original_name",length=200)
	private String originalName;
	
	/** 资源后缀 **/
	@Column(name = "file_ext",length=200)
	private String fileExt;
	
	/** 资源前缀 **/
	@Column(name = "file_pre_name",length=200)
	private String filePreName;
	
	/** 是否内部文件夹，是则不能删除 **/
	@Column(name = "default_folder")
	private Boolean defaultFolder;
	
	/** 资源描述 **/	
	@Column(name="description",length=500)
	private String desc;
	
	/**资源关键字*/
	private String keywords;
	
	/**备注*/
	@Column(name="file_remark")
	private String remark;

	/** 资源大小，以K为单位 **/
	@Column(name = "size")
	private Long size;
	
	/** 资源大小，以b为单位 **/
	@Column(name="detail_size")
	private String detailSize;

	/** 资源的Content-Type **/
	@Column(name = "content_type",length=100)
	private String contentType;

	/** 资源创建时间 **/
	@Column(name = "create_date",length=0)
	private Timestamp createDate;
	
	@Column(name = "file_last_modified")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fileLastModified;
	
	/**父资源id**/
	@Column(name = "parent_id")
	private Long parentId;

	/** 移动前原有父资源id，外键（GroupResouce主键id） ，主要是用于回收站的还原**/
	@Column(name="pre_parent_id")
	private Long preParentId;
	

	/** 资源类型，与数据库字段对接 **/
	@Column(name="`type`")
	//@Enumerated(EnumType.ORDINAL)
	private int resourceType;

	/** 资源状态，与数据库字段对接 **/
	@Column(name="resource_status")
	//@Enumerated(EnumType.STRING)
	private String resourceStatus;
	
	/** 缩略图 */
	@Column(name="thumbnail_image")
	private String thumbnail;
	
	private String icon;
	
	@Column(name="thumbnail2")
	private String thumbnailForOther;
	
	
	/**文档类型，与数据库字段对接*/
	@Column(name="document_type")
	//@Enumerated(EnumType.ORDINAL)
	private int documentTypeValue;
	
	/**上传比例（0-1）*/
	@Column(name="upload_rate")
	private double rate;
	
	/**是否已经共享*/
	private boolean shared;
	
	/** 资源的链接路径 **/
	@Column(name ="link_path")
	private String linkPath;
	
	/**
	 * 顺序号
	 */
	private double priority = 0;
	
	/** CRC校验码 */
	@Column(name = "check_code")
	private String checkCode;
	
	/**保留区1*/
	private String reserveField1;
	
	/**保留区2*/
	private String reserveField2;
	
	/**保留区双精度1*/
	@Column(name="reserve_dou_field1")
	private Double reserveDoubleField1;
	
	/**排版**/
	private String paiban;
	
	/**个人文件**/
	private boolean privateFile = false;
	
	@Column(name="publish_date")
	private Timestamp publishDate;

	/**添加路径path*/
	private String path;	

	/**添加应用标志*/
	private String applicationId;
	
	//===========添加应用标志=====
	/**finishSign : 0 -未完成，1-已完成*/
	@Column(name = "finish_sign")
	private String finishSign;
	
	@Column(name = "view_uuid")
	private String viewUuid;
	
	//存储对应的domainId。用于分域后，上传资源到个人柜子，指明资源关联的域。不做对象关联的原因是避免域删除导致资源级联删除。因为域删除不应该删除用户资源
	@Column(name = "domain_tag")
	private long domainTag;
	
	@Transient
	private Map<String, Object> reserveFieldMap = new HashMap<String, Object>();
	
	/**该资源对应的分享集合*/
	@OneToMany(mappedBy="resource", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<GroupResourceShare> sharedResources = new HashSet<GroupResourceShare>();
	
	/**该资源对应的接收集合*/
	@OneToMany(mappedBy="resource", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<GroupResourceReceive> receivedResources = new HashSet<GroupResourceReceive>();
	
	/**该资源对应的分享外链集合*/
	@OneToMany(mappedBy="groupResource", cascade=CascadeType.REMOVE, fetch=FetchType.LAZY)
	private Set<ResourceCode> webmailResource = new HashSet<ResourceCode>();

	
	public GroupResource() {
		// TODO Auto-generated constructor stub
	}
	public GroupResource(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Group getGroup() {
		return agroup;
	}

	public void setGroup(Group group) {
		this.agroup = group;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}
	public Map<String, Object> getReserveFieldMap() {
		return reserveFieldMap;
	}
	public void setReserveFieldMap(Map<String, Object> reserveFieldMap) {
		this.reserveFieldMap = reserveFieldMap;
	}
	

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}


	public String getResourceStatus() {
		return resourceStatus;
	}

	public void setResourceStatus(String resourceStatus) {
		this.resourceStatus = resourceStatus;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public Set<GroupResourceShare> getSharedResources() {
		return sharedResources;
	}

	public void setSharedResources(Set<GroupResourceShare> sharedResources) {
		this.sharedResources = sharedResources;
	}

	public Set<GroupResourceReceive> getReceivedResources() {
		return receivedResources;
	}
	
	public void setReceivedResources(Set<GroupResourceReceive> resources) {
		this.receivedResources = resources;
	}
	

	public int getDocumentTypeValue() {
		return documentTypeValue;
	}

	public void setDocumentTypeValue(int documentTypeValue) {
		this.documentTypeValue = documentTypeValue;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Double getReserveDoubleField1() {
		return reserveDoubleField1;
	}
	public void setReserveDoubleField1(Double reserveDoubleField1) {
		this.reserveDoubleField1 = reserveDoubleField1;
		reserveFieldMap.put(PROP_RESERVE_DOUBLE_FIELD_1, reserveDoubleField1);
	}
	public String getReserveField1() {
		return reserveField1;
	}
	public void setReserveField1(String reserveField1) {
		this.reserveField1 = reserveField1;
		reserveFieldMap.put(PROP_RESERVE_FIELD_1, reserveField1);
	}
	public String getReserveField2() {
		return reserveField2;
	}
	public void setReserveField2(String reserveField2) {
		this.reserveField2 = reserveField2;
		reserveFieldMap.put(PROP_RESERVE_FIELD_2, reserveField2);
	}
	public Map<String, Object> getReserveFields() {
		return reserveFieldMap;
	}

	public Object getReserveInfo(String key) {
		return reserveFieldMap.get(key);
	}
	
	public void setReserveInfo(String key, Object value) {
		reserveFieldMap.put(key, value);
		if (PROP_RESERVE_FIELD_1.equals(key)) {
			this.reserveField1 = (String)value;
		} else if (PROP_RESERVE_FIELD_1.equals(key)) {
			this.reserveField1 = (String)value;
		} else if (PROP_RESERVE_DOUBLE_FIELD_1.equals(key)) {
			this.reserveDoubleField1 = (Double)value;
		}
	}
	public double getPriority() {
		return priority;
	}
	public void setPriority(double priority) {
		this.priority = priority;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getThumbnailForOther() {
		return thumbnailForOther;
	}
	public void setThumbnailForOther(String thumbnailForOther) {
		this.thumbnailForOther = thumbnailForOther;
	}
	public String getLinkPath() {
		return linkPath;
	}
	public void setLinkPath(String linkPath) {
		this.linkPath = linkPath;
	}
	public String getCheckCode() {
		return checkCode;
	}
	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public boolean isPrivateFile() {
		return privateFile;
	}
	public void setPrivateFile(boolean privateFile) {
		this.privateFile = privateFile;
	}
	public String getPaiban() {
		return paiban;
	}
	public void setPaiban(String paiban) {
		this.paiban = paiban;
	}
	
	//===========添加路径path=====
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Boolean getDefaultFolder() {
		return defaultFolder;
	}
	public void setDefaultFolder(Boolean defaultFolder) {
		this.defaultFolder = defaultFolder;
	}
	@Transient
	public boolean isInnerDefaultFolder(){
		return defaultFolder != null && defaultFolder.booleanValue();
	}
	public String getOriginalName() {
		return originalName;
	}
	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}
	public String getFileExt() {
		return fileExt;
	}
	public void setFileExt(String fileExt) {
		this.fileExt = fileExt;
	}
	public String getFilePreName() {
		return filePreName;
	}
	public void setFilePreName(String filePreName) {
		this.filePreName = filePreName;
	}
	public Long getPreParentId() {
		return preParentId;
	}
	public void setPreParentId(Long preParentId) {
		this.preParentId = preParentId;
	}
	
	
	public String getDetailSize() {
		return detailSize;
	}
	public void setDetailSize(String detailSize) {
		this.detailSize = detailSize;
	}
	@Transient
	public long[] getParentIdsByPath(){
		if (path != null && path.length() > 1) {
			String[] ids = path.substring(1).split("/");
			long[] ss = new long[ids.length];
			int kk = 0;
			for (String i : ids) {
				ss[kk++] = Long.parseLong(i); 
			}
			return ss;
		}
		if (this.parentId > 0) {
			return new long[]{this.parentId};
		}
		return null;
	}
	public String getViewUuid() {
		return viewUuid;
	}
	public void setViewUuid(String viewUuid) {
		this.viewUuid = viewUuid;
	}
	public Date getFileLastModified() {
		return fileLastModified;
	}
	public void setFileLastModified(Date fileLastModified) {
		this.fileLastModified = fileLastModified;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getFinishSign() {
		return finishSign;
	}
	public void setFinishSign(String finishSign) {
		this.finishSign = finishSign;
	}

	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupResource other = (GroupResource) obj;
        if (this.getId() == other.getId()) {
                return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
	public Group getAgroup() {
		return agroup;
	}
	public void setAgroup(Group agroup) {
		this.agroup = agroup;
	}
	public Timestamp getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Timestamp publishDate) {
		this.publishDate = publishDate;
	}
	public Set<ResourceCode> getWebmailResource() {
		return webmailResource;
	}
	public void setWebmailResource(Set<ResourceCode> webmailResource) {
		this.webmailResource = webmailResource;
	}
	public long getCreatorId() {
		return creatorId;
	}
	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}
	public long getDomainTag() {
		return domainTag;
	}
	public void setDomainTag(long domainTag) {
		this.domainTag = domainTag;
	}
	
}
