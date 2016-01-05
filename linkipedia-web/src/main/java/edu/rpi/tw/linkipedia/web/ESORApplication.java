package edu.rpi.tw.linkipedia.web;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class ESORApplication extends Application {
	public Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(EntitySearchResource.class);
		return s;
	}
}
