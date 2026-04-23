package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", "SmartCampus API");
        info.put("version", "1.0");
        info.put("description", "RESTful API for Smart Campus sensor and room management");
        info.put("contact", "smartcampus@university.edu");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        info.put("_links", links);

        return info;
    }
}
