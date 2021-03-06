package com.enjin.rpc.mappings.mappings.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Instruction {
    @Getter
    private InstructionCode code;
    @Getter
    private Object          data;
}
