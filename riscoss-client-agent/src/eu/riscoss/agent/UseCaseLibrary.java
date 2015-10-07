package eu.riscoss.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;


public class UseCaseLibrary {
	
	static List<UseCase> usecases = new ArrayList<>();
	
	static {
		
		Reflections reflections = new Reflections( UseCase.class.getPackage().getName() );
		
		Set<Class<? extends UseCase>> subTypes = reflections.getSubTypesOf(UseCase.class);
		
		for( Class<? extends UseCase> cls : subTypes ) {
			try {
				UseCase uc = (UseCase)cls.newInstance();
				usecases.add( uc );
			}
			catch( Exception ex ) {}
		}
		
	}
	
	private static UseCaseLibrary instance = new UseCaseLibrary();
	
	public static UseCaseLibrary get() {
		return instance;
	}
	
	public Iterable<UseCase> useCases() {
		return usecases;
	}

}
