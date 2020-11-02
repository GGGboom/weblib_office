package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * 资源共享接收方
 * 
 * 尚未加入资源码，这个是和webmail对接需要使用的
 * 
 * 
 * @author patrick
 *
 */


@Entity
@Table(name = "weblib_group_resource_share")
public class GroupResourceShare extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**分享类型，内部分享*/
	public static final int INNER_SHARE = 0;
	/**分享类型，外链分享*/
	public static final int LINK_SHARE = 1;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	/**共享的资源*/
	@ManyToOne(fetch=FetchType.EAGER,optional=false)
	@JoinColumn(name="resource_id")
	private GroupResource resource;
	
	
	/**提供者*/
	@ManyToOne(fetch=FetchType.EAGER,optional=false)
	@JoinColumn(name="provider_id")
	private Member provider;
	
	/**接收者*/
	private String recipient;
	
	/**创建时间*/
	@Column(name = "create_date")
	private Date createDate;
	
	/**备注*/
	@Column(name="remark_text")
	private String remark;
	
	/**分享类型，内部分享和外链*/
	@Column(name="share_type")
	private Integer shareType = INNER_SHARE;
	
	/**对应的分享外链集合**/
	/**目测没用到**/
	@ManyToOne(fetch=FetchType.EAGER,optional=true)
	@JoinColumn(name="resource_code_id")
	private ResourceCode resourceCode;
	
	
	/**该分享对应的接收者集合*/
	@OneToMany(mappedBy="share",fetch=FetchType.LAZY,cascade=CascadeType.REMOVE)
	private Set<GroupResourceReceive> groupResourceReceives = new HashSet<GroupResourceReceive>();
	
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="share_wrap_id",nullable=true)
	private ShareWrap shareWrap;

	public GroupResourceShare() {
	}
	
	public GroupResourceShare(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public GroupResource getResource() {
		return resource;
	}

	public void setResource(GroupResource resource) {
		this.resource = resource;
	}

	public Member getProvider() {
		return provider;
	}

	public void setProvider(Member provider) {
		this.provider = provider;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}


	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getShareType() {
		return shareType;
	}

	public void setShareType(Integer shareType) {
		this.shareType = shareType;
	}

	public Set<GroupResourceReceive> getGroupResourceReceives() {
		return groupResourceReceives;
	}

	public void setGroupResourceReceives(
			Set<GroupResourceReceive> groupResourceReceives) {
		this.groupResourceReceives = groupResourceReceives;
	}

	public ResourceCode getResourceCode() {
		return resourceCode;
	}

	public void setResourceCode(ResourceCode resourceCode) {
		this.resourceCode = resourceCode;
	}

	/**
	 * @return the shareWrap
	 */
	public ShareWrap getShareWrap() {
		return shareWrap;
	}

	/**
	 * @param shareWrap the shareWrap to set
	 */
	public void setShareWrap(ShareWrap shareWrap) {
		this.shareWrap = shareWrap;
	}
	


	

}
