package fr.inria.wimmics.query.prov;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.util.FileManager;

public class ProvExperiment {
	
	public static void main(String[] args) {
		ProvExperiment e = new ProvExperiment();
		/*String queryStr = "PREFIX dc10:  <http://purl.org/dc/elements/1.0/>"
				+ "PREFIX dc11:  <http://purl.org/dc/elements/1.1/>"
				
				+ "SELECT ?title ?author"
				+ "WHERE  { { ?book dc10:title ?title .  ?book dc10:creator ?author }"
				+ "UNION"
				+ "{ ?book dc11:title ?title .  ?book dc11:creator ?author }"
				+ "}";*/
		//e.extractTriplePatterns(queryStr);
		
		String queryStr = "PREFIX : <http://ex.org/> "
				+ " SELECT ?name ?email "
				+ " WHERE {"
				+ "		?course :courseType :underGrad."
				+ "		?prof :course ?course.  "
				+ "		?prof :email ?email."
				+ "		?prof :name ?name "
				+ "}";
		Query query = QueryFactory.create(queryStr);
		System.out.println(query);
		Model model = ModelFactory.createDefaultModel();
		InputStream in= FileManager.get().open("files/why_prov.ttl");
		model.read(in,"","TURTLE");
		ProvenanceGenerator pg = new ProvenanceGenerator(model);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet rset = qe.execSelect();
		ResultBinding result=null;
		while(rset.hasNext()) {
			result = (ResultBinding)rset.next();
			ArrayList<ArrayList<Triple>> whyProv = pg.generateProvenance(query, result);
			System.out.println("Result: "+result);
			//break;
			for(ArrayList<Triple> derivation:whyProv) {
				System.out.println("derivation:");
				int i = 0;
				for(Triple t:derivation) {
					if(i != 0) System.out.print(" , ");
					System.out.print(t);
					
					i++;
				}
				System.out.println();
			}
			System.out.println();
			//break;
			
		}
		
		
		
		
	}
}
