package com.example.test_management_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CreateTestRunRequestDto {

    private String environment;

    private String tags;

}
