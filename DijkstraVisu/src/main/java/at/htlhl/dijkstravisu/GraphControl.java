package at.htlhl.dijkstravisu;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Vertex;

import java.util.*;


public class GraphControl {

    private Graph<VertexData, EdgeData> graph;
    private VertexData startVertex;
    private VertexData endVertex;

    public GraphControl() {
        graph = new GraphEdgeList<>();
        buildGraph();
    }

    private void buildGraph() {
        VertexData seattle = createCityAndInsert("Seattle");
        VertexData sanFrancisco = createCityAndInsert("San Francisco");
        VertexData losAngeles = createCityAndInsert("Los Angeles");
        VertexData riverside = createCityAndInsert("Riverside");
        VertexData phoenix = createCityAndInsert("Phoenix");
        VertexData chicago = createCityAndInsert("Chicago");
        VertexData boston = createCityAndInsert("Boston");
        VertexData newYork = createCityAndInsert("New York");
        VertexData atlanta = createCityAndInsert("Atlanta");
        VertexData miami = createCityAndInsert("Miami");
        VertexData dallas = createCityAndInsert("Dallas");
        VertexData houston = createCityAndInsert("Houston");
        VertexData detroit = createCityAndInsert("Detroit");
        VertexData philadelphia = createCityAndInsert("Philadelphia");
        VertexData washington = createCityAndInsert("Washington");
        graph.insertEdge(seattle, chicago, new EdgeData(1737));
        graph.insertEdge(seattle, sanFrancisco, new EdgeData(678));
        graph.insertEdge(sanFrancisco, riverside, new EdgeData(386));
        graph.insertEdge(sanFrancisco, losAngeles, new EdgeData(348));
        graph.insertEdge(losAngeles, riverside, new EdgeData(50));
        graph.insertEdge(losAngeles, phoenix, new EdgeData(357));
        graph.insertEdge(riverside, phoenix, new EdgeData(307));
        graph.insertEdge(riverside, chicago, new EdgeData(1704));
        graph.insertEdge(phoenix, dallas, new EdgeData(887));
        graph.insertEdge(phoenix, houston, new EdgeData(1015));
        graph.insertEdge(dallas, chicago, new EdgeData(805));
        graph.insertEdge(dallas, atlanta, new EdgeData(721));
        graph.insertEdge(dallas, houston, new EdgeData(225));
        graph.insertEdge(houston, atlanta, new EdgeData(702));
        graph.insertEdge(houston, miami, new EdgeData(968));
        graph.insertEdge(atlanta, chicago, new EdgeData(588));
        graph.insertEdge(atlanta, washington, new EdgeData(543));
        graph.insertEdge(atlanta, miami, new EdgeData(604));
        graph.insertEdge(miami, washington, new EdgeData(923));
        graph.insertEdge(chicago, detroit, new EdgeData(238));
        graph.insertEdge(detroit, boston, new EdgeData(613));
        graph.insertEdge(detroit, washington, new EdgeData(396));
        graph.insertEdge(detroit, newYork, new EdgeData(482));
        graph.insertEdge(boston, newYork, new EdgeData(190));
        graph.insertEdge(newYork, philadelphia, new EdgeData(81));
        graph.insertEdge(philadelphia, washington, new EdgeData(123));
    }

    private VertexData createCityAndInsert(String name) {
        VertexData city = new VertexData(name);
        graph.insertVertex(city);
        return city;
    }

    public List<VertexData> shortestPath(VertexData startVertex, VertexData endVertex) {

        // Initialisieren von Distanzen und Vorgängern
        Map<Vertex<VertexData>, Integer> distances = new HashMap<>();
        Map<Vertex<VertexData>, Vertex<VertexData>> predecessors = new HashMap<>();
        List<Vertex<VertexData>> unvisited = new ArrayList<>(graph.vertices());

        // Start- und Endknoten finden
        Vertex<VertexData> start = findVertex(startVertex);
        Vertex<VertexData> end = findVertex(endVertex);

        // Distanzen initialisieren
        for (Vertex<VertexData> vertex : graph.vertices()) {
            distances.put(vertex, Integer.MAX_VALUE); // Unendlich für alle außer Start
            predecessors.put(vertex, null);
        }

        distances.put(start, 0); // Startknoten hat Distanz 0

        while (!unvisited.isEmpty()) {
            // Nächsten Knoten mit der kleinsten Distanz finden
            Vertex<VertexData> currentVertex = getMinVertexDistance(unvisited, distances);

            // Wenn kein erreichbarer Knoten übrig ist, abbrechen
            if (currentVertex == null) {
                break;
            }

            unvisited.remove(currentVertex); // Knoten aus unbesuchten entfernen

            // Wenn Endknoten erreicht, beenden
            if (currentVertex.equals(end)) {
                break;
            }

            // Alle Nachbarn des aktuellen Knotens verarbeiten
            for (Edge<EdgeData, VertexData> edge : graph.incidentEdges(currentVertex)) {
                Vertex<VertexData> neighbor = graph.opposite(currentVertex, edge);

                // Falls der Nachbar bereits besucht wurde, überspringen
                if (!unvisited.contains(neighbor)) {
                    continue;
                }

                // Neue Distanz berechnen
                int newDistance = distances.get(currentVertex) + edge.element().getDistance();
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance); // Aktualisiere die Distanz
                    predecessors.put(neighbor, currentVertex); // Vorgänger setzen
                }
            }
        }

        // Pfad anhand der Vorgänger rekonstruieren
        List<VertexData> path = new ArrayList<>();
        for (Vertex<VertexData> at = end; at != null; at = predecessors.get(at)) {
            path.add(at.element());
        }

        Collections.reverse(path); // Pfad umkehren, da von Endknoten rückwärts aufgebaut

        // Überprüfen, ob der Startknoten tatsächlich im Pfad ist
        if (path.isEmpty() || !path.get(0).equals(startVertex)) {
            return Collections.emptyList(); // Kein gültiger Pfad gefunden
        }

        return path;
    }

    public Vertex<VertexData> findVertex(VertexData vertex) {
        return graph.vertices().stream()
                .filter(v -> v.element().equals(vertex))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Knoten " + vertex + " nicht im Graphen gefunden"));
    }

    public Edge<EdgeData, VertexData> findEdge(VertexData startVertex, VertexData endVertex) {
        for (Edge<EdgeData, VertexData> edge : graph.edges()) {
            Vertex<VertexData>[] vertices = edge.vertices();
            Vertex<VertexData> start = vertices[0];
            Vertex<VertexData> end = vertices[1];

            if (start.element().equals(startVertex) && end.element().equals(endVertex)) {
                return edge;
            }

            if (end.element().equals(startVertex) && start.element().equals(endVertex)) {
                return edge;
            }
        }
        return null;
    }

    private Vertex<VertexData> getMinVertexDistance(List<Vertex<VertexData>> vertices, Map<Vertex<VertexData>, Integer> distances) {
        Vertex<VertexData> minVertex = null;
        int minDistance = Integer.MAX_VALUE;

        for (Vertex<VertexData> vertex : vertices) {
            int distance = distances.get(vertex);
            if (distance < minDistance) {
                minDistance = distance;
                minVertex = vertex;
            }
        }
        return minVertex;
    }

    public EdgeData getEdge(VertexData from, VertexData to) {
        for (Edge<EdgeData, VertexData> edge : graph.edges()) {
            Vertex<VertexData>[] vertices = edge.vertices(); // Hole die beiden Knoten der Kante
            Vertex<VertexData> start = vertices[0];
            Vertex<VertexData> end = vertices[1];

            // Prüfen, ob die Kante die beiden Knoten verbindet (in beide Richtungen)
            if ((start.element().equals(from) && end.element().equals(to)) ||
                    (start.element().equals(to) && end.element().equals(from))) {
                return edge.element(); // Rückgabe der Kanten-Daten
            }
        }
        return null;
    }

    public Graph<VertexData, EdgeData> getGraph() {
        return graph;
    }
}
