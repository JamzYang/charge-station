package com.charge.station.config;

import com.charge.station.domain.model.chargepoint.ChargePoint;
import com.charge.station.domain.model.chargepoint.ChargePointId;
import com.charge.station.domain.model.shared.DeviceStatus;
import com.charge.station.domain.model.station.Station;
import com.charge.station.domain.model.station.StationId;
import com.charge.station.domain.model.station.StationStatus;
import com.charge.station.domain.model.station.Location;
import com.charge.station.domain.repository.ChargePointRepository;
import com.charge.station.domain.repository.StationRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试仓储配置
 *
 * 为测试环境提供内存实现的仓储Bean。
 *
 * @author 架构师团队
 * @version 1.0
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,
    KafkaAutoConfiguration.class
})
public class TestRepositoryConfig {

    @Bean
    @Primary
    public StationRepository testStationRepository() {
        return new InMemoryStationRepository();
    }

    @Bean
    @Primary
    public ChargePointRepository testChargePointRepository() {
        return new InMemoryChargePointRepository();
    }

    /**
     * 内存实现的充电站仓储
     */
    private static class InMemoryStationRepository implements StationRepository {
        private final ConcurrentHashMap<StationId, Station> stations = new ConcurrentHashMap<>();
        private final AtomicLong counter = new AtomicLong(0);

        @Override
        public Station save(Station station) {
            stations.put(station.getStationId(), station);
            return station;
        }

        @Override
        public Optional<Station> findById(StationId stationId) {
            return Optional.ofNullable(stations.get(stationId));
        }

        @Override
        public Optional<Station> findByName(String name) {
            return stations.values().stream()
                .filter(station -> station.getStationInfo().name().equals(name))
                .findFirst();
        }

        @Override
        public boolean existsByName(String name) {
            return findByName(name).isPresent();
        }

        @Override
        public boolean existsByNameAndIdNot(String name, StationId excludeStationId) {
            return stations.values().stream()
                .anyMatch(station -> station.getStationInfo().name().equals(name) 
                    && !station.getStationId().equals(excludeStationId));
        }

        @Override
        public List<Station> findByOperatorId(String operatorId) {
            return stations.values().stream()
                .filter(station -> station.getOperatorId().equals(operatorId))
                .toList();
        }

        @Override
        public List<Station> findByStatus(StationStatus status) {
            return stations.values().stream()
                .filter(station -> station.getStatus().equals(status))
                .toList();
        }

        @Override
        public List<Station> findNearbyStations(Location centerLocation, double radiusMeters) {
            return stations.values().stream()
                .filter(station -> station.isWithinRadius(centerLocation, radiusMeters))
                .toList();
        }

        @Override
        public List<Station> findNearbyActiveStations(Location centerLocation, double radiusMeters) {
            return stations.values().stream()
                .filter(station -> station.getStatus() == StationStatus.ACTIVE)
                .filter(station -> station.isWithinRadius(centerLocation, radiusMeters))
                .toList();
        }

        @Override
        public List<Station> findAll(int page, int size) {
            return stations.values().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
        }

        @Override
        public long count() {
            return stations.size();
        }

        @Override
        public long countByOperatorId(String operatorId) {
            return stations.values().stream()
                .filter(station -> station.getOperatorId().equals(operatorId))
                .count();
        }

        @Override
        public long countByStatus(StationStatus status) {
            return stations.values().stream()
                .filter(station -> station.getStatus().equals(status))
                .count();
        }

        @Override
        public void deleteById(StationId stationId) {
            stations.remove(stationId);
        }

        @Override
        public boolean existsById(StationId stationId) {
            return stations.containsKey(stationId);
        }
    }

    /**
     * 内存实现的充电桩仓储
     */
    private static class InMemoryChargePointRepository implements ChargePointRepository {
        private final ConcurrentHashMap<ChargePointId, ChargePoint> chargePoints = new ConcurrentHashMap<>();

        @Override
        public ChargePoint save(ChargePoint chargePoint) {
            chargePoints.put(chargePoint.getChargePointId(), chargePoint);
            return chargePoint;
        }

        @Override
        public Optional<ChargePoint> findById(ChargePointId chargePointId) {
            return Optional.ofNullable(chargePoints.get(chargePointId));
        }

        @Override
        public Optional<ChargePoint> findBySerialNumber(String serialNumber) {
            return chargePoints.values().stream()
                .filter(cp -> serialNumber.equals(cp.getSerialNumber()))
                .findFirst();
        }

        @Override
        public boolean existsBySerialNumber(String serialNumber) {
            return findBySerialNumber(serialNumber).isPresent();
        }

        @Override
        public boolean existsBySerialNumberAndIdNot(String serialNumber, ChargePointId excludeChargePointId) {
            return chargePoints.values().stream()
                .anyMatch(cp -> serialNumber.equals(cp.getSerialNumber()) 
                    && !cp.getChargePointId().equals(excludeChargePointId));
        }

        @Override
        public List<ChargePoint> findByStationId(StationId stationId) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getStationId().equals(stationId))
                .toList();
        }

        @Override
        public List<ChargePoint> findByStatus(DeviceStatus status) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getStatus().equals(status))
                .toList();
        }

        @Override
        public List<ChargePoint> findByStationIdAndStatus(StationId stationId, DeviceStatus status) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getStationId().equals(stationId) && cp.getStatus().equals(status))
                .toList();
        }

        @Override
        public List<ChargePoint> findAvailableChargePoints() {
            return findByStatus(DeviceStatus.AVAILABLE);
        }

        @Override
        public List<ChargePoint> findAvailableChargePointsByStationId(StationId stationId) {
            return findByStationIdAndStatus(stationId, DeviceStatus.AVAILABLE);
        }

        @Override
        public List<ChargePoint> findOfflineChargePoints(Instant lastHeartbeatBefore) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getLastHeartbeat() == null || cp.getLastHeartbeat().isBefore(lastHeartbeatBefore))
                .toList();
        }

        @Override
        public List<ChargePoint> findAll(int page, int size) {
            return chargePoints.values().stream()
                .skip((long) page * size)
                .limit(size)
                .toList();
        }

        @Override
        public long count() {
            return chargePoints.size();
        }

        @Override
        public long countByStationId(StationId stationId) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getStationId().equals(stationId))
                .count();
        }

        @Override
        public long countByStatus(DeviceStatus status) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getStatus().equals(status))
                .count();
        }

        @Override
        public long countByStationIdAndStatus(StationId stationId, DeviceStatus status) {
            return chargePoints.values().stream()
                .filter(cp -> cp.getStationId().equals(stationId) && cp.getStatus().equals(status))
                .count();
        }

        @Override
        public void deleteById(ChargePointId chargePointId) {
            chargePoints.remove(chargePointId);
        }

        @Override
        public void deleteByStationId(StationId stationId) {
            chargePoints.entrySet().removeIf(entry -> entry.getValue().getStationId().equals(stationId));
        }

        @Override
        public boolean existsById(ChargePointId chargePointId) {
            return chargePoints.containsKey(chargePointId);
        }
    }
}
