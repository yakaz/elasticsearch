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

package org.elasticsearch.plugin.script.dynamicboosting;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ofavre
 */
public class DynamicValueService extends AbstractLifecycleComponent<DynamicValueService> {

    public final Map<String,DynamicValue> dynamicValues;

    @Inject public DynamicValueService(Settings settings) {
        super(settings);
        dynamicValues = new ConcurrentHashMap<String,DynamicValue>();
        // This map is to be updated somehow
    }

    public DynamicValue getValue(String key) {
        return dynamicValues.get(prepareKey(key));
    }

    protected String prepareKey(String key) {
        if (key == null) return null;
        // Strip things
        int pos = key.indexOf('@');
        if (pos >= 0) return key.substring(0,pos);
        return key;
    }

    @Override protected void doStart() throws ElasticSearchException {
        // Start fresh
        dynamicValues.clear();
    }

    @Override protected void doStop() throws ElasticSearchException {
        // Clean-up
        dynamicValues.clear();
    }

    @Override protected void doClose() throws ElasticSearchException {
        // Clean-up
        dynamicValues.clear();
    }

}
