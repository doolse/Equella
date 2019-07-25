package com.tle.web.sections.registry;

import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

public class ExtensionExceptionHandlerMatch implements ExceptionHandlerMatch {

  private final Extension extension;
  private final PluginTracker<SectionsExceptionHandler> pluginTracker;

  public ExtensionExceptionHandlerMatch(
      Extension extension, PluginTracker<SectionsExceptionHandler> pluginTracker) {
    this.extension = extension;
    this.pluginTracker = pluginTracker;
  }

  @Override
  public String getClassMatch() {
    Parameter param = extension.getParameter("exceptionClass");
    return param != null ? param.valueAsString() : null;
  }

  @Override
  public SectionsExceptionHandler getHandler() {
    return pluginTracker.getBeanByExtension(extension);
  }
}
