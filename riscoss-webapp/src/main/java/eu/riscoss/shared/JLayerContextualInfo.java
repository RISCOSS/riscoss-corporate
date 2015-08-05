package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JLayerContextualInfo {
	
	List<JLayerContextualInfoElement> 	contextInfoList = new ArrayList<>();
	
	public void addContextualInfoInteger(String id, String name, String description, String defval, String min, String max) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement(id, name, description, defval, "Integer");
		List<String> info = new ArrayList<>();
		info.add(min);
		info.add(max);
		contextInfo.setInfo(info);
		
		contextInfoList.add(contextInfo);
		
	}
	
	public void addContextualInfoBoolean(String id, String name, String description, String defval) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement(id, name, description, defval, "Boolean");
		
		contextInfoList.add(contextInfo);
		
	}
	
	public void addContextualInfoCalendar(String id, String name, String description, String defval) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement(id, name, description, defval, "Date");

		contextInfoList.add(contextInfo);
		
	}
	
	public void addContextualInfoList(String id, String name, String description, String defval, ArrayList<String> elements) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement(id, name, description, defval, "List");
		contextInfo.setInfo(elements);
		
		contextInfoList.add(contextInfo);
		
	}
	
	public JLayerContextualInfoElement getContextualInfoElement(int index) {
		return this.contextInfoList.get(index);
	}
	
	public void deleteContextualInfoElement(int index) {
		this.contextInfoList.remove(index);
	}
	
	public int getSize() {
		return contextInfoList.size();
	}
	
}
