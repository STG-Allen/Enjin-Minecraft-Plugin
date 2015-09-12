package com.enjin.rpc.mappings.mappings.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Status {
    @Getter
    @SerializedName(value = "hasranks")
    private boolean ranksEnabled;
    @Getter
    @SerializedName(value = "pluginversion")
    private String pluginVersion;
    @Getter
    private List<String> worlds;
    @Getter
    private List<String> groups;
    @Getter
    @SerializedName(value = "maxplayers")
    private int maxPlayers;
    @Getter
    private int players;
    @Getter
    @SerializedName(value = "playerlist")
    private List<PlayerInfo> playersList;
    @Getter
    @SerializedName(value = "playergroups")
    private Map<String, PlayerGroupInfo> playerGroups;
    @Getter
    @SerializedName(value = "executed_commands")
    private List<ExecutedCommand> executedCommands;
    @Getter
    private Map<String, Object> stats;
}
