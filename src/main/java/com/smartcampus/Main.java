package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus");

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println("==============================================");
        System.out.println("  SmartCampus API Started");
        System.out.println("  Available at: " + BASE_URI);
        System.out.println("==============================================");
        System.out.println("Press ENTER to stop the server...");
        System.in.read();
        server.shutdownNow();
        System.out.println("Server stopped.");
    }
}
