package fr.inria.wimmics.query.prov;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;





import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.util.FileManager;

public class DBPSBExperiments {
	
	final static boolean PRINT_DERIVATIONS = false;
	final static int QUERY_REPETITION = 5;
	final static int RESULT_LIMIT = 4000;
	
	final static long QUERY_TIMEOUT_MILISEC = 10*60000;//10*60000; //10 minutes
	final static String tdbDircectory = "/Users/hrakebul/Documents/code/sw/jena_and_fuseki/bm_100_ng";
	JenaTDBWrapper tdbDbpedia;
	
	
	public DBPSBExperiments() {
		tdbDbpedia = new JenaTDBWrapper(QUERY_TIMEOUT_MILISEC);
		tdbDbpedia.loadTDBDataset(tdbDircectory);
	}
	
	public QueryMetaData executeQuery(String queryString) {

		/*
		Query query = QueryFactory.create(queryString);
		List<ResultBinding> results = new ArrayList<ResultBinding>();
		System.out.println(query);
		
		Stopwatch watch = Stopwatch.createStarted();
		QueryExecution qe = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet rset = qe.execSelect();
		ResultBinding result=null;
		int i = 0;
		while(rset.hasNext()) {
			result = (ResultBinding)rset.next();
			results.add(result);
			i++;
		}
		
		QueryMetaData meta = new QueryMetaData();
		
		meta.queryExecutionTime = watch.elapsed(TimeUnit.MILLISECONDS);
		meta.queryStr = queryString;
		meta.numberOfRecords = i;
		meta.results = results;
		return meta;*/
		
		return tdbDbpedia.executeSelectTimeout(queryString);
		
	
	}
	
	public ArrayList<ArrayList<Triple>> generateProvenance(String queryString,  QueryMetaData meta, ResultBinding result) {
		
		Query query = QueryFactory.create(queryString);
		//ProvenanceGenerator pg = new ProvenanceGenerator(endpoint);
		ProvenanceGenerator pg = new ProvenanceGenerator(tdbDbpedia.getDataset());
		//if(meta.results.isEmpty()) return new ArrayList<ArrayList<Triple>>();
		//ResultBinding result = meta.results.get(0);
		
		Stopwatch watch = Stopwatch.createStarted();
		ArrayList<ArrayList<Triple>> whyProv = pg.generateProvenance(query, result);
		
		meta.provGenerationTime = watch.elapsed(TimeUnit.MILLISECONDS);
		meta.numberOfDerivations = whyProv.size();
		
		
		if(PRINT_DERIVATIONS) {
			
			System.out.println("Result: "+result);
			for(ArrayList<Triple> derivation:whyProv) {
				System.out.println("derivation:");
				int i = 0;
				for(Triple t:derivation) {
					if(i != 0) System.out.print(" , ");
					System.out.print(t);
					
					i++;
				}				
			}
			System.out.println();
		}
	
		
		return whyProv;
		
	}

	
	public static void main(String[] args) {
		
		DBPSBExperiments experiment = new DBPSBExperiments();
		
		String[] dbpsbQueries = new String[26];
		
		dbpsbQueries[1] = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?var1 WHERE { <http://dbpedia.org/resource/Pat_Kerwick> rdf:type ?var1 . }";
		dbpsbQueries[3] = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX space: <http://purl.org/net/schemas/space/> PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> PREFIX dbpedia-prop: <http://dbpedia.org/property/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  SELECT ?var4 ?var8 ?var10 WHERE { ?var5 dbpedia-owl:thumbnail ?var4 . ?var5 rdf:type dbpedia-owl:Person . ?var5 rdfs:label \"Paolo Boselli\"@en . ?var5 foaf:page ?var8 . OPTIONAL { ?var5 foaf:homepage ?var10 .} . }";
		dbpsbQueries[4] = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX dbpedia: <http://dbpedia.org/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?var5 ?var6 ?var9 ?var8 ?var4 WHERE { { <http://dbpedia.org/resource/In_Position_EP> ?var5 ?var6 . ?var6 foaf:name ?var8 . } UNION { ?var9 ?var5 <http://dbpedia.org/resource/In_Position_EP> ; foaf:name ?var4 . } }";
		dbpsbQueries[5] = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbpp: <http://dbpedia.org/property/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?var3 ?var4 ?var5 WHERE { { ?var3 dbpp:series 1 ; foaf:name ?var4 ; rdfs:comment ?var5 ; rdf:type <http://www.w3.org/2002/07/owl#Thing> . } UNION { ?var3 dbpp:series ?var8 . ?var8 dbpp:redirect 1 . ?var3 foaf:name ?var4 ; rdfs:comment ?var5 ; rdf:type <http://www.w3.org/2002/07/owl#Thing> . } }";
		dbpsbQueries[6] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT DISTINCT ?var3 ?var5 ?var7 WHERE { ?var3 rdf:type <http://dbpedia.org/class/yago/Company108058098> . ?var3 dbpedia2:numEmployees ?var5 FILTER ( xsd:integer(?var5) >= 0 ) . ?var3 foaf:homepage ?var7 . }";
		dbpsbQueries[7] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX umbelBus: <http://umbel.org/umbel/sc/Business> PREFIX umbelCountry: <http://umbel.org/umbel/sc/IndependentCountry> SELECT distinct ?var0 ?var1 ?var2 ?var3 ?var5 ?var6 ?var7 ?var10 WHERE { ?var0 rdfs:comment ?var1. ?var0 foaf:page <http://en.wikipedia.org/wiki/Polish%E2%80%93Ukrainian_Peace_Force_Battalion> OPTIONAL{?var0 skos:subject ?var6} OPTIONAL{?var0 dbpedia2:industry ?var5}OPTIONAL{?var0 dbpedia2:location ?var2}OPTIONAL{?var0 dbpedia2:locationCountry ?var3}OPTIONAL{?var0 dbpedia2:locationCity ?var9; dbpedia2:manufacturer ?var0}OPTIONAL{?var0 dbpedia2:products ?var11; dbpedia2:model ?var0}OPTIONAL{?var0 <http://www.georss.org/georss/point> ?var10}OPTIONAL{?var0 rdf:type ?var7}}";
		dbpsbQueries[8] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?var2 ?var4 WHERE { { ?var2 rdf:type <http://www.w3.org/2002/07/owl#Thing>. ?var2 dbpedia2:population ?var4. FILTER (xsd:integer(?var4) > -1000) } UNION { ?var2 rdf:type <http://www.w3.org/2002/07/owl#Thing>. ?var2 dbpedia2:populationUrban ?var4. FILTER (xsd:integer(?var4) > -1000) } }";
		dbpsbQueries[9] = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT * WHERE { ?var2 a <http://dbpedia.org/ontology/Settlement>; rdfs:label \"Mexico City\"@en . ?var6 a <http://dbpedia.org/ontology/Airport>. {?var6 <http://dbpedia.org/ontology/city> ?var2} UNION {?var6 <http://dbpedia.org/ontology/location> ?var2} {?var6 <http://dbpedia.org/property/iata> ?var5.} UNION {?var6 <http://dbpedia.org/ontology/iataLocationIdentifier> ?var5. } OPTIONAL { ?var6 foaf:homepage ?var6_home. } OPTIONAL { ?var6 <http://dbpedia.org/property/nativename> ?var6_name.} }"; 
		dbpsbQueries[10] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT DISTINCT ?var7 { ?var3 foaf:page ?var7. ?var3 rdf:type <http://dbpedia.org/ontology/SoccerPlayer> . ?var3 dbpedia2:position ?var16 . ?var3 <http://dbpedia.org/property/clubs> ?var8 . ?var8 <http://dbpedia.org/ontology/capacity> ?var9 . ?var3 <http://dbpedia.org/ontology/birthPlace> ?var31 . ?var31 ?var33 ?var34. OPTIONAL {?var3 <http://dbpedia.org/ontology/number> ?var35.} Filter (?var33 = <http://dbpedia.org/property/populationEstimate> || ?var33 = <http://dbpedia.org/property/populationCensus> || ?var33 = <http://dbpedia.org/property/statPop> ) Filter (xsd:integer(?var34) > 700 ) . Filter (xsd:integer(?var9) < 30167  ) . Filter (?var16 = 'Goalkeeper'@en || ?var16 = <http://dbpedia.org/resource/Goalkeeper_%28association_football%29> || ?var16 = <http://dbpedia.org/resource/Goalkeeper_%28football%29>) }";
		dbpsbQueries[11] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> PREFIX umbelBus: <http://umbel.org/umbel/sc/Business> PREFIX umbelCountry: <http://umbel.org/umbel/sc/IndependentCountry> SELECT distinct ?var3 ?var4 ?var2 WHERE { {<http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:subsid ?var3 OPTIONAL{?var2 <http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:parent} OPTIONAL{<http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:divisions ?var4}} UNION {?var2 <http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:parent OPTIONAL{<http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:subsid ?var3} OPTIONAL{<http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:divisions ?var4}} UNION {<http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:divisions ?var4 OPTIONAL{<http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:subsid ?var3} OPTIONAL{?var2 <http://dbpedia.org/resource/Madhya_Gujarat_Vij_Company_Limited> dbpedia2:parent}} }";
		dbpsbQueries[12] = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX space: <http://purl.org/net/schemas/space/> PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> PREFIX dbpedia-prop: <http://dbpedia.org/property/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?var5 WHERE { ?var2 rdf:type dbpedia-owl:Person . ?var2 dbpedia-owl:nationality ?var4 . ?var4 rdfs:label ?var5 . ?var2 rdfs:label \"Leonid Czernowecki\"@pl . FILTER (lang(?var5) = 'en') }"; 
		dbpsbQueries[13] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT * WHERE {{ <http://dbpedia.org/resource/Sun_God_Festival> rdfs:comment ?var0. FILTER (lang(?var0) = 'en')} UNION {<http://dbpedia.org/resource/Sun_God_Festival> foaf:depiction ?var1} UNION {<http://dbpedia.org/resource/Sun_God_Festival> foaf:homepage ?var2}}";
		dbpsbQueries[14] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?var6 ?var8 ?var10 ?var4 WHERE { ?var4 skos:subject <http://dbpedia.org/resource/Category:2003_albums> . ?var4 foaf:name ?var6 . OPTIONAL { ?var4 rdfs:comment ?var8 . FILTER (LANG(?var8) = 'en') . } OPTIONAL { ?var4 rdfs:comment ?var10 . FILTER (LANG(?var10) = 'de') . } }";
		dbpsbQueries[15] = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?var2 ?var3 WHERE { ?var2 rdf:type <http://dbpedia.org/ontology/Fish> ; rdfs:label ?var3 . FILTER regex(str(?var3), 'pes', 'i') }";
		dbpsbQueries[17] = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX dct: <http://purl.org/dc/terms/> PREFIX map: <file:/home/moustaki/work/motools/musicbrainz/d2r-server-0.4/mbz_mapping_raw.n3#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX event: <http://purl.org/NET/c4dm/event.owl#> PREFIX rel: <http://purl.org/vocab/relationship/> PREFIX lingvoj: <http://www.lingvoj.org/ontology#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dbpprop: <http://dbpedia.org/property/> PREFIX dbowl: <http://dbpedia.org/ontology/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX tags: <http://www.holygoat.co.uk/owl/redwood/0.1/tags/> PREFIX db: <http://dbtune.org/musicbrainz/resource/> PREFIX geo: <http://www.geonames.org/ontology#> PREFIX bio: <http://purl.org/vocab/bio/0.1/> PREFIX mo: <http://purl.org/ontology/mo/> PREFIX vocab: <http://dbtune.org/musicbrainz/resource/vocab/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX mbz: <http://purl.org/ontology/mbz#> SELECT DISTINCT ?var2 ?var3 { {?var2 <http://www.w3.org/2004/02/skos/core#subject> <http://dbpedia.org/resource/Category:Frazioni_of_the_Province_of_Pordenone>.} UNION {?var2 <http://www.w3.org/2004/02/skos/core#subject> <http://dbpedia.org/resource/Category:Prefectures_in_France>.} UNION {?var2 <http://www.w3.org/2004/02/skos/core#subject> <http://dbpedia.org/resource/Category:German_state_capitals>.} ?var2 <http://www.w3.org/2000/01/rdf-schema#label> ?var3. FILTER (lang(?var3)='fr') }";		
		dbpsbQueries[18] = "PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX : <http://dbpedia.org/resource/> PREFIX dbpedia2: <http://dbpedia.org/property/> PREFIX dbpedia: <http://dbpedia.org/> PREFIX skos: <http://www.w3.org/2004/02/skos/core#> SELECT ?var3 ?var4 ?var5 WHERE { { <http://dbpedia.org/resource/Pat_Kerwick> ?var3 ?var4. FILTER ( (STR(?var3) = 'http://www.w3.org/2000/01/rdf-schema#label' && lang(?var4) = 'en') || (STR(?var3) = 'http://dbpedia.org/ontology/abstract' && lang(?var4) = 'en') || (STR(?var3) = 'http://www.w3.org/2000/01/rdf-schema#comment' && lang(?var4) = 'en') || (STR(?var3) != 'http://dbpedia.org/ontology/abstract' && STR(?var3) != 'http://www.w3.org/2000/01/rdf-schema#comment' && STR(?var3) != 'http://www.w3.org/2000/01/rdf-schema#label') ) } UNION { ?var5 ?var3 <http://dbpedia.org/resource/Pat_Kerwick> FILTER ( STR(?var3) = 'http://dbpedia.org/ontology/owner' || STR(?var3) = 'http://dbpedia.org/property/redirect' ) } }";
		dbpsbQueries[19] = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?var1 WHERE { { ?var1 <http://www.w3.org/2000/01/rdf-schema#label> \"Tipperary\"@en } UNION { ?var1 <http://www.w3.org/2000/01/rdf-schema#label> \"Tipperary\"@en }. FILTER(regex(str(?var1),'http://dbpedia.org/resource/') || regex(str(?var1),'http://dbpedia.org/ontology/') || regex(str(?var1),'http://www.w3.org/2002/07/owl') || regex(str(?var1),'http://www.w3.org/2001/XMLSchema') || regex(str(?var1),'http://www.w3.org/2000/01/rdf-schema') || regex(str(?var1),'http://www.w3.org/1999/02/22-rdf-syntax-ns')) }";		
		dbpsbQueries[22] = "SELECT ?var2 WHERE { ?var3 <http://xmlns.com/foaf/0.1/homepage> ?var2 . ?var3 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.opengis.net/gml/_Feature> . }";
		dbpsbQueries[23] = "PREFIX dc: <http://purl.org/dc/elements/1.1/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX space: <http://purl.org/net/schemas/space/> PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> PREFIX dbpedia-prop: <http://dbpedia.org/property/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT ?var4 WHERE { ?var2 rdf:type dbpedia-owl:Person . ?var2 rdfs:label \"Pat Kerwick\"@en . ?var2 foaf:page ?var4 . }";
		dbpsbQueries[24] = "SELECT * where { ?var1 a <http://dbpedia.org/ontology/Organisation> . ?var2 <http://dbpedia.org/ontology/foundationPlace> <http://dbpedia.org/resource/Illinois> . ?var4 <http://dbpedia.org/ontology/developer> ?var2 . ?var4 a <http://www.w3.org/2002/07/owl#Thing> . }";
		dbpsbQueries[25] = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX dbpprop:<http://dbpedia.org/property/> SELECT ?var0 ?var1 ?var2 ?var3 where { ?var6 rdf:type <http://www.w3.org/2002/07/owl#Thing>. ?var6 dbpprop:name ?var0. ?var6 dbpprop:pages ?var1. ?var6 dbpprop:isbn ?var2. ?var6 dbpprop:author ?var3.}";
		
		int[] indices = {1,3,4,5,6,7,8,9,10,11,12,13,14,15,17,18,19,22,23,24,25};
		//int[] indices = {1,3,7,17};
		//String endpoint = "http://localhost:3030/dbpedia/sparql";
		
		System.out.println("Repetition: "+QUERY_REPETITION);
		
		for(int index:indices) {
			String queryString = dbpsbQueries[index];
			System.out.println("------------------------------");
			System.out.println("Query template:\t\t"+index);
			System.out.println("------------------------------");
			
			QueryMetaData metaAvg = new QueryMetaData();
			
			for(int i = 0;i < QUERY_REPETITION;i++) {
			
				QueryMetaData meta = experiment.executeQuery(queryString);



				//System.out.println("Query execution time:\t"+meta.queryExecutionTime);
				//System.out.println("# of results:\t\t"+meta.numberOfRecords);
			
			
	//			if(meta.numberOfRecords > RESULT_LIMIT) {
	//				continue;
	//			}
			
				for(ResultBinding result : meta.results) {
					QueryMetaData tt = new QueryMetaData();
					experiment.generateProvenance(queryString, tt, result);
					meta.provGenerationTime +=  tt.provGenerationTime;
				}
				//System.out.println("Prov generation time:\t"+meta.provGenerationTime);
				//System.out.println("Avg. Prov gen. time:\t"+meta.provGenerationTime/(double)meta.numberOfRecords);
			
				//System.out.println("# of derivations:\t"+tt.numberOfDerivations);
				
				metaAvg.queryExecutionTime += meta.queryExecutionTime;
				metaAvg.provGenerationTime += meta.provGenerationTime;
				metaAvg.numberOfRecords = meta.numberOfRecords;
				
			}
			metaAvg.queryExecutionTime /= QUERY_REPETITION;
			metaAvg.provGenerationTime /= QUERY_REPETITION;
			
			System.out.println("Query execution time:\t"+metaAvg.queryExecutionTime);
			System.out.println("# of results:\t\t"+metaAvg.numberOfRecords);
			System.out.println("Prov generation time:\t"+metaAvg.provGenerationTime);
			System.out.println("Avg. Prov gen. time:\t"+metaAvg.provGenerationTime/(double)metaAvg.numberOfRecords);
		
		}
		
		System.out.println("Experiments finished");
		System.exit(0);
	}
}
