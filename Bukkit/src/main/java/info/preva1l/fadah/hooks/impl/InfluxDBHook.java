package info.preva1l.fadah.hooks.impl;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.Hook;
import info.preva1l.fadah.hooks.Reloadable;

import java.time.Instant;
import java.util.logging.Level;

@Reloadable(async = true)
public class InfluxDBHook extends Hook {
    private InfluxDBClient client;
    private WriteApiBlocking writeApi;

    @Override
    public boolean onEnable() {
        Config.Hooks.InfluxDB conf = Config.i().getHooks().getInfluxdb();
        if (!conf.isEnabled()) {
            return false;
        }
        try {
            String url = conf.getUri();
            String token = conf.getToken();
            String org = conf.getOrg();
            String bucket = conf.getBucket();
            this.client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
            this.writeApi = client.getWriteApiBlocking();
            return true;
        } catch (Exception e) {
            Fadah.getConsole().log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    public void log(String message) {
        Point point = Point.measurement("Transaction-Logs")
                .time(Instant.now(), WritePrecision.MS)
                .addField("message", message);
        writeApi.writePoint(point);
    }

    @Override
    public void onDisable() {
        if (client == null) return;
        client.close();
    }
}
