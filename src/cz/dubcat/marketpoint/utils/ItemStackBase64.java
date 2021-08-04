package cz.dubcat.marketpoint.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ItemStackBase64 {
    public static String toBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public static ItemStack fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            
            return item;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        } catch (IOException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
    
    public static List<String> convertItemStackListToBase64List(List<ItemStack> items){
        List<String> itemsList = new ArrayList<>();
        items.forEach(item -> {
            itemsList.add(toBase64(item));
        });
        
        return itemsList;
    }
    
    public static List<ItemStack> convertBase64ListToItemStackList(List<String> items){
        List<ItemStack> itemsList = new ArrayList<>();
        items.forEach(item -> {
            try {
                itemsList.add(fromBase64(item));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        return itemsList;
    }
}
