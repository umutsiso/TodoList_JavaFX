package com.siso.todolist;

import com.siso.todolist.datamodel.ToDoData;
import com.siso.todolist.datamodel.TodoItem;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {
    @FXML private TextField shortDescriptionField;
    @FXML private TextArea detailsArea;
    @FXML private DatePicker deadlinePicker;

    public TodoItem processResults(){
        String shortDescription = shortDescriptionField.getText().trim();
        String details = detailsArea.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();
        TodoItem newItem = new TodoItem(shortDescription, details, deadlineValue);
        ToDoData.getInstance().addToDoItem(new TodoItem(shortDescription, details, deadlineValue));
        return newItem;
    }

}
