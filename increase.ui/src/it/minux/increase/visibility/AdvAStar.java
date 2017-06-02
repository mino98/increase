package it.minux.increase.visibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.neo4j.graphalgo.EstimateEvaluator;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphalgo.impl.util.PathImpl;
import org.neo4j.graphalgo.impl.util.WeightedPathImpl;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.helpers.collection.PrefetchingIterator;

/**
 * Multigoal AStar with advanced cost evaluator API
 * @author brujito
 *
 */
public class AdvAStar implements PathFinder<WeightedPath>
{
    private final RelationshipExpander expander;
    private final AdvCostEvaluator<Double> lengthEvaluator;
    private final EstimateEvaluator<Double> estimateEvaluator;
    
    public AdvAStar( RelationshipExpander expander,
    		AdvCostEvaluator<Double> lengthEvaluator, EstimateEvaluator<Double> estimateEvaluator )
    {
        this.expander = expander;
        this.lengthEvaluator = lengthEvaluator;
        this.estimateEvaluator = estimateEvaluator;
    }
    
    public WeightedPath findSinglePath( Node start, Node end )
    {
    	Set<Node> goal = new HashSet<Node>();
    	goal.add(end);
    	return findSinglePath(start, goal);
    }
    
    public WeightedPath findSinglePath( Node start, Set<Node> goal)
    {
        Doer doer = new Doer( start, goal );
        while ( doer.hasNext() )
        {
            Node node = doer.next();
            GraphDatabaseService graphDb = node.getGraphDatabase();
//            if ( node.equals( end ) )
            if (goal.contains(node)) 
            {
                // Hit, return path
                double weight = doer.score.get( node.getId() ).wayLength;
                LinkedList<Relationship> rels = new LinkedList<Relationship>();
                Relationship rel = graphDb.getRelationshipById( doer.cameFrom.get( node.getId() ) );
                while ( rel != null )
                {
                    rels.addFirst( rel );
                    node = rel.getOtherNode( node );
                    Long nextRelId = doer.cameFrom.get( node.getId() );
                    rel = nextRelId == null ? null : graphDb.getRelationshipById( nextRelId );
                }
                Path path = toPath( start, rels );
                return new WeightedPathImpl( weight, path );
            }
        }
        return null;
    }
    
    public Iterable<WeightedPath> findAllPaths( Node node, Node end )
    {
        WeightedPath path = findSinglePath( node, end );
        return path != null ? Arrays.asList( path ) : Collections.<WeightedPath>emptyList();
    }
    
    private Path toPath( Node start, LinkedList<Relationship> rels )
    {
        PathImpl.Builder builder = new PathImpl.Builder( start );
        for ( Relationship rel : rels )
        {
            builder = builder.push( rel );
        }
        return builder.build();
    }
    
    private static class Data
    {
        private double wayLength; // acumulated cost to get here (g)
        private double estimate; // heuristic estimate of cost to reach end (h)
        
        double getFscore()
        {
            return wayLength + estimate;
        }
    }

    private class Doer extends PrefetchingIterator<Node>
    {
        private final Set<Node> goal;
        private Node lastNode;
        private boolean expand;
        private final Set<Long> visitedNodes = new HashSet<Long>();
        private final Set<Node> nextNodesSet = new HashSet<Node>();
        private final TreeMap<Double, Collection<Node>> nextNodes =
                new TreeMap<Double, Collection<Node>>();
        private final Map<Long, Long> cameFrom = new HashMap<Long, Long>();
        private final Map<Long, Data> score = new HashMap<Long, Data>();
        
        Doer( Node start, Set<Node> goal )
        {
            this.goal = goal;
            
            Data data = new Data();
            data.wayLength = 0;
            
            double minEstimate = Double.MAX_VALUE;
            for (Node end : goal) {
            	double estimate = estimateEvaluator.getCost( start, end );
            	if (estimate < minEstimate) {
            		minEstimate = estimate;
            	}
            }
            
            data.estimate = minEstimate;
            addNext( start, data.getFscore() );
            this.score.put( start.getId(), data );
        }
        
        private void addNext( Node node, double fscore )
        {
            Collection<Node> nodes = this.nextNodes.get( fscore );
            if ( nodes == null )
            {
                nodes = new HashSet<Node>();
                this.nextNodes.put( fscore, nodes );
            }
            nodes.add( node );
            this.nextNodesSet.add( node );
        }

        private Node popLowestScoreNode()
        {
            Iterator<Map.Entry<Double, Collection<Node>>> itr =
                    this.nextNodes.entrySet().iterator();
            if ( !itr.hasNext() )
            {
                return null;
            }
            
            Map.Entry<Double, Collection<Node>> entry = itr.next();
            Node node = entry.getValue().isEmpty() ? null : entry.getValue().iterator().next();
            if ( node == null )
            {
                return null;
            }
            
            if ( node != null )
            {
                entry.getValue().remove( node );
                this.nextNodesSet.remove( node );
                if ( entry.getValue().isEmpty() )
                {
                    this.nextNodes.remove( entry.getKey() );
                }
                this.visitedNodes.add( node.getId() );
            }
            return node;
        }

        @Override
        protected Node fetchNextOrNull()
        {
            // FIXME
            if ( !this.expand )
            {
                this.expand = true;
            }
            else
            {
                expand();
            }
            
            Node node = popLowestScoreNode();
            this.lastNode = node;
            return node;
        }

        private void expand()
        {
            for ( Relationship rel : expander.expand( this.lastNode ) )
            {
                Node node = rel.getOtherNode( this.lastNode );
                if ( this.visitedNodes.contains( node.getId() ) )
                {
                    continue;
                }
                
                Data lastNodeData = this.score.get( this.lastNode.getId() );
                
                List<Node> previosNodes = getBestPathNodes();
                
                
                double tentativeGScore = lastNodeData.wayLength +
                        lengthEvaluator.getCost( rel, Direction.OUTGOING, previosNodes );
                boolean isBetter = false;
                
                double minEstimate = Double.MAX_VALUE;
                for (Node end : goal) {
                	double estimate = estimateEvaluator.getCost( node, end );
                	if (estimate < minEstimate) {
                		minEstimate = estimate;
                	}
                }
                
                double estimate = minEstimate; 
                	//estimateEvaluator.getCost( node, this.end );
                
                if ( !this.nextNodesSet.contains( node ) )
                {
                    addNext( node, estimate + tentativeGScore );
                    isBetter = true;
                }
                else if ( tentativeGScore < this.score.get( node.getId() ).wayLength )
                {
                    isBetter = true;
                }
                
                if ( isBetter )
                {
                    this.cameFrom.put( node.getId(), rel.getId() );
                    Data data = new Data();
                    data.wayLength = tentativeGScore;
                    data.estimate = estimate;
                    this.score.put( node.getId(), data );
                }
            }
        }

		private List<Node> getBestPathNodes() {
			List<Node> nodes = new ArrayList<Node>();
			
			GraphDatabaseService graphDb = this.lastNode.getGraphDatabase();
			
			Node node = this.lastNode;
			nodes.add(node);
			
			Long relId = cameFrom.get( node.getId() );
			if (relId == null) {
				return nodes;
			}
			
			Relationship rel = graphDb.getRelationshipById( relId );
            while ( rel != null )
            {
                node = rel.getOtherNode( node );
                nodes.add(node);
    			Long nextRelId = cameFrom.get( node.getId() );
                rel = nextRelId == null ? null : graphDb.getRelationshipById( nextRelId );
            }
			
			return nodes;
		}
    }
}
