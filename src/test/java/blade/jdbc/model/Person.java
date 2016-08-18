package blade.jdbc.model;

import java.util.Date;

import blade.jdbc.Model;
import blade.jdbc.annotation.Column;
import blade.jdbc.annotation.GeneratedValue;
import blade.jdbc.annotation.Id;
import blade.jdbc.annotation.Table;

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
