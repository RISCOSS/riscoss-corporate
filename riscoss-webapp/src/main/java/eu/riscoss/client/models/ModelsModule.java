/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/**
 * @author 	Alberto Siena, Mirko Morandini
 **/

package eu.riscoss.client.models;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.riskanalysis.KeyValueGrid;
import eu.riscoss.client.ui.LinkHtml;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

public class ModelsModule implements EntryPoint {

//	public class MyFancyLookingButton extends Composite implements HasClickHandlers {
//		SimplePanel widget = new SimplePanel();
//		public MyFancyLookingButton() {
//			DecoratorPanel widget = new DecoratorPanel();
//			//initWidget( new Anchor( "Choose..." ) );
//			initWidget(widget);
//			widget.setWidget(new HTML("New..."));
//		}
//		public HandlerRegistration addClickHandler(ClickHandler handler) {
//			return addDomHandler(handler, ClickEvent.getType());
//		}
//	}
	
	DockPanel dock = new DockPanel();

	CellTable<ModelInfo> table;
	ListDataProvider<ModelInfo> dataProvider;
	// private FlowPanel panelImages = new FlowPanel();
	SimplePanel rightPanel = new SimplePanel();

	public ModelsModule() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.riscoss.client.ActivablePanel#getWidget()
	 */
	public Widget getWidget() {
		return dock;
	}

	public void onModuleLoad() {

		exportJS();

		table = new CellTable<ModelInfo>();

		table.addColumn(new Column<ModelInfo, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(ModelInfo object) {
				return new LinkHtml(object.getName(), "javascript:editModel(\"" + object.getName() + "\")");
			};
		}, "Model");

		Column<ModelInfo, String> c = new Column<ModelInfo, String>(new ButtonCell()) {
			@Override
			public String getValue(ModelInfo object) {
				return "Delete";
			}
		};
		c.setFieldUpdater(new FieldUpdater<ModelInfo, String>() {
			@Override
			public void update(int index, ModelInfo object, String value) {
				deleteModel(object);
			}
		});
		table.addColumn(c, "");

		dataProvider = new ListDataProvider<ModelInfo>();
		dataProvider.addDataDisplay(table);

		Resource resource = new Resource(GWT.getHostPageBaseURL() + "api/models/list");

		resource.get().send(new JsonCallback() {

			public void onSuccess(Method method, JSONValue response) {
				GWT.log(response.toString());
				if (response.isArray() != null) {
					for (int i = 0; i < response.isArray().size(); i++) {
						JSONObject o = (JSONObject) response.isArray().get(i);
						dataProvider.getList().add(new ModelInfo(o.get("name").isString().stringValue()));
					}
				}
			}

			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
		});

		// Attach the image viewer to the document
		// RootPanel.get().add(panelImages);

		//MyFancyLookingButton button = new MyFancyLookingButton();
		//SingleUploader uploader = new SingleUploader();//FileInputType.CUSTOM.with(button));// "New..."
		SingleUploader uploader = new SingleUploader(FileInputType.BUTTON);// "New..."
		//with a specific caption:
		//SingleUploader uploader = new SingleUploader(FileInputType.CUSTOM.with(new Button("New...")));// "New..."
		uploader.setTitle("Upload new model");
		uploader.setAutoSubmit(true);
		uploader.setServletPath(uploader.getServletPath() + "?t=modelblob");
		uploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
			@Override
			public void onFinish(IUploader uploader) {
				UploadedInfo info = uploader.getServerInfo();
				String name = info.name;
				//Log.println("SERVERMESS2 " + uploader.getServerMessage().getMessage());
				
				String response = uploader.getServerMessage().getMessage();
				if (response.trim().equalsIgnoreCase("Success"))
					dataProvider.getList().add(new ModelInfo(name));
				else
					Window.alert("Error: " + response );
			}
		});

		SimplePager pager = new SimplePager();
		pager.setDisplay(table);

		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.add(table);
		tablePanel.add(pager);
		
		
		dock.add(tablePanel, DockPanel.CENTER);
		dock.add(uploader, DockPanel.NORTH);
		dock.add(rightPanel, DockPanel.EAST);

		dock.setCellWidth(tablePanel, "40%");
		dock.setCellHeight(rightPanel, "100%");
		table.setWidth("100%");

		dock.setSize("100%", "100%");

		RootPanel.get().add(dock);
	}

	protected void deleteModel(ModelInfo info) {
		RiscossJsonClient.deleteModel(info.getName(), new JsonCallbackWrapper<ModelInfo>(info) {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, JSONValue response) {
				dataProvider.getList().remove(getValue());
			}
		});
	}

	public native void exportJS() /*-{
		var that = this;
		$wnd.editModel = $entry(function(amt) {
			that.@eu.riscoss.client.models.ModelsModule::setSelectedModel(Ljava/lang/String;)(amt);
		});
	}-*/;

	static class EditModelDialog implements IsWidget {

		// // Load the image in the document and in the case of success attach
		// it to the viewer
		// private IUploader.OnFinishUploaderHandler onFinishUploaderHandler =
		// new IUploader.OnFinishUploaderHandler() {
		// public void onFinish(IUploader uploader) {
		// popup.hide();
		// if (uploader.getStatus() == Status.SUCCESS) {
		// text = null;
		// new PreloadedImage(uploader.fileUrl(), showImage);
		// }
		// }
		// };

		// // Attach an image to the pictures viewer
		// private OnLoadPreloadedImageHandler showImage = new
		// OnLoadPreloadedImageHandler() {
		// public void onLoad(PreloadedImage image) {
		// image.setWidth("75px");
		// panelImages.add(image);
		// }
		// };
		
		public class MyNameButton extends Button{
			public String name;
			public MyNameButton(String name, String html, ClickHandler handler){
				super(html, handler);
				this.name = name;
			}
		}

		DockPanel panel = new DockPanel();
		String text = null;
		String name;
		TextArea area;
		KeyValueGrid chunksGrid;
		IndexedTab tab;

		// PopupPanel popup = new PopupPanel( false, true );
		// SingleUploader u = new SingleUploader();
		// String servletPath = u.getServletPath();

		public EditModelDialog() {
			// u.addOnFinishUploadHandler(onFinishUploaderHandler);
			// u.setAutoSubmit( true );
			// popup.add( u );
			// popup.setSize( "300px", "32px" );
		}

		public void editModel(String name) {
			this.name = name;
			RiscossJsonClient.getModelinfo(name, new JsonCallback() {
				@Override
				public void onSuccess(Method method, JSONValue response) {
					showEditDialog(response.isObject());
				}

				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
			});
		}

		void showEditDialog(JSONObject json) {
			
			Grid grid = new Grid(2, 2);
			grid.setWidget(0, 0, new Label("Name:"));
			//Label txt = new Label();
			TextBox txt = new TextBox();
			txt.setReadOnly(true);
			String jsname = json.get("name").isString().stringValue();
			txt.setText(jsname);
			grid.setWidget(0, 1, txt);

//			grid.setWidget(1, 0, new Label("Description:"));
//			txt = new TextBox();
//			txt.setReadOnly(true);
//			grid.setWidget(1, 1, txt);
			
			//Uploader///////////
			SingleUploader docuUploader = new SingleUploader(); //FileInputType.CUSTOM.with(new Button("Upload model documentation")));// "New..."
			docuUploader.setTitle("Upload model documentation");
			docuUploader.setAutoSubmit(true);
			
			docuUploader.setServletPath(docuUploader.getServletPath() + "?t=modeldescblob");
					
			docuUploader.add(new Hidden("Modelname", jsname));
				
			docuUploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
				@Override
				public void onFinish(IUploader uploader) {
					UploadedInfo info = uploader.getServerInfo();
					String name = info.name;
					//Log.println("SERVERMESS2 " + uploader.getServerMessage().getMessage());
					
					String response = uploader.getServerMessage().getMessage();
					if (!response.trim().startsWith("Error"))
						Window.confirm(response); //TODO change!
					else
						Window.alert("Error: " + response );
				}
			});
			
			grid.setWidget( 1, 0, docuUploader);
			
			//Downloader/////////
			Button docuDownloader = new MyNameButton(jsname, "Download documentation", new ClickHandler() {
				public void onClick(ClickEvent event) {
					Window.open(GWT.getHostPageBaseURL() +  "models/descBlob?name="+ name, "_self", "enabled");					
				}
			});
			
			grid.setWidget( 1, 1, docuDownloader);
			
			area = new TextArea();
			chunksGrid = new KeyValueGrid();

			tab = new IndexedTab();
			tab.addTab("Properties", grid);
			tab.addTab("Model info", chunksGrid);
			tab.addTab("Raw Model content", area);
			tab.getTabPanel().selectTab(0);
			tab.getTabPanel().setSize("100%", "100%");
			tab.addTabHandler("Model info", new IndexedTab.TabHandler() {
				@Override
				public void onTabActivated() {
					List<String> list = new ArrayList<String>();
					list.add(name);
					RiscossJsonClient.listChunks(list, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}

						@Override
						public void onSuccess(Method method, JSONValue response) {
							JSONObject jo = response.isObject();
							if (jo.get("inputs") != null) {
								JSONArray inputs = jo.get("inputs").isArray();
								VerticalPanel p = new VerticalPanel();
								for (int i = 0; i < inputs.size(); i++) {
									JSONObject jinput = inputs.get(i).isObject();
									p.add(new Label(jinput.get("id").isString().stringValue()));
								}
								chunksGrid.add("Requested Indicators:", p);
							}

							if (jo.get("outputs") != null) {
								JSONArray inputs = jo.get("outputs").isArray();
								VerticalPanel p = new VerticalPanel();
								for (int i = 0; i < inputs.size(); i++) {
									JSONObject jinput = inputs.get(i).isObject();
									p.add(new Label(jinput.get("id").isString().stringValue()));
								}
								chunksGrid.add("Provided Output:", p);
							}

							EditModelDialog.this.tab.removeListeners("Model info");
						}
					});
				}
			});
			tab.addTabHandler("Raw Model content", new IndexedTab.TabHandler() {
				@Override
				public void onTabActivated() {
					if (text == null) {
						RiscossJsonClient.getModelBlob(name, new JsonCallback() {
							@Override
							public void onFailure(Method method, Throwable exception) {
								Window.alert(exception.getMessage());
							}

							@Override
							public void onSuccess(Method method, JSONValue response) {
								area.setText(response.isObject().get("blob").isString().stringValue());
							}
						});
					}
				}
			});

			panel.add(tab, DockPanel.CENTER);
			panel.add(new Button("Save", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onDoneClicked();
				}
			}), DockPanel.SOUTH);
			panel.setSize("100%", "100%");
		}

		// protected void onTabActivated( TabPanel tab, Integer selectedItem ) {
		// if( selectedItem != 1 ) return;
		// if( text == null ) {
		// RiscossJsonClient.getModelBlob( name, new JsonCallback() {
		// @Override
		// public void onFailure(Method method, Throwable exception) {
		// Window.alert( exception.getMessage() );
		// }
		//
		// @Override
		// public void onSuccess(Method method, JSONValue response) {
		// area.setText( response.isObject().get( "blob"
		// ).isString().stringValue() );
		// }} );
		// }
		// }

		protected void onDoneClicked() {
			// dialog.hide();
		}

		@Override
		public Widget asWidget() {
			return panel;
		}
	}

	public void setSelectedModel(String name) {
		if (rightPanel.getWidget() != null) {
			rightPanel.getWidget().removeFromParent();
		}
		EditModelDialog panel = new EditModelDialog();
		rightPanel.setWidget(panel);
		panel.editModel(name);
	}
}
