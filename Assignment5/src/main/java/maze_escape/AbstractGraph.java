package maze_escape;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;


/**
 * An abstract graph implementation.
 * <br>
 * This class implements graph search algorithms on a graph with abstract vertex type V
 * for every vertex in the graph, its neighbours can be found by use of abstract method getNeighbours(fromVertex)
 * this abstraction can be used for both directed and undirected graphs
 *
 * @param <V> The type of the objects stored.
 * @author ADS Team
 * @author Hamza el Haouti
 * @author Rida Zeâmari
 */
public abstract class AbstractGraph<V> {

  /**
   * Retrieves all neighbours of the given fromVertex
   * if the graph is directed, the implementation of this method shall follow the outgoing edges of fromVertex
   *
   * @param fromVertex The vertex whose neighbours, you want.
   * @return A set with all the neighbours of the given fromVertex.
   */
  public abstract Set<V> getNeighbours(V fromVertex);

  /**
   * retrieves all vertices that can be reached directly or indirectly from the given firstVertex
   * if the graph is directed, only outgoing edges shall be traversed
   * firstVertex shall be included in the result as well
   * if the graph is connected, all vertices shall be found
   *
   * @param firstVertex the start vertex for the retrieval.
   * @return A set with all the vertices that can be reached directly or indirectly from the given firstVertex.
   */
  public Set<V> getAllVertices(V firstVertex) {
    Set<V> allVertices = new HashSet<>();

    collectAllVertices(firstVertex, allVertices);

    return allVertices;
  }

  private void collectAllVertices(V vertex, Set<V> visited) {
    if (!visited.add(vertex)) return;

    for (V neighbour : getNeighbours(vertex)) collectAllVertices(neighbour, visited);
  }


  /**
   * Formats the adjacency list of the subgraph starting at the given firstVertex
   * according to the format:
   * vertex1: [neighbour11,neighbour12,…]
   * vertex2: [neighbour21,neighbour22,…]
   * …
   * Uses a pre-order traversal of a spanning tree of the sub-graph starting with firstVertex as the root
   * if the graph is directed, only outgoing edges shall be traversed
   * , and using the getNeighbours() method to retrieve the roots of the child subtrees.
   *
   * @param firstVertex the start vertex for the adjacency list.
   * @return A string representing the adjacency list of the subgraph starting at the given firstVertex
   */
  public String formatAdjacencyList(V firstVertex) {
    StringBuilder stringBuilder = new StringBuilder("Graph adjacency list:\n");

    formatAdjacencyList(firstVertex, stringBuilder, new HashSet<>());

    return stringBuilder.toString();
  }

  private void formatAdjacencyList(V vertex, StringBuilder stringBuilder, Set<V> visited) {
    // Mark vertex as visited. If Vertex already visited, stop recursion.
    if (!visited.add(vertex)) return;

    // Add the representation of the vertex and its neighbours to the StringBuilder.
    stringBuilder.append(vertex).append(": ").append(getNeighbours(vertex)).append("\n");

    // Repeat the process for all other neighbours.
    for (V neighbour : getNeighbours(vertex)) formatAdjacencyList(neighbour, stringBuilder, visited);
  }


  /**
   * Represents a directed path of connected vertices in the graph.
   */
  public class GPath {

    private static final int DISPLAY_CUT = 10;

    /**
     * Representation invariants:
     * 1. vertices contains a sequence of vertices that are neighbours in the graph,
     * i.e. FOR ALL i: 1 < i < vertices.length: getNeighbours(vertices[i-1]).contains(vertices[i])
     * 2. a path with one vertex equal start and target vertex
     * 3. a path without vertices is empty, does not have a start nor a target
     */
    private final Deque<V> vertices = new LinkedList<>();

    /**
     * Helper attribute to capture total path length from a function on two neighbouring vertices
     */
    private double totalWeight = 0.0;

    /**
     * Helper attribute to be able to track visited vertices in searches, only for analysis purposes
     */
    private Set<V> visited = new HashSet<>();

    @Override
    public String toString() {
      var stringBuilder = new StringBuilder(
        String.format("Weight=%.2f Length=%d visited=%d (",
          this.totalWeight,
          this.vertices.size(),
          this.visited.size()
        )
      );

      var separator = "";
      var count = 0;
      final var tailCut = this.vertices.size() - 1 - DISPLAY_CUT;

      for (V v : this.vertices) {
        // limit the length of the text representation for long paths.
        if (count < DISPLAY_CUT || count > tailCut) {
          stringBuilder.append(separator).append(v.toString());
          separator = ", ";
        } else if (count == DISPLAY_CUT) {
          stringBuilder.append(separator).append("...");
        }
        count++;
      }

      return stringBuilder.append(")").toString();
    }

    /**
     * Recalculates the total weight of the path from a given weightMapper that calculates the weight of
     * the path segment between two neighbouring vertices.
     *
     * @param weightMapper
     */
    public void reCalculateTotalWeight(BiFunction<V, V, Double> weightMapper) {
      this.totalWeight = 0.0;
      V previous = null;
      for (V v : this.vertices) {
        // the first vertex of the iterator has no predecessor and hence no weight contribution
        if (previous != null) this.totalWeight += weightMapper.apply(previous, v);
        previous = v;
      }
    }

    public Queue<V> getVertices() {
      return this.vertices;
    }

    public double getTotalWeight() {
      return this.totalWeight;
    }

    public Set<V> getVisited() {
      return this.visited;
    }

  }

  /**
   * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
   * All vertices that are being visited by the search should also be registered in path.visited
   *
   * @param startVertex  Where to start the search
   * @param targetVertex Where to end the search
   * @return the path from startVertex to targetVertex
   * or null if target cannot be matched with a vertex in the sub-graph from startVertex
   */
  public GPath depthFirstSearch(V startVertex, V targetVertex) {
    if (startVertex == null || targetVertex == null) return null;

    Set<V> visited = new HashSet<>(); // Independent visited set
    return depthFirstSearch(startVertex, targetVertex, visited, new LinkedList<>());
  }

  private GPath depthFirstSearch(
    V startVertex,
    V targetVertex,
    Set<V> visited,
    Deque<V> path
  ) {
    // If the start vertex was visited earlier, stop.
    if (visited.contains(startVertex)) return null;

    // Mark the current vertex as visited
    visited.add(startVertex);

    // Add the current vertex to the path
    path.addLast(startVertex);

    // If the target is found, return the path
    if (startVertex.equals(targetVertex)) {
      GPath result = new GPath();
      result.vertices.addAll(path);
      result.visited = new HashSet<>(visited);
      return result;
    }

    // Recursively search neighbors
    for (V neighbour : this.getNeighbours(startVertex)) {
      GPath result = depthFirstSearch(neighbour, targetVertex, visited, path);
      if (result != null) return result; // Valid path found
    }

    // Backtrack: Remove the current vertex from the path
    path.removeLast();

    // There is no path found.
    return null;
  }


  /**
   * Uses a breadth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
   * All vertices that are being visited by the search should also be registered in path.visited
   *
   * @param startVertex  Where to start the search
   * @param targetVertex Where to end the search
   * @return the path from startVertex to targetVertex
   * or null if target cannot be matched with a vertex in the sub-graph from startVertex
   */
  public GPath breadthFirstSearch(V startVertex, V targetVertex) {
    if (startVertex == null || targetVertex == null) return null;

    GPath path = new GPath();
    path.vertices.addLast(targetVertex);
    path.visited.add(targetVertex);

    if (startVertex.equals(targetVertex)) return path;

    Queue<V> fifoQueue = new LinkedList<>();
    Map<V, V> visitedFrom = new HashMap<>();

    fifoQueue.offer(startVertex);

    // Initialises the queue with the start vertex.
    // Start vertex is marked as visited without a preceding vertex on its path.
    visitedFrom.put(startVertex, null);

    V current = fifoQueue.poll();
    while (current != null) {
      for (V neighbour : this.getNeighbours(current)) {
        path.visited.add(current);

        // Build a path if target is found.
        if (neighbour.equals(targetVertex)) {
          while (current != null) {
            path.vertices.addFirst(current);

            current = visitedFrom.get(current);
          }
          return path;
        } else if (!visitedFrom.containsKey(neighbour)) {
          visitedFrom.put(neighbour, current);
          fifoQueue.offer(neighbour);
        }
      }
      // Picks the next vertex in turn.
      current = fifoQueue.poll();
    }

    // There is no path found.
    return null;
  }

  /**
   * helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
   * your may change this class or delete it altogether follow a different approach in your implementation
   */
  private final class MSTNode implements Comparable<MSTNode> {

    private final V vertex;
    private final MSTNode parentMSTNode;
    private final double weightSumTo;
    private boolean marked = false;

    private MSTNode(V vertex, MSTNode parentMSTNode, double weightSumTo) {
      this.vertex = vertex;
      this.parentMSTNode = parentMSTNode;
      this.weightSumTo = weightSumTo;
    }

    /**
     * Implemented to help with comparing the shortest current path, sofar
     *
     * @param other the object to be compared.
     * @return
     */
    @Override
    public int compareTo(MSTNode other) {
      return Double.compare(this.weightSumTo, other.weightSumTo);
    }

  }

  public GPath dijkstraShortestPath(V startVertex, V targetVertex, BiFunction<V, V, Double> weightMapper) {
    if (startVertex == null || targetVertex == null || weightMapper == null) return null;

    var mst = new HashMap<V, MSTNode>();
    var mstCandidates = new PriorityQueue<MSTNode>();
    var visited = new HashSet<V>();

    var startWrapper = new MSTNode(startVertex, null, 0D);

    mstCandidates.add(startWrapper);
    mst.put(startVertex, startWrapper);

    while (!mstCandidates.isEmpty()) {
      var currMSTNode = mstCandidates.poll();

      visited.add(currMSTNode.vertex);

      // Skip if this is an outdated entry in the PriorityQueue
      if (currMSTNode.marked) continue;

      // Early exit: Once we extract the target vertex, we can stop.
      if (currMSTNode.vertex.equals(targetVertex))
        return generatePathToVertex(currMSTNode, visited);

      for (V neighbour : getNeighbours(currMSTNode.vertex)) {
        addToMST(weightMapper, neighbour, currMSTNode, mst, mstCandidates);
      }
    }

    return null;
  }

  private void addToMST(
    BiFunction<V, V, Double> weightMapper,
    V neighbour,
    MSTNode currMSTNode,
    HashMap<V, MSTNode> mst,
    PriorityQueue<MSTNode> mstCandidates
  ) {
    double tentativeDist = currMSTNode.weightSumTo
      + weightMapper.apply(currMSTNode.vertex, neighbour);

    var neighbourMSTNode = mst.getOrDefault(neighbour, null);

    if (neighbourMSTNode == null) {
      neighbourMSTNode = new MSTNode(neighbour, currMSTNode, tentativeDist);
      mstCandidates.add(neighbourMSTNode);
      mst.put(neighbour, neighbourMSTNode);
    } else if (tentativeDist < neighbourMSTNode.weightSumTo) {
      neighbourMSTNode.marked = true;
      var newNeighbourWrapper = new MSTNode(neighbour, currMSTNode, tentativeDist);
      mstCandidates.add(newNeighbourWrapper);
      mst.put(neighbour, newNeighbourWrapper);
    }
  }

  private GPath generatePathToVertex(MSTNode wrappedTargetVertex, HashSet<V> visitedVertices) {
    var path = new GPath();

    path.visited = visitedVertices;
    path.totalWeight = wrappedTargetVertex.weightSumTo;

    for (var vertexToAdd = wrappedTargetVertex; vertexToAdd != null; vertexToAdd = vertexToAdd.parentMSTNode) {
      path.vertices.addFirst(vertexToAdd.vertex);
    }

    return path;
  }

}