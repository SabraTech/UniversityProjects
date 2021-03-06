package eg.edu.alexu.csd.oop.db.sql.parser.parameters;

import eg.edu.alexu.csd.oop.db.sql.parser.MyEntry;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class UpdateParameters.
 */
public class UpdateParameters {

  /** The table name. */
  private String tableName;

  /** The required. */
  private ArrayList<MyEntry<String, String>> required;

  /** The column to compare. */
  private String columnToCompare;

  /** The comparing char. */
  private String comparingChar;

  /** The value to compare to. */
  private String valueToCompareTo;

  /** The boolean to know if the query is conditional. */
  private boolean queryIsConditional;

  /**
   * Instantiates a new update parameters for conditional query.
   *
   * @param required
   *          the required
   * @param tableName
   *          the table name
   * @param columnName
   *          the column name
   * @param compareChar
   *          the compare char
   * @param value
   *          the value
   */
  public UpdateParameters(ArrayList<MyEntry<String, String>> required, String tableName,
      String columnName, String compareChar, String value) {
    this.required = required;
    this.tableName = tableName;
    this.columnToCompare = columnName;
    this.comparingChar = compareChar;
    this.valueToCompareTo = value;
    queryIsConditional = true;
  }

  /**
   * Instantiates a new update parameters for a query that is not conditional.
   *
   * @param required
   *          the required
   * @param tableName
   *          the table name
   */
  public UpdateParameters(ArrayList<MyEntry<String, String>> required, String tableName) {
    this.required = required;
    this.tableName = tableName;
    queryIsConditional = false;
  }

  /**
   * Gets the required columns names and values.
   *
   * @return the required columns names and values
   */
  public ArrayList<MyEntry<String, String>> getRequiredColumnsNamesAndValues() {
    return required;
  }

  /**
   * Gets the table name.
   *
   * @return the table name
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Gets the column to compare to name.
   *
   * @return the column to compare to name
   */
  public String getColumnToCompareToName() {
    if (!queryIsConditional) {
      throw new RuntimeException("not provided");
    }
    return columnToCompare;
  }

  /**
   * Gets the comparing char.
   *
   * @return the comparing char
   */
  public String getComparingChar() {
    if (!queryIsConditional) {
      throw new RuntimeException("not provided");
    }
    return comparingChar;
  }

  /**
   * Gets the value to compare to.
   *
   * @return the value to compare to
   */
  public String getValueToCompareTo() {
    if (!queryIsConditional) {
      throw new RuntimeException("not provided");
    }
    return valueToCompareTo;
  }
}
