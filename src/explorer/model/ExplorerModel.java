package explorer.model;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ExplorerModel {
    private File currentDirectory;

    public ExplorerModel() {
        this.currentDirectory = null;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public List<File> getFilesInCurrentDirectory() {
        if (currentDirectory == null) {
            return Arrays.asList(File.listRoots());
        }

        File[] files = currentDirectory.listFiles();
        if (files != null) {
            return Arrays.stream(files).filter(file -> !file.isHidden()).toList();
        }
        return List.of();
    }

    public boolean navigateToDirectory(File directory) {
        if (directory != null && directory.isDirectory()) {
            currentDirectory = directory;
            return true;
        }
        return false;
    }

    public void navigateToRoot() {
        currentDirectory = null;
    }
}
