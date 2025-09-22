CREATE TABLE IF NOT EXISTS electricity_price_hourly (
  ts_utc     TIMESTAMP WITHOUT TIME ZONE PRIMARY KEY,
  local_day  DATE GENERATED ALWAYS AS (
                (((ts_utc AT TIME ZONE 'UTC') AT TIME ZONE '${tz}')::date)
             ) STORED,
  nps_eesti  NUMERIC(10,4) NOT NULL
);

CREATE INDEX IF NOT EXISTS ix_eph_ts_utc    ON electricity_price_hourly(ts_utc);
CREATE INDEX IF NOT EXISTS ix_eph_local_day ON electricity_price_hourly(local_day);
 