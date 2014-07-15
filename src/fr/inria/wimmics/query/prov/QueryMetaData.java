package fr.inria.wimmics.query.prov;

import java.util.List;

import com.hp.hpl.jena.sparql.core.ResultBinding;

public 	class QueryMetaData {
	public String queryStr;
	public int templateType;
	public double queryExecutionTime = 0;
	public double provGenerationTime = 0;
	public int numberOfRecords = 0;
	public int numberOfDerivations = 0;
	public List<ResultBinding> results = null;
}