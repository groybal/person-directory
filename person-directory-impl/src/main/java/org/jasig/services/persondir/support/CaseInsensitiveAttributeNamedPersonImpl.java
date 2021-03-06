/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.services.persondir.support;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jasig.services.persondir.IPersonAttributes;

/**
 * Custom IPersonAttributes that uses a case insensitive Map to hide attribute name case.  The attribute names
 * remain in case-specific form.
 */
public class CaseInsensitiveAttributeNamedPersonImpl extends AttributeNamedPersonImpl {
    private static final long serialVersionUID = 1L;

    public CaseInsensitiveAttributeNamedPersonImpl(final Map<String, List<Object>> attributes) {
        super(attributes);
    }

    public CaseInsensitiveAttributeNamedPersonImpl(final String userNameAttribute, final Map<String, List<Object>> attributes) {
        super(userNameAttribute, attributes);
    }

    public CaseInsensitiveAttributeNamedPersonImpl(final IPersonAttributes personAttributes) {
        super(personAttributes);
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.BasePersonImpl#createImmutableAttributeMap(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Object>> createImmutableAttributeMap(int size) {
        // NOTE:  Collections4 API for ListOrderedMap indicates it should not wrap CaseInsensitiveMap.  I found
        // that if you do not wrap the CaseInsensitiveMap with the ListOrderedMap, the attribute names become
        // all lower case which at this point breaks backwards compatibility.
        // To remove the ListOrderedMap you must make default person directory behavior the case-sensitive
        // behavior, but also insure case-insensitive comparison in
        // AbstractQueryPersonAttributeDao.mapPersonAttributes.  James W 6/15
        // See https://issues.jasig.org/browse/PERSONDIR-89
        // https://commons.apache.org/proper/commons-collections/apidocs/index.html?org/apache/commons/collections4/map/ListOrderedMap.html
        return ListOrderedMap.listOrderedMap(new CaseInsensitiveMap(size > 0 ? size : 1));
    }
}
