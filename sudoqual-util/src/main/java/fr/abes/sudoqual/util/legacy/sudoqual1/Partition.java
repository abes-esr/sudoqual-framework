package fr.abes.sudoqual.util.legacy.sudoqual1;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import fr.abes.sudoqual.util.legacy.sudoqual1.exception.InconsistentPartitionException;

/**
 *
 * @author clement
 */
@Deprecated
public class Partition implements IPartition {
    
    private final Set<String> authorities;
    private final Set<String> contextuals;
    
    private final Map<String, Object> data;
    
    // the initial link from a contextual references to an authority reference
    private final Map<String, String> authorityInitialLinkOf;
    private final Map<String, Set<String>> contextualInitialLinksOf;
    // the sameAs link from a contextual references to an authority reference
    private final Map<String, String> authoritySameAs;
    private final Map<String, Set<String>> contextualsSameAs;
    // the set of suggested links from a contextual references to authority references
    private final Map<String, Set<String>> authoritiesSuggestedLinksOf;
    private final Map<String, Set<String>> contextualsSuggestedLinksOf;
    // the set of differentFrom links from a contextual references to authority references
    private final Map<String, Set<String>> authoritiesDifferentFrom;
    private final Map<String, Set<String>> contextualsDifferentFrom;
    
    ////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR 
    ////////////////////////////////////////////////////////////////////////////

    public Partition() {
        this.authorities = ConcurrentUtils.<String> createConcurrentSet();
        this.contextuals = ConcurrentUtils.<String> createConcurrentSet();
        
        this.data = ConcurrentUtils.<String, Object> createConcurrentMap();
        
        this.authorityInitialLinkOf = ConcurrentUtils.<String, String> createConcurrentMap();
        this.contextualInitialLinksOf = ConcurrentUtils.<String, Set<String>> createConcurrentMap();

        this.authoritySameAs = ConcurrentUtils.<String, String> createConcurrentMap();
        this.contextualsSameAs = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        
        this.authoritiesSuggestedLinksOf = ConcurrentUtils.<String, Set<String>> createConcurrentMap();;
        this.contextualsSuggestedLinksOf = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        
        this.authoritiesDifferentFrom = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        this.contextualsDifferentFrom = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        
    }
    
    public Partition(IPartition partition) throws InconsistentPartitionException {
        this.authorities = ConcurrentUtils.<String> createConcurrentSet(partition.getAuthorities());
        this.contextuals = ConcurrentUtils.<String> createConcurrentSet(partition.getContextuals());
        
        this.data = ConcurrentUtils.<String, Object> createConcurrentMap();
        
        this.authorityInitialLinkOf = ConcurrentUtils.<String, String> createConcurrentMap();
        this.contextualInitialLinksOf = ConcurrentUtils.<String, Set<String>> createConcurrentMap();

        this.authoritySameAs = ConcurrentUtils.<String, String> createConcurrentMap();
        this.contextualsSameAs = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        
        this.authoritiesSuggestedLinksOf = ConcurrentUtils.<String, Set<String>> createConcurrentMap();;
        this.contextualsSuggestedLinksOf = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        
        this.authoritiesDifferentFrom = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        this.contextualsDifferentFrom = ConcurrentUtils.<String, Set<String>> createConcurrentMap();
        
        
        for(String rc : this.getContextuals()) {
            if(partition.getAuthorityInitialLinkOf(rc) != null)
                this.setInitialLink(rc, partition.getAuthorityInitialLinkOf(rc));
            
            if(partition.getAuthoritySameAs(rc) != null)
                this.setSameAsLink(rc, partition.getAuthoritySameAs(rc));
            
            for(String ra : partition.getAuthorityDifferentFrom(rc)) {
                this.addDifferentFromLink(rc, ra);
            }
            
            for(String ra : partition.getAuthoritySuggestedLinksOf(rc)) {
                this.addSuggestedLink(ra, rc);
            }
        }

    }
    
    ////////////////////////////////////////////////////////////////////////////
    // GETTERS
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public Set<String> getAuthorities() {
        return Collections.unmodifiableSet(authorities);
    }

    @Override
    public Set<String> getContextuals() {
        return Collections.unmodifiableSet(contextuals);
    }
    
    @Override
    public Object getData(String reference) {
        return this.data.get(reference);
    }
    
    @Override
    public String getAuthoritySameAs(String contextual) {
        return this.authoritySameAs.get(contextual);
    }
    
    @Override 
    public Set<String> getContextualsSameAs(String authority) {
        Set<String> SameAsSet = this.contextualsSameAs.get(authority);
        if(SameAsSet == null) {
            SameAsSet = Collections.<String>emptySet();
        } else {
            SameAsSet = Collections.unmodifiableSet(SameAsSet);
        }
        return SameAsSet;
    }
    
    @Override
    public String getAuthorityInitialLinkOf(String contextual) {
        return this.authorityInitialLinkOf.get(contextual);
    }
    
    @Override
    public Set<String> getContextualInitialLinksOf(String authority) {
        Set<String> initialLinkSet = this.contextualInitialLinksOf.get(authority);
        if(initialLinkSet == null) {
            initialLinkSet = Collections.<String>emptySet();
        } else {
            initialLinkSet = Collections.unmodifiableSet(initialLinkSet);
        }
        return initialLinkSet;
    }
    
       @Override
    public Set<String> getAuthoritySuggestedLinksOf(String contextual) {
        Set<String> suggestedLinkSet = this.authoritiesSuggestedLinksOf.get(contextual);
        if(suggestedLinkSet == null) {
            suggestedLinkSet = Collections.<String>emptySet();
        } else {
            suggestedLinkSet = Collections.unmodifiableSet(suggestedLinkSet);
        }
        return suggestedLinkSet;
    }
    
     @Override
    public Set<String> getContextualSuggestedLinksOf(String authority) {
        Set<String> suggestedLinkSet = this.contextualsSuggestedLinksOf.get(authority);
        if(suggestedLinkSet == null) {
            suggestedLinkSet = Collections.<String>emptySet();
        } else {
            suggestedLinkSet = Collections.unmodifiableSet(suggestedLinkSet);
        }
        return suggestedLinkSet;
    }
    
       @Override
    public Set<String> getAuthorityDifferentFrom(String contextual) {
        Set<String> diffFromSet = this.authoritiesDifferentFrom.get(contextual);
        if(diffFromSet == null) {
            diffFromSet = Collections.<String>emptySet();
        } else {
            diffFromSet = Collections.unmodifiableSet(diffFromSet);
        }
        return diffFromSet;
    }
    
     @Override
    public Set<String> getContextualDifferentFrom(String authority) {
        Set<String> diffFromSet = this.contextualsDifferentFrom.get(authority);
        if(diffFromSet == null) {
            diffFromSet = Collections.<String>emptySet();
        } else {
            diffFromSet = Collections.unmodifiableSet(diffFromSet);
        }
        return diffFromSet;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // METHODS
    ////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void addAuthority(String authority) {
        this.authorities.add(authority);
    }
    
    @Override
    public void addContextual(String contextual) {
        this.contextuals.add(contextual);
    }
    
    @Override
    public void setData(String reference, Object data) {
        this.data.put(reference, data);
    }
    
    @Override
    public void setSameAsLink(String contextual, String authority) throws InconsistentPartitionException {
        this.addAuthority(authority);
        this.addContextual(contextual);
        
        Set<String> diffFrom = this.getAuthorityDifferentFrom(contextual);
        if(diffFrom != null && diffFrom.contains(authority)) {
            throw new InconsistentPartitionException(String.format("Inconsistent Partition: %s and %s are sameAs and differentFrom.", contextual, authority));
        }
        
        this.authoritySameAs.put(contextual, authority);
        putIntoSetFromMap(contextualsSameAs, authority, contextual);
    }

    @Override
    public void setInitialLink(String contextual, String authority) {
        this.addAuthority(authority);
        this.addContextual(contextual);
        
        this.authorityInitialLinkOf.put(contextual, authority);
        putIntoSetFromMap(contextualInitialLinksOf, authority, contextual);
    }
    
    @Override
    public void addSuggestedLink(String contextual, String authority) {
        this.addAuthority(authority);
        this.addContextual(contextual);
        
        putIntoSetFromMap(this.authoritiesSuggestedLinksOf, contextual, authority);
        putIntoSetFromMap(this.contextualsSuggestedLinksOf, authority, contextual);
    }

    @Override
    public void addDifferentFromLink(String contextual, String authority) throws InconsistentPartitionException {
        this.addAuthority(authority);
        this.addContextual(contextual);
        
        String sameAs = this.getAuthoritySameAs(contextual);
        if(sameAs != null && sameAs.equals(authority)) {
            throw new InconsistentPartitionException(String.format("Inconsistent Partition: %s and %s are sameAs and differentFrom.", contextual, authority));
        }
        
        putIntoSetFromMap(this.authoritiesDifferentFrom, contextual, authority);
        putIntoSetFromMap(this.contextualsDifferentFrom, authority, contextual);
    }

    //  @Override
    //  public void setDifferentFromLinks(String contextualReference, Set<String> authorityReferenceSet) {
    //      this.differentFrom.put(contextualReference, authorityReferenceSet);
    //  }
    
    ////////////////////////////////////////////////////////////////////////////
    // OBJECT METHODS 
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object other) {
        if (this == other) {
                return true;
        }
        if (other == null) {
                return false;
        }
        if (!(other instanceof IPartition)) {
                return false;
        }
        return this.equals((IPartition) other);
    }
    
    public boolean equals(IPartition other) {
        if(!this.getAuthorities().equals(other.getAuthorities())
                || !this.getContextuals().equals(other.getContextuals())) {
            return false;
        }
        
        for(String auth : this.getAuthorities()) {
            if(!Objects.equals(this.getContextualDifferentFrom(auth),other.getContextualDifferentFrom(auth))
                    || !Objects.equals(this.getContextualInitialLinksOf(auth),other.getContextualInitialLinksOf(auth))
                    || !Objects.equals(this.getContextualSuggestedLinksOf(auth),other.getContextualSuggestedLinksOf(auth))
                    || !Objects.equals(this.getContextualsSameAs(auth),other.getContextualsSameAs(auth))
                    )
                return false;
        }
        
        for(String cont : this.getContextuals()) {
             if(!Objects.equals(this.getAuthorityDifferentFrom(cont),other.getAuthorityDifferentFrom(cont))
                    || !Objects.equals(this.getAuthorityInitialLinkOf(cont),other.getAuthorityInitialLinkOf(cont))
                    || !Objects.equals(this.getAuthoritySuggestedLinksOf(cont),other.getAuthoritySuggestedLinksOf(cont))
                    || !Objects.equals(this.getAuthoritySameAs(cont),other.getAuthoritySameAs(cont))
                    )
                return false;
        }
        return true;
    }

    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Authorities:\n");
        for(String ra : this.getAuthorities()) {
            sb.append("\t")
                    .append(ra)
                    .append('\n');
        }
        
        sb.append("Contextual references:\n");
        for(String rc : this.getContextuals()) {
            String initialLink = this.getAuthorityInitialLinkOf(rc);
            String sameAs = this.getAuthoritySameAs(rc);
            Set<String> differentFrom = this.getAuthorityDifferentFrom(rc);
            Set<String> suggestedLinks = this.getAuthoritySuggestedLinksOf(rc);
            
            sb.append("\t").append(rc).append('\n');
            
            if(initialLink != null) {
                sb.append("\t\tinitial link: ").append(initialLink).append("\n");
            }
            
            if(sameAs != null) {
                sb.append("\t\tsame as: ").append(sameAs).append("\n");
            }
            
            if(!differentFrom.isEmpty()) {
                sb.append("\t\tdifferent from: \n");
                for(String raDiffFrom : differentFrom) {
                    sb.append("\t\t\t").append(raDiffFrom).append("\n");
                }
            }
            
            if(!suggestedLinks.isEmpty()) {
                sb.append("\t\tsuggested links: \n");
                for(String raSuggested : suggestedLinks) {
                    sb.append("\t\t\t").append(raSuggested).append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // PRIVATE 
    ////////////////////////////////////////////////////////////////////////////
    
    private static void putIntoSetFromMap(Map<String, Set<String>> map, String key, String value) {
        Set<String> set = map.get(key);
        if(set == null) {
            synchronized (map) {
                set = map.get(key);
                if(set == null) {
                    set = ConcurrentUtils.<String> createConcurrentSet();
                    map.put(key, set);
                }
            }
        }
        set.add(value);
    }
}
