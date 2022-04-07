package com.db.migration.entity;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MigrationDto {
    @NotNull
    @JsonProperty("tableName")
    private String tableName;

    @NotNull
    @JsonProperty("alterStatement")
    private String alterStatement;

    @JsonProperty("perconaOptions")
    private String[] perconaOptions;
}
