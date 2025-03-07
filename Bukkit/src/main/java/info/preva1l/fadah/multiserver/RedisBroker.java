package info.preva1l.fadah.multiserver;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.utils.GsonCodec;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.SingleServerConfig;

public final class RedisBroker extends Broker {
    @Getter private static RedissonClient redisson;
    private static String CHANNEL = "NONE";
    private RTopic topic;

    public RedisBroker(@NotNull Fadah plugin) {
        super(plugin);
    }

    @Blocking
    @Override
    public void connect() throws IllegalStateException {
        redisson = initReddison();

        topic = getRedisson().getTopic(CHANNEL);
        topic.addListenerAsync(Message.class, (charSequence, message) -> handle(message));
    }

    @Override
    protected void send(@NotNull Message message) {
        topic.publishAsync(message);
    }

    @Override
    @Blocking
    public void destroy() {
        if (getRedisson() != null) {
            getRedisson().shutdown();
        }
    }

    @NotNull
    private RedissonClient initReddison() {
        Config.Broker conf = Config.i().getBroker();
        final String password = conf.getPassword();
        final String host = conf.getHost();
        final int port = conf.getPort();
        CHANNEL = conf.getChannel();

        org.redisson.config.Config config = new org.redisson.config.Config()
                .setCodec(new GsonCodec(gson));
        SingleServerConfig ssc = config.useSingleServer()
                .setAddress("redis://%s:%s".formatted(host, port));
        if (!password.isEmpty()) ssc.setPassword(password);

        return Redisson.create(config);
    }
}
