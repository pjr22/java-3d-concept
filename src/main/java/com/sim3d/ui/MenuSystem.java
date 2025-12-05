package com.sim3d.ui;

import com.sim3d.graphics.MenuRenderer;
import java.util.List;

public class MenuSystem {
    private boolean menuVisible = false;
    private int selectedOption = 0;
    private final List<String> menuOptions = List.of("Resume", "Exit");
    private MenuRenderer menuRenderer;
    private boolean initialized = false;

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

    public void initialize() {
        if (!initialized) {
            menuRenderer = new MenuRenderer();
            initialized = true;
        }
    }
    
    public void render(int windowWidth, int windowHeight) {
        if (!menuVisible || !initialized) {
            return;
        }

        // Render menu background
        menuRenderer.renderMenuBackground(windowWidth, windowHeight);
        
        // Render menu panel
        menuRenderer.renderMenuPanel(windowWidth, windowHeight);
        
        // Render menu title
        menuRenderer.renderMenuTitle("PAUSED", windowWidth, windowHeight);
        
        // Render menu options
        menuRenderer.renderMenuOptions(menuOptions, selectedOption, windowWidth, windowHeight);
    }
    
    public void cleanup() {
        if (menuRenderer != null) {
            menuRenderer.cleanup();
        }
    }
}
