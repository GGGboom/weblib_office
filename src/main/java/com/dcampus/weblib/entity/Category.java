package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;


/**
 * category分类对应的是存储管理中的文件柜所在目录
 * name的规则是按照时间的毫秒，保证唯一性
 * 父分类为为0时表示根目录
 * 
 *添加path分类路径
 * 
 * @author patrick
 *
 */

@Entity
@Table(name = "weblib_category")
public class Category extends BaseEntity implements IBaseBean<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 记录状态：普通状态*/
	public static final int STATUS_NORMAL = 1;
	/** 记录状态：关闭状态 */
	public static final int STATUS_CLOSE = 2;
	
	/**分类id*/
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	
	/**父分类,为0时表示根目录*/
	@Column(name = "parent_id")
	private long parentId;
	 
	/**添加分类路径**/
	private String path;

	/** 分类名字 **/
	@Column(length=100)
	private String name;
	
	/** 分类显示名称 */
	@Column(name = "display_name", length=100)
	private String displayName;

	/** 分类描述 **/
	@Lob
	@Basic(fetch = FetchType.LAZY)   
	@Type(type="text") 
	@Column(name="description")
	private String desc;

	/** 分类创建时间 **/
	@Column(name = "create_date")
	private Timestamp createDate;
	
	/** 分类状态，供数据库使用 **/
	@Column(name="`status`")
	private int categoryStatus;
	
	/** 排列序号 **/
	@Column(name="order_no")
	private double order;

	//新增三个的字段

	@Column(name = "total_capacity")
	private Long totalCapacity;// kb

	@Column(name = "available_capacity")
	private Long availableCapacity;// kb

	@Column(name = "creator_name")
	private String creatorName;

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	/**该目录分类下的子柜子集合*/
	@OneToMany(mappedBy="category" ,cascade=CascadeType.REMOVE, fetch=FetchType. LAZY)
    private Set<Group> groups = new HashSet<Group>();

	public Long getTotalCapacity() {
		return totalCapacity;
	}

	public void setTotalCapacity(Long totalCapacity) {
		this.totalCapacity = totalCapacity;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}


	/******************************************************/


	public double getOrder() {
		return order;
	}

	public void setOrder(double order) {
		this.order = order;
	}
	
	public static final Comparator<Category> COMPARE_SORT_ORDER = new Comparator<Category>() {
        public int compare(Category r1, Category r2) {
        	double order1 = r1.getOrder();
        	double order2 = r2.getOrder();
            if (order1 == order2) {
            	return r1.getId() > r2.getId() ? 1 : -1; 
            }
            return (order1 < order2) ? -1 : 1;
        }
    };


	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getParentId() {
		return parentId;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}

	public int getCategoryStatus() {
		return categoryStatus;
	}

	public void setCategoryStatus(int categoryStatus) {
		this.categoryStatus = categoryStatus;
	}
	
	

}
