package com.code_intelligence.jazzer.api;

import java.util.function.BiFunction;

@FunctionalInterface
public interface Function2<T1, T2, R> extends BiFunction<T1, T2, R> {
  @Override
  R apply(T1 arg1, T2 arg2);
}
