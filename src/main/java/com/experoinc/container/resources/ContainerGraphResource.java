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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.both;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.repeat;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.select;

/**
 * @author twilmes
 */

@Path("/containers")
@Produces(MediaType.APPLICATION_JSON)
public class ContainerGraphResource {

    private final GraphTraversalSource g;

    public ContainerGraphResource(final GraphTraversalSource g) {
        this.g = g;
    }

    @POST
    public Response createContainer(Container container) {
        // does this container already exist?
        if (g.V().has("Container", "containerId", container.getContainerId()).hasNext()) {
            return Response.status(Response.Status.CONFLICT).build();
        }
        g.addV("Container")
                .property("containerId", container.getContainerId())
                .property("application", container.getApplication()).next();
        return Response.ok().build();
    }

    @POST
    @Path("{id}/connections/{neighborId}")
    public Response connectContainers(@PathParam("id") int containerId, @PathParam("neighborId") int neighborId) {
        g.V().has("Container", "containerId", containerId).as("a")
                .has("Container", "containerId", neighborId).addE("connectsTo").from("a").iterate();
        return Response.ok().build();
    }

    @GET
    public List<Map<String, Object>> getContainers() {
        return g.V().hasLabel("Container").valueMap().toList();
    }

    @GET
    @Path("{id}")
    public Map<String, Object> getContainer(@PathParam("id") int containerId) {
        return g.V().has("Container", "containerId", containerId).valueMap().next();
    }


    @GET
    @Path("{id}/connectedTo")
    public List<Map<String, Object>> connectedTo(@PathParam("id") int containerId, @DefaultValue("1") @QueryParam("hops")  int hops) {
        return g.V().has("Container", "containerId", containerId)
                .repeat(both("connectsTo")).times(hops).dedup().valueMap().toList();
    }

    @GET
    @Path("{id}/upstream")
    public List<Map<String, Object>> getUpstream(@PathParam("id") int containerId) {
        return g.V().has("Container", "containerId", containerId)
                .repeat(out("connectsTo")).emit().dedup().valueMap().toList();
    }

    @GET
    @Path("{id}/downstream")
    public List<Map<String, Object>> getDownstream(@PathParam("id") int containerId) {
        return g.V().has("Container", "containerId", containerId)
                .repeat(in("connectsTo")).emit().dedup().valueMap().toList();
    }

    @GET
    @Path("/dependencyStats")
    public List<Map<String, Object>> getDependencyStats() {
        return g.V().hasLabel("Container")
                .project("containerId", "dependencies")
                    .by("containerId")
                    .by(repeat(out("connectsTo")).emit().dedup().count())
                        .order().by(select("dependencies"), decr).toList();
    }
}
