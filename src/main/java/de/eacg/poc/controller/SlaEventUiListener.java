package de.eacg.poc.controller;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.ws.rs.core.MediaType;
import java.io.IOException;


@Component
public class SlaEventUiListener {

    private static final Logger logger = LoggerFactory.getLogger(SlaEventUiListener.class);

    private SseBroadcaster sseBroadcaster = new SseBroadcaster() {
        @Override
        public void onException(ChunkedOutput<OutboundEvent> chunkedOutput, Exception exception) {
//            try {
                logger.error("Failed to handle message, closing and removing ChunkedOutput");
//                chunkedOutput.close();
//                this.remove(chunkedOutput);
//            } catch (IOException e) {
//                logger.error("Failed to cleanup SSE", e);
//            }
        }
    };


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
}
