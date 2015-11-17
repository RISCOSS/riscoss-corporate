package eu.riscoss.agent.services;

import java.io.File;

import eu.riscoss.agent.RiscossRESTClient;

public class ModelsService extends RESTService {

	public ModelsService(RiscossRESTClient rest) {
		super(rest);
	}
	
	public void upload( File file ) {
		rest.post( "models/" + getDomain() + "/" + file.getName() + "/upload" ).send( file );
	}
	
	public void upload( String fileName, String fileContent ) {
		rest.post( "models/" + getDomain() + "/" + fileName + "/upload" ).send( fileContent );
	}
	
}
