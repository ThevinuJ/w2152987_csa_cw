package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
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

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody(400, "Room id is required and cannot be blank."))
                    .build();
        }

        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody(409, "Room with id '" + room.getId() + "' already exists."))
                    .build();
        }

        store.getRooms().put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Room with id '" + roomId + "' not found."))
                    .build();
        }
        return Response.ok(room).build();
    }

    // --- THE FIX: NEW PUT METHOD ADDED HERE ---
    @PUT
    @Path("/{roomId}")
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        if (!store.getRooms().containsKey(roomId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Room with id '" + roomId + "' not found."))
                    .build();
        }

        updatedRoom.setId(roomId);
        store.getRooms().put(roomId, updatedRoom);
        return Response.ok(updatedRoom).build();
    }
    // ------------------------------------------

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody(404, "Room with id '" + roomId + "' not found."))
                    .build();
        }

        // Check if any sensor is linked to this room
        for (Sensor sensor : store.getSensors().values()) {
            if (roomId.equals(sensor.getRoomId())) {
                throw new RoomNotEmptyException(
                        "Cannot delete room '" + roomId + "' because it still has sensors assigned to it.");
            }
        }

        store.getRooms().remove(roomId);
        return Response.noContent().build();
    }

    private Map<String, Object> errorBody(int status, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}