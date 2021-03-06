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

package com.github.nalukit.nalu.client.internal.route;

import com.github.nalukit.nalu.client.internal.CompositeControllerReference;
import com.github.nalukit.nalu.client.plugin.IsNaluProcessorPlugin;
import com.github.nalukit.nalu.client.tracker.IsTracker;

import java.util.List;

public final class RouterImpl
    extends AbstractRouter {
  
  public RouterImpl(IsNaluProcessorPlugin plugin,
                    ShellConfiguration shellConfiguration,
                    RouterConfiguration routerConfiguration,
                    List<CompositeControllerReference> compositeControllerReferences,
                    IsTracker tracker,
                    String startRoute,
                    boolean hasHistory,
                    boolean usingHash,
                    boolean usingColonForParametersInUrl,
                    boolean stayOnSide) {
    super(compositeControllerReferences,
          shellConfiguration,
          routerConfiguration,
          plugin,
          tracker,
          startRoute,
          hasHistory,
          usingHash,
          usingColonForParametersInUrl,
          stayOnSide);
    this.plugin.register(hash -> RouterImpl.super.handleRouting(hash,
                                                                false));
  }
  
}
