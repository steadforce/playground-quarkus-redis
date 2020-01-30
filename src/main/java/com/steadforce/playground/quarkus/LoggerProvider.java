package com.steadforce.playground.quarkus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Showcase for CDI Producer to make slf4j logger injectable.
 * 
 * @author steadforce gmbh
 *
 */
@ApplicationScoped
public class LoggerProvider {
    @Produces
    public Logger getLogger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
    }
}
