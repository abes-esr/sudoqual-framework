/**
 * This file is part of the SudoQual project.
 */
package fr.abes.sudoqual.linking_module.multithreads;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * @author Clément Sipieter {@literal <clement@6pi.fr>}
 */
public final class ConcurrentUtils {
    
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
