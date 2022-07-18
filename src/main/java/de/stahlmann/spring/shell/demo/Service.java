package de.stahlmann.spring.shell.demo;

import java.util.Collection;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Service {
	private String serviceName;
	private Collection<String> arguments;
	private boolean backup;

	public boolean backup() {
		return backup;
	}
}
