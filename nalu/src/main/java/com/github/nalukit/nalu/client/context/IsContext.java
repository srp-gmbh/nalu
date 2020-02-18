/*
 * Copyright (c) 2018 - 2020 - Frank Hossfeld
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package com.github.nalukit.nalu.client.context;

import com.github.nalukit.nalu.client.context.module.IsMainContext;
import com.github.nalukit.nalu.client.context.module.IsModuleContext;

/**
 * Marks a class as Nalu application context inside a single and module application.
 *
 * Keep in mind, in a multi module project you need to use:
 * <ul>
 *   <li>{@link IsMainContext} inside the application project</li>
 *   <li>{@link IsModuleContext} inside the module projects</li>
 * </ul>
 */
public interface IsContext {
}
