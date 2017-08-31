package com.siso.todolist;

import com.siso.todolist.datamodel.ToDoData;
import com.siso.todolist.datamodel.TodoItem;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML private ListView<TodoItem> toDoListView;
    @FXML private TextArea textArea;
    @FXML private Label deadlineLabel;
    @FXML private BorderPane mainBorderPane;
    @FXML private ContextMenu listContextMenu;
    @FXML private ToggleButton toolTipToggle;

    private FilteredList<TodoItem> filteredList;

    private Predicate<TodoItem> noFilter = new Predicate<TodoItem>() {
        @Override
        public boolean test(TodoItem todoItem) {
            return true;
        }
    };
    private Predicate<TodoItem> noPastFilter = new Predicate<TodoItem>() {
        @Override
        public boolean test(TodoItem todoItem) {
            if (todoItem.getDeadline().isAfter(LocalDate.now().minusDays(1))){
                return true;
            } else {
                return false;
            }
        }
    };

    public void initialize(){
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TodoItem item = toDoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);
        toDoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observable, TodoItem oldValue, TodoItem newValue) {
                if(newValue != null){
                    TodoItem item = toDoListView.getSelectionModel().getSelectedItem();
                    textArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });



        //Event handler for key pressed for Delete !
        toDoListView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                TodoItem selectedItem = toDoListView.getSelectionModel().getSelectedItem();
                if(selectedItem != null){
                    if (event.getCode().equals(KeyCode.DELETE)){
                        deleteItem(selectedItem);
                    }
                }
            }
        });


        filteredList = new FilteredList<TodoItem>(ToDoData.getInstance().getTodoItems(), noFilter);


        SortedList<TodoItem> sortedList = new SortedList<TodoItem>(filteredList,
                new Comparator<TodoItem>() {
                    @Override
                    public int compare(TodoItem o1, TodoItem o2) {
                        if (o1.equals(02)){
                            return 0;
                        }
                        int dateComparison = o1.getDeadline().compareTo(o2.getDeadline());
                        return dateComparison;
                    }
                });

        toDoListView.setItems(sortedList);
        toDoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        toDoListView.getSelectionModel().selectFirst();


        toDoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> param) {
                ListCell<TodoItem> cell = new ListCell<TodoItem>(){

                    @Override
                    protected void updateItem(TodoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if (item.getDeadline().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            } else if(item.getDeadline().equals(LocalDate.now().plusDays(1))){
                                setTextFill(Color.BLUE);
                            } else if(item.getDeadline().isBefore(LocalDate.now())){
                                setTextFill(Color.DARKGRAY);
                            }
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }
                        });
                return cell;
            }
        });
        }
        //END OF INITIALIZE



        ///SHOW THE DIALOG ! SET IT AS THE ROOT !
        @FXML
        public void showNewItemDialog() {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(mainBorderPane.getScene().getWindow());
            dialog.setTitle("Add New To Do Item");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
            try {
                dialog.getDialogPane().setContent(fxmlLoader.load());

            } catch (IOException e) {
                System.out.println("Couldn't load dialog");
                e.printStackTrace();
                return;
            }

            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DialogController controller = fxmlLoader.getController();
                TodoItem newItem = controller.processResults();
//                toDoListView.getItems().setAll(ToDoData.getInstance().getTodoItems());
                toDoListView.getSelectionModel().select(newItem);
            }
        }
        public void deleteItem(TodoItem item){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Todo Item");
            alert.setHeaderText("Delete item: "+ item.getShortDescription());
            alert.setContentText("Are you sure you want to delete the item ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK){
                ToDoData.getInstance().deleteToDoItem(item);
            }
        }

        @FXML
        public void toolTipToggle(){
            TodoItem selectedItem = toDoListView.getSelectionModel().getSelectedItem();
            if(toolTipToggle.isSelected()){
                filteredList.setPredicate(noPastFilter);
                if(filteredList.isEmpty()){
                    textArea.clear();
                    deadlineLabel.setText("");
                } else if(filteredList.contains(selectedItem)) {
                    toDoListView.getSelectionModel().select(selectedItem);
                } else {
                    toDoListView.getSelectionModel().selectFirst();
                }
            } else {
                filteredList.setPredicate(noFilter);
                toDoListView.getSelectionModel().select(selectedItem);
            }
        }

        @FXML
        public void handleExit(){
            Platform.exit();
        }







//    @FXML
//    public void handleClickListView(){
//        TodoItem item = toDoListView.getSelectionModel().getSelectedItem();
//        StringBuilder sb = new StringBuilder(item.getDetails());
//        textArea.setText(sb.toString());
//        deadlineLabel.setText(item.getDeadline().toString());
//
//    }





}



