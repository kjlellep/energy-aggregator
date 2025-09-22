CREATE TABLE IF NOT EXISTS weather_daily (
  day date PRIMARY KEY,
  avg_temp_c double precision NOT NULL,
  computed_at timestamp NOT NULL DEFAULT now()
);
