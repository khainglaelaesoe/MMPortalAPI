package com.portal.exception;

public class JournalArticleNotFoundException extends RuntimeException {

	public JournalArticleNotFoundException(Long id) {
	    super("Could not find Journal Article " + id);
	  }
	}
