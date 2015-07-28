package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JLayerContextualInfo {
	
	List<JLayerContextualInfoElement> 	contextInfoList = new ArrayList<>();
	
	public void addContextualInfoInteger(String name, String min, String max) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement();
		contextInfo.setName(name);
		contextInfo.setType("Integer");
		List<String> info = new ArrayList<>();
		info.add(min);
		info.add(max);
		contextInfo.setInfo(info);
		
		contextInfoList.add(contextInfo);
		
	}
	
	public void addContextualInfoBoolean(String name) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement();
		contextInfo.setName(name);
		contextInfo.setType("Boolean");
		
		contextInfoList.add(contextInfo);
		
	}
	
	public void addContextualInfoCalendar(String name) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement();
		contextInfo.setName(name);
		contextInfo.setType("Date");
		
		contextInfoList.add(contextInfo);
		
	}
	
	public void addContextualInfoList(String name, ArrayList<String> elements) {
		
		JLayerContextualInfoElement contextInfo = new JLayerContextualInfoElement();
		contextInfo.setName(name);
		contextInfo.setType("List");
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
