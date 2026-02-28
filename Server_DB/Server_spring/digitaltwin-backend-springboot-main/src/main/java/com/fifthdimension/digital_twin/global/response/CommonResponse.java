package com.fifthdimension.digital_twin.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {

    private Integer statusCode;
    private T data;
    private String message;

    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(HttpStatus.OK.value(), data, message);
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(HttpStatus.OK.value(), data, null);
    }

    public static <T> CommonResponse<T> success(String message) {
        return new CommonResponse<>(HttpStatus.OK.value(), null, message);
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(HttpStatus.OK.value(), null, null);
    }
}