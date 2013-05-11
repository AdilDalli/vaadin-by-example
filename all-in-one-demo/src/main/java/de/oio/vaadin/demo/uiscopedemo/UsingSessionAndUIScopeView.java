package de.oio.vaadin.demo.uiscopedemo;

import org.roklib.webapps.uridispatching.AbstractURIActionHandler;
import org.vaadin.appbase.service.templating.ITemplatingService;
import org.vaadin.appbase.session.SessionContext;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.oio.vaadin.DemoUI;
import de.oio.vaadin.views.CustomLayoutView;

public class UsingSessionAndUIScopeView extends CustomLayoutView {
	/**
	 * Textfield for changing the UI-scoped variable.
	 */
	private TextField uiScopedValueTF;

	/**
	 * Textfield for changing the session-scoped variable.
	 */
	private TextField sessionScopedValueTF;
	/**
	 * Property that contains the HTML code for the table which gives an
	 * overview of all UI-scoped variables that are currently active for the
	 * current session. For the sake of simplicity, we're putting this data in a
	 * simple HTML table.
	 */
	private Property<String> overviewTableProperty;

	public UsingSessionAndUIScopeView(ITemplatingService templatingService,
			SessionContext context) {
		super(templatingService, context, "demos/UsingSessionAndUIScope");
		// create the data property for the overview table
		overviewTableProperty = new ObjectProperty<String>("");
		// update the contents of the overview table
		refreshOverviewTable();
	}

	@Override
	public void buildLayout() {
		super.buildLayout();
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);

		AbstractURIActionHandler demoHandler = DemoUI.getCurrent()
				.getUriActionHandlerProvider().getDemoHandlersMap()
				.get(UsingSessionAndUIScopeDemo.DEMO_NAME);

		// we add a label that contains a link to the demo application itself.
		// When this link is opened in a new browser window or tab, a new UI
		// object for this window will be created by the framework. All field
		// variables of this class live in this new UI object and are thus
		// multiplied in memory for every new UI object.
		layout.addComponent(new Label(
				"Open this application in a new browser window/tab <a href=\"#!"
						+ demoHandler.getParameterizedActionURI(true)
						+ "\" target=\"_blank\" style=\"color: red;\">with this link</a> and"
						+ " refresh the browser contents after having edited the scoped variables.",
				ContentMode.HTML));

		DemoUI.getCurrentUIScopedVariable().addValueChangeListener(
				new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						refreshOverviewTable();
					}
				});

		// Print the ID of the current UI. This ID is automatically assigned by
		// Vaadin to new UI objects.
		layout.addComponent(new Label("Current UI's ID: "
				+ String.valueOf(UI.getCurrent().getUIId())));

		layout.addComponent(createFormLayout());
		layout.addComponent(new Label(overviewTableProperty, ContentMode.HTML));

		// Add a refresh button that will update the overview table with the
		// most current values from each currently active UI object.
		Button refreshBtn = new Button("Refresh");
		refreshBtn.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				refreshOverviewTable();
			}
		});
		layout.addComponent(refreshBtn);
		getLayout().addComponent(layout, "mainPanel");
	}

	/**
	 * Updates the contents of the overview table. This table displays the
	 * contents of each UI-scoped variable that is currently active in the
	 * session. Using {@link VaadinSession#getUIs()} we're getting a handle on
	 * each {@link UI} object in the current session. We iterate over all UI
	 * objects and print the UI-scoped variable for each individual UI object in
	 * the table. Each time a new UI is opened, a new row will be added to the
	 * table.
	 */
	private void refreshOverviewTable() {
		StringBuilder buf = new StringBuilder();
		buf.append("<h2>Overview of all UI scoped variables in the current session</h2>");

		// add the number of currently active UI objects
		buf.append("There are currently ")
				.append(VaadinSession.getCurrent().getUIs().size())
				.append(" UI objects active for this session (Session ID <em>");
		buf.append(VaadinSession.getCurrent().getSession().getId()).append(
				"</em>).");

		// add a table showing the UI-scoped variable values for each currently
		// active UI object
		buf.append("<table>");

		for (UI ui : VaadinSession.getCurrent().getUIs()) {
			buf.append("<tr><td>");
			buf.append("UI ID ").append(ui.getUIId()).append(": ");
			buf.append("</td><td>");
			// cast the UI object to our UI subclass and get the UI-scoped
			// variable with the non-static method getUIScopedVariable(). We
			// can't use the static version here as this will always return the
			// variable for the current UI.
			buf.append(((DemoUI) ui).getUIScopedVariable().getValue());
			buf.append("</td></tr>");
		}

		buf.append("</table>");

		// update the lable with the new table contents
		overviewTableProperty.setValue(buf.toString());
	}

	/**
	 * Creates the form layout that contains the two textfields to edit the
	 * session and UI-scoped variables.
	 */
	private FormLayout createFormLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.setCaption("Edit session/UI scoped variables:");
		uiScopedValueTF = new TextField("UI-scoped value:",
				DemoUI.getCurrentUIScopedVariable());
		sessionScopedValueTF = new TextField("Session-scoped value:",
				DemoUI.getSessionScopedVariable());

		uiScopedValueTF.setImmediate(true);
		sessionScopedValueTF.setImmediate(true);

		formLayout.addComponent(uiScopedValueTF);
		formLayout.addComponent(sessionScopedValueTF);

		return formLayout;
	}
}
