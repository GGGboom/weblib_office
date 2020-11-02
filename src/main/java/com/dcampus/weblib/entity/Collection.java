package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 收藏用户用户组等
 * @author patrick
 *
 */


@Entity
@Table(name = "weblib_collection")
public class Collection extends BaseEntity implements IBaseBean<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	/**
	 * 收藏者
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collector_id")
	private Member collector;
	/**
	 * 收藏品（用户、用户组、组织、自建组）
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_id")
	private Member collection;
	
	/**
	 * 收藏类型（person、team、folder 、shareTeam）
	 */
	@Column(name = "collection_type")
	private String collectionType;
	
	@Column(name = "collect_date")
	private Timestamp collectDate;

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
	 * @return the collector
	 */
	public Member getCollector() {
		return collector;
	}

	/**
	 * @param collector the collector to set
	 */
	public void setCollector(Member collector) {
		this.collector = collector;
	}

	/**
	 * @return the collection
	 */
	public Member getCollection() {
		return collection;
	}

	/**
	 * @param collection the collection to set
	 */
	public void setCollection(Member collection) {
		this.collection = collection;
	}

	/**
	 * @return the collectionType
	 */
	public String getCollectionType() {
		return collectionType;
	}

	/**
	 * @param collectionType the collectionType to set
	 */
	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
	}

	/**
	 * @return the collectDate
	 */
	public Timestamp getCollectDate() {
		return collectDate;
	}

	/**
	 * @param collectDate the collectDate to set
	 */
	public void setCollectDate(Timestamp collectDate) {
		this.collectDate = collectDate;
	}
}


