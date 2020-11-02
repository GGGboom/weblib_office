package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author fzhang at 2017年10月26日
 *	分享组信息，包括创建者，分享组id,创建时间等信息
 */
@Entity
@Table(name = "weblib_share_team_info")
public class ShareTeamInfo extends BaseEntity implements IBaseBean<Long> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7671499839339741521L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "creator_id")
	private Member creator;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "share_team_id",referencedColumnName="id")
	private Member shareTeam;

	@Column(name ="create_date")
	private Timestamp createDate;
	
	@Column(name="`desc`")
	private String desc;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the creator
	 */
	public Member getCreator() {
		return creator;
	}

	/**
	 * @param creator the creator to set
	 */
	public void setCreator(Member creator) {
		this.creator = creator;
	}

	/**
	 * @return the shareTeam
	 */
	public Member getShareTeam() {
		return shareTeam;
	}

	/**
	 * @param shareTeam the shareTeam to set
	 */
	public void setShareTeam(Member shareTeam) {
		this.shareTeam = shareTeam;
	}

	/**
	 * @return the createDate
	 */
	public Timestamp getCreateDate() {
		return createDate;
	}

	/**
	 * @param createDate the createDate to set
	 */
	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	
}
