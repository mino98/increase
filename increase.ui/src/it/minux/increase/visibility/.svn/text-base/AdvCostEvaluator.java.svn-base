package it.minux.increase.visibility;

import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * In order to make the solving of shortest path problems as general as
 * possible, the algorithms accept objects handling all relevant tasks regarding
 * costs of paths. This allows the user to represent the costs in any possible
 * way, and to calculate them in any way. The usual case is numbers that we just
 * add together, but what if we have for example factors we would like to
 * multiply instead? This is handled by this system, which works as follows. A
 * CostEvaluator is used to get the cost for a single relationship. These costs
 * are then added through a CostAccumulator. Costs for alternative paths are
 * compared with a common java.util.Comparator.
 *
 * @author Patrik Larsson
 * @param <T> The data type the edge weights are represented by.
 */
public interface AdvCostEvaluator<T>
{
    /**
     * This is the general method for looking up costs for relationships. This
     * can do anything, like looking up a property or running some small
     * calculation.
     *
     * @param relationship
     * @param direction The direction in which the relationship is being
     *            evaluated, either {@link Direction#INCOMING} or
     *            {@link Direction#OUTGOING}.
     * @param previosNodes 
     * @return The cost for this edge/relationship
     */
    T getCost( Relationship relationship, Direction direction, List<Node> previosNodes );
}
