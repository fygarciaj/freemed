/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record and Practice Management System
 * Copyright (C) 1999-2009 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.freemedsoftware.gwt.client.screen;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.freemedsoftware.gwt.client.JsonUtil;
import org.freemedsoftware.gwt.client.ScreenInterface;
import org.freemedsoftware.gwt.client.Util;
import org.freemedsoftware.gwt.client.Util.ProgramMode;
import org.freemedsoftware.gwt.client.i18n.AppConstants;
import org.freemedsoftware.gwt.client.widget.CustomDatePicker;
import org.freemedsoftware.gwt.client.widget.CustomTable;
import org.freemedsoftware.gwt.client.widget.PatientTagWidget;
import org.freemedsoftware.gwt.client.widget.PatientWidget;
import org.freemedsoftware.gwt.client.widget.SupportModuleWidget;
import org.freemedsoftware.gwt.client.widget.CustomTable.TableRowClickHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;

public class UtilitiesScreen extends ScreenInterface {

	protected CustomTable utilityTable;

	protected FlexTable utilityParametersTable;

	protected HorizontalPanel utilityActionPanel;

	protected HashMap<Integer, String> utilityParameters = new HashMap<Integer, String>();

	protected String thisUtilityUUID = null;

	protected Label thisUtilityName = new Label();

	protected PushButton utilityActionButton;

	private static List<UtilitiesScreen> utilitiesScreenList = null;

	// Creates only desired amount of instances if we follow this pattern
	// otherwise we have public constructor as well
	public static UtilitiesScreen getInstance() {
		UtilitiesScreen utilitiesScreen = null;

		if (utilitiesScreenList == null) {
			utilitiesScreenList = new ArrayList<UtilitiesScreen>();
		}
		if (utilitiesScreenList.size() < AppConstants.MAX_UTILITIES_TABS) {
			// creates & returns new next instance of SupportDataScreen
			utilitiesScreenList.add(utilitiesScreen = new UtilitiesScreen());
		} else { // returns last instance of UtilitiesScreen from list
			utilitiesScreen = utilitiesScreenList
					.get(AppConstants.MAX_UTILITIES_TABS - 1);
		}
		return utilitiesScreen;
	}

	public static boolean removeInstance(UtilitiesScreen utilitiesScreen) {
		return utilitiesScreenList.remove(utilitiesScreen);
	}

	public UtilitiesScreen() {
		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		initWidget(horizontalPanel);
		horizontalPanel.setSize("100%", "100%");

		final VerticalPanel verticalPanel = new VerticalPanel();
		horizontalPanel.add(verticalPanel);
		verticalPanel.setSize("100%", "100%");

		final Label pleaseChooseALabel = new Label("Please choose a utility.");
		verticalPanel.add(pleaseChooseALabel);
		pleaseChooseALabel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		utilityTable = new CustomTable();
		verticalPanel.add(utilityTable);
		utilityTable.setAllowSelection(false);
		utilityTable.setSize("100%", "100%");
		utilityTable.setIndexName("utility_uuid");
		utilityTable.addColumn("Name", "utility_name");
		utilityTable.addColumn("Description", "utility_desc");
		utilityTable.setTableRowClickHandler(new TableRowClickHandler() {
			@Override
			public void handleRowClick(HashMap<String, String> data, int col) {
				String uuid = data.get("utility_uuid");
				thisUtilityUUID = uuid;
				getUtilityInformation(uuid);
			}
		});

		final VerticalPanel paramContainer = new VerticalPanel();
		horizontalPanel.add(paramContainer);

		// Utility label
		paramContainer.add(thisUtilityName);

		utilityParametersTable = new FlexTable();
		paramContainer.add(utilityParametersTable);
		utilityParametersTable.setVisible(false);
		utilityParametersTable.setSize("100%", "100%");

		utilityActionPanel = new HorizontalPanel();
		utilityActionPanel.setVisible(false);

		// Button
		utilityActionButton = new PushButton();
		utilityActionButton
				.setHTML("<img src=\"resources/images/check_go.32x32.png\" /><br/>"
						+ "Run");
		utilityActionButton.setStylePrimaryName("freemed-PushButton");
		utilityActionButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				runUtility();
			}
		});
		utilityActionPanel.add(utilityActionButton);

		paramContainer.add(utilityActionPanel);

		// After everything is initialized, start population routine.
		populate();
	}

	protected void runUtility() {
		// Get utility type
		String type = null;

		// Open window for request
		Window.open(Util.getJsonRequest(
				"org.freemedsoftware.module.Utilities.GenerateUtility",
				new String[] { thisUtilityUUID, type,
						JsonUtil.jsonify(getParameters()) }), "Utility", "");
	}

	public void populate() {
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
			// TODO: handle stubbed
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			String[] params = {};
			RequestBuilder builder = new RequestBuilder(
					RequestBuilder.POST,
					URL
							.encode(Util
									.getJsonRequest(
											"org.freemedsoftware.module.Utilities.GetUtilities",
											params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(
							com.google.gwt.http.client.Request request,
							Throwable ex) {
						GWT.log("Exception", ex);
					}

					@SuppressWarnings("unchecked")
					public void onResponseReceived(
							com.google.gwt.http.client.Request request,
							com.google.gwt.http.client.Response response) {
						if (200 == response.getStatusCode()) {
							HashMap<String, String>[] result = (HashMap<String, String>[]) JsonUtil
									.shoehornJson(JSONParser.parse(response
											.getText()),
											"HashMap<String,String>[]");
							if (result != null) {
								utilityTable.loadData(result);
							} else {
							}
						} else {
						}
					}
				});
			} catch (RequestException e) {
				GWT.log("Exception", e);
			}
		} else {
			// TODO: Make this work with GWT-RPC
		}
	}

	/**
	 * Get array of parameter values for current utility.
	 * 
	 * @return
	 */
	public String[] getParameters() {
		List<String> r = new ArrayList<String>();
		for (int iter = 0; iter < utilityParameters.size(); iter++) {
			r.add(iter, utilityParameters.get(Integer.valueOf(iter)));
		}
		return r.toArray(new String[0]);
	}

	/**
	 * Callback to convert utility parameter information into a form.
	 * 
	 * @param data
	 */
	protected void populateUtilityParameters(HashMap<String, String> data) {
		utilityParametersTable.clear(true);
		utilityParameters.clear();

		thisUtilityName.setText(data.get("utility_name"));

		for (int iter = 0; iter < new Integer(data.get("utility_param_count"))
				.intValue(); iter++) {
			final int i = iter;
			final String iS = new Integer(iter).toString();
			String type = data.get("utility_param_type_" + iS);
			utilityParametersTable.setText(iter, 0, data
					.get("utility_param_name_" + iS));
			Widget w = null;
			if (type.compareToIgnoreCase("Date") == 0) {
				w = new CustomDatePicker();
				((CustomDatePicker) w)
						.addValueChangeHandler(new ValueChangeHandler<Date>() {
							@Override
							public void onValueChange(
									ValueChangeEvent<Date> event) {
								Date dt = ((CustomDatePicker) event.getSource())
										.getValue();
								CustomDatePicker w = ((CustomDatePicker) event
										.getSource());
								String formatted = w.getFormat().format(
										new DateBox(), dt);

								utilityParameters.put(i, formatted);
							}
						});
			} else if (type.compareToIgnoreCase("Provider") == 0) {
				w = new SupportModuleWidget("ProviderModule");
				((SupportModuleWidget) w)
						.addChangeHandler(new ValueChangeHandler<Integer>() {
							@Override
							public void onValueChange(
									ValueChangeEvent<Integer> event) {
								utilityParameters.put(i,
										((SupportModuleWidget) event
												.getSource()).getValue()
												.toString());
							}
						});
			} else if (type.compareToIgnoreCase("Tag") == 0) {
				w = new PatientTagWidget();
				((PatientTagWidget) w)
						.addChangeHandler(new ValueChangeHandler<String>() {
							@Override
							public void onValueChange(
									ValueChangeEvent<String> event) {
								utilityParameters.put(i,
										((PatientTagWidget) event.getSource())
												.getValue());
							}
						});
			} else if (type.compareToIgnoreCase("Patient") == 0) {
				w = new PatientWidget();
				((PatientWidget) w)
						.addChangeHandler(new ValueChangeHandler<Integer>() {
							@Override
							public void onValueChange(
									ValueChangeEvent<Integer> event) {
								utilityParameters.put(i, ((PatientWidget) event
										.getSource()).getValue().toString());
							}
						});
			} else {
				// Default to text box
				w = new TextBox();
				((TextBox) w).addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent evt) {
						utilityParameters.put(i, ((TextBox) evt.getSource())
								.getText());
					}
				});
			}
			utilityParameters.put(iter, "");
			utilityParametersTable.setWidget(iter, 1, w);
		}

		// Show this when everything is populated
		utilityParametersTable.setVisible(true);
		utilityActionPanel.setVisible(true);
	}

	/**
	 * Get parameters for a specific utility by uuid.
	 * 
	 * @param uuid
	 */
	public void getUtilityInformation(String uuid) {
		if (Util.getProgramMode() == ProgramMode.STUBBED) {
			// TODO: handle stubbed
		} else if (Util.getProgramMode() == ProgramMode.JSONRPC) {
			String[] params = { uuid };
			RequestBuilder builder = new RequestBuilder(
					RequestBuilder.POST,
					URL
							.encode(Util
									.getJsonRequest(
											"org.freemedsoftware.module.Utilities.GetUtilityParameters",
											params)));
			try {
				builder.sendRequest(null, new RequestCallback() {
					public void onError(
							com.google.gwt.http.client.Request request,
							Throwable ex) {
						GWT.log("Exception", ex);
					}

					@SuppressWarnings("unchecked")
					public void onResponseReceived(
							com.google.gwt.http.client.Request request,
							com.google.gwt.http.client.Response response) {
						if (200 == response.getStatusCode()) {
							HashMap<String, String> result = (HashMap<String, String>) JsonUtil
									.shoehornJson(JSONParser.parse(response
											.getText()),
											"HashMap<String,String>");
							if (result != null) {
								populateUtilityParameters(result);
							}
						} else {
						}
					}
				});
			} catch (RequestException e) {
				GWT.log("Exception", e);
			}
		} else {
			// TODO: Make this work with GWT-RPC
		}
	}

	@Override
	public void closeScreen() {
		// TODO Auto-generated method stub
		super.closeScreen();
		removeInstance(this);
	}
}
