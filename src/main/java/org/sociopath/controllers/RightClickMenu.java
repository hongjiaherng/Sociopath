package org.sociopath.controllers;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import org.sociopath.controllers.GraphSimulationController.VertexFX;

public class RightClickMenu {

    ContextMenu menu;
    VertexFX sourceNode;
    MenuItem delete, changeName, studentInfo;

    public RightClickMenu() {
        this.menu = new ContextMenu();
        this.delete = new MenuItem("Delete");
        this.changeName = new MenuItem("Change name");
        this.studentInfo = new MenuItem("Student info");

        menu.getItems().addAll(studentInfo, delete, changeName);
        menu.setOpacity(0.9);
    }

    public RightClickMenu(VertexFX node) {
        this();
        this.sourceNode = node;
        delete.setOnAction(e -> {
//            MainPageController.canvasRef.deleteVertex(sourceNode);
        });
        changeName.setOnAction(e -> {
//            MainPageController.canvasRef.changeName(sourceNode);
        });
        studentInfo.setOnAction(e -> {
//
        });
    }

    public ContextMenu getMenu() {
        return menu;
    }
}
