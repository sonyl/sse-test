package de.eacg.poc.controller;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@Path("/events")
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);

    @Autowired
    private SlaEventUiListener slaEventUiListener;

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Data getInfo(@Context HttpServletRequest info) {
        Data d = new Data();
        d.clientName = info.getRemoteHost();
        d.date = new Date();
        return d;
    }

    @GET
    @Path("/forever")
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getContinousServerSentEvents() {
        final EventOutput eventOutput = new EventOutput();

        new Thread(() -> {
            try {
                int i = 0;
                while(true) {
                    TimeUnit.SECONDS.sleep(1);
                    final OutboundEvent.Builder eventBuilder
                            = new OutboundEvent.Builder();
                    eventBuilder.name("forever" +  (i % 2));
                    eventBuilder.data(String.class,
                            "Hello world " + (++i) + "!");
                    final OutboundEvent event = eventBuilder.build();
                    eventOutput.write(event);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(
                        "Error when writing the event.", e);
            } finally {
                try {
                    eventOutput.close();
                } catch (IOException ioClose) {
                    throw new RuntimeException(
                            "Error when closing the event output.", ioClose);
                }
            }
        }).start();
        return eventOutput;
    }

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput getServerSentEvents() {
        logger.debug("new Eventoutput registered");
        return slaEventUiListener.getEventOutput();
    }

    @POST
    public Response postMessage(String messsage) {
        logger.debug("new message received: {}", messsage);
        slaEventUiListener.postMessage(messsage);
        String output = "POST: " + messsage;
        return Response.status(Response.Status.OK).entity(output).build();
    }
}

