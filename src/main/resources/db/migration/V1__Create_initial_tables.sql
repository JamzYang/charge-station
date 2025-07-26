-- 创建PostGIS扩展
CREATE EXTENSION IF NOT EXISTS postgis;

-- 充电站表
CREATE TABLE stations (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(200) NOT NULL,
    description TEXT,
    location GEOMETRY(POINT, 4326) NOT NULL,
    operator_id VARCHAR(32) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- 充电桩表
CREATE TABLE charge_points (
    id VARCHAR(32) PRIMARY KEY,
    station_id VARCHAR(32) NOT NULL,
    name VARCHAR(100) NOT NULL,
    model VARCHAR(50),
    vendor VARCHAR(50),
    serial_number VARCHAR(100),
    firmware_version VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'UNAVAILABLE',
    last_heartbeat TIMESTAMP WITH TIME ZONE,
    power_output DECIMAL(10,2),
    connector_count INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_charge_points_station FOREIGN KEY (station_id) REFERENCES stations(id)
);

-- 充电枪表
CREATE TABLE connectors (
    id BIGSERIAL PRIMARY KEY,
    charge_point_id VARCHAR(32) NOT NULL,
    connector_id INTEGER NOT NULL,
    connector_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNAVAILABLE',
    max_power DECIMAL(10,2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_connectors_charge_point FOREIGN KEY (charge_point_id) REFERENCES charge_points(id),
    CONSTRAINT uk_connectors_charge_point_connector UNIQUE (charge_point_id, connector_id)
);

-- 发件箱事件表
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 创建索引
CREATE UNIQUE INDEX idx_stations_name ON stations(name);
CREATE INDEX idx_stations_location ON stations USING GIST(location);
CREATE INDEX idx_stations_operator ON stations(operator_id);
CREATE INDEX idx_stations_status ON stations(status);

CREATE INDEX idx_charge_points_station ON charge_points(station_id);
CREATE INDEX idx_charge_points_status ON charge_points(status);
CREATE INDEX idx_charge_points_serial ON charge_points(serial_number);

CREATE INDEX idx_connectors_charge_point ON connectors(charge_point_id);
CREATE INDEX idx_connectors_status ON connectors(status);

CREATE INDEX idx_outbox_events_status_created_at ON outbox_events(status, created_at);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events(aggregate_type, aggregate_id);

-- 添加注释
COMMENT ON TABLE stations IS '充电站表';
COMMENT ON TABLE charge_points IS '充电桩表';
COMMENT ON TABLE connectors IS '充电枪表';
COMMENT ON TABLE outbox_events IS '发件箱事件表，用于事务性事件发布';

COMMENT ON COLUMN stations.location IS '地理位置，使用PostGIS POINT类型';
COMMENT ON COLUMN stations.status IS '充电站状态：ACTIVE, INACTIVE, MAINTENANCE';
COMMENT ON COLUMN charge_points.status IS '充电桩状态：AVAILABLE, PREPARING, CHARGING, SUSPENDED_EVSE, SUSPENDED_EV, FINISHING, RESERVED, UNAVAILABLE, FAULTED, OFFLINE';
COMMENT ON COLUMN connectors.status IS '充电枪状态：AVAILABLE, PREPARING, CHARGING, SUSPENDED_EVSE, SUSPENDED_EV, FINISHING, RESERVED, UNAVAILABLE, FAULTED';
COMMENT ON COLUMN outbox_events.status IS '事件状态：PENDING, PUBLISHED, FAILED';
