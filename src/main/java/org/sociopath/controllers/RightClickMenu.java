package org.sociopath.controllers;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import org.sociopath.controllers.GraphSimulationController.VertexFX;
import org.sociopath.controllers.GraphSimulationController.EdgeFX;

public class RightClickMenu {

    ContextMenu menu;
    VertexFX sourceVertex;
    EdgeFX sourceEdge;
    MenuItem delete, change, studentInfo;

    public RightClickMenu() {
        this.menu = new ContextMenu();
        this.delete = new MenuItem("Delete");
        this.change = new MenuItem("Change");

        menu.setOpacity(0.9);
    }

    public RightClickMenu(VertexFX node) {
        this();
        this.studentInfo = new MenuItem("Student info");
        this.sourceVertex = node;

        menu.getItems().addAll(studentInfo, change, delete);

        delete.setOnAction(e -> {
            MainPageController.canvasRef.deleteVertexFX(sourceVertex);
        });
        change.setOnAction(e -> {
            MainPageController.canvasRef.changeVertexFXName(sourceVertex);
        });
        studentInfo.setOnAction(e -> {
            MainPageController.canvasRef.studentInfoCard(sourceVertex);
        });
    }

    public RightClickMenu(EdgeFX edge) {
        this();
        this.sourceEdge = edge;

        menu.getItems().addAll(change, delete);

        change.setOnAction(e -> {
            MainPageController.canvasRef.changeRepOrRelationTypeFX(sourceEdge);
        });
        delete.setOnAction(e -> {
            MainPageController.canvasRef.deleteEdgeFX(sourceEdge, true);
        });
    }

    public ContextMenu getMenu() {
        return menu;
    }
}
