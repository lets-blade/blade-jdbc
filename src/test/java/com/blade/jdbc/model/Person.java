package com.blade.jdbc.model;

import java.util.Date;

import com.blade.jdbc.Model;
import com.blade.jdbc.annotation.Column;
import com.blade.jdbc.annotation.GeneratedValue;
import com.blade.jdbc.annotation.Id;
import com.blade.jdbc.annotation.Table;

@Table(name = "person")
public class Person extends Model {
	
	private static final long serialVersionUID = 4137914504885271361L;

	@Id
	@Column(unique=true)
	@GeneratedValue 
	public Integer id;
	
	public String name;
	
	@Column(name="last_name")
	public String lastName;
	
	public Date dob;
	
	public Date created_at;

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", lastName=" + lastName + ", dob=" + dob + ", created_at="
				+ created_at + "]";
	}
	
}
