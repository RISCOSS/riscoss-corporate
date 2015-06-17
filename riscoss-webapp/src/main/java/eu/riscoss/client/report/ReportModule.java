package eu.riscoss.client.report;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.Callback;
import eu.riscoss.client.JsonUtil;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.ui.MissingDataInputForm;
import eu.riscoss.shared.AnalysisOption;
import eu.riscoss.shared.AnalysisResult;

public class ReportModule implements EntryPoint {
	
	DockPanel dock = new DockPanel();

	public ReportModule() {
	}
	
	// Dialog box to enter missing data, if necessary
	MissingDataInputForm inputForm = null;
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#getWidget()
	 */
	public Widget getWidget() {
		return dock;
	}

	public void onModuleLoad() {
		
		startAnalysisWorkflow( AnalysisOption.RequestMissingData );
		
	}
	
	protected void startAnalysisWorkflow( AnalysisOption opt ) {
		startAnalysisWorkflow( opt, null );
	}
	
	protected void startAnalysisWorkflow( AnalysisOption opt, JSONObject values ) {
		
		if( values == null ) values = new JSONObject();
		
		RiscossJsonClient.runAnalysis( 
				Window.Location.getParameter( "target" ), Window.Location.getParameter( "rc" ), 
				"full", opt, values,
				new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
//						Window.alert( "" + response.isObject() );
						if( response.isObject() == null ) return;
						
						try {
						String strResult = JsonUtil.getValue( response, "result", AnalysisResult.Failure.name() );
						
						AnalysisResult result = AnalysisResult.valueOf( strResult );
						if( result == null ) result = AnalysisResult.Failure;
						
						
						switch( result ) {
						case DataMissing:
							showInputForm( response );
							break;
						case Done:
							showResults( response.isObject() );
							break;
						case Failure:
							Window.alert( "Unexpected failure" );
							break;
						default:
							break;
					}
						}
						catch( Exception ex ) {
							Window.alert( ex.getMessage() );
						}
					}} );
		
	}
	
	protected void showInputForm(JSONValue response) {
		
//		Window.alert( "" + response );
		
		JSONObject missingData = JsonUtil.getObject( response, "missingData" );
		if( missingData == null ) {
			Window.alert( "Missing data was declared but no forther information received" );
			return;
		}
		
		if( inputForm == null )
			inputForm = new MissingDataInputForm();
		
		inputForm.load( missingData );
		
		inputForm.show( new Callback<JSONObject>() {
			@Override
			public void onError( Throwable t ) {
				Window.alert( t.getMessage() );
			}
			
			@Override
			public void onDone( JSONObject o ) {
				startAnalysisWorkflow( AnalysisOption.RunThrough, o );
			}
		});
		
		// TODO autohide must be set to false in deployment
		
	}

	protected void showResults( JSONObject object ) {
		
		if( dock != null ) {
			while( dock.getWidgetCount() > 0 ) {
				dock.remove( dock.getWidget( 0 ) );
			}
			dock.removeFromParent();
			dock = new DockPanel();
		}
		
		if( object.get( "results" ) == null ) return;
		JSONArray response = object.get( "results" ).isArray();
		
//		Window.alert( "2: " + response );
		RiskAnalysisReport report = new RiskAnalysisReport();
		report.showResults( response );
		
		DisclosurePanel dp = new DisclosurePanel( "Input values used for this evaluation" );
		
		JSONArray inputs = getArray( object, "inputs" );
		Grid inputgrid = new Grid( inputs.size(), 2 );
		for( int i = 0; i < inputs.size(); i++ ) {
			JSONObject o = inputs.get( i ).isObject();
//			Window.alert( "" + o );
			inputgrid.setWidget( i, 0, new Label( o.get( "id" ).isString().stringValue() ) );
			inputgrid.setWidget( i, 1, new Label( o.get( "value" ).isString().stringValue() ) );
		}
		dp.setContent( inputgrid );
		
		
		Anchor a = new Anchor( "Change input values..." );
		a.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if( inputForm != null )
					inputForm.show( new Callback<JSONObject>() {
						@Override
						public void onError( Throwable t ) {
							Window.alert( t.getMessage() );
						}
						
						@Override
						public void onDone( JSONObject o ) {
							startAnalysisWorkflow( AnalysisOption.RunThrough, o );
						}
					} );
			}
		});
		
		
		dock.setSize( "100%", "100%" );
//		grid.setSize( "100%", "100%" );
		
		dock.add( new HTML( "<h1>" + 
				"Risk analysis report of " +
				object.get( "info" ).isObject().get( "entity" ).isString().stringValue() + 
				"</h1>" ), DockPanel.NORTH );
		
		dock.add( report, DockPanel.CENTER );
		dock.add( a, DockPanel.NORTH );
		dock.add( dp, DockPanel.SOUTH );
		
		RootPanel.get().add( dock );
	}
	
	static final JSONArray EMPTY_ARRAY = new JSONArray();
	
	private JSONArray getArray( JSONObject object, String key ) {
		if( object == null ) return EMPTY_ARRAY;
		JSONValue v = object.get( key );
		if( v == null ) return EMPTY_ARRAY;
		if( v.isArray() == null ) return EMPTY_ARRAY;
		return v.isArray();
	}
}
