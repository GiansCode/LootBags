package io.alerium.lootbags;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

import pw.valaria.requirementsprocessor.RequirementsUtil;

public class RequirementsPredicate implements Predicate<CommandSender> {

    private final ConfigurationSection requirements;

    RequirementsPredicate(ConfigurationSection requirements) {

        if (requirements != null && requirements.get("requirements") == null) {
            this.requirements = new YamlConfiguration();
            this.requirements.set("requirements", requirements);
        } else {
            this.requirements = requirements;
        }
    }


    @Override
    public boolean test(CommandSender commandSender) {
        if (commandSender instanceof Player && requirements != null) {
            return RequirementsUtil.handle((Player) commandSender, requirements);
        }
        return true;
    }
}
