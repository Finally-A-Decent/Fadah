package info.preva1l.fadah.data.gson;

import com.google.gson.*;
import info.preva1l.fadah.Fadah;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Base64;

public class BukkitSerializableAdapter implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {
    private final LegacyBukkitSerializableAdapter legacyAdapter;

    public BukkitSerializableAdapter() {
        this.legacyAdapter = new LegacyBukkitSerializableAdapter(Fadah.getInstance());
    }

    @Override
    public ConfigurationSerializable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(json.getAsString()));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            ConfigurationSerializable[] objs = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < objs.length; i++)
                objs[i] = (ConfigurationSerializable) dataInput.readObject();

            return objs[0];
        } catch (Exception pass) {
            return legacyAdapter.deserialize(json, typeOfT, context);
        }
    }

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type typeOfSrc, JsonSerializationContext context) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(1);
            dataOutput.writeObject(src);

            return new JsonPrimitive(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        } catch (Exception pass) {
            throw new RuntimeException(pass);
        }
    }
}