package eu.riscoss.client.importing;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
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
	
	HorizontalPanel 			mainView;
	VerticalPanel				leftPanel;
	VerticalPanel				page;

	@Override
	public void onModuleLoad() {
		
		loadLayoutStructure();
		
		insertUploadElements();
		
		RootPanel.get().add(page);
		
	}

	private void insertUploadElements() {
		
		Button uploadEntities = new Button("Upload entities file");
		uploadEntities.setStyleName("button");
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
				
			}
		});
		
		Button uploadConfig = new Button("Upload configuration file");
		uploadConfig.setStyleName("button");
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

			}
		});
		
		Button importEnt = new Button("Import entities info");
		importEnt.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
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
			}
		});
		importEnt.setStyleName("deleteButton");
		
		leftPanel.add(upload);
		leftPanel.add(uploadConf);
		leftPanel.add(importEnt);
		
	}

	private void loadLayoutStructure() {
		
		page = new VerticalPanel();
		mainView = new HorizontalPanel();
		leftPanel = new VerticalPanel();
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("500px");
		page.setWidth("100%");

		mainView.add(leftPanel);
		
		Label title = new Label("Import entities");
		title.setStyleName("title");
		page.add(title);
		
		page.add(mainView);
		
	}

}
