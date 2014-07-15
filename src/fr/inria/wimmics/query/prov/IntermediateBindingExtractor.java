package fr.inria.wimmics.query.prov;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class IntermediateBindingExtractor {

	public static Query buildIntermediateQuery(Query query, ResultBinding result) {
		
		Query tQuery = QueryFactory.create("SELECT * {<http://example.com/s> <http://example.com/p> <http://example.com/o>}");
		//Query tQuery = QueryFactory.make();
		
		tQuery.setQueryPattern(query.getQueryPattern());
		tQuery.setQuerySelectType();
		//tQuery.addResultVar("*");
		tQuery.setResultVars();
		
		//variable bindings
		List<Var> vars = new ArrayList<Var>();
		Iterator<Var> varIt = result.getBinding().vars();
		while(varIt.hasNext()) {
			vars.add(varIt.next());
		}
		List<Binding> bindings = new ArrayList<Binding>();
		bindings.add(result.getBinding());
		tQuery.setValuesDataBlock(vars, bindings);		
		
		System.out.println(tQuery);
		return tQuery;
	}
}
