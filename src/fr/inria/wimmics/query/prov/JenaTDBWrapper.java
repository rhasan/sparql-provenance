package fr.inria.wimmics.query.prov;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryCancelledException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;


public class JenaTDBWrapper {

	Dataset dataset = null;
	long timeOutMiliSeconds;
	
	public JenaTDBWrapper(long timeOutMiliSeconds) {
		this.timeOutMiliSeconds = timeOutMiliSeconds;
	}
	
	public void loadTDBDataset(String tdbDircectory) {
		
		Location loc = new Location(tdbDircectory);
		TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
		dataset = TDBFactory.createDataset(loc) ;
		
	}
	public Dataset getDataset() {
		return dataset;
	}		
	
	public QueryMetaData executeSelectTimeout(String queryString) throws QueryCancelledException {
		//TDB.sync(dataset ) ; //flushing all the caches
		
		//System.out.println("Executing:"+queryString);
	    Query query = QueryFactory.create(queryString) ;
	    //System.out.println("Executing:"+query);
	    
	    QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
	    qexec.setTimeout(timeOutMiliSeconds, TimeUnit.MILLISECONDS,timeOutMiliSeconds, TimeUnit.MILLISECONDS);
	    
	    //qexec.getContext().set(TDB.symUnionDefaultGraph, true) ;

	    
		Stopwatch watch = Stopwatch.createStarted();
		watch.reset();

		watch.start();
	    ResultSet rset = qexec.execSelect() ;
	    List<ResultBinding> results = new ArrayList<ResultBinding>();
	    
	    ResultBinding result=null;
		int i = 0;
		while(rset.hasNext()) {
			result = (ResultBinding)rset.next();
			results.add(result);
			i++;
		}	    
	    
		
	    long elapsed = watch.elapsed(TimeUnit.MILLISECONDS);

	    QueryMetaData metrics = new QueryMetaData();
	    metrics.queryExecutionTime = elapsed;
		
		metrics.results = results;
		metrics.numberOfRecords = i;
	    metrics.queryStr = queryString;

	    
        //System.out.println("Number of records: "+metrics.numberOfRecords);
        
        //System.out.println("Finished!");	    
	    return metrics;
	}	
	
	public QueryMetaData executeSelectTimeoutWithoutCaching(String queryString) throws QueryCancelledException {
		TDB.sync(dataset ) ; //flushing all the caches
		
		
	    return executeSelectTimeout(queryString);
	}
	
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		//String tdbDircectory = "/Users/hrakebul/Documents/code/sw/jena_and_fuseki/tdb-test";
		String tdbDircectory = "/Users/hrakebul/Documents/code/sw/jena_and_fuseki/bm_100_ng";
		
		//String sparqlQueryString = "SELECT (count(*) AS ?count) { <http://dbpedia.org/resource/Berlin> ?p ?o }" ;
		//String sparqlQueryString = "SELECT (count(*) AS ?count) { ?s ?p ?o }" ;
		
		//String sparqlQueryString = "SELECT ?var2 WHERE { ?var3 <http://xmlns.com/foaf/0.1/homepage> ?var2 . ?var3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.opengis.net/gml/_Feature> . }";
		//String sparqlQueryString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX space: <http://purl.org/net/schemas/space/> PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> PREFIX dbpedia-prop: <http://dbpedia.org/property/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  SELECT ?var4 ?var8 ?var10 WHERE { ?var5 dbpedia-owl:thumbnail ?var4 . ?var5 rdf:type dbpedia-owl:Person . ?var5 rdfs:label \"Shimaki Kensaku\"@en . ?var5 foaf:page ?var8 . OPTIONAL { ?var5 foaf:homepage ?var10 .} . }";
		String sparqlQueryString = "SELECT * WHERE { <http://dbpedia.org/resource/Berlin> ?p ?o }";
		JenaTDBWrapper tdbTest = new JenaTDBWrapper(100);
		tdbTest.loadTDBDataset(tdbDircectory);
		
		//tdbTest.queryTDBDataset(sparqlQueryString);
		
		try {
			System.out.println(tdbTest.executeSelectTimeout(sparqlQueryString).queryExecutionTime);
			//tdbTest.executeSelectWithoutCaching(sparqlQueryString);
			//tdbTest.executeSelectWithoutCaching(sparqlQueryString);
		
        } catch (Exception e) {
        	System.out.println(e);
    	} /*catch (NoResultException e) {

    		System.out.println("No record returned.");
		}	*/
		//tdbTest.dataset.close();
		TDBFactory.release(tdbTest.dataset);
		System.out.println("Dataset closed.");
	}	
	
}
