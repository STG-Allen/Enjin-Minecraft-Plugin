package com.enjin.officialplugin.sync.data;

import com.enjin.core.Enjin;

public class AddWhitelistPlayerInstruction {
    public static void handle(String player) {
        Enjin.getPlugin().getInstructionHandler().addToWhitelist(player);
    }
}