package at.htlhl.dijkstravisu;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;

import java.util.List;

// Die GraphView-Klasse erweitert BorderPane und dient als Benutzeroberfläche zur Anzeige und Interaktion mit einem Graphen.
public class GraphView extends BorderPane {

    // Komponenten und Datenfelder für die Darstellung und Steuerung des Graphen
    private SmartGraphPanel<VertexData, EdgeData> smartGraphPanel;
    private ContentZoomScrollPane contentZoomScrollPane;
    private GraphControl graphControl;
    private VertexData startVertex; // Startknoten für den Dijkstra-Algorithmus
    private VertexData endVertex;   // Endknoten für den Dijkstra-Algorithmus

    private double lastContextScreenX; // Koordinaten des letzten Kontextmenüaufrufs
    private double lastContextScreenY;
    private SmartGraphVertexNode<VertexData> lastSelectedVertex; // Zuletzt ausgewählter Knoten

    // Konstruktor initialisiert die Benutzeroberfläche und die Logik
    public GraphView(GraphControl graphControl) {
        super();
        this.graphControl = graphControl;

        // Erstelle das Graphen-Panel mit einer zirkulären Anordnung der Knoten
        SmartPlacementStrategy strategy = new SmartCircularSortedPlacementStrategy();
        smartGraphPanel = new SmartGraphPanel<>(graphControl.getGraph(), strategy);
        smartGraphPanel.setAutomaticLayout(true);

        // Füge Zoom- und Scroll-Funktionen hinzu
        contentZoomScrollPane = new ContentZoomScrollPane(smartGraphPanel);
        setCenter(contentZoomScrollPane);

        // Erstelle die Steuerelemente (Buttons)
        Button testButton = new Button("Test");
        testButton.setOnAction(new TestEventHandler());

        Button dijkstraButton = new Button("Dijkstra");
        dijkstraButton.setOnAction(new DijkstraEventHandler());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(new ClearEventHandler());

        // Füge die Buttons zu einer Toolbar hinzu
        ToolBar toolBar = new ToolBar(testButton, dijkstraButton, clearButton);
        setTop(toolBar);

        // Lege eine Aktion für Doppelklicks auf Knoten fest
        smartGraphPanel.setVertexDoubleClickAction(graphVertex -> graphVertex.setStyleClass("htlVertex"));

        // Erstelle ein Kontextmenü für Knoten
        ContextMenu contextMenu = buildContextMenu();
        smartGraphPanel.setOnContextMenuRequested(event -> {
            lastContextScreenX = event.getScreenX();
            lastContextScreenY = event.getScreenY();

            // Finde den Knoten, auf den das Kontextmenü angewendet wird
            SmartGraphVertexNode<VertexData> foundVertex = findVertex(event.getX(), event.getY());
            lastSelectedVertex = foundVertex;

            // Zeige das Kontextmenü an, wenn ein Knoten gefunden wurde
            if (foundVertex != null) {
                contextMenu.show(foundVertex, event.getScreenX(), event.getScreenY());
            }
        });
    }

    // Initialisierung des Graphen nach dem Sichtbarwerden
    public void initAfterVisible() {
        smartGraphPanel.init();
    }

    // Erstelle ein Kontextmenü mit Optionen zum Festlegen des Start- und Endknotens
    private ContextMenu buildContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem selectStart = new MenuItem("Set as Start Vertex");
        selectStart.setOnAction(event -> {
            if (lastSelectedVertex != null) {
                resetVertexStyle(startVertex, "vertex");
                startVertex = lastSelectedVertex.getUnderlyingVertex().element();
                lastSelectedVertex.setStyleClass("startVertex");
            } else {
                showAlert("Error", "No Vertex Selected", "Please select a vertex to set as start.");
            }
        });

        MenuItem selectEnd = new MenuItem("Set as End Vertex");
        selectEnd.setOnAction(event -> {
            if (lastSelectedVertex != null) {
                resetVertexStyle(endVertex, "vertex");
                endVertex = lastSelectedVertex.getUnderlyingVertex().element();
                lastSelectedVertex.setStyleClass("endVertex");
            } else {
                showAlert("Error", "No Vertex Selected", "Please select a vertex to set as end.");
            }
        });

        contextMenu.getItems().addAll(selectStart, selectEnd);
        return contextMenu;
    }

    // Setzt den Stil eines Knotens zurück
    private void resetVertexStyle(VertexData vertexData, String styleClass) {
        if (vertexData != null) {
            SmartStylableNode node = smartGraphPanel.getStylableVertex(graphControl.findVertex(vertexData));
            if (node != null) {
                node.setStyleClass(styleClass);
            }
        }
    }

    // Sucht nach einem Knoten anhand der Position
    private SmartGraphVertexNode<VertexData> findVertex(double x, double y) {
        for (Vertex<VertexData> v : graphControl.getGraph().vertices()) {
            SmartStylableNode node = smartGraphPanel.getStylableVertex(v);
            if (node instanceof SmartGraphVertexNode) {
                SmartGraphVertexNode<VertexData> vertexNode = (SmartGraphVertexNode) node;
                if (vertexNode.getBoundsInParent().contains(x, y)) {
                    return vertexNode;
                }
            }
        }
        return null;
    }

    // Zeigt eine Warnmeldung an
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // EventHandler für den Test-Button
    private class TestEventHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            System.out.println("Test button clicked");
        }
    }

    // EventHandler für den Dijkstra-Button, der den kürzesten Pfad berechnet
    private class DijkstraEventHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            if (startVertex == null || endVertex == null) {
                showAlert("Error", "Missing Vertices", "Please select start and end vertices.");
                return;
            }

            // Berechne den kürzesten Pfad
            List<VertexData> path = graphControl.shortestPath(startVertex, endVertex);
            if (path.isEmpty()) {
                showAlert("No Path", "Path Not Found", "No valid path exists between the selected vertices.");
            } else {
                // Berechne die Gesamtdistanz und zeige den Pfad an
                double totalDistance = 0;
                for (int i = 0; i < path.size() - 1; i++) {
                    EdgeData edge = graphControl.getEdge(path.get(i), path.get(i + 1));
                    if (edge != null) {
                        totalDistance += edge.getDistance();
                    }
                }
                System.out.println("Shortest Path: " + path + ", Distance: " + totalDistance);

                StringBuilder pathString = new StringBuilder();
                path.forEach(vertex -> pathString.append(vertex.getName()).append(" -> "));
                pathString.setLength(pathString.length() - 4); // Entferne das letzte " -> "

                showAlert("Shortest Path", "Path Found", "Path: " + pathString + "\nTotal Distance: " + totalDistance + " km");
            }
        }
    }

    // EventHandler für den Clear-Button, der die Auswahl zurücksetzt
    private class ClearEventHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent actionEvent) {
            startVertex = null;
            endVertex = null;

            // Setze alle Stile zurück
            graphControl.getGraph().edges().forEach(edge -> {
                SmartStylableNode edgeNode = smartGraphPanel.getStylableEdge(edge.element());
                if (edgeNode != null) edgeNode.setStyleClass("edge");
            });

            graphControl.getGraph().vertices().forEach(vertex -> {
                SmartStylableNode vertexNode = smartGraphPanel.getStylableVertex(vertex.element());
                if (vertexNode != null) vertexNode.setStyleClass("vertex");
            });

            showAlert("Graph Cleared", null, "All selections have been reset.");
        }
    }
}
