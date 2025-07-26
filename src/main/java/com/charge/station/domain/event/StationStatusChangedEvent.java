package com.charge.station.domain.event;

import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.model.station.StationStatus;
import lombok.Getter;

import java.time.Instant;

/**
 * 充电站状态变更事件
 * 
 * 当充电站状态发生变更时发布此事件。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
public class StationStatusChangedEvent extends DomainEvent {
    
    public static final String EVENT_TYPE = "station.status_changed";
    
    private final StationId stationId;
    private final StationStatus oldStatus;
    private final StationStatus newStatus;

    public StationStatusChangedEvent(StationId stationId, StationStatus oldStatus, 
                                   StationStatus newStatus, Instant occurredOn) {
        super(EVENT_TYPE, stationId.value(), occurredOn);
        this.stationId = stationId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String toString() {
        return "StationStatusChangedEvent{" +
                "stationId=" + stationId +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}
