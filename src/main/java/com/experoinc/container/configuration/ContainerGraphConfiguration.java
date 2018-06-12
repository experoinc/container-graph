// Copyright 2018 Expero Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.experoinc.container.configuration;

import com.experoinc.dropwizard.tinkerpop.TinkerPopFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author twilmes
 */
public class ContainerGraphConfiguration extends Configuration {

    @Valid
    @NotNull
    private TinkerPopFactory tinkerPopFactory = new TinkerPopFactory();

    @JsonProperty("tinkerPop")
    public TinkerPopFactory getTinkerPopFactory() {
        return tinkerPopFactory;
    }

    @JsonProperty("tinkerPop")
    public void setTinkerPopFactory(TinkerPopFactory tinkerPopFactory) {
        this.tinkerPopFactory = tinkerPopFactory;
    }
}
