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

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.script.DynamicBoostingNativeScriptFactory;
import org.elasticsearch.script.ScriptModule;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.*;

/**
 * @author ofavre
 */
public class ScriptDynamicBoostingPlugin extends AbstractPlugin {

    public static final String NAME = "script-dynamicboosting";
    public static final String DESCRIPTION = "Script for boosting documents based on a dynamic attribute.";

    @Override public String name() {
        return NAME;
    }

    @Override public String description() {
        return DESCRIPTION;
    }

    @Override public void processModule(Module module) {
        if (module instanceof ScriptModule) {
            @SuppressWarnings(value="unchecked") ScriptModule scriptModule = (ScriptModule) module;
            scriptModule.registerScript("dynamicboosting", DynamicBoostingNativeScriptFactory.class);
            scriptModule.registerScript("dynamic_boosting", DynamicBoostingNativeScriptFactory.class);
        }
    }

    @Override public Collection<Class<? extends Module>> modules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(DynamicValueModule.class);
        return modules;
    }

    @Override public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> modules = newArrayList();
        modules.add(DynamicValueService.class);
        return modules;
    }
    
}
