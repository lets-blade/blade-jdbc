package com.blade.jdbc.test.model;

import com.blade.jdbc.annotation.Table;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by biezhi on 2016/12/25.
 */
@Table(name = "person")
public class Person implements Serializable {

    private Integer id;
    private String name;
    private String last_name;
    private Date dob;

    @Column(name = "created_at")
    private Date createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
