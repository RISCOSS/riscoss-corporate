package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRiskData;
import eu.riscoss.shared.JMissingData;

public class MissingDataDialog {
	
	DialogBox dialog = new DialogBox();
	
	MultiLayerInputForm inputForm = new MultiLayerInputMatrix();

	private String ras;
	
	public MissingDataDialog( JMissingData md, String ras ) {
		
		this.ras = ras;
		
		dialog.setText( "Enter Missing Data Information" );
		
		inputForm.load( md );
		
		DockPanel dock = new DockPanel();
		dock.add( inputForm, DockPanel.CENTER );
		dock.add( new Button( "Done", new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onDone();
			}
		} ), DockPanel.SOUTH );
		dialog.setWidget( dock );
		
	}

	protected void onDone() {
		
		CodecRiskData crd = GWT.create( CodecRiskData.class );
		String values = crd.encode( inputForm.getValueMap() ).toString();
		
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/" + ras + "/missing-data" )
			.put().header( "values", values ).send( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
		
		dialog.hide();
	}

	public void show() {
		dialog.show();
	}
	
}
