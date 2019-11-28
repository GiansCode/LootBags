package io.alerium.lootbags;

import org.bukkit.ChatColor;

/**
 * Copyright © 2016 Jordan Osterberg and Shadow Technical Systems LLC. All rights reserved. Please email jordan.osterberg@shadowsystems.tech for usage rights and other information.
 */
public class ChatUtil {

    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
