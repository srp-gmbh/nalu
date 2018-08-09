package com.github.mvp4g.nalu.client;

import com.github.mvp4g.nalu.client.filter.IsFilter;
import com.github.mvp4g.nalu.client.internal.ClientLogger;
import com.github.mvp4g.nalu.client.internal.application.ControllerFactory;
import com.github.mvp4g.nalu.client.internal.route.HashResult;
import com.github.mvp4g.nalu.client.internal.route.RouteConfig;
import com.github.mvp4g.nalu.client.internal.route.RouterConfiguration;
import com.github.mvp4g.nalu.client.ui.AbstractComponentController;
import com.github.mvp4g.nalu.client.ui.IsConfirmator;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import elemental2.dom.HashChangeEvent;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Router {

  /* list of added lements */
  private Map<String, HTMLElement> addedElements;

  /* List of the routes of the application */
  private RouterConfiguration                            routerConfiguration;
  /* List of the Selectors of the application */
  private Map<String, String>                            selectors;
  /* List of active components */
  private Map<String, AbstractComponentController<?, ?>> activeComponents;
  /* List of confirm components */
  private Map<String, AbstractComponentController<?, ?>> confirmComponents;
  //* hash of last successful routing */
  private String                                         lastExecutedHash;


  public Router(RouterConfiguration routerConfiguration) {
    super();
    // initialize lists
    this.addedElements = new HashMap<>();
    // save the router configuration reference
    this.routerConfiguration = routerConfiguration;
    // inistantiate lists, etc.
    this.activeComponents = new HashMap<>();
    this.confirmComponents = new HashMap<>();
    // register event handler
    this.registerEventHandler();
  }

  private void registerEventHandler() {
    // the OnHashChange event handling
    DomGlobal.window.onhashchange = e -> {
      // cast event ...
      HashChangeEvent hashChangeEvent = (HashChangeEvent) e;
      // look for a routing ...
      return this.handleRouting(this.getHash(hashChangeEvent.newURL));
    };
  }

  private String handleRouting(String hash) {
    // ok, everything is fine, route!
    // parse hash ...
    HashResult hashResult = this.parse(hash);
    // First we have to check if there is a filter
    // if there are filters ==>  filter the rote
    for (IsFilter filter : this.routerConfiguration.getFilters()) {
      if (!filter.filter(addLeadgindSledge(hashResult.getRoute()),
                         hashResult.getParameterValues()
                                   .toArray(new String[0]))) {
        StringBuilder sb = new StringBuilder();
        sb.append("Router: filter >>")
          .append(filter.getClass()
                        .getCanonicalName())
          .append("<< intercepts routing! New route: >>")
          .append(filter.redirectTo())
          .append("<<");
        if (Arrays.asList(filter.parameters())
                  .size() > 0) {
          sb.append(" with parameters: ");
          Stream.of(filter.parameters())
                .forEach(p -> sb.append(">>")
                                .append(p)
                                .append("<< "));
        }
        ClientLogger.get()
                    .logSimple(sb.toString(),
                               0);
        this.route(filter.redirectTo(),
                          true,
                          filter.parameters());
        return null;
      }
    }
    // search for a matching routing
    List<RouteConfig> routeConfiguraions = this.routerConfiguration.match(hashResult.getRoute());
    // chech weather or not the routing is possible ...
    if (this.confirmRouting(routeConfiguraions)) {
      // call stop for all elements
      this.stopController(routeConfiguraions);
      // routing
      for (RouteConfig routeConfiguraion : routeConfiguraions) {
        AbstractComponentController<?, ?> component = ControllerFactory.get()
                                                                       .controller(routeConfiguraion.getClassName(),
                                                                                   hashResult.getParameterValues()
                                                                                             .toArray(new String[0]));
        if (!Objects.isNull(component)) {
          component.setRouter(this);
          this.addElementToDOM(routeConfiguraion.getSelector(),
                               component);
          component.start();
          this.lastExecutedHash = hash;
        } else {
          DomGlobal.window.alert("Ups ... not found!");
        }
      }
    } else {
      // we don not want to leave!
      DomGlobal.window.history.pushState(null,
                                         null,
                                         "#" + this.lastExecutedHash);
    }
    return null;
  }

  private String getHash(String url) {
    return url.substring(url.indexOf("#") + 1);
  }

  public HashResult parse(String hash) {
    HashResult hashResult = new HashResult();
    String hashValue = hash;
    // extract route first:
    if (hash.startsWith("/")) {
      hashValue = hashValue.substring(1);
    }
    if (hashValue.contains("/")) {
      hashResult.setRoute(hashValue.substring(0,
                                              hashValue.indexOf("/")));
      String parametersFromHash = hashValue.substring(hashValue.indexOf("/") + 1);
      // lets get the parameters!
      List<String> paramsList = Stream.of(parametersFromHash.split("/"))
                                      .collect(Collectors.toList());
      // reset sledge in parms ....
      paramsList.forEach(parm -> hashResult.getParameterValues()
                                           .add(parm.replace(Nalu.NALU_SLEDGE_REPLACEMENT,
                                                             "/")));
    } else {
      hashResult.setRoute(hashValue);
    }
    return hashResult;
  }

  private String addLeadgindSledge(String value) {
    if (value.startsWith("/")) {
      return value;
    }
    return "/" + value;
  }

  private boolean confirmRouting(List<RouteConfig> routeConfiguraions) {
    return routeConfiguraions.stream()
                             .map(config -> this.confirmComponents.get(config.getSelector()))
                             .filter(Objects::nonNull)
                             .map(AbstractComponentController::mayStop)
                             .filter(Objects::nonNull)
                             .allMatch(message -> DomGlobal.window.confirm(message));
  }

  private void stopController(List<RouteConfig> routeConfiguraions) {
    routeConfiguraions.stream()
                      .map(config -> this.activeComponents.get(config.getSelector()))
                      .filter(Objects::nonNull)
                      .forEach(AbstractComponentController::stop);
    routeConfiguraions.stream()
                      .map(config -> this.activeComponents.get(config.getSelector()))
                      .filter(Objects::nonNull)
                      .forEach(c -> this.activeComponents.remove(c));
    routeConfiguraions.stream()
                      .map(config -> this.confirmComponents.get(config.getSelector()))
                      .filter(Objects::nonNull)
                      .forEach(c -> this.confirmComponents.remove(c));
  }

  private void addElementToDOM(String selector,
                               AbstractComponentController<?, ?> controller) {
    Element selectorElement = DomGlobal.document.querySelector("#" + selector);
    if (selectorElement == null) {
      // TODO better message
      DomGlobal.window.alert("Ups ... selector not found!");
    } else {
      // first, remove all childs
      if (this.addedElements.containsKey(selector)) {
        this.addedElements.get(selector)
                          .remove();
        this.addedElements.remove(selector);
      }
      // add controller
      HTMLElement newElement = controller.asElement();
      selectorElement.appendChild(newElement);
      this.addedElements.put(selector,
                             newElement);
      // save to active components
      this.activeComponents.put(selector,
                                controller);
      // save controller
      if (controller instanceof IsConfirmator) {
        this.confirmComponents.put(selector,
                                   controller);
      }
    }
  }

  public void route(String newRoute,
                    String... parms) {
    this.route(newRoute,
               false,
               parms);
  }

  private void route(String newRoute,
                     boolean replaceState,
                     String... parms) {
    StringBuilder sb = new StringBuilder();
    if (newRoute.startsWith("/")) {
      sb.append(newRoute.substring(1));
    } else {
      sb.append(newRoute);
    }
    if (parms != null) {
      Stream.of(parms)
            .filter(Objects::nonNull)
            .forEach(s -> sb.append("/")
                            .append(s.replace("/",
                                              Nalu.NALU_SLEDGE_REPLACEMENT)));
    }
    String newRouteWithParams = sb.toString();
    if (replaceState) {
      DomGlobal.window.history.replaceState(null,
                                            null,
                                            "#" + newRouteWithParams);
    } else {
      DomGlobal.window.history.pushState(null,
                                         null,
                                         "#" + newRouteWithParams);
    }
    this.handleRouting(newRouteWithParams);
  }
}
