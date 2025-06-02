package me.mortaldev.jbcreditshop.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.utils.ItemStackHelper;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer extends JsonSerializer<ItemStack> {
  @Override
  public void serialize(
      ItemStack itemStack, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (itemStack == null) {
      jsonGenerator.writeNull();
      Main.log("Writing null ItemStack.");
    } else {
      String serialized = ItemStackHelper.serialize(itemStack);
      jsonGenerator.writeString(serialized);
    }
  }
}
