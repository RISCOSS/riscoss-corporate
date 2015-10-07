package eu.riscoss.agent;


public interface UseCase {

	void run( RiscossRESTClient client ) throws Exception;
	
}
