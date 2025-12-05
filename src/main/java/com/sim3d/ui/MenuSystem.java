package com.sim3d.ui;

import java.util.List;

public class MenuSystem {
    private boolean menuVisible = false;
    private int selectedOption = 0;
    private final List<String> menuOptions = List.of("Resume", "Exit");

    public void toggle() {
        menuVisible = !menuVisible;
        if (menuVisible) {
            selectedOption = 0;
        }
    }

    public void show() {
        menuVisible = true;
        selectedOption = 0;
    }

    public void hide() {
        menuVisible = false;
    }

    public boolean isVisible() {
        return menuVisible;
    }

    public void navigateUp() {
        if (selectedOption > 0) {
            selectedOption--;
        }
    }

    public void navigateDown() {
        if (selectedOption < menuOptions.size() - 1) {
            selectedOption++;
        }
    }

    public int select() {
        return selectedOption;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public String getSelectedOptionText() {
        return menuOptions.get(selectedOption);
    }

    public List<String> getMenuOptions() {
        return menuOptions;
    }

    public void render() {
        if (!menuVisible) {
            return;
        }

        System.out.println("\n=== MENU ===");
        for (int i = 0; i < menuOptions.size(); i++) {
            if (i == selectedOption) {
                System.out.println("> " + menuOptions.get(i) + " <");
            } else {
                System.out.println("  " + menuOptions.get(i));
            }
        }
        System.out.println("============\n");
    }
}
