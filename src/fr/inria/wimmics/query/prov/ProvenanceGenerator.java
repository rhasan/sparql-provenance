package fr.inria.wimmics.query.prov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.ResultBinding;

public class ProvenanceGenerator {
	
	//Map<String, ArrayList<Triple>> bgpTriples = new HashMap<String, ArrayList<Triple>>();
	Dataset mainDataset = null;

	boolean hasUnion = false, hasJoin = false, hasOptional = false;	
	
	Model mainModel = null;
	String mainEndpoint = null;
	
	boolean hasModel = false;
	boolean hasEndpoint = false;
	boolean hasDataset = false;
	
	public ProvenanceGenerator(Model model) {
		mainModel = model;
		hasModel = true;
	}
	
	public ProvenanceGenerator(String endpoint) {
		mainEndpoint = endpoint;
		hasEndpoint = true;
	}
	
	public ProvenanceGenerator(Dataset dataset) {
		mainDataset = dataset;
		hasDataset = true;
	}	

	private QueryExecution getQueryExecutionEngine(Query query) {
		if(hasModel) {
			return QueryExecutionFactory.create(query, mainModel);
		} else if(hasEndpoint) {
			return QueryExecutionFactory.sparqlService(mainEndpoint,query);
		} else if(hasDataset) {
			return QueryExecutionFactory.create(query, mainDataset) ;
		}
		return null;

	}
	
	public ArrayList<ArrayList<Triple>> generateProvenance(Query query, ResultBinding result) {
		
		//bgpTriples = new HashMap<String, ArrayList<Triple>>();
		
		Query iQuery = IntermediateBindingExtractor.buildIntermediateQuery(query, result);
		//QueryExecution qe = QueryExecutionFactory.create(iQuery, model);
		QueryExecution qe = getQueryExecutionEngine(iQuery);
		
		
		ResultSet rset = qe.execSelect();
		
		ArrayList<ArrayList<Triple>> whyProv = new ArrayList<ArrayList<Triple>>();

		
		while(rset.hasNext()) {

			QuerySolution sln = rset.next();
			System.out.println("Solution"+sln);
			
			
			//Map<String,String> resultMap = new HashMap<String, String>();
			
			
			Iterator<String> it = sln.varNames();
			
			while(it.hasNext()) {
				String var = "?"+it.next();
				String val = sln.get(var).toString();
				//resultMap.put(var, val);
				//System.out.print(var+": "+val+"\t");
				
				if(sln.get(var).isLiteral()) {
					Literal l = (Literal)sln.get(var);
					//System.out.print("Literal: "+l+"\t");
				}
			}
			
			//System.out.println(resultMap);
			Map<String, ArrayList<Triple>> bgpTriples = extractTriplesFromBGP(query, sln);
			
			
//			for(Entry<String, ArrayList<Triple>> entry:bgpTriples.entrySet()) {
//				
//				System.out.println("BGP: ["+entry.getKey()+"]");
//				
//				if(entry.getValue().isEmpty())System.out.println("No triples extracted");
//				int i = 0;
//				for(Triple t:entry.getValue()) {
//					if(i != 0) System.out.print(" , ");
//					System.out.print(t);
//					
//					i++;
//				}
//				System.out.println();
//			}
//			
			ArrayList<Triple> provTriples = new ArrayList<Triple>();
			if(bgpTriples.isEmpty() == false) {
				if(hasJoin || hasUnion || hasOptional) {
					ArrayList<Triple> t = processOperators( query, sln, bgpTriples);
					provTriples.addAll(t);
				}
				else {
					for(Entry<String, ArrayList<Triple>> entry:bgpTriples.entrySet()) {
						provTriples.addAll(entry.getValue());
					}
					
				}
				
			}
			
			if(provTriples.isEmpty() == false)
				whyProv.add(provTriples);
			
		}
		return whyProv;
	}
	
	
	private ArrayList<Triple> processOperators( Query query, QuerySolution resultMap, Map<String, ArrayList<Triple>> bgpTriples) {
		
		Op op = Algebra.compile(query);
		ArrayList<Triple> provTriples = walkAlgebraTree4Operator(op,resultMap, bgpTriples);
		return provTriples;
	}


	private ArrayList<Triple> walkAlgebraTree4Operator(Op op,
			final QuerySolution resultMap,
			final Map<String, ArrayList<Triple>> bgpTriples) {
		
		
		final ArrayList<Triple> provTriples = new ArrayList<Triple>();
		final Map<String, Boolean> bgpProcessed = new HashMap<String, Boolean>();
		
		for(String entryBgp:bgpTriples.keySet()) {
			
			bgpProcessed.put(entryBgp, false);
		}
		//System.out.println("###################: "+bgpProcessed);
		
		
		
		OpWalker.walk(op, new OpVisitorBase() {

			public void visit(OpUnion opUnion) {
				
				if(opUnion.getLeft() instanceof OpBGP) {
					OpBGP bgpLeft = (OpBGP) opUnion.getLeft();
					//System.out.println("opUnion Left "+bgpLeft);
					
					//List<Triple> newTriples = replaceVariablesByValues(model,bgpLeft,resultMap);
					
					if(bgpProcessed.containsKey(bgpLeft.getPattern().toString()) && bgpProcessed.get(bgpLeft.getPattern().toString()) == false ) {
						//System.out.println("taking Left ");
						provTriples.addAll(bgpTriples.get(bgpLeft.getPattern().toString()));
						bgpProcessed.put(bgpLeft.getPattern().toString(),true);
					}

				}
				if(opUnion.getRight() instanceof OpBGP) {
					OpBGP bgpRight = (OpBGP) opUnion.getRight();
					//System.out.println("opUnion Right "+bgpRight);

					if(bgpProcessed.containsKey(bgpRight.getPattern().toString()) && bgpProcessed.get(bgpRight.getPattern().toString()) == false ) {
						//System.out.println("taking Right");
						provTriples.addAll(bgpTriples.get(bgpRight.getPattern().toString()));
						bgpProcessed.put(bgpRight.getPattern().toString(),true);
					}

				
				}
					

			}

			public void visit(OpLeftJoin opLeftJoin) {
				
				if(opLeftJoin.getLeft() instanceof OpBGP) {
					OpBGP bgpLeft = (OpBGP) opLeftJoin.getLeft();
					//System.out.println("Left "+bgpLeft);
					
					//List<Triple> newTriples = replaceVariablesByValues(model,bgpLeft,resultMap);
					
					if(bgpProcessed.containsKey(bgpLeft.getPattern().toString()) && bgpProcessed.get(bgpLeft.getPattern().toString()) == false ) {
						provTriples.addAll(bgpTriples.get(bgpLeft.getPattern().toString()));
						bgpProcessed.put(bgpLeft.getPattern().toString(),true);
					}

				}
				if(opLeftJoin.getRight() instanceof OpBGP) {
					OpBGP bgpRight = (OpBGP) opLeftJoin.getRight();
					//System.out.println("Right "+bgpRight);

					if(bgpProcessed.containsKey(bgpRight.getPattern().toString()) && bgpProcessed.get(bgpRight.getPattern().toString()) == false ) {
						provTriples.addAll(bgpTriples.get(bgpRight.getPattern().toString()));
						bgpProcessed.put(bgpRight.getPattern().toString(),true);
					}

				
				}
					

			}
			
			public void visit(OpJoin opJoin) {
				
				if(opJoin.getLeft() instanceof OpBGP) {
					OpBGP bgpLeft = (OpBGP) opJoin.getLeft();
					//System.out.println("Left "+bgpLeft);
					
					//List<Triple> newTriples = replaceVariablesByValues(model,bgpLeft,resultMap);
					
					if(bgpProcessed.containsKey(bgpLeft.getPattern().toString()) && bgpProcessed.get(bgpLeft.getPattern().toString()) == false ) {
						provTriples.addAll(bgpTriples.get(bgpLeft.getPattern().toString()));
						bgpProcessed.put(bgpLeft.getPattern().toString(),true);
					}

				}
				if(opJoin.getRight() instanceof OpBGP) {
					OpBGP bgpRight = (OpBGP) opJoin.getRight();
					//System.out.println("Right "+bgpRight);

					if(bgpProcessed.containsKey(bgpRight.getPattern().toString()) && bgpProcessed.get(bgpRight.getPattern().toString()) == false ) {
						provTriples.addAll(bgpTriples.get(bgpRight.getPattern().toString()));
						bgpProcessed.put(bgpRight.getPattern().toString(),true);
					}

				
				}
					

			}			
			
			
		});
		return provTriples;
		
	}


	public Map<String, ArrayList<Triple>> extractTriplesFromBGP(Query query, QuerySolution resultMap) {
		
		Op op = Algebra.compile(query) ;
		
		//System.out.println(op);
		
		Map<String, ArrayList<Triple>> bgpTriples = walkAlgebraTree4BGP(op,resultMap);
		return bgpTriples;
	}
	
	private Map<String, ArrayList<Triple>> walkAlgebraTree4BGP(Op op, final QuerySolution resultMap) {
		//final List<Triple> provTriples = new ArrayList<Triple>();
		final Map<String, ArrayList<Triple>> bgpTriples = new HashMap<String, ArrayList<Triple>>();
		OpWalker.walk(op, new OpVisitorBase() {
			
			public void visit(OpBGP opBGP) {
				//System.out.println("bgp ");
				
				//System.out.println("Pattern "+opBGP.getPattern());
				
				ArrayList<Triple> newTriples = replaceVariablesByValues(opBGP,resultMap);
				if(newTriples.isEmpty() == false) {
					bgpTriples.put(opBGP.getPattern().toString(), newTriples);
				}
//				provTriples.addAll(newTriples);				
				
			}
			
			public void visit(OpJoin opJoin) {
				//System.out.println("join ");
				hasJoin = true;
				
			}
			
			public void visit(OpLeftJoin opLeftJoin)  {
				hasOptional = true;
			}
			
			public void visit(OpUnion opUnion) {
				//System.out.println("union ");
				hasUnion = true;
//				if(opUnion.getLeft() instanceof OpBGP) {
//					OpBGP bgpLeft = (OpBGP) opUnion.getLeft();
//					//System.out.println("Left "+bgpLeft);
//					
//					List<Triple> newTriples = replaceVariablesByValues(model,bgpLeft,resultMap);
//					provTriples.addAll(newTriples);
//
//				}
//				if(opUnion.getRight() instanceof OpBGP) {
//					OpBGP bgpRight = (OpBGP) opUnion.getRight();
//					//System.out.println("Right "+bgpRight);
//
//					List<Triple> newTriples = replaceVariablesByValues(model,bgpRight,resultMap);
//					provTriples.addAll(newTriples);
//
//				
//				}
					

			}

			
		});

		return bgpTriples;
	}	
	
	private ArrayList<Triple> replaceVariablesByValues(OpBGP bgp, QuerySolution resultMap) {
		
		

		if(resultContainsAllVariable(bgp,resultMap)) {
			ArrayList<Triple> matchedTriples = new ArrayList<Triple>();
			if(allTriplesExist(bgp,matchedTriples,resultMap)) {
				return matchedTriples;
			}
		}
		return new ArrayList<Triple>(); //empty
	}

	private boolean allTriplesExist(OpBGP bgp,
			List<Triple> matchedTriples, QuerySolution resultMap) {
		
		for(Triple triple:bgp.getPattern().getList()) {
			
			Triple newTriple = replaceVariablesByValues(triple, resultMap);
			Query ask = JenaExplanationUtils.getAskQuery(newTriple);
			boolean askAnswer = false;
			if(hasEndpoint) {
				askAnswer = JenaExplanationUtils.sendAskQuery(ask, mainEndpoint);
			} else if(hasModel) {
				askAnswer = JenaExplanationUtils.sendAskQuery(ask, mainModel);
			} else if(hasDataset) {
				askAnswer = JenaExplanationUtils.sendAskQuery(ask, mainDataset);
			}
			
			
			//System.out.println("Ask Query:"+ask);
			//System.out.println("Answer: "+askAnswer);
			
			if(askAnswer){
				matchedTriples.add(newTriple);
			} else {
				return false;
			}
		}
		
		return true;
	}

	private Triple replaceVariablesByValues(Triple triple,
			QuerySolution resultMap) {
		
		Node subject = triple.getSubject();
		if(triple.getSubject().isVariable()) {
			//String name = resultMap.get(triple.getSubject().toString());
			//subject = JenaExplanationUtils.createNode(name);
			subject = resultMap.get(triple.getSubject().toString()).asNode();
			
			
			
		} 
		
		Node predicate = triple.getPredicate();
		if(triple.getPredicate().isVariable()) {
			//String name = resultMap.get(triple.getPredicate().toString());
			//predicate = JenaExplanationUtils.createNode(name);
			predicate = resultMap.get(triple.getPredicate().toString()).asNode();
		}
		
		Node object = triple.getObject();
		if(triple.getObject().isVariable()) {
			//String name = resultMap.get(triple.getObject().toString());
			//object = JenaExplanationUtils.createNode(name);
			object = resultMap.get(triple.getObject().toString()).asNode();
		} 

		Triple t = new Triple(subject, predicate, object);
		
		return t;
	}

	private boolean resultContainsAllVariable(OpBGP bgp,
			QuerySolution resultMap) {
		
		for(Triple triple:bgp.getPattern().getList()) {
			if(triple.getSubject().isVariable()) {
				//System.out.println(triple.getSubject().toString());
				String var = triple.getSubject().toString();
				if(resultMap.contains(var)==false)
					return false;
			}
			if(triple.getPredicate().isVariable()) {
				//System.out.println(triple.getPredicate());
				String var = triple.getPredicate().toString();
				if(resultMap.contains(var)==false)
					return false;
				
			}					
			if(triple.getObject().isVariable()) {
				String var = triple.getObject().toString();
				if(resultMap.contains(var)==false)
					return false;
			}					
		}
		
		return true;
	}

}
