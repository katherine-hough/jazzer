package com.code_intelligence.jazzer.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.jodah.typetools.TypeResolver;
import net.jodah.typetools.TypeResolver.Unknown;

public class Meta {
  public static Object consume(FuzzedDataProvider data, Class<?> type) {
    if (type == byte.class || type == Byte.class) {
      return data.consumeByte();
    } else if (type == short.class || type == Short.class) {
      return data.consumeShort();
    } else if (type == int.class || type == Integer.class) {
      return data.consumeInt();
    } else if (type == long.class || type == Long.class) {
      return data.consumeLong();
    } else if (type == float.class || type == Float.class) {
      return data.consumeFloat();
    } else if (type == double.class || type == Double.class) {
      return data.consumeDouble();
    } else if (type == boolean.class || type == Boolean.class) {
      return data.consumeBoolean();
    } else if (type == char.class || type == Character.class) {
      return data.consumeChar();
    } else if (type == CharSequence.class || type == String.class) {
      return data.consumeString(data.remainingBytes() / 2);
    } else if (type == byte[].class || type == Byte[].class) {
      return data.consumeBytes(data.remainingBytes() / 2);
    } else if (type == InputStream.class || type == ByteArrayInputStream.class) {
      return new ByteArrayInputStream(data.consumeBytes(data.remainingBytes() / 2));
    } else if (type.isEnum()) {
      return data.pickValue(type.getEnumConstants());
    } else if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
      Constructor<?> chosenConstructor = data.pickValue(type.getConstructors());
      Class<?>[] argumentTypes = chosenConstructor.getParameterTypes();
      List<?> arguments = Arrays.stream(chosenConstructor.getParameterTypes())
          .map((argumentType) -> consume(data, argumentType))
          .collect(Collectors.toList());
    }
    return null;
  }

  private static Object consumeChecked(FuzzedDataProvider data, Class<?>[] types, int i) {
    if (types[i] == Unknown.class) {
      throw new IllegalArgumentException("Failed to determine type of argument " + (i + 1));
    }
    Object result = consume(data, types[i]);
    if (result != null && !types[i].isAssignableFrom(result.getClass())) {
      throw new IllegalStateException(
          "consume returned " + result.getClass() + ", but need " + types[i]);
    }
    return result;
  }

  public static Object autofuzz(FuzzedDataProvider data, Method method) {
    if (Modifier.isStatic(method.getModifiers())) {
      return autofuzz(data, method, null);
    } else {
      return autofuzz(data, method, consume(data, method.getDeclaringClass()));
    }
  }

  public static Object autofuzz(FuzzedDataProvider data, Method method, Object thisObject) {
    Object[] arguments = consumeArguments(data, method);
    try {
      return method.invoke(thisObject, arguments);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  public static <R> R autofuzz(FuzzedDataProvider data, Constructor<R> constructor) {
    Object[] arguments = consumeArguments(data, constructor);
    try {
      return constructor.newInstance(arguments);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  private static Object[] consumeArguments(FuzzedDataProvider data, Executable executable) {
     return Arrays.stream(executable.getParameterTypes())
        .map((type) -> consume(data, type))
        .toArray();
  }

  public static <T1> void autofuzz(FuzzedDataProvider data, Consumer1<T1> func) {
    Class<?> type = TypeResolver.resolveRawArgument(Consumer1.class, func.getClass());
    if (type == Unknown.class) {
      throw new IllegalArgumentException("Failed to determine type of argument 1");
    }
    Object result = consume(data, type);
    if (result != null && !type.isAssignableFrom(result.getClass())) {
      throw new IllegalStateException(
          "consume returned " + result.getClass() + ", but need " + type);
    }
    func.accept((T1) result);
  }

  public static <T1, R> R autofuzz(FuzzedDataProvider data, Function1<T1, R> func) {
    Class<?>[] types = TypeResolver.resolveRawArguments(Function1.class, func.getClass());
    return func.apply((T1) consumeChecked(data, types, 0));
  }

  public static <T1, T2, R> R autofuzz(FuzzedDataProvider data, Function2<T1, T2, R> func) {
    Class<?>[] types = TypeResolver.resolveRawArguments(Function2.class, func.getClass());
    return func.apply((T1) consumeChecked(data, types, 0), (T2) consumeChecked(data, types, 1));
  }
}
