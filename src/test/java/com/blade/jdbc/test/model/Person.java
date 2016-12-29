package com.blade.jdbc.test.model;

import com.blade.jdbc.annotation.Table;

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
    private Date created_at;

    public Integer getId() {
        return id;
    }

    public Person setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public String getLast_name() {
        return last_name;
    }

    public Person setLast_name(String last_name) {
        this.last_name = last_name;
        return this;
    }

    public Date getDob() {
        return dob;
    }

    public Person setDob(Date dob) {
        this.dob = dob;
        return this;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public Person setCreated_at(Date created_at) {
        this.created_at = created_at;
        return this;
    }
}
