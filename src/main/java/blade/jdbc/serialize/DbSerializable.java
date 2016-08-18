package blade.jdbc.serialize;

/**
 * Serializes a class to and from a string. Implementations must have a zero-arg
 * constructor and must be thread-safe.
 */
public interface DbSerializable {

	String serialize(Object in);

	Object deserialize(String in, Class<?> targetClass);

}
