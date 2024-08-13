package com.demo.myshop.core;
//package com.demo.myshop.core.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ApiUtils {

    // 성공 응답을 생성하는 메서드
    public static <T> ApiResult<T> success(T message) {
        return new ApiResult<>(message, null);
    }

    // 오류 응답을 생성하는 메서드
    public static <T> ApiResult<T> error(String errorMessage) {
        return new ApiResult<>(null, errorMessage);
    }

    // 통합된 응답 데이터 클래스
    @AllArgsConstructor
    @Getter
    public static class ApiResult<T> {
        private final T message; // 성공 시 반환할 데이터
        private final String error; // 오류 메시지 (없을 경우 null)

        @Override
        public String toString() {
            return "ApiResult{" +
                    "message=" + message +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}