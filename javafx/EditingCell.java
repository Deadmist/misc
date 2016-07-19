import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Provides a table cell that is editable with a textfield and supports committing the new value when the textfield is
 * exited. (The default TableCell only commits on pressing "Enter")<br>
 * Supports any Object for which a StringConverter can be supplied.<br>
 * This class also provides static methods that can be used with TableColumn.setCellFactory().<br>
 * <p>
 * Creating a column with EditingCells, both for Strings and Integers:
 * <pre>
 * {@code
 *      TableColumn<Person, String> name;
 *      TableColumn<Person, Integer> age;
 *
 *      //Set cellfactorys to EditingCells
 *      name.setCellFactory(EditingCell.factory());
 *      age.setCellFactory(EditingCell.factory(new IntegerStringConverter())); //IntegerStringConverter is provided by javafx
 *
 *      //Set event handler to update person items on changes
 *      name.setOnEditCommit(event -> {
 *          Person p = event.getTableView().getItems().get(event.getTablePosition().getRow());
 *          p.setName(event.getNewValue());
 *      });
 *
 *      age.setOnEditCommit(event -> {
 *          Person p = event.getTableView().getItems().get(event.getTablePosition().getRow());
 *          p.setAge(event.getNewValue());  //Age is correctly returned as Integer
 *      });
 *
 *     }
 *
 * </pre>
 * <p>
 * Credit to <a href="http://stackoverflow.com/a/25118925">Minas Mina</a> for providing most of this class.<br>
 * My additions:
 * <ul>
 * <li> Support for StringConverter</li>
 * <li> VBox so the TextField fills the whole width of the cell</li>
 * <li> Methods for creating factories</li>
 * <li> Textfield automatically gets focus when shown, type away! </li>
 * </ul>
 *
 * @author Deadmist
 */
public class EditingCell<S, T> extends TableCell<S, T> {

    private final TextField editTextField;
    private final VBox vBox;

    StringConverter<T> converter;

    /**
     * Creates a new EditingCell.
     *
     * @param con StringConverter to convert the object into a string for the textbox
     */
    public EditingCell(StringConverter<T> con) {

        super();
        editTextField = new TextField();
        vBox = new VBox(editTextField);

        //Make sure the vbox expands to fill the whole cell width
        vBox.prefWidthProperty().bind(this.widthProperty());
        vBox.setMaxWidth(Double.MAX_VALUE);

        //Create a nice padding on the right side, the left is padded automatically (for some reason)
        vBox.setPadding(new Insets(0, 7, 0, 0));

        converter = con;

        editTextField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER))
                commitEdit(converter.fromString(editTextField.getText()));
        });

        editTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                commitEdit(converter.fromString(editTextField.getText()));
        });

        editTextField.textProperty().bindBidirectional(textProperty());
    }

    /**
     * Creates a new EditingCell, only works with String
     */
    public EditingCell() {

        super();

        editTextField = new TextField();
        vBox = new VBox(editTextField);

        //Make sure the vbox expands to fill the whole cell width
        vBox.prefWidthProperty().bind(this.widthProperty());
        vBox.setMaxWidth(Double.MAX_VALUE);

        //Create a nice padding on the right side, the left is padded automatically (for some reason)
        vBox.setPadding(new Insets(0, 7, 0, 0));

        editTextField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER))
                commitEdit((T) editTextField.getText());
        });

        editTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                commitEdit((T) editTextField.getText());
        });

        editTextField.textProperty().bindBidirectional(textProperty());
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setGraphic(vBox);
        editTextField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(null);
    }

    @Override
    public void updateItem(final T item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (item == null) {
                setGraphic(null);
            } else {
                if (isEditing()) {
                    setGraphic(vBox);
                    editTextField.requestFocus();
                    String text;
                    if (converter != null) {
                        text = converter.toString(getItem());
                    } else {
                        text = (String) getItem();
                    }

                    setText(text);
                } else {
                    setGraphic(null);
                    String text;
                    if (converter != null) {
                        text = converter.toString(getItem());
                    } else {
                        text = (String) getItem();
                    }

                    setText(text);
                }
            }
        }
    }

    /**
     * This returns a callback that produces EditingCells,
     * use this together with TableColumn.setCellFactory().
     *
     * @param <A> Type of the Table<b>Row</b> item
     * @param <B> Type of content of the TableCell. <b>This factory only supports String!</b>
     *            Use the other factory method for other types
     * @return Callback producing EditingCells
     */
    public static <A, B> Callback<TableColumn<A, B>, TableCell<A, B>> factory() {
        return param -> new EditingCell<>();
    }

    /**
     * This returns a callback that produces EditingCells,
     * use this together with TableColumn.setCellFactory().
     *
     * @param converter StringConverter to convert the type of the TableColumn into a String and back.<br>
     *                  javafx.util.converter.* probably already has a converter for most standard types
     * @param <A>       Type of the Table<b>Row</b> item
     * @param <B>       Type of the content of the TableCell
     * @return Callback returning EditingCells
     */
    public static <A, B> Callback<TableColumn<A, B>, TableCell<A, B>> factory(StringConverter<B> converter) {
        return param -> new EditingCell<>(converter);
    }
}
