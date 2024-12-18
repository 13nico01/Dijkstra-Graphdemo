module at.htlhl.dijkstravisu.dijkstravisu {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires smartgraph;

    opens at.htlhl.dijkstravisu to javafx.fxml;
    exports at.htlhl.dijkstravisu;
}