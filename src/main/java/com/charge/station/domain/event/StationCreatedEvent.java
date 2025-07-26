package com.charge.station.domain.event;

import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.model.station.StationId;
import lombok.Getter;

import java.time.Instant;

/**
 * 充电站创建事件
 * 
 * 当新的充电站被创建时发布此事件。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
public class StationCreatedEvent extends DomainEvent {
    
    public static final String EVENT_TYPE = "station.created";
    
    private final StationId stationId;
    private final String stationName;
    private final Location location;
    private final String operatorId;

    public StationCreatedEvent(StationId stationId, String stationName, Location location, 
                              String operatorId, Instant occurredOn) {
        super(EVENT_TYPE, stationId.value(), occurredOn);
        this.stationId = stationId;
        this.stationName = stationName;
        this.location = location;
        this.operatorId = operatorId;
    }

    @Override
    public String toString() {
        return "StationCreatedEvent{" +
                "stationId=" + stationId +
                ", stationName='" + stationName + '\'' +
                ", location=" + location +
                ", operatorId='" + operatorId + '\'' +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}
