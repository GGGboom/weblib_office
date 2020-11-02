package com.dcampus.weblib.entity;

import com.dcampus.common.persistence.BaseEntity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "weblib_app_module")
public class AppModule extends BaseEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 模块名字*
     */
    @Column(length = 100)
    private String name;

    /**
     * 模块描述*
     */
    @Column(name = "description", length = 500)
    private String desc;

    /**
     * 创建时间*
     */
    @Column(name = "create_date")

    private Timestamp createDate;

    /**
     * 模块url*
     */
    @Column(name = "url", length = 500)
    private String url;

    /**
     * 模块logo地址*
     */
    @Column(name = "logo", length = 500)
    private String logo;

    /**
     * 模块app标识符*
     */
    @Column(name = "app_schema", length = 500)
    private String schema;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
