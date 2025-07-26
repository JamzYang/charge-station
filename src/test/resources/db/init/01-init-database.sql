-- 初始化测试数据库脚本
-- 在 PostgreSQL 容器启动时自动执行

-- 确保 PostGIS 扩展已启用
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 创建测试用户（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'test') THEN
        CREATE USER test WITH PASSWORD 'test';
    END IF;
END
$$;

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE station_test TO test;
GRANT ALL ON SCHEMA public TO test;

-- 设置默认权限
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO test;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO test;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO test;

-- 创建测试数据清理函数
-- 注意：在表创建之前，这个函数可能会失败，这是正常的
CREATE OR REPLACE FUNCTION clean_test_data()
RETURNS void AS $$
BEGIN
    -- 清理所有测试数据，保留表结构
    -- 使用动态SQL避免表不存在时的错误
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'outbox_events') THEN
        TRUNCATE TABLE outbox_events CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'charge_points') THEN
        TRUNCATE TABLE charge_points CASCADE;
    END IF;

    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'stations') THEN
        TRUNCATE TABLE stations CASCADE;
    END IF;

    -- 重置序列（如果存在）
    IF EXISTS (SELECT FROM information_schema.sequences WHERE sequence_name = 'outbox_events_id_seq') THEN
        PERFORM setval('outbox_events_id_seq', 1, false);
    END IF;
END;
$$ LANGUAGE plpgsql;
