package philo.magicsproutjpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtils {

	private ReflectionUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static <T> T newInstance(Class<T> clazz) {

		try {
			return clazz.getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Class<T> getGenericType(Class<T> clazz, int genericIndex) {

		ParameterizedType superclass = (ParameterizedType) clazz.getGenericSuperclass();
		Type[] typeArguments = superclass.getActualTypeArguments();
		Type typeArgument = typeArguments[genericIndex - 1];

		return (Class<T>) typeArgument;
	}
}
