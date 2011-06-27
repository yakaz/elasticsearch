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

import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.plugin.script.dynamicboosting.DynamicValue;
import org.elasticsearch.plugin.script.dynamicboosting.DynamicValueService;
import org.elasticsearch.plugin.script.dynamicboosting.ScriptDynamicBoostingPlugin;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author ofavre
 */
public class DynamicBoostingNativeScriptFactory implements NativeScriptFactory {

    private DynamicValueService dynamicValueService;

    @Inject DynamicBoostingNativeScriptFactory(DynamicValueService dynamicValueService) {
        this.dynamicValueService = dynamicValueService;
    }

    @Override
    public ExecutableScript newScript(@Nullable Map<String, Object> params) {
        Map<DynamicValue,Float> boostValues = new EnumMap<DynamicValue,Float>(DynamicValue.class);
        Float boostMissing = DynamicBoostingScript.DEFAULT_BOOST_MISSING;
        Float boostError = DynamicBoostingScript.DEFAULT_BOOST_ERROR;

        if (params == null) {
            throw new ElasticSearchIllegalArgumentException("parameters missing for native script [" + ScriptDynamicBoostingPlugin.NAME + "]");
        }

        // Get and check parameters
        String keyField = getSetting(params, String.class, false, "key_field", "keyField", "keyfield");
        Map<String,Object> rawBoost = null;
        try {
            rawBoost = getSetting(params, Map.class, false, "boost_per_value", "boostPerValue", "boostpervalue");
        } catch (Throwable ex) {
            throw new ElasticSearchIllegalArgumentException("error parsing parameter [boost_per_value,boostPerValue,boostpervalue] for native script [" + ScriptDynamicBoostingPlugin.NAME + "]", ex);
        }
        if (rawBoost == null) {
            throw new ElasticSearchIllegalArgumentException("missing parameter [boost_per_value,boostPerValue,boostpervalue] for native script [" + ScriptDynamicBoostingPlugin.NAME + "]");
        }

        // Read values from the composed parameter "boost_per_value"
        for (Map.Entry<String,Object> entry : rawBoost.entrySet()) {
            // Check the value first (easier)
            if (entry.getValue() == null) continue;
            if (!(entry.getValue() instanceof Number)) continue;
            @SuppressWarnings(value="unchecked") Number fvalue = (Number)entry.getValue();
            try {
                DynamicValue value = DynamicValue.valueOf(entry.getKey());
                boostValues.put(value, fvalue.floatValue());
            } catch (IllegalArgumentException ex) {
                if ("missing".equals(entry.getKey())) {
                    boostMissing = fvalue.floatValue();
                } else if ("error".equals(entry.getKey())) {
                    boostError = fvalue.floatValue();
                }
            }
        }

        try {
            if ("_uid".equals(keyField)) {
                return new DynamicBoostingScriptDocId(boostValues, boostMissing, boostError, dynamicValueService);
            } else {
                return new DynamicBoostingScript(keyField, boostValues, boostMissing, boostError, dynamicValueService);
            }
        } catch (Throwable ex) {
            throw new ElasticSearchIllegalArgumentException("error instantiating native script [" + ScriptDynamicBoostingPlugin.NAME + "]");
        }
    }

    private <T> T getSetting(Map<String, Object> params, Class<? extends T> type, boolean allowNull, String... names) {
        Object value = null;
        boolean nullFound = false;
        for (String name : names) {
            value = params.get(name);
            if (value != null) {
                if (type.isInstance(value)) {
                    @SuppressWarnings(value="unchecked") T rtn = (T) value;
                    return rtn;
                }
            } else {
                nullFound = true;
            }
        }
        if (nullFound && allowNull) return null;
        StringBuilder printableNames = new StringBuilder();
        printableNames.append('[');
        boolean first = true;
        for (String name : names) {
            if (first) first = false;
            else printableNames.append(',');
            printableNames.append(name);
        }
        printableNames.append(']');
        throw new ElasticSearchIllegalArgumentException("no parameter for '"+printableNames.toString()+"' with type "+type.getName()+" were found for native script [" + ScriptDynamicBoostingPlugin.NAME + "]");
    }

}
