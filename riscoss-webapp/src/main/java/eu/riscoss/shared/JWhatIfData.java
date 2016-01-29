package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWhatIfData {
	
	public static class JWhatIfItem {
		public List<String> models = new ArrayList<>();
		public Map<String,String> values = new HashMap<>();
	}
	
	public Map<String,JWhatIfItem> items = new HashMap<>();
	
}
