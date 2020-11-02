package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "weblib_share_response")
public class ShareResponse extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "responder_id")
	private Member responder;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "share_wrap_id")
	private ShareWrap shareWrap;
	
	@Column(name="content")
	private String content;
	
	@Column(name="response_date")
	private Timestamp responseDate;

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
	 * @return the responder
	 */
	public Member getResponder() {
		return responder;
	}

	/**
	 * @param responder
	 *            the responder to set
	 */
	public void setResponder(Member responder) {
		this.responder = responder;
	}

	/**
	 * @return the shareWrap
	 */
	public ShareWrap getShareWrap() {
		return shareWrap;
	}

	/**
	 * @param shareWrap
	 *            the shareWrap to set
	 */
	public void setShareWrap(ShareWrap shareWrap) {
		this.shareWrap = shareWrap;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the responseDate
	 */
	public Timestamp getResponseDate() {
		return responseDate;
	}

	/**
	 * @param responseDate
	 *            the responseDate to set
	 */
	public void setResponseDate(Timestamp responseDate) {
		this.responseDate = responseDate;
	}
}
