package it.minux.increase.strategy;

import java.util.Comparator;
import org.neo4j.graphdb.Node;

public class NodeComparator implements Comparator<Node> {
	
	private static final String PROP_R_SCORE = "score";
	
	public int compare(Node n1, Node n2) {
		if(!n1.hasProperty(PROP_R_SCORE) || !n2.hasProperty(PROP_R_SCORE))
			return 0;
		
		double n1score = (Double) n1.getProperty(PROP_R_SCORE);
		double n2score = (Double) n2.getProperty(PROP_R_SCORE);
		
		if(n1score < n2score)
			return 1;
		else if(n1score == n2score)
			return 0;
		else
			return 1;
	}
}
