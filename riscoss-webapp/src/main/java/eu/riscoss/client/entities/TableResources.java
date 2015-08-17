package eu.riscoss.client.entities;

import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.user.cellview.client.CellTable;

public interface TableResources extends CellTable.Resources {

    /**
       * The styles applied to the table.
       */
    interface TableStyle extends CellTable.Style {
    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "CellTable.css" })
    TableStyle cellTableStyle();

}
