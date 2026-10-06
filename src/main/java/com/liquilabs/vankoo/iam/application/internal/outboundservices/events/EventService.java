package com.liquilabs.vankoo.iam.application.internal.outboundservices.events;

public interface EventService {
    void publishEvent(Object event);
}
