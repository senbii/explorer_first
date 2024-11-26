package explorer.controller;

import explorer.model.ExplorerModel;
import explorer.view.ExplorerView;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class ExplorerController {
    private final ExplorerModel model;
    private final ExplorerView view;

    // Иконки
    private final Image folderIcon = new Image("file:src/resources/icons/folder.png");
    private final Image fileIcon = new Image("file:src/resources/icons/file.png");
    private final Image driveIcon = new Image("file:src/resources/icons/drive.png");

    // Иконки для типов файлов
    private final Map<String, Image> fileTypeIcons = new HashMap<>();

    // Clipboard for cut and paste operations
    private File clipboardFile = null;
    private boolean isCut = false;

    public ExplorerController(ExplorerModel model, ExplorerView view) {
        this.model = model;
        this.view = view;
        initializeIcons();
        initialize();
    }

    private void initializeIcons() {
        fileTypeIcons.put("jpg", new Image("file:src/resources/icons/image.png"));
        fileTypeIcons.put("jpeg", new Image("file:src/resources/icons/image.png"));
        fileTypeIcons.put("png", new Image("file:src/resources/icons/image.png"));
        fileTypeIcons.put("txt", new Image("file:src/resources/icons/text.png"));
        fileTypeIcons.put("doc", new Image("file:src/resources/icons/word.png"));
        fileTypeIcons.put("docx", new Image("file:src/resources/icons/word.png"));
        // Добавьте дополнительные типы файлов при необходимости
    }

    private void initialize() {
        updateView();

        // Настроим контекстное меню
        createContextMenu();

        view.getFileListView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleDoubleClick();
            } else if (event.getButton() == MouseButton.SECONDARY) {
                showContextMenu(event.getX(), event.getY());
            }
        });

        view.getBackButton().setOnAction(event -> handleBackButtonClick());

        // Используем кастомный ListCell для отображения иконок
        view.getFileListView().setCellFactory(listView -> new FileListCell());
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot > 0) ? name.substring(lastDot + 1).toLowerCase() : "";
    }

    private void handleDoubleClick() {
        String selectedItem = view.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            File selectedFile = new File(model.getCurrentDirectory(), selectedItem);
            if (selectedFile.isDirectory()) {
                model.navigateToDirectory(selectedFile);
                updateView();
            } else if (model.getCurrentDirectory() == null) {
                // Если мы находимся на уровне дисков, переходим в корень выбранного диска
                File root = new File(selectedItem);
                if (root.exists() && root.isDirectory()) {
                    model.navigateToDirectory(root);
                    updateView();
                }
            }
        }
    }

    private void handleBackButtonClick() {
        if (model.getCurrentDirectory() == null) {
            return; // Если мы на уровне дисков, ничего не делаем
        }

        File parent = model.getCurrentDirectory().getParentFile();
        if (parent != null) {
            model.navigateToDirectory(parent);
        } else {
            model.navigateToRoot(); // Вернуться к выбору дисков
        }
        updateView();
    }

    private void updateView() {
        File currentDir = model.getCurrentDirectory();
        String path = (currentDir == null) ? "Drives" : currentDir.getAbsolutePath();
        view.getCurrentPathLabel().setText("Current Directory: " + path);

        // Получаем список дисков, если на начальном экране
        if (currentDir == null) {
            // Добавляем доступные диски
            File[] drives = File.listRoots();
            view.getFileListView().setItems(FXCollections.observableArrayList(
                    drives != null ? getDriveNames(drives) : new String[0]
            ));
        } else {
            // Если мы не на начальном экране, показываем файлы в текущей директории
            view.getFileListView().setItems(FXCollections.observableArrayList(
                model.getFilesInCurrentDirectory().stream()
                    .map(File::getName)
                    .toList()
            ));
        }
    }

    private String[] getDriveNames(File[] drives) {
        String[] driveNames = new String[drives.length];
        for (int i = 0; i < drives.length; i++) {
            driveNames[i] = drives[i].getPath();
        }
        return driveNames;
    }

    // Класс для отображения ячеек в ListView
    private class FileListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                File file = new File(model.getCurrentDirectory(), item);
                setText(item);

                ImageView iconView = null;

                if (file.isDirectory()) {
                    iconView = new ImageView(folderIcon);
                } else if (file.isFile()) {
                    String extension = getFileExtension(file);
                    iconView = new ImageView(fileTypeIcons.getOrDefault(extension, fileIcon));
                } else if (model.getCurrentDirectory() == null) {
                    // Если мы на уровне дисков, отображаем диск с названием
                    iconView = new ImageView(driveIcon);
                    setText(file.getPath().substring(0, 2));  // Отображаем только букву диска (например, "C:\")
                }

                if (iconView != null) {
                    iconView.setFitWidth(17);
                    iconView.setFitHeight(17);
                    setGraphic(iconView);
                }
            }
        }
    }

    // Создание контекстного меню с действиями
    private void createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        // Переименование
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(event -> handleRename());
        
        // Копирование
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(event -> handleCopy());
        
        // Вырезание
        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setOnAction(event -> handleCut());
        
        // Удаление
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> handleDelete());
        
        // Вставка
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setOnAction(event -> handlePaste());

        contextMenu.getItems().addAll(renameItem, copyItem, cutItem, deleteItem, pasteItem);
        view.getFileListView().setContextMenu(contextMenu);
    }

    private void showContextMenu(double x, double y) {
        ContextMenu contextMenu = (ContextMenu) view.getFileListView().getContextMenu();
        contextMenu.show(view.getFileListView(), x, y);
    }

    private void handleRename() {
        String selectedItem = view.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            File selectedFile = new File(model.getCurrentDirectory(), selectedItem);
            if (selectedFile.isFile() || selectedFile.isDirectory()) {
                // Вставьте логику переименования
                // Например, откройте диалог для ввода нового имени
            }
        }
    }

    private void handleCopy() {
        String selectedItem = view.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            File selectedFile = new File(model.getCurrentDirectory(), selectedItem);
            if (selectedFile.exists()) {
                clipboardFile = selectedFile;
                isCut = false;
            }
        }
    }

    private void handleCut() {
        String selectedItem = view.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            File selectedFile = new File(model.getCurrentDirectory(), selectedItem);
            if (selectedFile.exists()) {
                clipboardFile = selectedFile;
                isCut = true;
            }
        }
    }

    private void handleDelete() {
        String selectedItem = view.getFileListView().getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            File selectedFile = new File(model.getCurrentDirectory(), selectedItem);
            if (selectedFile.exists() && selectedFile.delete()) {
                updateView();
            }
        }
    }

    private void handlePaste() {
        if (clipboardFile != null && model.getCurrentDirectory() != null) {
            File destination = new File(model.getCurrentDirectory(), clipboardFile.getName());
            try {
                if (isCut) {
                    Files.move(clipboardFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    isCut = false;
                } else {
                    Files.copy(clipboardFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                updateView();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
