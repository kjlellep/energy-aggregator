package ee.kjlellep.energyaggregator.prices.app;

import ee.kjlellep.energyaggregator.prices.config.PriceImportProperties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PriceImportService {

    private final JdbcTemplate jdbc;
    private final ZoneId zoneId;
    private final DateTimeFormatter dateTimeFormatter;
    private final int batchSize;

    public PriceImportService(JdbcTemplate jdbc, PriceImportProperties props) {
        this.jdbc = jdbc;
        this.zoneId = ZoneId.of(props.timezone());
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(props.datetimePattern());
        this.batchSize = props.batchSize();
    }

    public record Result(int inserted, int updated, int unchanged, int skipped) {}

    @Transactional
    public Result upsertCsv(InputStream csvStream) throws java.io.IOException {
        Map<LocalDateTime, BigDecimal> parsed = new LinkedHashMap<>(8192);
        int skipped = 0;

        try (
            Reader reader = new InputStreamReader(csvStream, StandardCharsets.UTF_8);
            CSVParser parser = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setQuote('"')
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);
        ) {
            Map<String, Integer> headers = parser.getHeaderMap();
            Map<String, String> normToOriginal = new HashMap<>();
            for (String h : headers.keySet()) normToOriginal.put(norm(h), h);

            String colEpoch = findHeader(
                normToOriginal,
                "ajatempel (utc)", "ajatempel", "timestamp (utc)", "epoch"
            );
            String colLocalDt = findHeader(
                normToOriginal,
                "kuupaev (eesti aeg)", "kuupäev (eesti aeg)", "kuupaev", "kuupäev"
            );
            String colPrice = findHeader(
                normToOriginal,
                "nps eesti", "nps estonia"
            );

            if (colPrice == null || (colEpoch == null && colLocalDt == null)) {
                throw new IllegalArgumentException(
                    "CSV must contain 'NPS Eesti' and either 'Ajatempel (UTC)' or 'Kuupäev (Eesti aeg)'."
                );
            }

            for (CSVRecord r : parser) {
                try {
                    LocalDateTime tsUtc = extractUtc(r, colEpoch, colLocalDt);
                    String raw = r.get(colPrice);
                    if (raw == null || raw.isBlank()) { skipped++; continue; }
                    BigDecimal price = new BigDecimal(raw.replace(',', '.'));
                    parsed.put(tsUtc, price);
                } catch (Exception e) {
                    skipped++;
                }
            }
        }

        if (parsed.isEmpty()) return new Result(0, 0, 0, skipped);

        int insertedCount = 0;
        int updatedCount = 0;
        int unchangedCount = 0;

        List<Map.Entry<LocalDateTime, BigDecimal>> rows = new ArrayList<>(parsed.entrySet());
        for (int i = 0; i < rows.size(); i += this.batchSize) {
            int end = Math.min(i + this.batchSize, rows.size());
            var batch = rows.subList(i, end);

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO electricity_price_hourly (ts_utc, nps_eesti) VALUES ");
            for (int j = 0; j < batch.size(); j++) {
                if (j > 0) sql.append(',');
                sql.append("(?, ?)");
            }

            sql.append(" ON CONFLICT (ts_utc) DO UPDATE SET nps_eesti = EXCLUDED.nps_eesti")
                .append(" WHERE electricity_price_hourly.nps_eesti IS DISTINCT FROM EXCLUDED.nps_eesti")
                .append(" RETURNING (xmax = 0) AS inserted;");

            Object[] params = new Object[batch.size() * 2];
            int p = 0;
            for (var k : batch) {
                params[p++] = Timestamp.valueOf(k.getKey());
                params[p++] = k.getValue();
            }

            var rs = this.jdbc.queryForList(sql.toString(), params);
            int batchInserted = 0, batchUpdated = 0;
            for (var row : rs) {
                boolean inserted = Boolean.TRUE.equals(row.get("inserted"));
                if (inserted) batchInserted++; else batchUpdated++;
            }
            insertedCount += batchInserted;
            updatedCount += batchUpdated;
            unchangedCount += (batch.size() - batchInserted - batchUpdated);
        }

        return new Result(insertedCount, updatedCount, unchangedCount, skipped);
    }

    private LocalDateTime extractUtc(CSVRecord r, String colEpoch, String colLocalDt) {
        if (colEpoch != null) {
            String s = r.get(colEpoch).trim().replace("\"", "");
            if (!s.isEmpty()) {
                long epochSec = Long.parseLong(s);
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSec), ZoneOffset.UTC);
            }
        }
        String dt = r.get(colLocalDt).trim();
        LocalDateTime local = LocalDateTime.parse(dt, this.dateTimeFormatter);
        return local.atZone(this.zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private static String findHeader(Map<String, String> normToOriginal, String... candidates) {
        for (String c : candidates) {
            String k = norm(c);
            if (normToOriginal.containsKey(k)) return normToOriginal.get(k);
        }
        return null;
    }

    private static String norm(String s) {
        String noDiacritic = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noDiacritic.toLowerCase(Locale.ROOT).trim();
    }
}
