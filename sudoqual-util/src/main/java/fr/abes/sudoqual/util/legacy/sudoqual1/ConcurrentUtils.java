/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.abes.sudoqual.util.legacy.sudoqual1;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author clement
 */
@Deprecated
final class ConcurrentUtils {
    
    private ConcurrentUtils() {
    }
    
    public static <T> Set<T> createConcurrentSet() {
        return Collections.newSetFromMap(ConcurrentUtils.<T,Boolean> createConcurrentMap());
    }
    
    public static <T,U> ConcurrentMap<T,U> createConcurrentMap() {
        return new ConcurrentHashMap<>();
    }
    
    public static <T> Set<T> createConcurrentSet(Collection<T> c) {
        Set<T> tmp = ConcurrentUtils.<T> createConcurrentSet();
        tmp.addAll(c);
        return tmp;
    }
    

}
