package tk.pathfinder.Map;

import tk.pathfinder.Networking.Beacon;
import tk.pathfinder.exceptions.NoValidPathException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Navigation {

    /**
     * Find the shortest path between two locations.
     * @param map The map to navigate.
     * @param current The closest node to the user's current position.
     * @param goal The user's desired destination.
     * @return A list of edges, in order from current to goal.
     * @throws NoValidPathException if there is no possible way to get from the current location to the destination.
     */
    public static Path NavigatePath(Map map, Node current, Room goal) throws NoValidPathException {
        return NavigatePath(map, current, goal, null);
    }

       /**
     * Find the shortest path between two locations.
     * @param map The map to navigate.
     * @param current The closest node to the user's current position.
     * @param goal The user's desired destination.
     * @param connector_preference The preferred method of ascending floors.
     * @return A list of edges, in order from current to goal.
     * @throws NoValidPathException if there is no possible way to get from the current location to the destination.
     */
    public static Path NavigatePath(Map map, Node current, Room goal, FloorConnector.FloorConnectorTypes connector_preference) throws NoValidPathException {
        // on the same floor
        if(current.getFloor() == goal.getFloor()){
            Path p = aStar(map, current, goal);
            if(p == null)
                throw new NoValidPathException(current, goal);
            return p;
        }

        // go to a floor connector first
        List<FloorConnector> connectors = new ArrayList<>();
        Iterator<FloorConnector> i;
        int goalFloor = goal.getFloor();
        if(connector_preference == null)
            i = map.getFloorConnectors();
        else i = map.getFloorConnectors(connector_preference);

        while(i.hasNext()){
            FloorConnector c = i.next();
            if(!c.isFloorAccessible(goalFloor) || !c.isOperational())
                continue;
            connectors.add(i.next());
        }

        // TODO taking multiple connectors, taking others if the preference is not available.
        if(connectors.size() == 0){
            throw new NoValidPathException(current, goal);
        }
        Collections.sort(connectors, (o1, o2) ->
                Double.compare(o1.getPoint().distance(current.getPoint()),
                o2.getPoint().distance(current.getPoint())));

        // navigate to the floor connector, then navigate to the goal
        FloorConnector c = connectors.get(0);
        return aStar(map, current, c).append(aStar(map, c, goal));
    }

    private static Path aStar(Map map, Node start, Node goal){
        // evaluated nodes
        List<Node> closed = new ArrayList<>();

        // discovered nodes
        List<Node> open = new ArrayList<>();
        open.add(start);

        // each node can be most efficiently reached from the previous node.
        HashMap<Node, Node> cameFrom = new HashMap<>();

        // the cost of getting from the start node to the given node.
        HashMap<Node, Double> score = new HashMap<>();
        // the cost of getting from the start node to the goal by passing the node.
        HashMap<Node, Double> f = new HashMap<>();
        for(Iterator<Node> i = map.getNodes(); i.hasNext(); ){
            Node n = i.next();
            score.put(n, Double.MAX_VALUE);
            f.put(n, Double.MAX_VALUE);
        }
        score.put(start, 0.0);
        f.put(start, h(start, goal));

        while(!open.isEmpty()){
            Node current = minScore(open, f);

            // we've struck gold
            if(current == goal)
                return getPathResult(map, cameFrom, goal);

            open.remove(current);
            closed.add(current);

            // check all the neighbors
            for(Iterator<Edge> i = map.getNextEdges(current); i.hasNext(); ){
                Edge e = i.next();
                Node neighbor = e.getOther(current);

                // ignore if already evaluated
                if(closed.contains(neighbor))
                    continue;

                // calculate the new score
                double g = score.get(current) + e.getWeight();

                // new node
                if(!open.contains(neighbor))
                    open.add(neighbor);
                // not the best we've seen
                else if(g >= score.get(neighbor))
                    continue;

                // this is the best node so far
                cameFrom.put(neighbor, current);
                score.put(neighbor, g);
                f.put(neighbor, g + h(neighbor, goal));
            }
        }

        return null;
    }

    // get the node with the lowest f-score.
    private static Node minScore(List<Node> open, HashMap<Node, Double> f){
        double min = Double.MAX_VALUE;
        Node res = open.get(0);

        if(open.size() > 1)
            for(int i = 1; i < open.size(); i++){
                Node n = open.get(i);
                double score = f.get(n);
                if(score < min){
                    min = score;
                    res = n;
                }
            }
        return res;
    }

    // our heuristic value
    private static double h(Node node, Node goal){
        return node.getPoint().distance(goal.getPoint());
    }

    // assemble the path generated from the A* algorithm
    private static Path getPathResult(Map map, HashMap<Node, Node> cameFrom, Node current){
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        nodes.add(current);

        while(cameFrom.keySet().contains(current)){
            current = cameFrom.get(current);
            nodes.add(current);
        }

        for(int i = nodes.size() - 1; i > 0; i--){
            Edge e = map.getEdge(nodes.get(i), nodes.get(i-1));
            if(e == null)
                throw new RuntimeException("An invalid path was encountered.");
            edges.add(e);
        }

        return new Path(edges.toArray(new Edge[0]));
    }

    /**
     * Approximate location on the map given three closest beacons
     * @param b1 A beacon.
     * @param b2 A beacon.
     * @param b3 A beacon.
     * @return An approximate location.
     */
    public static Point triangulate(Beacon b1, Beacon b2, Beacon b3){
        List<Beacon> beacons = new ArrayList<>();

        // get our beacons
        if(b1 != null)
            beacons.add(b1);
        if(b2 != null)
            beacons.add(b2);
        if(b3 != null)
            beacons.add(b3);

        // not enough beacons
        if(beacons.size() == 0)
            throw new IllegalArgumentException();

        // all we know
        if(beacons.size() == 1){
            return beacons.get(0).getLocation();
        }

        int s1 = beacons.get(0).getLevel();
        int s2 = beacons.get(1).getLevel();
        // Find the midpoint of the line.
        int m_x; int m_z;
        int y = beacons.get(0).getLocation().getY();

        if(s1 == 0 && s2 == 0)
            throw new IllegalArgumentException("The beacons aren't there.");

        else if(s1 == 0){
            m_x = beacons.get(1).getLocation().getX();
            m_z = beacons.get(1).getLocation().getZ();
            return new Point(m_x, y, m_z);
        }
        else if(s2 == 0){
            m_x = beacons.get(0).getLocation().getX();
            m_z = beacons.get(0).getLocation().getZ();
            return new Point(m_x, y, m_z);
        }

        Point m = findWeightedMidpoint(beacons.get(0).getLocation(), beacons.get(1).getLocation(), s1, s2, true);
        int sc = m.getY();
        m = new Point(m.getX(), beacons.get(0).getLocation().getY(), m.getZ());

        // we're done
        if(beacons.size() == 2)
            return m;

        int s3 = beacons.get(2).getLevel();

        if(s3 == 0)
            return m;

        return findWeightedMidpoint(m, beacons.get(2).getLocation(), sc, s3, false);
    }

    // find the weighted midpoint of the two points with signal strength.
    // if the last parameter is true, it will return the weighted signal strength as the Y value.
    private static Point findWeightedMidpoint(Point p1, Point p2, int s1, int s2, boolean add_strength){
        int dx = Math.abs(p2.getX() - p1.getX());
        int dy = Math.abs(p2.getZ() - p1.getZ());
        double weight = s2/(double)s1;
        int dist_x = (int)Math.round(dx / (weight + 1));
        int dist_y = (int)Math.round(dy / (weight + 1));
        int x; int y;

        if(p1.getX() < p2.getX())
            x = p1.getX() + dist_x;
        else
            x = p2.getX() + dist_x;

        if(p1.getZ() < p2.getZ())
            y = p1.getZ() + dist_y;
        else
            y = p2.getZ() + dist_y;

        int sig_dist = (int)Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        int ds = (int)Math.round(sig_dist / (weight + 1));
        int signal;
        if(s1 < s2)
            signal = s1 + ds;
        else signal = s2 + ds;

        if(add_strength)
            return new Point(x, signal, y);
        else return new Point(x, p1.getY(), y);
    }
}
