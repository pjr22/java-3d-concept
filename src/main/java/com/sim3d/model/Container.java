package com.sim3d.model;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Container extends GameObject {
    private boolean isOpen;
    private List<String> items;

    public Container(String id, String name) {
        super(id, name);
        this.isOpen = false;
        this.items = new ArrayList<>();
        this.modelType = "cube";
    }

    public Container(String id, String name, String model, Transform transform, Vector3f color) {
        super(id, name);
        this.isOpen = false;
        this.items = new ArrayList<>();
        this.modelType = model != null ? model : "cube";
        if (transform != null) this.transform = transform;
        if (color != null) this.color.set(color);
    }

    @Override
    public void update(float deltaTime) {
    }

    public void open() {
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void addItem(String item) {
        items.add(item);
    }

    public boolean removeItem(String item) {
        return items.remove(item);
    }

    public List<String> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean hasItem(String item) {
        return items.contains(item);
    }

    public int getItemCount() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
