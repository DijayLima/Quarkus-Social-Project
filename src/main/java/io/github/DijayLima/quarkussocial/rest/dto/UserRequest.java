package io.github.DijayLima.quarkussocial.rest.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserRequest {

    @NotBlank(message = "Name is Required")
    private String name;
    @NotNull(message = "Age is Required")
    private Integer age;

    public String getName() {
        return name;
    }
}
