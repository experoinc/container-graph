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

package com.experoinc.container.data;

import groovy.util.logging.Slf4j;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.select;

/**
 * @author twilmes
 */
@Slf4j
public class LoadGraph {

    private static final Logger logger = LoggerFactory.getLogger(LoadGraph.class);

    public static void setupSchema(final Client client) {
        // if schema is already loaded, skip this
        final String mgmtScript = "" +
                "mgmt = graph.openManagement()\n" +
                "if (mgmt.getVertexLabel('Service') != null) return false\n" +
                "mgmt.makeVertexLabel('Service').make()\n" +
                "mgmt.makeVertexLabel('Client').make()\n" +
                "mgmt.makeVertexLabel('Container').make()\n" +
                "mgmt.makeEdgeLabel('hosts').make()\n" +
                "mgmt.makeEdgeLabel('connectsTo').make()\n" +
                "mgmt.makePropertyKey('name').dataType(String.class).make()\n" +
                "mgmt.makePropertyKey('application').dataType(String.class).make()\n" +
                "mgmt.makePropertyKey('ipAddress').dataType(String.class).make()\n" +
                "mgmt.makePropertyKey('clientId').dataType(Integer.class).make()\n" +
                "containerId = mgmt.makePropertyKey('containerId').dataType(Integer.class).make()\n" +
                "mgmt.buildIndex('containerById', Vertex.class).addKey(containerId).buildCompositeIndex()\n" +
                "mgmt.commit()\n" +
                "return true";

        final ResultSet result = client.submit(mgmtScript);
        logger.info("Schema loaded? {}", result.one().getBoolean());
    }

    public static void loadData(final GraphTraversalSource g) {
        if (!g.V().hasNext()) {
            final List<Integer> clientIds = new LinkedList<>();
            for (int i = 0; i < 50; i++) clientIds.add(i);
            g.inject(clientIds).unfold().as("clientId").addV("Client").property("clientId", select("clientId")).store("clients").fold()
                    .addV("Service").property("name", "Google Kubernetes Engine").as("gke")
                    .addV("Container").property("application", "JanusGraph").property("containerId", 1).as("jg1")
                    .addV("Container").property("application", "JanusGraph").property("containerId", 2).as("jg2")
                    .addV("Container").property("application", "JanusGraph").property("containerId", 3).as("jg3")
                    .addV("Container").property("application", "Elasticsearch").property("containerId", 4).as("elastic")
                    .addV("Container").property("application", "API").property("containerId", 5).property("ipAddress", "10.1.0.1").as("api1").store("apis")
                    .addV("Container").property("application", "API").property("containerId", 6).property("ipAddress", "10.1.0.2").as("api2").store("apis")
                    .addV("Container").property("application", "API").property("containerId", 7).property("ipAddress", "10.1.0.3").as("api3").store("apis")
                    .addV("Service").property("name", "Cloud Bigtable").as("bigtable")
                    .addE("hosts").from("gke").to("jg1")
                    .addE("hosts").from("gke").to("jg2")
                    .addE("hosts").from("gke").to("jg3")
                    .addE("hosts").from("gke").to("elastic")
                    .addE("hosts").from("gke").to("api1")
                    .addE("hosts").from("gke").to("api2")
                    .addE("hosts").from("gke").to("api3")
                    .addE("connectsTo").from("jg1").to("elastic")
                    .addE("connectsTo").from("jg2").to("elastic")
                    .addE("connectsTo").from("jg3").to("elastic")
                    .addE("connectsTo").from("jg1").to("bigtable")
                    .addE("connectsTo").from("api1").to("jg1")
                    .addE("connectsTo").from("api2").to("jg2")
                    .addE("connectsTo").from("api3").to("jg1")
                    .cap("clients").unfold().as("client")
                    .local(select("apis").sample(Scope.local, 1).unfold()).addE("connectedTo").from("client").iterate();
            logger.info("Loaded graph");
        } else {
            logger.info("Graph already loaded");
        }
    }
}
