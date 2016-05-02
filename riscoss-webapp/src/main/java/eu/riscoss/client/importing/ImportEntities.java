package eu.riscoss.client.importing;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;

public class ImportEntities implements EntryPoint {
	
	private final String 		ENT_XLSX_FILE_NAME = "Supersede_IPR_Registry.xlsx";
	private final String 		CONF_XML_FILE_NAME = "Supersede_Config_Stored.xml";
	
	HorizontalPanel 			mainView;
	VerticalPanel				leftPanel;
	VerticalPanel				page;
	
	Grid						grid;
	
	boolean						entFileLoaded = false;
	boolean						confFileLoaded = false;

	@Override
	public void onModuleLoad() {
		
		loadLayoutStructure();
		
		checkIfFilesLoaded();
				
		RootPanel.get().add(page);
		
	}

	private void checkIfFilesLoaded() {
		RiscossJsonClient.checkImportFiles( new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (response.isObject().get("entFile").isBoolean().booleanValue())
					entFileLoaded = true;
				if (response.isObject().get("confFile").isBoolean().booleanValue())
					confFileLoaded = true;
				
				insertUploadElements();
			}
		});
	}

	private void insertUploadElements() {
		
		//Upload entities xlsx file button
		Button uploadEntities = new Button("Upload entities file");
		uploadEntities.setStyleName("deleteButton");
		uploadEntities.setWidth("100%");
		SingleUploader upload = new SingleUploader(FileInputType.CUSTOM.with(uploadEntities));
		upload.setTitle("Upload new entities document");
		upload.setServletPath(upload.getServletPath() + "?t=importentities&domain=" + RiscossJsonClient.getDomain()+"&token="+RiscossCall.getToken() );
		upload.setAutoSubmit(true);
		upload.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
			@Override
			public void onFinish(IUploader uploader) {
				
				UploadedInfo info = uploader.getServerInfo();
				String name = info.name;
				Log.println(info.name + ": " + info.toString());
				
				String response = uploader.getServerMessage().getMessage();
				
				Window.Location.reload();
				
			}
		});
		
		//Upload configuration xml file button
		Button uploadConfig = new Button("Upload configuration file");
		uploadConfig.setStyleName("deleteButton");
		uploadConfig.setWidth("100%");
		SingleUploader uploadConf = new SingleUploader(FileInputType.CUSTOM.with(uploadConfig));
		uploadConf.setTitle("Upload new entities document");
		uploadConf.setServletPath(uploadConf.getServletPath() + "?t=configimport&domain=" + RiscossJsonClient.getDomain()+"&token="+RiscossCall.getToken() );
		uploadConf.setAutoSubmit(true);
		uploadConf.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
			@Override
			public void onFinish(IUploader uploader) {
				
				UploadedInfo info = uploader.getServerInfo();
				String name = info.name;
				Log.println(info.name + ": " + info.toString());
				
				String response = uploader.getServerMessage().getMessage();
				
				Window.Location.reload();

			}
		});
		
		//Execute import entities button
		Button importEnt = new Button("Import entities info");
		importEnt.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (entFileLoaded && confFileLoaded) {
					RiscossJsonClient.importEntities(new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
	
						@Override
						public void onSuccess(Method method, JSONValue response) {
							Window.alert("Entity information imported correctly");
						}
					});
				} else {
					if (!entFileLoaded && !confFileLoaded) 
						Window.alert("Missing xlsx and config xml file");
					else if (!entFileLoaded)
						Window.alert("Missing xlsx entity file");
					else
						Window.alert("Missing config xml file");
				}
			}
		});
		importEnt.setStyleName("deleteButton");
		
		
		Label noConfFile;
		
		if (!entFileLoaded) {
			Label entFile = new Label("No xlsx entity info file has been loaded");
			grid.setWidget(0, 0, entFile);
		} else {
			Anchor entFile = new Anchor(ENT_XLSX_FILE_NAME, GWT.getHostPageBaseURL() +  "models/download?domain=" + RiscossJsonClient.getDomain() + 
					"&name="+ ENT_XLSX_FILE_NAME+"&type=xlsxEnt&token="+RiscossCall.getToken());
			grid.setWidget(0, 0, entFile);
		}
		
		if (!confFileLoaded) {
			Label confFile = new Label("No xml configuration file has been loaded");
			grid.setWidget(1, 0, confFile);
		} else {
			Anchor confFile = new Anchor(CONF_XML_FILE_NAME, GWT.getHostPageBaseURL() +  "models/download?domain=" + RiscossJsonClient.getDomain() + 
					"&name="+ CONF_XML_FILE_NAME+"&type=xmlConf&token="+RiscossCall.getToken());
			grid.setWidget(1, 0, confFile);
		}
		
		grid.setWidget(0, 1, upload);
		grid.setWidget(1, 1, uploadConf);
		grid.setWidget(2, 0, importEnt);
		
	}

	private void loadLayoutStructure() {
		
		page = new VerticalPanel();
		mainView = new HorizontalPanel();
		leftPanel = new VerticalPanel();
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("700px");
		page.setWidth("100%");
		
		Label title = new Label("Import entities");
		title.setStyleName("title");
		
		//Main layout
		page.add(title);
		page.add(mainView);
		
		//Left layout
		mainView.add(leftPanel);
		grid = new Grid(3,2);
		grid.setCellPadding(5);
		leftPanel.add(grid);
		
	}

}
