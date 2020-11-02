package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;

/**
 * 容量分配
 * @author patrick
 *
 */
@Entity
@Table(name = "weblib_capacity_distribution")
public class CapacityDistribution  extends BaseEntity {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static enum FromType {
		GLOBAL_CAPACITY, DOMAIN_CAPACITY
	}

	public static enum ToType {
		PERSONAL_GROUP, DOMAIN_GROUP, DOMAIN
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "from_domain_id")
	private Domain fromDomain;// 不做级联删除

	@Enumerated(EnumType.STRING)
	@Column(name = "from_type")
	private FromType fromType;// 容量来源类别

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "to_group_id")
	private Group toGroup;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "to_domain_id")
	private Domain toDomain;// 不做级联删除

	@Enumerated(EnumType.STRING)
	@Column(name = "to_type")
	private ToType toType;// 容量去向类别

	@Column(name = "capacity")
	private long capacity;// KB 为单位

	@Column(name = "member_id")
	private long memberId;// 仅做记录，不做关联。不做级联删除

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
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the fromDomain
	 */
	public Domain getFromDomain() {
		return fromDomain;
	}

	/**
	 * @param fromDomain
	 *            the fromDomain to set
	 */
	public void setFromDomain(Domain fromDomain) {
		this.fromDomain = fromDomain;
	}

	/**
	 * @return the fromType
	 */
	public FromType getFromType() {
		return fromType;
	}

	/**
	 * @param fromType
	 *            the fromType to set
	 */
	public void setFromType(FromType fromType) {
		this.fromType = fromType;
	}

	/**
	 * @return the toGroup
	 */
	public Group getToGroup() {
		return toGroup;
	}

	/**
	 * @param toGroup
	 *            the toGroup to set
	 */
	public void setToGroup(Group toGroup) {
		this.toGroup = toGroup;
	}

	/**
	 * @return the toDomain
	 */
	public Domain getToDomain() {
		return toDomain;
	}

	/**
	 * @param toDomain
	 *            the toDomain to set
	 */
	public void setToDomain(Domain toDomain) {
		this.toDomain = toDomain;
	}

	/**
	 * @return the toType
	 */
	public ToType getToType() {
		return toType;
	}

	/**
	 * @param toType
	 *            the toType to set
	 */
	public void setToType(ToType toType) {
		this.toType = toType;
	}

	/**
	 * @return the capacity
	 */
	public long getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity
	 *            the capacity to set
	 */
	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the memberId
	 */
	public long getMemberId() {
		return memberId;
	}

	/**
	 * @param memberId
	 *            the memberId to set
	 */
	public void setMemberId(long memberId) {
		this.memberId = memberId;
	}
}
