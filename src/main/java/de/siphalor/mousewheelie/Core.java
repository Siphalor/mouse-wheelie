package de.siphalor.mousewheelie;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Core {

	public static final String MODID = "mousewheelie";
	public static int scrollFactor = -1;

	public static <T> T getField(Object object, String fieldName) throws IllegalAccessException, NoSuchFieldException {
		Field field = object.getClass().getDeclaredField(fieldName);
		if(!field.isAccessible()) {
			field.setAccessible(true);
		}
		return (T) field.get(object);
	}

	public static <T> void setField(Object object, String fieldName, T value) throws NoSuchFieldException, IllegalAccessException {
		Field field = object.getClass().getDeclaredField(fieldName);
		if(!field.isAccessible()) {
			field.setAccessible(true);
		}
		field.set(object, value);
	}

	public static <T> T callMethod(Object object, String methodName, Object... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class[] classes = Arrays.stream(args).map(obj -> obj.getClass()).toArray(Class[]::new);
        Method method = object.getClass().getDeclaredMethod(methodName, classes);
        if(!method.isAccessible()) {
        	method.setAccessible(true);
        }
        return (T) method.invoke(object, args);
	}

}
