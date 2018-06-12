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

package com.experoinc.container;

import com.experoinc.container.configuration.ContainerGraphConfiguration;
import com.experoinc.container.data.LoadGraph;
import com.experoinc.container.resources.ContainerResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

/**
 * @author twilmes
 */
public class ContainerGraphApplication extends Application<ContainerGraphConfiguration> {

    public static void main(String[] args) throws Exception {
        new ContainerGraphApplication().run(args);
    }

    public void run(ContainerGraphConfiguration configuration,
                    Environment environment) {
        final Cluster cluster = configuration.getTinkerPopFactory().build(environment);
        final Graph graph = EmptyGraph.instance();
        final GraphTraversalSource g = graph.traversal().
                withRemote(DriverRemoteConnection.using(cluster, "g"));

        LoadGraph.setupSchema(cluster.connect());
        LoadGraph.loadData(g);

        final ContainerResource containerResource = new ContainerResource(g);

        environment.jersey().register(containerResource);
    }
}
