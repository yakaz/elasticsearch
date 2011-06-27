/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.script;


import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.index.mapper.Uid;
import org.elasticsearch.plugin.script.dynamicboosting.DynamicValue;
import org.elasticsearch.plugin.script.dynamicboosting.DynamicValueService;
import org.elasticsearch.plugin.script.dynamicboosting.ScriptDynamicBoostingPlugin;
import org.elasticsearch.search.lookup.FieldLookup;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author ofavre
 */
public class DynamicBoostingScriptDocId extends AbstractSearchScript {

    public static final float DEFAULT_BOOST = 1.0f;
    public static final float DEFAULT_BOOST_MISSING = 1.0f;
    public static final float DEFAULT_BOOST_ERROR = Float.NaN;

    private DynamicValueService dynamicValueService;
    private Float boostError;
    private Float boostMissing;
    private final Map<DynamicValue,Float> boostPerValue;

    public DynamicBoostingScriptDocId(Map<DynamicValue,Float> boostPerValue, Float boostMissing, Float boostError, DynamicValueService dynamicValueService) {
        this.dynamicValueService = dynamicValueService;
        this.boostPerValue = new EnumMap<DynamicValue, Float>(DynamicValue.class);
        this.boostPerValue.putAll(boostPerValue);
        for (DynamicValue value : DynamicValue.values()) {
            if (!this.boostPerValue.containsKey(value)) {
                this.boostPerValue.put(value, DEFAULT_BOOST);
            }
        }
        this.boostError = boostError != null ? boostError : DEFAULT_BOOST_ERROR;
        this.boostMissing = boostMissing != null ? boostMissing : DEFAULT_BOOST_MISSING;
    }

    @Override public Object run() {
        float score = score();
        if (Float.isNaN(score)) score = 1.0f;
        @SuppressWarnings(value="unchecked") FieldLookup flookup = (FieldLookup)fields().get("_uid");
        Object value = flookup.getValue();
        if (value != null && value instanceof Uid) {
            @SuppressWarnings(value="unchecked") String uid = ((Uid) value).id();
            // Strip some part of the id
            int end = uid.indexOf('@');
            if (end < 0) return score * boostError;
            DynamicValue dynamicValue = dynamicValueService.getValue(uid.substring(0, end));
            return score * (dynamicValue == null ? boostMissing : boostPerValue.get(dynamicValue));
        } else {
            return score * boostError;
        }
    }

}
