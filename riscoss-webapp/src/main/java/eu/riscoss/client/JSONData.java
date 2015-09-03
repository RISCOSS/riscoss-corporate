package eu.riscoss.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONValue;

import eu.riscoss.client.RiscossCall.Argument;

public class JSONData {

	protected String domain = null;
	protected String token = "";
	protected String service = "";
	protected String fx = "";

	// TODO use value!!
	protected JSONValue value;
	protected List<Argument> args = new ArrayList<Argument>();

}
