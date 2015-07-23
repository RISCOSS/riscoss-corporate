package eu.riscoss.client.rma;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.DialogBox;

import eu.riscoss.shared.JAHPComparison;

public class GoalEvaluationDialog {
	
	ArrayList<JAHPComparison> comparisons;
	
	DialogBox dialog;
	
	public GoalEvaluationDialog( ArrayList<JAHPComparison> comparisons ) {
		this.comparisons = comparisons;
	}

	public void show() {
		
		dialog = new DialogBox( true );
		
		dialog.setTitle( "Set goal preferences" );
		
		PreferenceMatrix matrix = new PreferenceMatrix( comparisons );
		
		dialog.setWidget( matrix );
		
		dialog.show();
	}

}
