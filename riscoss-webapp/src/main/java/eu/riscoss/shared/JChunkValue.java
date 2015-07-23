package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeName;

@JsonSubTypes(
		{	@Type(value=JChunkValue.JEvidence.class, name="E"), 
			@Type(value=JChunkValue.JDistribution.class, name="D"),
			@Type(value=JChunkValue.JInteger.class, name="I"),
			@Type(value=JChunkValue.JReal.class, name="R"),
			@Type(value=JChunkValue.JNaN.class, name="N")})
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="@class")
public abstract class JChunkValue {
	
	public abstract EChunkDataType getDataType();
	
	@JsonTypeName("E")
	public static class JEvidence extends JChunkValue {
		public double p, m;
		public EChunkDataType getDataType() {
			return EChunkDataType.EVIDENCE;
		}
	}
	
	@JsonTypeName("D")
	public static class JDistribution extends JChunkValue {
		public List<Double> values = new ArrayList<Double>();
		public EChunkDataType getDataType() {
			return EChunkDataType.DISTRIBUTION;
		}
	}
	
	@JsonTypeName("R")
	public static class JReal extends JChunkValue {
		public double min, max;
		public double value;
		public EChunkDataType getDataType() {
			return EChunkDataType.REAL;
		}
	}
	
	@JsonTypeName("I")
	public static class JInteger extends JChunkValue {
		public int min, max;
		public int value;
		public EChunkDataType getDataType() {
			return EChunkDataType.INTEGER;
		}
	}
	
	@JsonTypeName("N")
	public static class JNaN extends JChunkValue {
		public EChunkDataType getDataType() {
			return EChunkDataType.NaN;
		}
	}
	
}
