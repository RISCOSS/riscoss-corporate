package eu.riscoss.agent.usecases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.Workflow;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureEntityExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructure;
import eu.riscoss.agent.tasks.PostRiskData;
import eu.riscoss.agent.tasks.SelectDomain;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.fbk.io.XmlWriter;
import eu.riscoss.fbk.language.Model;
import eu.riscoss.fbk.language.Program;
import eu.riscoss.fbk.language.Proposition;
import eu.riscoss.fbk.language.Relation;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.shared.JAHPComparison;
import eu.riscoss.shared.JAHPInput;
import eu.riscoss.shared.JCBRankInput;
import eu.riscoss.shared.JGAInput;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.JRiskAnalysisResult;
import eu.riscoss.shared.JRiskAnalysisResultItem;

public class UCMA_GA implements UseCase {

	Gson gson = new Gson();
	
	public static void main( String[] args ) throws Exception {
		new UCMA_GA().run( new RiscossRESTClient() );
	}
	
	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		String domain = "MitigationActivities";
		
		rest.login( "admin", "admin" );
		
		Workflow w = new Workflow( rest );
		
		w.execute( new EnsureDomainExistence( domain ) );
		w.execute( new SelectDomain( domain ) );
		w.execute( new EnsureLayerStructure( domain, new String[] { "Project", "OSSComponent" } ) );
		
		w.execute( new EnsureEntityExistence( "Project", "p1" ) );
		w.execute( new EnsureEntityExistence( "OSSComponent", "c1" ) );
		w.execute( new EnsureEntityExistence( "OSSComponent", "c2" ) );
		
		w.execute( new PostRiskData( new RiskData(
				"i1", "c1", new Date(), RiskDataType.NUMBER, "0.5" ) ) );
		
		{
			Program program = new Program();
			Model model = program.getModel();
			model.addProposition( new Proposition( "indicator", "i1" ).withProperty( "input", "true" ).withProperty( "datatype", "real" ) );
			model.addProposition( new Proposition( "indicator", "i2" ).withProperty( "input", "true" ).withProperty( "datatype", "real" ) );
			model.addProposition( new Proposition( "situation", "s1" ) );
			model.addProposition( new Proposition( "situation", "s2" ) );
			model.addProposition( new Proposition( "event", "e1" ).withProperty( "output", "true" ) );
			model.addProposition( new Proposition( "event", "e2" ).withProperty( "output", "true" ) );
			model.addProposition( new Proposition( "goal", "g1" ).withProperty( "output", "true" ) );
			model.addProposition( new Proposition( "goal", "g2" ).withProperty( "output", "true" ) );
			model.addRelation( new Relation( "indicate" )
					.withSource( model.getProposition( "i1" ) )
					.withTarget( model.getProposition( "s1" ) ) );
			model.addRelation( new Relation( "indicate" )
					.withSource( model.getProposition( "i2" ) )
					.withTarget( model.getProposition( "s2" ) ) );
			model.addRelation( new Relation( "expose" )
					.withSource( model.getProposition( "s1" ) )
					.withTarget( model.getProposition( "e1" ) ) );
			model.addRelation( new Relation( "expose" )
					.withSource( model.getProposition( "s2" ) )
					.withTarget( model.getProposition( "e2" ) ) );
			model.addRelation( new Relation( "impact" )
					.withSource( model.getProposition( "e1" ) )
					.withTarget( model.getProposition( "g1" ) ) );
			model.addRelation( new Relation( "impact" )
					.withSource( model.getProposition( "e1" ) )
					.withTarget( model.getProposition( "g2" ) ) );
			model.addRelation( new Relation( "impact" )
					.withSource( model.getProposition( "e2" ) )
					.withTarget( model.getProposition( "g1" ) ) );
			model.addRelation( new Relation( "impact" )
					.withSource( model.getProposition( "e2" ) )
					.withTarget( model.getProposition( "g2" ) ) );
			String blob = new XmlWriter().generateXml( program ).asString();
			
			rest.domain( domain ).models().delete( "testModel.xml" );
			rest.domain( domain ).models().upload( "testModel.xml", blob );
			
			rest.domain( domain ).rcs().create( "test-rc1" );
			rest.domain( domain ).rcs().rc( "test-rc1" ).associate( "OSSComponent", "testModel.xml" );
		}
		
		{	// Create and execute a risk analysis session
			Gson gson = new Gson();
			JRASInfo ras = gson.fromJson( rest.domain( domain ).analysis().createSession( "c1", "test-rc1" ), JRASInfo.class );
			
			String ret = rest.domain( domain ).analysis().session( ras.getId() ).execute();
			
			System.out.println( "" + ret );
			
			JRiskAnalysisResult result = gson.fromJson( ret, JRiskAnalysisResult.class );
			
			for( JRiskAnalysisResultItem r : result.results ) {
				System.out.println( r.id + " = " + r.value );
			}
			
			JGAInput input = new JGAInput();
			
			input.setEnabledIndicators( "i1" );
			input.addObjective( "g1", new Evidence( 0.2, 0 ).pack() );
			input.setProblen( "inputOptimization" );
			
			ret = rest.domain( domain ).analysis().session( ras.getId() ).applyMitigationTechnique( "GA", new Gson().toJson( input ) );
			
			result = gson.fromJson( ret, JRiskAnalysisResult.class );
			
			for( JRiskAnalysisResultItem r : result.results ) {
				System.out.println( r.id + " = " + r.value );
			}
			
			System.out.println( rest.domain( domain ).analysis().session( ras.getId() ).getMitigationTechniqueParams( "CBRank" ) );
			
		}
		
		
		rest.logout();
		
	}
	
}
