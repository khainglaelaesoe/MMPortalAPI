package com.portal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class JournalArticleNotFoundAdvice {
	  @ResponseBody
	  @ExceptionHandler(JournalArticleNotFoundException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	  String employeeNotFoundHandler(JournalArticleNotFoundException ex) {
	    return ex.getMessage();
	  }

}
