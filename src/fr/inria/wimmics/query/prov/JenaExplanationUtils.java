package fr.inria.wimmics.query.prov;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.iri.IRIFactory;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.Template;



public class JenaExplanationUtils {

	public static Node createNode(String name) {
		if(isValidIRI(name)) {
			return NodeFactory.createURI(name);
		}
		return NodeFactory.createLiteral(name);
		
	}
	
	public static boolean isBlank(String name) {
		if(name.startsWith("_:")) return true;
		return false;
	}
	public static String getBlankNodeId(String name) {
		return name.substring(2);
	}
	
	public static boolean isValidIRI(String iri) {
		
		try {
			IRIFactory.jenaImplementation().construct(iri);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	public static ResultBinding convertToResultBinding(Model model, Map<String,String> m) {
		List<String> vars = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		for(Entry<String, String> entry:m.entrySet()) {
			//log.info(entry.getKey()+"->"+entry.getValue());
			vars.add(entry.getKey());
			values.add(entry.getValue());
			
		}		
		return convertToResultBinding(model, vars, values);
	}
	public static ResultBinding convertToResultBinding(Model model, List<String> vars, List<String> values) {
		
		BindingHashMap binding = new BindingHashMap();
		
		List<Var> varList = Var.varList(vars);
		
		
		for(int i=0;i<vars.size();i++) {
			Node val = null;
			String valStr = values.get(i);
			if(isBlank(valStr)) {
				AnonId id = new AnonId(getBlankNodeId(valStr));
				val = NodeFactory.createAnon(id);
			} else {
				if(isValidIRI(valStr)) {
					val = NodeFactory.createURI(valStr);
				} else {
					val = NodeFactory.createLiteral(valStr);
				}
			}

			Var var = varList.get(i);
			binding.add(var, val);
		}
		
		ResultBinding resultBinding = new ResultBinding(model, binding);
		
		return resultBinding;
	}
	
	
	
	public static Query getAskQuery(Triple triple) {
		
		ElementPathBlock block = new ElementPathBlock();
		block.addTriple(triple);
		
		
		ElementGroup body = new ElementGroup();
		body.addElement(block);
		
		
		
		Query askQuery = QueryFactory.make();
		askQuery.setQueryAskType();
		askQuery.setQueryPattern(body);	
		//System.out.println(askQuery);
		
		return askQuery;
	}
	
	
	public static boolean sendAskQuery(Query query, String endpoint) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query);
		
		return qe.execAsk();
	}
	
	public static boolean sendAskQuery(Query query, Model model) {
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		
		return qe.execAsk();
	}
	
	public static boolean sendAskQuery(Query query, Dataset dataset) {
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		
		return qe.execAsk();
	}
	

}
