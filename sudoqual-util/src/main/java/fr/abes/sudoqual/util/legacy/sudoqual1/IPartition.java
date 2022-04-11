/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.abes.sudoqual.util.legacy.sudoqual1;

import java.util.Set;

import fr.abes.sudoqual.util.legacy.sudoqual1.exception.InconsistentPartitionException;

/**
 * This class represents a partition between a set of authority references and
 * a set of contextual references. Where these references could be considered 
 * sameAs or differentFrom. We also embed an initial link which represents the
 * sameAs link found in the database. 
 * @author clement
 */
@Deprecated
public interface IPartition {
    
    Set<String> getAuthorities();
    
    Set<String> getContextuals();
    
    Object getData(String reference);
    
    String getAuthoritySameAs(String contextual);
    
    Set<String> getContextualsSameAs(String authority);

    String getAuthorityInitialLinkOf(String contextual);
    
    Set<String> getContextualInitialLinksOf(String authority);
    
    Set<String> getAuthoritySuggestedLinksOf(String contextual);
    
    Set<String> getContextualSuggestedLinksOf(String authority);
    
    Set<String> getAuthorityDifferentFrom(String contextual);
    
    Set<String> getContextualDifferentFrom(String authority);
    
    void addAuthority(String ppn);
    
    void addContextual(String ppn);
    
    void setData(String reference, Object data);
    
    void setSameAsLink(String contextualReference, String authorityReference) throws InconsistentPartitionException;
    
    void setInitialLink(String contextualReference, String authorityReference);
    
    void addSuggestedLink(String contextual, String authority);
    
    void addDifferentFromLink(String contextualReference, String authorityReference) throws InconsistentPartitionException;
    
   // void setDifferentFromLinks(String contextualReference, Set<String> authorityReference);
    
}
