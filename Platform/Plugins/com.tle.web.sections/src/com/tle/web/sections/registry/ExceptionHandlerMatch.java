package com.tle.web.sections.registry;

import com.tle.web.sections.errors.SectionsExceptionHandler;

public interface ExceptionHandlerMatch {
  String getClassMatch();

  SectionsExceptionHandler getHandler();
}
