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

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.riskanalysis.KeyValueGrid;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.RiscossUtil;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploader;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;
import gwtupload.client.SingleUploader;

public class ModelsModule implements EntryPoint {

	private static final String BUTTON_UPDATE_MODEL	= "Upload new model";
	private static final String BUTTON_UPLOAD_DESC 	= "Upload new documentation";
	private static final String BUTTON_NEW_MODEL 	= "UPLOAD MODEL"; // "New...";
	private static final String BUTTON_CHANGE_NAME 	= "change";
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	VerticalPanel		tablePanel = new VerticalPanel();
	
	Grid				grid;
	TextBox				tb = new TextBox();
	Button				editName;
	Button				saveEditName;
	Button				cancelEditName;
	
	String				selectedModel;
	
	JSONValue			modelsList;

	CellTable<ModelInfo> table;
	ListDataProvider<ModelInfo> dataProvider;
	// private FlowPanel panelImages = new FlowPanel();
	SimplePanel rightPanel2 = new SimplePanel();

	public void onModuleLoad() {

		exportJS();
		
		RiscossJsonClient.listModels(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub	
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				modelsList = response;
			}
		});

		table = new CellTable<ModelInfo>(15, (Resources) GWT.create(TableResources.class));

		table.addColumn(new Column<ModelInfo, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(ModelInfo object) {
				return new LinkHtml(object.getName(), "javascript:editModel(\"" + object.getName() + "\")");
			};
		}, "Model");

		dataProvider = new ListDataProvider<ModelInfo>();
		dataProvider.addDataDisplay(table);

		RiscossJsonClient.listModels( new JsonCallback() {

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
		
		SimplePager pager = new SimplePager();
		pager.setDisplay(table);

		tablePanel = new VerticalPanel();
		tablePanel.add(table);
		table.setWidth("100%");
		tablePanel.add(pager);
		tablePanel.setWidth("100%");
		table.setWidth("100%");
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("400px");
		//leftPanel.setHeight("100%");
		rightPanel.setStyleName("rightPanelLayer");
		Label title = new Label("Model management");
		title.setStyleName("title");
		page.add(title);

		Button uploadModel = new Button("Upload model");

		uploadModel.setStyleName("button");
		SingleUploader upload = new SingleUploader(FileInputType.CUSTOM.with(uploadModel));
		upload.setTitle("Upload new model");
		upload.setAutoSubmit(true);
		upload.setServletPath(upload.getServletPath() + "?t=modelblob&domain=" + RiscossJsonClient.getDomain()+"&token="+RiscossCall.getToken() );
		upload.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
			@Override
			public void onFinish(IUploader uploader) {
				
				UploadedInfo info = uploader.getServerInfo();
				String name = info.name;
				if (name == null || name.equals("") ) 
					return;
				
				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				if (!RiscossUtil.sanitize(name).equals(name)){
					//info: firefox has some problem with this window, and fires assertion errors in dev mode
					Window.alert("Name contains prohibited characters (##,@,\") \nPlease rename file");
					return;
				}
				
				for(int i=0; i< modelsList.isArray().size(); i++){
					JSONObject o = (JSONObject)modelsList.isArray().get(i);
					if (name.equals(o.get( "name" ).isString().stringValue())){
						//info: firefox has some problem with this window, and fires assertion errors in dev mode
						Window.alert("Model name already in use.\nPlease rename file.");
						return;
					}
				}
				//Log.println("SERVERMESS2 " + uploader.getServerMessage().getMessage());
				
				String response = uploader.getServerMessage().getMessage();
				if (response.trim().equalsIgnoreCase("Success"))
					dataProvider.getList().add(new ModelInfo(name));
				else
					Window.alert("Error: " + response );

				setSelectedModel(name);
			}
		});
		leftPanel.add(upload);
		
		leftPanel.add(tablePanel);
		
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		page.setWidth("100%");

		RootPanel.get().add(page);
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

	public class EditModelDialog implements IsWidget {

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
		TextBox txt;

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
			
			Grid grid = new Grid(2, 3);
			String jsname = json.get("name").isString().stringValue();
			
			//Uploader///////////
			
			String descfilename = json.get("modeldescfilename").isString().stringValue();
//			Label descfLabel = new Label("Documentation:\n"+descfilename);
			
						
			if (descfilename==null || descfilename.equals("")){
				Label descfLabel = new Label("No documentation uploaded.");
				grid.setWidget( 1, 0, descfLabel);
			} else {

				Anchor descfAnchor = new Anchor("Download documentation:\n"+descfilename, GWT.getHostPageBaseURL() +  "models/download?domain=" + RiscossJsonClient.getDomain() + 
						"&name="+ name+"&type=desc&token="+RiscossCall.getToken());
				grid.setWidget( 1, 0, descfAnchor);

			}
			
			Button uploadDesc = new Button(BUTTON_UPLOAD_DESC);
			uploadDesc.setStyleName("button");
			SingleUploader docuUploader = new SingleUploader(FileInputType.CUSTOM.with(uploadDesc)); 
			docuUploader.setTitle("Upload model documentation");
			docuUploader.setAutoSubmit(true);
			
			docuUploader.setServletPath(docuUploader.getServletPath() + "?t=modeldescblob&domain=" + RiscossJsonClient.getDomain()+"&token="+RiscossCall.getToken());
					
			docuUploader.add(new Hidden("Modelname", jsname));
				
			docuUploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
				@Override
				public void onFinish(IUploader uploader) {
					UploadedInfo info = uploader.getServerInfo();
					String name = info.name;
					//Log.println("SERVERMESS2 " + uploader.getServerMessage().getMessage());
					
					String response = uploader.getServerMessage().getMessage();
					if (!response.trim().startsWith("Error")){
						//Window.confirm(response); //TODO change!
						Window.Location.reload(); //TODO update on the fly without reloading
					} else
						Window.alert("Error: " + response );
				}
			});
			
			grid.setWidget( 1, 1, docuUploader);
			
			//Downloader/////////done in Anchor now!
			
//			Button docuDownloader = new MyNameButton(jsname, "Download documentation", new ClickHandler() {
//				public void onClick(ClickEvent event) {
//					Window.open(GWT.getHostPageBaseURL() +  "models/descBlob?name="+ name, "_self", "enabled");					
//				}
//			});
			
			//modeldescfilename
			//grid.setWidget( 1, 2, docuDownloader);
			
			//Model Update Uploader///////////
			Button updateModel = new Button(BUTTON_UPDATE_MODEL);
			updateModel.setStyleName("button");
			SingleUploader updateUploader = new SingleUploader(FileInputType.CUSTOM.with(updateModel));
			updateUploader.setTitle("Update the model to a new version");
			updateUploader.setAutoSubmit(true);
			
			updateUploader.setServletPath(updateUploader.getServletPath() + "?t=modelupdateblob&domain="+ RiscossJsonClient.getDomain()+"&token="+RiscossCall.getToken());
			updateUploader.add(new Hidden("modelname", jsname));
				
			updateUploader.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
				@Override
				public void onFinish(IUploader uploader) {
					UploadedInfo info = uploader.getServerInfo();
					//String name = info.name;
					//Log.println("SERVERMESS2 " + uploader.getServerMessage().getMessage());
					
					String response = uploader.getServerMessage().getMessage();
					if (!response.trim().startsWith("Error")){
						Window.confirm(response); //TODO change!
						Window.Location.reload();
					}
					else
						Window.alert("Error: " + response );
				}
			});
			
			
			Anchor fAnchor = new Anchor("Download model:\n"+json.get("modelfilename"), GWT.getHostPageBaseURL() + 
					"models/download?domain=" + RiscossJsonClient.getDomain() + "&name="+ name+"&type=model&token="+RiscossCall.getToken());
			grid.setWidget( 0, 0, fAnchor);

			//grid.setWidget( 1, 0, new Label("Model filename: \n"+json.get("modelfilename").isString().stringValue()));
			
			grid.setWidget( 0, 1, updateUploader);
			
			
			////////////////////////////////////
			
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
								int height = (int)(Window.getClientHeight()*0.7); 
								area.setHeight(height + "px");
							}
						});
					}
				}
			});

			panel.add(tab, DockPanel.CENTER);
//			panel.add(new Button("Save", new ClickHandler() {
//				@Override
//				public void onClick(ClickEvent event) {
//					onDoneClicked();
//				}
//			}), DockPanel.SOUTH);
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

		
		@Override
		public Widget asWidget() {
			return panel;
		}
	}
	
	TextBox nameM;
	
	public void setSelectedModel(String name) {
		selectedModel = name;
		if (rightPanel2.getWidget() != null) {
			rightPanel2.getWidget().removeFromParent();
		}
		EditModelDialog panel = new EditModelDialog();
		rightPanel2.setWidget(panel);
		panel.editModel(name);
		
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setStyleName("rightPanelLayer");
		rightPanel.setWidth("90%");
		
		Label title = new Label(name);
		title.setStyleName("subtitle");
		rightPanel.add(title);
		
		grid = new Grid(1,2);
		grid.setStyleName("properties");
		
		Label nameL = new Label("Name");
		nameL.setStyleName("bold");
		grid.setWidget(0,0,nameL);
		
		nameM = new TextBox();
		nameM.setWidth("250px");
		nameM.setText(name);
		nameM.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
					
					String name = nameM.getText().trim();
	
					RiscossJsonClient.changeModelName(selectedModel, name, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							//Window.alert(exception.getMessage());
							Window.alert("Name already existing. Nothing changed. "); 
						}

						@Override
						public void onSuccess(Method method, JSONValue response) {
							Window.Location.reload();
//							dataProvider.getList().add(new ModelInfo(txt.getText()));
//							dataProvider.getList().remove(name);
//							dataProvider.refresh();
						}
					});
				}
			}
		});
		grid.setWidget(0, 1, nameM);
		
		rightPanel.add(grid);
		
		HorizontalPanel buttons = new HorizontalPanel();
		Button delete = new Button("Delete");
		delete.setStyleName("button");
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				RiscossJsonClient.deleteModel(selectedModel, new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}

					@Override
					public void onSuccess(Method method, JSONValue response) {
						//dataProvider.getList().remove(getValue());
						mainView.remove(rightPanel);
						table = new CellTable<ModelInfo>(15, (Resources) GWT.create(TableResources.class));

						table.addColumn(new Column<ModelInfo, SafeHtml>(new SafeHtmlCell()) {
							@Override
							public SafeHtml getValue(ModelInfo object) {
								return new LinkHtml(object.getName(), "javascript:editModel(\"" + object.getName() + "\")");
							};
						}, "Model");

						dataProvider = new ListDataProvider<ModelInfo>();
						dataProvider.addDataDisplay(table);
						
						RiscossJsonClient.listModels(new JsonCallback() {

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
						
						SimplePager pager = new SimplePager();
						pager.setDisplay(table);

						leftPanel.remove(tablePanel);
						
						tablePanel = new VerticalPanel();
						tablePanel.add(table);
						tablePanel.add(pager);
						
						leftPanel.add(tablePanel);
						
						
					}
				});
			}
		});
		buttons.add(delete);
		
		rightPanel.add(buttons);
		
		EditModelDialog panel2 = new EditModelDialog();
		panel2.editModel(name);
		rightPanel.add(panel);
		mainView.add(rightPanel);
		
	}
}
