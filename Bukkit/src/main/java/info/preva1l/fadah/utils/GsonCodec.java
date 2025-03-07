package info.preva1l.fadah.utils;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 25/02/2025
 *
 * @author Preva1l
 */
public class GsonCodec extends BaseCodec {
    private Gson gson;
    private final Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    public GsonCodec(Gson gson) {
        this.gson = gson;
    }

    private final Encoder encoder = in -> {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try (ByteBufOutputStream os = new ByteBufOutputStream(out)) {
            os.writeUTF(gson.toJson(in));
            os.writeUTF(in.getClass().getName());
            return os.buffer();
        } catch (IOException e) {
            out.release();
            throw e;
        } catch (Exception e) {
            out.release();
            throw new IOException(e);
        }
    };

    private final Decoder<Object> decoder = (buf, state) -> {
        try (ByteBufInputStream is = new ByteBufInputStream(buf)) {
            String string = is.readUTF();
            String type = is.readUTF();
            return gson.fromJson(string, getClassFromType(type));
        }
    };

    public Class<?> getClassFromType(String name) {
        Class<?> clazz = classMap.get(name);
        if (clazz == null) {
            try {
                clazz = Class.forName(name);
                classMap.put(name, clazz);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find class named " + name, e);
            }
        }
        return clazz;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (gson.getClass().getClassLoader() != null) {
            return gson.getClass().getClassLoader();
        }
        return super.getClassLoader();
    }
}