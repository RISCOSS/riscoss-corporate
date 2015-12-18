package eu.riscoss.client.rma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.Callback;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.RiscossWebApp;
import eu.riscoss.client.SimpleRiskCconf;
import eu.riscoss.client.codec.CodecAHPInput;
import eu.riscoss.client.codec.CodecChunkList;
import eu.riscoss.client.codec.CodecStringList;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.client.riskconfs.ModelSelectionDialog;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.shared.EChunkType;
import eu.riscoss.shared.JAHPComparison;
import eu.riscoss.shared.JAHPInput;
import eu.riscoss.shared.JAHPResult;
import eu.riscoss.shared.JChunkItem;
import eu.riscoss.shared.JChunkList;
import eu.riscoss.shared.JStringList;

public class RMAModule implements EntryPoint {
	
	List<String> models;
	
	DockPanel			dock = new DockPanel();
	SimplePanel			contentPanel = new SimplePanel();
	RiskEvaluationForm	riskForm = new RiskEvaluationForm();
	PreferenceMatrix	preferenceMatrix = new PreferenceMatrix( new ArrayList<JAHPComparison>() );
	RMAOutputPanel		outputPanel = new RMAOutputPanel();
	
	Set<String> selection = new HashSet<String>();
	
	List<JChunkItem> goals = new ArrayList<JChunkItem>();
	List<JChunkItem> risks = new ArrayList<JChunkItem>();
	
	ArrayList<JAHPComparison>				goalComparisons = new ArrayList<JAHPComparison>();
	Map<String,ArrayList<JAHPComparison>>	riskComparisons = new HashMap<String,ArrayList<JAHPComparison>>();
	
	String 			rasID;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel 		leftPanel = new VerticalPanel();
	HorizontalPanel 	buttons = new HorizontalPanel();
	
	@Override
	public void onModuleLoad() {
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		
		
		
		if (Window.Location.getParameter("id") != null) rasID = Window.Location.getParameter("id");
		else rasID = "";
		HorizontalPanel h = new HorizontalPanel();
		
		if (rasID.equals("")) {
			Anchor a = new Anchor( "Select models..." );
			a.addClickHandler( new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					ModelSelectionDialog dialog = new ModelSelectionDialog();
					dialog.show( new Callback<List<String>>() {
						@Override
						public void onError( Throwable t ) {
							// Do nothing
						}
						@Override
						public void onDone( List<String> list ) {
							models = list;
							RiscossJsonClient.listChunkslist(list, new JsonCallback() {
								@Override
								public void onSuccess( Method method, JSONValue response ) {
									loadModel( response );
									}
								@Override
								public void onFailure( Method method, Throwable exception ) {
									Window.alert( exception.getMessage() );
									}
							} );
						}
					});
				}
			});
			h.add(a);
		}
		else {
			loadRASInfo();
		}
		dock.add( h, DockPanel.NORTH );
		dock.setCellHeight( h, "1%" );
		dock.add( contentPanel,DockPanel.CENTER );
		dock.setCellVerticalAlignment( contentPanel, DockPanel.ALIGN_TOP );
		dock.setSize( "100%", "100%" );
		
		Label title ;
		if (!rasID.equals("")) title = new Label("Apply mitigation");
		else title = new Label("AHP session analysis");
		title.setStyleName("title");
		
		leftPanel.add(buttons);
		leftPanel.add(dock);
		leftPanel.setWidth("100%");
		mainView.setWidth("100%");
		mainView.add(leftPanel);
		
		page.add(title);
		page.add(mainView);
		
		page.setWidth("100%");
		
		RootPanel.get().add( page );
		
	}
	
	public void loadRASInfo() {
		RiscossJsonClient.getSessionSummary(rasID, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JsonRiskAnalysis ras = new JsonRiskAnalysis( response );
				RiscossJsonClient.getRCContent(ras.getRC(), new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						SimpleRiskCconf rc = new SimpleRiskCconf( response );
						ArrayList<String> mList = new ArrayList<>();
						for (int i = 0; i < rc.getLayerCount(); ++i) {
							for (String s : rc.getModelList(rc.getLayer(i))) {
								mList.add(s);
							}
						}
						models = mList;
						RiscossJsonClient.listChunkslist(mList, new JsonCallback() {
							@Override
							public void onSuccess( Method method, JSONValue response ) {
								loadModel( response );
								setParams();
							}
							@Override
							public void onFailure( Method method, Throwable exception ) {
								Window.alert( exception.getMessage() );
							}
						} );
					}
				});
			}
		});
	}
	
	public Widget getWidget() {
		return dock;
	}
	
	Grid criteriaSelectionForm;
	
	protected void loadModel( JSONValue response ) {
		
		CodecChunkList codec = GWT.create( CodecChunkList.class );
		
		JChunkList clist = codec.decode( response );
		
		if( clist == null ) return;
		
		goals.clear();
		risks.clear();
		selection.clear();
		goalComparisons.clear();
		riskComparisons.clear();
		
		for( JChunkItem item : clist.outputs ) {
			if( item.getType() == EChunkType.GOAL ) {
				goals.add( item );
			}
			else if( item.getType() == EChunkType.RISK ) {
				risks.add( item );
			}
		}
		
		criteriaSelectionForm = new Grid( goals.size() +1, 2 );
		
		int n = 0;
		for( JChunkItem chunk : goals ) {
			CheckBox chk = new CheckBox();
			chk.addClickHandler( new ClickWrapper<String>( chunk.getId() ) {
				@Override
				public void onClick( ClickEvent event ) {
					CheckBox chk = (CheckBox)event.getSource();
					if( chk.getValue() )
						selection.add( getValue() );
					else
						selection.remove( getValue() );
					preferenceMatrix.loadValues( createComparisonList( selection ) );
				}
			});
			Anchor a = new Anchor( chunk.getLabel() );
			a.addClickHandler( new ClickWrapper<String>( chunk.getId() ) {
				@Override
				public void onClick( ClickEvent event ) {
					setSelectedGoal( getValue() );
				}} );
			criteriaSelectionForm.setWidget( n, 1, a );
			criteriaSelectionForm.setWidget( n, 0, chk );
			n++;
		}
		
		criteriaSelectionForm.setWidth( "100%" );
		
		
		DockPanel outputContainer = new DockPanel();
		
		Button run = new Button("Run");
		run.setStyleName("deleteButton");
		run.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				onRun();
			}
		});
		VerticalPanel space = new VerticalPanel();
		space.setHeight("40px");
		if (rasID.equals("")) {
			buttons.add(run);
		}
		else {
			run.setText("What-if");
			Button apply = new Button("Apply");
			apply.setStyleName("deleteButton");
			apply.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					onApply();
				}
			});
			Button cancel = new Button("Cancel");
			cancel.setStyleName("deleteButton");
			cancel.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					back();
				}
			});
			HorizontalPanel p = new HorizontalPanel();
			buttons.add(run);
			buttons.add(apply);
			buttons.add(cancel);
		}
		outputContainer.add( outputPanel, DockPanel.CENTER );
		
		
		Grid container = new Grid( 2, 2 );
		
		container.getColumnFormatter().setWidth( 0, "50%" );
		
		container.setWidget( 0, 0, criteriaSelectionForm );
		container.setWidget( 1, 0, riskForm );
		container.setWidget( 0, 1, preferenceMatrix );
		container.setWidget( 1, 1, outputContainer );
		
		container.getCellFormatter().setVerticalAlignment( 1, 0, HasVerticalAlignment.ALIGN_TOP );
		container.getCellFormatter().setVerticalAlignment( 1, 1, HasVerticalAlignment.ALIGN_TOP );
		
		container.setSize( "100%", "100%" );
		
		contentPanel.setWidget( container );
	}
	
	static interface ResultsDecoder extends JsonEncoderDecoder<JAHPResult> {}
	
	protected void setParams() {
		RiscossJsonClient.getMitigationActivityParameters(rasID, "AHP", new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				CodecAHPInput codec = GWT.create( CodecAHPInput.class );
				Log.println("Decoding");
				JAHPInput input = codec.decode(response);
				
				for (JAHPComparison ahp : input.goals) {
					goalComparisons.add(ahp);
					if (!selection.contains(ahp.getId1())) {
						selection.add(ahp.getId1());
						check(ahp.getId1());
					}
					if (!selection.contains(ahp.getId2())) {
						selection.add(ahp.getId2());
						check(ahp.getId2());
					}
					preferenceMatrix.insertRow(ahp);
				}

				int i = 0;
				for (String s : selection) {
					riskComparisons.put(s, (ArrayList<JAHPComparison>) input.risks.get(i));
					++i;
				}
			}
		});
	}
	
	protected void check(String id) {
		for (int i = 0; i < criteriaSelectionForm.getRowCount()-1; ++i) {
			String s = ((Anchor)criteriaSelectionForm.getWidget( i, 1 )).getText();
			if (s.equals(id)) {
				CheckBox c = ((CheckBox)criteriaSelectionForm.getWidget(i, 0));
				c.setChecked(true);
			};
		}
	}
	
	protected void onRun() {
		Log.println( "Creating ahp input" );
		JAHPInput ahp = new JAHPInput();
		
		ahp.setGoalCount( selection.size() );
		ahp.setRiskCount( risks.size() );
		
		Log.println( "Creating goal comparison list" );
		ahp.goals = preferenceMatrix.getValues();
		if( ahp.goals == null ) {
			ahp.goals = createComparisonList( selection );
		}
		
		Log.println( "Creating risk comparison matrix" );
		for( String id : selection ) {
			Log.println( "Creating risk comparison list for goal " + id );
			List<JAHPComparison> comp = riskComparisons.get( id );
			if( comp == null ) {
				Log.println( "Creating empty list for goal " + id );
				comp = createComparisonListFromChunks( risks );
			}
			ahp.risks.add( comp );
		}
		
		Log.println( "Creating codec" );
		CodecAHPInput codec = GWT.create( CodecAHPInput.class );
		
		Log.println( "Serializing" );
		String json = codec.encode( ahp ).toString();
		
		Log.println( "Calling" );
		RiscossJsonClient.runAHP( json, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				Log.println( "" + response );
//				CodecStringList codec = GWT.create( CodecStringList.class );
//				JStringList list = codec.decode( response );
//				outputPanel.setOutput( list.list );
				JsonEncoderDecoder<JAHPResult> codec = 
						GWT.create( ResultsDecoder.class );
				JAHPResult result = codec.decode( response );
				outputPanel.setOutput( result );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	protected void onApply() {
		Log.println( "Creating ahp input" );
		JAHPInput ahp = new JAHPInput();
		
		ahp.setGoalCount( selection.size() );
		ahp.setRiskCount( risks.size() );
		
		Log.println( "Creating goal comparison list" );
		ahp.goals = preferenceMatrix.getValues();
		if( ahp.goals == null ) {
			Window.alert("YYEY");
			ahp.goals = createComparisonList( selection );
		}
		
		Log.println( "Creating risk comparison matrix" );
		for( String id : selection ) {
			Log.println( "Creating risk comparison list for goal " + id );
			List<JAHPComparison> comp = riskComparisons.get( id );
			if( comp == null ) {
				Log.println( "Creating empty list for goal " + id );
				comp = createComparisonListFromChunks( risks );
			}
			ahp.risks.add( comp );
		}
		
		Log.println( "Creating codec" );
		CodecAHPInput codec = GWT.create( CodecAHPInput.class );
		
		Log.println( "Serializing" );
		String json = codec.encode( ahp ).toString();
		
		Log.println( "Calling" );
		RiscossJsonClient.applyMitigation(json, rasID, "AHP", new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				Log.println( "" + response );
				back();
			}
		});
	}
	
	protected void back() {
//		RiscossWebApp.loadPanel("riskanalysis.jsp?id=" + rasID);
//		FramePanel p = new FramePanel("riskanalysis.jsp?id=" + rasID);
//		RootPanel.get().clear();
//		RootPanel.get().add(p.getWidget());
//		p.activate();
		Window.Location.replace("riskanalysis.jsp?id=" + rasID);
	}
	
	protected void setSelectedGoal( String value ) {
		ArrayList<JAHPComparison> list = riskComparisons.get( value );
		if( list == null ) {
			list = createComparisonListFromChunks( risks );
			riskComparisons.put( value, list );
		}
		riskForm.loadValues( value, list );
	}
	
//	protected void onEvaluateClicked() {
//		new GoalEvaluationDialog( createComparisonList( selection ) ).show();
//	}
	
	public static ArrayList<JAHPComparison> createComparisonListFromChunks( Collection<JChunkItem> items ) {
		ArrayList<JAHPComparison> list = new ArrayList<JAHPComparison>();
		Set<String> visited = new HashSet<String>();
		for( JChunkItem r1 : items ) {
			for( JChunkItem r2 : items ) {
				if( r1.getId().equals( r2.getId() ) ) continue;
				if( visited.contains( r1.getId() + r2.getId() ) ) continue;
				if( visited.contains( r2.getId() + r1.getId() ) ) continue;
				JAHPComparison c = new JAHPComparison();
				c.setId1( r1.getId() );
				c.setId2( r2.getId() );
				c.value = 4;
				list.add( c );
				visited.add( r1.getId() + r2.getId() );
			}
		}
		return list;
	}	
	
	public static ArrayList<JAHPComparison> createComparisonList( Collection<String> id_list ) {
		ArrayList<JAHPComparison> comparisons = new ArrayList<JAHPComparison>();
		{	// Initializes a default 
			Set<String> visited = new HashSet<String>();
			for( String id1 : id_list ) {
				for( String id2 : id_list ) {
					if( id1.equals( id2 ) ) continue;
					if( visited.contains( id1 + id2 ) ) continue;
					if( visited.contains( id2 + id1 ) ) continue;
					JAHPComparison c = new JAHPComparison();
					c.setId1( id1 );
					c.setId2( id2 );
					c.value = 4;
//					Log.println( "Adding " + id1 + " - " + id2 );
					comparisons.add( c );
					visited.add( id1 + id2 );
				}
			}
		}
		return comparisons;
	}
	
}
