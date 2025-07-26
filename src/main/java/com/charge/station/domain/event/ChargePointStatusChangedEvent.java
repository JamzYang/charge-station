package com.charge.station.domain.event;

import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import lombok.Getter;

import java.time.Instant;

/**
 * 充电桩状态变更事件
 * 
 * 当充电桩状态发生变更时发布此事件。
 * 
 * @author 架构师团队
 * @version 1.0
 */
@Getter
public class ChargePointStatusChangedEvent extends DomainEvent {
    
    public static final String EVENT_TYPE = "chargepoint.status_changed";
    
    private final ChargePointId chargePointId;
    private final DeviceStatus oldStatus;
    private final DeviceStatus newStatus;

    public ChargePointStatusChangedEvent(ChargePointId chargePointId, DeviceStatus oldStatus, 
                                       DeviceStatus newStatus, Instant occurredOn) {
        super(EVENT_TYPE, chargePointId.value(), occurredOn);
        this.chargePointId = chargePointId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String toString() {
        return "ChargePointStatusChangedEvent{" +
                "chargePointId=" + chargePointId +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", eventId=" + getEventId() +
                ", occurredOn=" + getOccurredOn() +
                '}';
    }
}
