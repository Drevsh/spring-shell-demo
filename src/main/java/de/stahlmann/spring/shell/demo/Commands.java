package de.stahlmann.spring.shell.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.shell.component.MultiItemSelector;
import org.springframework.shell.component.MultiItemSelector.MultiItemSelectorContext;
import org.springframework.shell.component.PathInput;
import org.springframework.shell.component.PathInput.PathInputContext;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.SingleItemSelector.SingleItemSelectorContext;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class Commands extends AbstractShellComponent {

	private final Map<String, Service> services = new HashMap<>();

	@ShellMethod(key = "parse", value = "Parse service definitions", group = "install")
	public void parse() {
		// just a demo, i.e. return demo data here
		// in reals use case we are parsing files found on the file system
		services.put("service_a", Service.builder()
				.serviceName("service_a")
				.arguments(List.of("logging.level=DEBUG", "profile=int"))
				.backup(true)
				.build());
		services.put("service_b", Service.builder()
				.serviceName("service_b")
				.arguments(List.of("logging.level=TRACE", "profile=int"))
				.backup(false)
				.build());
	}

	@ShellMethod(key = "configure", value = "Configure service definitions", group = "install")
	public void configure() {
		var service = selectService();
		selectBackupLocation(service);
		configureArgument(service);
	}

	@ShellMethod(key = "print", value = "Print service definitions", group = "install")
	public String print() {
		return services.values()
				.stream()
				.map(Service::toString)
				.collect(Collectors.joining(";"));
	}

	private void configureArgument(Service service) {
		var items = service.getArguments()
				.stream()
				.map(arg -> SelectorItem.of(arg, arg))
				.toList();
		var component = new MultiItemSelector<>(getTerminal(), items, "Select which arguments to keep:", null);
		component.setResourceLoader(getResourceLoader());
		component.setTemplateExecutor(getTemplateExecutor());
		var context = component.run(MultiItemSelectorContext.empty());

		var result = context.getResultItems()
				.stream()
				.map(SelectorItem::getItem)
				.toList();
		service.setArguments(result);
	}

	private void selectBackupLocation(Service service) {
		if (service.backup()) {
			var component = new PathInput(getTerminal(), "Enter backup path");
			component.setResourceLoader(getResourceLoader());
			component.setTemplateExecutor(getTemplateExecutor());
			var context = component.run(PathInputContext.empty());

			// do something with the path
			var backupPath = context.getResultValue();
		}
	}

	private Service selectService() {
		var selectionItems = services.values()
				.stream()
				.map(Service::getServiceName)
				.map(name -> SelectorItem.of(name, name))
				.toList();
		var component = new SingleItemSelector<>(getTerminal(), selectionItems, "Select service to edit:", null);
		component.setResourceLoader(getResourceLoader());
		component.setTemplateExecutor(getTemplateExecutor());
		var context = component.run(SingleItemSelectorContext.empty());
		var serviceName = context.getResultItem()
				.flatMap(si -> Optional.ofNullable(si.getItem()))
				.get();

		return services.get(serviceName);
	}

}
