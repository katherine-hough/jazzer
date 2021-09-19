package com.code_intelligence.jazzer.api;

import java.util.function.Consumer;

@FunctionalInterface
public interface Consumer1<T1> extends Consumer<T1> {
  @Override
  void accept(T1 t1);
}
