package com.app.pofolit_be.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

   private final HttpStatus status;
   private final String errorCode;

   public CustomException(final String message, final HttpStatus status, final String errorCode) {
      super(message);
      this.status = status;
      this.errorCode = errorCode;
   }
}
