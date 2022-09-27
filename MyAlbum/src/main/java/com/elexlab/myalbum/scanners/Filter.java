package com.elexlab.myalbum.scanners;

public interface Filter<T> {
    boolean filter(T attributes);
}
