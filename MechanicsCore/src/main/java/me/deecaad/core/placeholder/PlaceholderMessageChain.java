package me.deecaad.core.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.core.MechanicsCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlaceholderMessageChain {

    private final List<PlaceholderMessage> chain;

    public PlaceholderMessageChain(PlaceholderMessage... chain) {
        this.chain = Arrays.asList(chain);
    }

    public PlaceholderMessageChain(List<PlaceholderMessage> chain) {
        this.chain = chain;
    }

    public List<PlaceholderMessage> getChain() {
        return chain;
    }

    public TagResolver[] getTagResolvers(PlaceholderData data) {
        for (PlaceholderMessage message : chain) {
            // It is possible but unlikely this is actually SLOWER than
            // looping through all HANDLERS since this MAY contain duplicates
            message.fillMap(data);
        }

        // Let other plugins customize the appearance of placeholders
        PlaceholderRequestEvent event = new PlaceholderRequestEvent(data);
        Bukkit.getPluginManager().callEvent(event);

        // Convert the placeholder map into the tag resolver format for the adventure api
        Map<String, String> placeholders = event.placeholders();
        TagResolver[] tagResolvers = new TagResolver[event.placeholders().size()];
        int i = 0;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            tagResolvers[i++] = Placeholder.parsed(entry.getKey(), entry.getValue());
        }

        return tagResolvers;
    }

    public List<Component> replaceAndDeserialize(PlaceholderData data) {
        TagResolver[] tagResolvers = getTagResolvers(data);
        boolean isPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        List<Component> temp = new ArrayList<>(chain.size());
        for (PlaceholderMessage message : chain) {
            String str = message.getTemplate();

            // Handle any PlaceholderAPI placeholders
            if (isPlaceholderAPI) {
                str = PlaceholderAPI.setPlaceholders(data.player(), str);
            }

            temp.add(MechanicsCore.getPlugin().message.deserialize(str, tagResolvers));
        }

        return temp;
    }

    public TextComponent.Builder replaceAndDeserializeAndMerge(PlaceholderData data) {
        TagResolver[] tagResolvers = getTagResolvers(data);
        boolean isPlaceholderAPI = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        TextComponent.Builder builder = Component.text();
        for (PlaceholderMessage message : chain) {
            String str = message.getTemplate();

            // Handle any PlaceholderAPI placeholders
            if (isPlaceholderAPI) {
                str = PlaceholderAPI.setPlaceholders(data.player(), str);
            }

            builder.append(MechanicsCore.getPlugin().message.deserialize(str, tagResolvers));
        }

        return builder;
    }
}
