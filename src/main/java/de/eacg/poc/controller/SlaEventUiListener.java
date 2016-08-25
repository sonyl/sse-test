package de.eacg.poc.controller;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.server.BroadcasterListener;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.MediaType;
import java.io.IOException;


@Component
public class SlaEventUiListener {

    private static final Logger logger = LoggerFactory.getLogger(SlaEventUiListener.class);

    private SseBroadcaster sseBroadcaster = new SseBroadcaster();


    public void postMessage(String message) {
        try {
            final OutboundEvent event = new OutboundEvent.Builder()
                    .name("broadcast")
                    .data(message)
                    .mediaType(MediaType.TEXT_PLAIN_TYPE)
                    .build();
            sseBroadcaster.broadcast(event);
            logger.debug("Event broadcasted: {}", message);

        } catch (Exception e) {
            logger.error("Failed to handle message", e);
        }
    }


    public EventOutput getEventOutput() {
        final EventOutput eventOutput = new EventOutput();
        this.sseBroadcaster.add(eventOutput);
        return eventOutput;
    }

    @PreDestroy
    public void destroy() throws IOException {
        sseBroadcaster.closeAll();
        logger.debug("SlaEventUiListener DESTROYED");
    }


    @Scheduled(fixedDelay=60000)
    public void connectionReaper() {
        logger.debug("connectionReaper called, cleanup SSE connections");
        try {
            final OutboundEvent event = new OutboundEvent.Builder()
                    .name("cleanup")
                    .comment("")
                    .build();
            final int[] statistics = new int[] {0, 0};
            final BroadcasterListener<OutboundEvent> listener = new BroadcasterListener<OutboundEvent>() {
                @Override
                public void onClose(ChunkedOutput<OutboundEvent> chunkedOutput) {
                    statistics[0]++;
                }
                @Override
                public void onException(ChunkedOutput<OutboundEvent> chunkedOutput, Exception exception) {
                    statistics[1]++;
                }

            };
            sseBroadcaster.add(listener);
            sseBroadcaster.broadcast(event);
            sseBroadcaster.remove(listener);
            logger.debug("SSE connection cleanup finished: connections closed: {}, exceptions {}", statistics[0], statistics[1]);

        } catch (Exception e) {
            logger.error("Failed to broadcast event", e);
        }
    }
}
