package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * @author fzhang at 2017年10月26日
 * 分享组和成员关联表
 *
 */
@Entity
@Table(name = "weblib_share_team_member")
public class ShareTeamMember extends BaseEntity implements IBaseBean<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6768951632307559900L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "share_team_id")
	private Member shareTeam;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "member_id")
	private Member member;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the shareTeam
	 */
	public Member getShareTeam() {
		return shareTeam;
	}

	/**
	 * @param shareTeam
	 *            the shareTeam to set
	 */
	public void setShareTeam(Member shareTeam) {
		this.shareTeam = shareTeam;
	}

	/**
	 * @return the member
	 */
	public Member getMember() {
		return member;
	}

	/**
	 * @param member
	 *            the member to set
	 */
	public void setMember(Member member) {
		this.member = member;
	}
}
