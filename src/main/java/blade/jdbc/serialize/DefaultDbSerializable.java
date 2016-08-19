package blade.jdbc.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DefaultDbSerializable implements DbSerializable {

	@Override
	public String serialize(Object obj) {
		try {
	        ByteArrayOutputStream bo = new ByteArrayOutputStream();
	        ObjectOutputStream so = new ObjectOutputStream(bo);
	        so.writeObject(obj);
	        so.flush();
	        // This encoding induces a bijection between byte[] and String (unlike UTF-8)
	        return bo.toString("ISO-8859-1");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return null;
	}

	@Override
	public Object deserialize(String str) {
		// deserialize the object
	    try {
	        // This encoding induces a bijection between byte[] and String (unlike UTF-8)
	        byte b[] = str.getBytes("ISO-8859-1"); 
	        ByteArrayInputStream bi = new ByteArrayInputStream(b);
	        ObjectInputStream si = new ObjectInputStream(bi);
	        return si.readObject();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	@Override
	public Object deserialize(String str, Class<?> cls) {
		// deserialize the object
	    try {
	        // This encoding induces a bijection between byte[] and String (unlike UTF-8)
	        byte b[] = str.getBytes("ISO-8859-1"); 
	        ByteArrayInputStream bi = new ByteArrayInputStream(b);
	        ObjectInputStream si = new ObjectInputStream(bi);
	        return cls.cast(si.readObject());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

}
