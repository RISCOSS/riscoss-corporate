package eu.riscoss.client.importconfig;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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

public class ConfigImportModule implements EntryPoint {
	
	private final String 		CONF_XML_FILE_NAME = "importation_config.xml";

	HorizontalPanel 			mainView;
	VerticalPanel				leftPanel;
	VerticalPanel				page;
	
	Grid						grid;
	
	boolean						confFileLoaded = false;
	
	@Override
	public void onModuleLoad() {
		
		loadLayoutStructure();
		
		checkIfConfFileLoaded();
		
		RootPanel.get().add(page);
		
	}

	private void checkIfConfFileLoaded() {
		RiscossJsonClient.checkImportFiles( new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (response.isObject().get("confFile").isBoolean().booleanValue())
					confFileLoaded = true;
				
				insertUploadElements();
			}
		});
	}

	protected void insertUploadElements() {
		
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
		
		if (!confFileLoaded) {
			Label confFile = new Label("No xml configuration file has been loaded");
			grid.setWidget(0, 0, confFile);
		} else {
			Anchor confFile = new Anchor(CONF_XML_FILE_NAME, GWT.getHostPageBaseURL() +  "models/download?domain=" + RiscossJsonClient.getDomain() + 
					"&name="+ CONF_XML_FILE_NAME+"&type=xmlConf&token="+RiscossCall.getToken());
			grid.setWidget(0, 0, confFile);
		}
		
		grid.setWidget(0, 1, uploadConf);
		
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
		
		Label title = new Label("Import configuration");
		title.setStyleName("title");
		
		//Main layout
		page.add(title);
		page.add(mainView);
		
		//Left layout
		mainView.add(leftPanel);
		grid = new Grid(1,2);
		grid.setCellPadding(5);
		leftPanel.add(grid);
	}

}
