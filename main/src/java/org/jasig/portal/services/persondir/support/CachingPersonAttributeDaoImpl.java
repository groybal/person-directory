/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.services.persondir.support;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.services.persondir.IPersonAttributeDao;

/**
 * A configurable caching implementation of {@link IPersonAttributeDao} 
 * which caches results from a wrapped IPersonAttributeDao. 
 * <br>
 * <br>
 * Configuration:
 * <table border="1">
 *     <tr>
 *         <th align="left">Property</th>
 *         <th align="left">Description</th>
 *         <th align="left">Required</th>
 *         <th align="left">Default</th>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">cachedPersonAttributesDao</td>
 *         <td>
 *             The {@link org.jasig.portal.services.persondir.IPersonAttributeDao} to delegate
 *             queries to on cache misses.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">userInfoCache</td>
 *         <td>
 *             The {@link java.util.Map} to use for result caching. This class does no cache
 *             maintenence. It is assumed the underlying Map implementation will ensure the cache
 *             is in a good state at all times.
 *         </td>
 *         <td valign="top">Yes</td>
 *         <td valign="top">null</td>
 *     </tr>
 *     <tr>
 *         <td align="right" valign="top">cacheKeyAttributes</td>
 *         <td>
 *             A Set of attribute names to use when building the cache key. The default
 *             implementation generates the key as a Map of attributeNames to values retrieved
 *             from the seed for the query. Zero length sets are treaded as null.
 *         </td>
 *         <td valign="top">No</td>
 *         <td valign="top">null</td>
 *     </tr>
 * </table>
 * 
 * 
 * @author dgrimwood@unicon.net
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Id
 */
public class CachingPersonAttributeDaoImpl extends AbstractDefaultQueryPersonAttributeDao {
    private long queries = 0;
    private long misses = 0;
    
    /*
     * The IPersonAttributeDao to delegate cache misses to.
     */
    private IPersonAttributeDao cachedPersonAttributesDao = null;
    
    /*
     * The cache to store query results in.
     */
    private Map userInfoCache = null; 
    
    /*
     * The set of attributes to use to generate the cache key.
     */
    private Set cacheKeyAttributes = null;
    
    
    /**
     * @return Returns the cachedPersonAttributesDao.
     */
    public IPersonAttributeDao getCachedPersonAttributesDao() {
        return this.cachedPersonAttributesDao;
    }
    /**
     * @param cachedPersonAttributesDao The cachedPersonAttributesDao to set.
     */
    public void setCachedPersonAttributesDao(IPersonAttributeDao cachedPersonAttributesDao) {
        if (cachedPersonAttributesDao == null) {
            throw new IllegalArgumentException("cachedPersonAttributesDao may not be null");
        }

        this.cachedPersonAttributesDao = cachedPersonAttributesDao;
    }
    
    /**
     * @return Returns the cacheKeyAttributes.
     */
    public Set getCacheKeyAttributes() {
        return this.cacheKeyAttributes;
    }
    /**
     * @param cacheKeyAttributes The cacheKeyAttributes to set.
     */
    public void setCacheKeyAttributes(Set cacheKeyAttributes) {
        this.cacheKeyAttributes = cacheKeyAttributes;
    }

    /**
     * @return Returns the userInfoCache.
     */
    public Map getUserInfoCache() {
        return this.userInfoCache;
    }
    /**
     * @param userInfoCache The userInfoCache to set.
     */
    public void setUserInfoCache(Map userInfoCache) {
        if (userInfoCache == null) {
            throw new IllegalArgumentException("userInfoCache may not be null");
        }

        this.userInfoCache = userInfoCache;
    }
    
    /**
     * @return Returns the number of cache misses.
     */
    public long getMisses() {
        return this.misses;
    }
    
    /**
     * @return Returns the number of queries.
     */
    public long getQueries() {
        return this.queries;
    }
    
    
    /**
     * Wraps the call to the specified cachedPersonAttributesDao IPersonAttributeDao delegate with
     * a caching layer. Results are cached using keys generated by {@link #getCacheKey(Map)}.
     * 
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getUserAttributes(java.util.Map)
     */
    public Map getUserAttributes(Map seed) {
        //Ensure the arguements and state are valid
        if (seed == null) {
            throw new IllegalArgumentException("The query seed Map cannot be null.");
        }
        
        if (this.cachedPersonAttributesDao == null) {
            throw new IllegalStateException("No 'cachedPersonAttributesDao' has been specified.");
        }
        if (this.userInfoCache == null) {
            throw new IllegalStateException("No 'userInfoCache' has been specified.");
        }
        
        final Object cacheKey = this.getCacheKey(seed);
        
        if (cacheKey != null) {
            final Map cacheResults = (Map)this.userInfoCache.get(cacheKey);
            if (cacheResults != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Retrieved query from cache. key='" + cacheKey + "', results='" + cacheResults + "'");
                }
                    
                this.queries++;
                if (log.isDebugEnabled()) {
                    log.debug("Cache Stats: queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
                }
                
                return cacheResults;
            }
        
            final Map queryResults = this.cachedPersonAttributesDao.getUserAttributes(seed);
        
            this.userInfoCache.put(cacheKey, queryResults);
            
            if (log.isDebugEnabled()) {
                log.debug("Retrieved query from wrapped IPersonAttributeDao and stored in cache. key='" + cacheKey + "', results='" + queryResults + "'");
            }
            
            this.queries++;
            this.misses++;
            if (log.isDebugEnabled()) {
                log.debug("Cache Stats: queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
            }

            return queryResults;
        }
        else {
            log.warn("No cache key generated, caching disabled for this query. query='" + seed + "', cacheKeyAttributes=" + this.cacheKeyAttributes + "', defaultAttributeName='" + this.getDefaultAttributeName() + "'");

            this.queries++;
            this.misses++;
            if (log.isDebugEnabled()) {
                log.debug("Cache Stats: queries=" + this.queries + ", hits=" + (this.queries - this.misses) + ", misses=" + this.misses);
            }
            
            return this.cachedPersonAttributesDao.getUserAttributes(seed);
        }
    }

    /**
     * @see org.jasig.portal.services.persondir.IPersonAttributeDao#getPossibleUserAttributeNames()
     */
    public Set getPossibleUserAttributeNames() {
        return this.cachedPersonAttributesDao.getPossibleUserAttributeNames();
    }
    
    /**
     * Generates a Serializable cache key from the seed parameters according to the documentation
     * of this class. If the return value is NULL caching will be disabled for this query.
     * 
     * @param querySeed The query to base the key on.
     * @return A Serializable cache key.
     */
    protected Serializable getCacheKey(Map querySeed) {
        final HashMap cacheKey = new HashMap();
        
        if (this.cacheKeyAttributes == null || this.cacheKeyAttributes.size() == 0) {
            final String defaultAttrName = this.getDefaultAttributeName();
            
            if (defaultAttrName == null) {
                throw new IllegalStateException("Both 'defaultAttrName' and 'cacheKeyAttributes' properties may not be null.");
            }
            
            cacheKey.put(defaultAttrName, querySeed.get(defaultAttrName));
            
            if (log.isDebugEnabled()) {
                log.debug("Created cacheKey='" + cacheKey + "' from query='" + querySeed + "' using default attribute='" + defaultAttrName + "'");
            }
        }
        else {
            for (final Iterator attrItr = this.cacheKeyAttributes.iterator(); attrItr.hasNext();) {
                final String attr = (String)attrItr.next();
                cacheKey.put(attr, querySeed.get(attr));
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Created cacheKey='" + cacheKey + "' from query='" + querySeed + "' using attributes='" + this.cacheKeyAttributes + "'");
            }
        }
        
        if (cacheKey.size() > 0) {
            return cacheKey;
        }
        else {
            return null;
        }
    }
}
