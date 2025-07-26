package com.charge.station.domain.event;

import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.station.StationId;
import lombok.Getter;

import java.time.Instant;

/**
 * 充电桩创建事件
 * 
 * 当新的充电桩被创建时发布此事件。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
public class ChargePointCreatedEvent extends DomainEvent {
    
    public static final String EVENT_TYPE = "chargepoint.created";
    
    private final ChargePointId chargePointId;
    private final StationId stationId;
    private final String chargePointName;
    private final String serialNumber;

    public ChargePointCreatedEvent(ChargePointId chargePointId, StationId stationId, 
                                  String chargePointName, String serialNumber, Instant occurredOn) {
        super(EVENT_TYPE, chargePointId.value(), occurredOn);
        this.chargePointId = chargePointId;
        this.stationId = stationId;
        this.chargePointName = chargePointName;
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "ChargePointCreatedEvent{" +
                "chargePointId=" + chargePointId +
                ", stationId=" + stationId +
                ", chargePointName='" + chargePointName + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}
