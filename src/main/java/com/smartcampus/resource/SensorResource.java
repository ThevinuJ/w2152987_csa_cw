package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors;

        if (type != null && !type.isBlank()) {
            sensors = store.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        } else {
            sensors = new ArrayList<>(store.getSensors().values());
        }

        return Response.ok(sensors).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Sensor id is required and cannot be blank."))
                    .build();
        }

        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody(409, "Sensor with id '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // Validate that the referenced room exists
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + sensor.getRoomId() + "' does not exist. Cannot create sensor.");
        }

        // Save sensor
        store.getSensors().put(sensor.getId(), sensor);

        // Add sensor id to the room's sensorIds list
        Room room = store.getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Sensor with id '" + sensorId + "' not found."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // --- NEWLY ADDED PUT METHOD ---
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        if (!store.getSensors().containsKey(sensorId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Sensor with id '" + sensorId + "' not found."))
                    .build();
        }

        updatedSensor.setId(sensorId);

        // Validate that the new room exists if they are changing rooms
        if (updatedSensor.getRoomId() != null && !store.getRooms().containsKey(updatedSensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with id '" + updatedSensor.getRoomId() + "' does not exist. Cannot update sensor.");
        }

        store.getSensors().put(sensorId, updatedSensor);
        return Response.ok(updatedSensor).build();
    }

    // --- NEWLY ADDED DELETE METHOD ---
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Sensor with id '" + sensorId + "' not found."))
                    .build();
        }

        // Clean up: Remove the sensor from the Room's list so it doesn't leave a ghost ID
        if (sensor.getRoomId() != null && store.getRooms().containsKey(sensor.getRoomId())) {
            Room room = store.getRooms().get(sensor.getRoomId());
            room.getSensorIds().remove(sensorId);
        }

        // Delete the sensor itself
        store.getSensors().remove(sensorId);
        return Response.noContent().build();
    }

    /**
     * Sub-resource locator for sensor readings.
     * Delegates all /sensors/{sensorId}/readings requests to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private Map<String, Object> errorBody(int status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}