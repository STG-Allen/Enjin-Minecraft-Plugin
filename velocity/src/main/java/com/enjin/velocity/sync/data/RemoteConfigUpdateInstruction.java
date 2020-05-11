package com.enjin.velocity.sync.data;

import com.enjin.core.Enjin;

import java.util.Map;


//Could potentially abstract this to common code...
public class RemoteConfigUpdateInstruction {
    public static void handle(Map<String, Object> updates) {
        Enjin.getPlugin().getInstructionHandler().configUpdated(updates);
    }
}
