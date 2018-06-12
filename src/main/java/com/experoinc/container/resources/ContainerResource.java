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

package com.experoinc.container.resources;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.both;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

/**
 * @author twilmes
 */

@Path("/containers")
@Produces(MediaType.APPLICATION_JSON)
public class ContainerResource {

    private final GraphTraversalSource g;

    public ContainerResource(final GraphTraversalSource g) {
        this.g = g;
    }

    @GET
    public List<Map<String, Object>> getContainers() {
        return g.V().hasLabel("Container").valueMap().toList();
    }

    @GET
    @Path("{id}")
    public Map<String, Object> getContainer(@PathParam("id") long containerId) {
        return g.V(containerId).hasLabel("Container").valueMap().next();
    }


    @GET
    @Path("{id}/connectedTo")
    public List<Map<String, Object>> connectedTo(@PathParam("id") long applicationId, @DefaultValue("1") @QueryParam("hops")  int hops) {
        return g.V(applicationId).repeat(both("connectsTo")).times(hops).dedup().valueMap().toList();
    }

    @GET
    @Path("{id}/dependsOn")
    public List<Map<String, Object>> dependsOn(@PathParam("id") long applicationId) {
        return g.V(applicationId).repeat(out("connectsTo")).emit().valueMap().toList();
    }

    @GET
    @Path("{id}/dependencyOf")
    public List<Map<String, Object>> dependencyOf(@PathParam("id") long applicationId) {
        return g.V(applicationId).repeat(in("connectsTo")).emit().valueMap().toList();
    }
}
