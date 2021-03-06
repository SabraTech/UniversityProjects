package eg.edu.alexu.csd.oop.db;

import eg.edu.alexu.csd.oop.db.sql.parser.*;
import eg.edu.alexu.csd.oop.db.sql.parser.parameters.*;
import eg.edu.alexu.csd.oop.db.xml.XmlHandler;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.filechooser.FileSystemView;

// TODO: Auto-generated Javadoc
/**
 * The Class Engine.
 */
public class Engine implements Database {

  /** The parser and validator. */
  private QueryValidatorAndParser parserAndValidator;

  /** The table. */
  private Object[][] table;

  /** The home path. */
  private File home;

  /** The databases directory. */
  private String databasesDirectory;

  /** The current data base directory. */
  private String currentDataBaseDirectory;

  /** The save and load handler. */
  private XmlHandler saveAndLoadHandler;

  /** The current table rows. */
  private ArrayList<Object[]> currentTableRows;

  /** The columns and their data types. */
  private String[] columnsNames, columnsDataTypes;

  /** The tables names and columns count. */
  private Map<String, Integer> tablesNamesAndColumnsCount;

  /**
   * Instantiates a new engine.
   */
  public Engine() {
    parserAndValidator = new QueryValidatorAndParser();
    table = null;
    home = FileSystemView.getFileSystemView().getHomeDirectory();
    databasesDirectory = home.getAbsolutePath() + File.separatorChar;
    saveAndLoadHandler = new XmlHandler();
    currentTableRows = null;
    tablesNamesAndColumnsCount = new HashMap<String, Integer>();
  }

  /**
   * creates database. returns true in case of success, false otherwise.
   *
   * @param databaseName          the database name.
   * @param dropIfExists          a boolean to know whether it's required to drop the database in
   *          case it exists.
   * @return the string
   */
  @Override
  public String createDatabase(String databaseName, boolean dropIfExists) {
    // TODO Auto-generated method stub
    String databasePath = databasesDirectory + databaseName + File.separatorChar;
    File file = new File(databasePath);
    if (!dropIfExists) {
      if (!file.exists()) {
        if (file.mkdir()) {
          currentDataBaseDirectory = databasePath;
          return currentDataBaseDirectory;
        } else {
          throw new RuntimeException("unable to create database");
        }
      } else {
        currentDataBaseDirectory = databasePath;
        return currentDataBaseDirectory;
      }
    } else {
      if (file.exists()) {
        if (file.isDirectory()) {
          // list of all files in it
          String[] files = file.list();
          for (String temp : files) {
            File dummyFile = new File(file, temp);
            dummyFile.delete();
          }
        }
        currentDataBaseDirectory = databasePath;
        return currentDataBaseDirectory;
      } else {
        if (file.mkdir()) {
          currentDataBaseDirectory = databasePath;
          return currentDataBaseDirectory;
        } else {
          throw new RuntimeException("unable to create database");
        }
      }
    }
  }

  /**
   * executes structure query. returns true in case of success, false otherwise.
   *
   * @param query          the required query.
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  @Override
  public boolean executeStructureQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsCreateDataBase(query)) {
      String databaseName = parserAndValidator.getDatabaseName(query);
      createDatabase(databaseName, false);
      return true;
    }
    if (parserAndValidator.queryIsDropDataBase(query)) {
      String databaseName = parserAndValidator.getDataBaseNameToDrop(query);
      createDatabase(databaseName, true);
      return true;
    }
    if (parserAndValidator.queryIsCreateTable(query)) {
      return createTable(query);
    } else if (parserAndValidator.queryIsDropTable(query)) {
      String tableName = parserAndValidator.getTableToDropName(query);
      if (!tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        return false;
      }
      if (saveAndLoadHandler.clearTable(currentDataBaseDirectory, tableName)) {
        tablesNamesAndColumnsCount.remove(tableName.toLowerCase());
        return true;
      } else {
        return false;
      }
    } else {
      throw new SQLException();
    }

  }

  /**
   * executes a query. returns an object array of the selected data.
   *
   * @param query          the query.
   * @return the object[][]
   * @throws SQLException the SQL exception
   */
  @Override
  public Object[][] executeQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsSelection(query)) {
      return carrySelectionQuery(query);
    } else {
      throw new RuntimeException("Invalid query " + query);
    }
  }

  /**
   * executes an update query.
   *
   * @param query          the required query.
   * @return the int
   * @throws SQLException the SQL exception
   */
  @Override
  public int executeUpdateQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsInsertion(query)) {
      return carryInsertionQuery(query);
    } else if (parserAndValidator.queryIsDeletion(query)) {
      return carryDeleteQuery(query);
    } else if (parserAndValidator.queryIsUpdate(query)) {
      return carryUpdateQuery(query);
    }

    return 0;
  }

  /**
   * Creates the table.
   *
   * @param query the query
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  private boolean createTable(String query) throws SQLException {
    TableCreationParameters creationParameters = parserAndValidator.getAddedTableParameters(query);
    String tableName = creationParameters.getTableName();
    if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
      return false;
    }
    if (currentDataBaseDirectory == null) {
      throw new RuntimeException("No database exist");
    }
    ArrayList<MyEntry<String, Class<?>>> columnsNamesAndClasses = creationParameters
        .getColumnsNamesAndClasses();
    columnsNames = new String[columnsNamesAndClasses.size()];
    columnsDataTypes = new String[columnsNamesAndClasses.size()];
    int counter = 0;
    for (MyEntry<String, Class<?>> dummyEntry : columnsNamesAndClasses) {
      columnsNames[counter] = dummyEntry.getFirst();
      if (dummyEntry.getSecond().getSimpleName().equals(String.class.getSimpleName())) {
        columnsDataTypes[counter] = "varchar";
      } else if (dummyEntry.getSecond().getSimpleName().equals(Integer.class.getSimpleName())) {
        columnsDataTypes[counter] = "int";
      }
      counter++;
    }
    currentTableRows = new ArrayList<Object[]>();
    table = new Object[1][columnsNames.length];
    tablesNamesAndColumnsCount.put(tableName.toLowerCase(), columnsNames.length);
    saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory, table,
        tableName.toLowerCase());
    return true;
  }

  /**
   * Carry selection query.
   *
   * @param query the query
   * @return the object[][]
   * @throws SQLException the SQL exception
   */
  private Object[][] carrySelectionQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsSelectAll(query)) {
      String tableName = parserAndValidator.getSelectAllFromTableParameters(query);
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] dataTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        Object[][] returnedTable = new Object[dataTable.length - 1][dataTable[0].length];
        for (int counter = 1; counter < dataTable.length; counter++) {
          for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
            returnedTable[counter - 1][counter2] = dataTable[counter][counter2];
          }
        }
        return returnedTable;
      }
    } else if (parserAndValidator.queryIsSelectWithCondition(query)) {
      SelectionParameters temp = parserAndValidator.getSelectColumnWithConditionParameters(query);
      String columnToSelect = temp.getColumnToSelectName();
      String tableName = temp.getTableName();
      String columnToCompareWithName = temp.getColumnToCompareName();
      String compareChar = temp.getComparingChar();
      String value = temp.getValueToCompareTo();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
      } else {
        throw new SQLException("no table with such a name");
      }
      int columnToCompareWithIndex = -1;
      int columnToSelectIndex = -1;
      for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
        if (columnsNames[counter2].equalsIgnoreCase(columnToCompareWithName)) {
          columnToCompareWithIndex = counter2;
        }
        if (columnsNames[counter2].equalsIgnoreCase(columnToSelect)) {
          columnToSelectIndex = counter2;
        }
      }
      if (columnToCompareWithIndex == -1) {
        throw new SQLException("no column with such a name exists" + columnToCompareWithName);
      }
      if (columnToSelectIndex == -1) {
        throw new SQLException("no column with such a name exists" + columnToSelect);
      }
      String typeOfColumnToCompareWith = columnsDataTypes[columnToCompareWithIndex];
      ArrayList<Object> selected = new ArrayList<Object>();
      if (typeOfColumnToCompareWith.equalsIgnoreCase("varChar")) {
        for (int counter = 1; counter < currentTableRows.size(); counter++) {
          if (((String) currentTableRows.get(counter)[columnToCompareWithIndex]).equalsIgnoreCase(
              value)) {
            selected.add((String) currentTableRows.get(counter)[columnToSelectIndex]);
          }
        }
        Object[][] finalTable = new Object[selected.size()][1];
        for (int counter = 0; counter < selected.size(); counter++) {
          finalTable[counter][0] = selected.get(counter);
        }
        return finalTable;
      } else {
        int compareInt = Integer.parseInt(value);
        if (compareChar.equals("<")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) < compareInt) {
              selected.add(currentTableRows.get(counter)[columnToSelectIndex]);
            }
          }
        } else if (compareChar.equals("=")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) == compareInt) {
              selected.add(currentTableRows.get(counter)[columnToSelectIndex]);
            }
          }
        } else if (compareChar.equals(">")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) > compareInt) {
              selected.add(currentTableRows.get(counter)[columnToSelectIndex]);
            }
          }
        }
        Object[][] finalTable = new Object[selected.size()][1];
        for (int counter = 0; counter < selected.size(); counter++) {
          finalTable[counter][0] = selected.get(counter);
        }
        return finalTable;
      }

    } else if (parserAndValidator.queryIsSelectAllWithCondition(query)) {
      SelectionParameters temp = parserAndValidator.getSelectAllWithConditionParameters(query);
      String tableName = temp.getTableName();
      String columnToCompareWithName = temp.getColumnToCompareName();
      columnToCompareWithName = columnToCompareWithName.replace(" ", "");
      String compareChar = temp.getComparingChar();
      String value = temp.getValueToCompareTo();
      value = value.replace(" ", "");
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
      } else {
        throw new SQLException("no table with such a name");
      }
      int columnToCompareWithIndex = -1;
      for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
        if (columnsNames[counter2].equalsIgnoreCase(columnToCompareWithName.replace(" ", ""))) {
          columnToCompareWithIndex = counter2;
        }
      }
      if (columnToCompareWithIndex == -1) {
        throw new SQLException("no column with such a name exists" + columnToCompareWithName);
      }
      String typeOfColumnToCompareWith = columnsDataTypes[columnToCompareWithIndex];
      ArrayList<Object[]> selected = new ArrayList<Object[]>();
      if (typeOfColumnToCompareWith.equalsIgnoreCase("varChar")) {
        for (int counter = 1; counter < currentTableRows.size(); counter++) {
          if (((String) currentTableRows.get(counter)[columnToCompareWithIndex]).equalsIgnoreCase(
              value)) {
            selected.add(currentTableRows.get(counter));
          }
        }
        Object[][] finalTable = new Object[selected.size()][];
        for (int counter = 0; counter < selected.size(); counter++) {
          finalTable[counter] = selected.get(counter);
        }
        return finalTable;
      } else {
        int compareInt = Integer.parseInt(value);
        if (compareChar.equals("<")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) < compareInt) {
              selected.add(currentTableRows.get(counter));
            }
          }
        } else if (compareChar.equals("=")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) == compareInt) {
              selected.add(currentTableRows.get(counter));
            }
          }
        } else if (compareChar.equals(">")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) > compareInt) {
              selected.add(currentTableRows.get(counter));
            }
          }
        }
        Object[][] finalTable = new Object[selected.size()][];
        for (int counter = 0; counter < selected.size(); counter++) {
          finalTable[counter] = selected.get(counter);
        }
        return finalTable;
      }

    } else if (parserAndValidator.queryIsSelectWithNames(query)) {
      SelectionParameters queryParameters = parserAndValidator.getSelectWithNameParameters(query);
      ArrayList<String> columnsToBeSelectedNames = queryParameters.getColumnsToSelectNames();
      String tableName = queryParameters.getTableName();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tableData = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        int[] index = new int[columnsToBeSelectedNames.size()];
        Arrays.fill(index, -1);
        for (int counter = 0; counter < columnsToBeSelectedNames.size(); counter++) {
          for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
            if (columnsToBeSelectedNames.get(counter).equalsIgnoreCase(columnsNames[counter2])) {
              index[counter] = counter2;
            }
          }
          if (index[counter] == -1) {
            throw new SQLException("invalid column name " + columnsToBeSelectedNames.get(counter));
          }
        }
        Object[][] requiredTable = new Object[tableData.length - 1][columnsToBeSelectedNames
            .size()];
        for (int counter = 1; counter < columnsNames.length; counter++) {
          for (int counter2 = 0; counter2 < index.length; counter2++) {
            requiredTable[counter - 1][counter2] = tableData[counter][index[counter2]];
          }
        }
        return requiredTable;
      }
    }
    return null;
  }

  /**
   * Carry insertion query.
   *
   * @param query the query
   * @return the int
   * @throws SQLException the SQL exception
   */
  private int carryInsertionQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsInsertIntoTableColumnWithValues(query)) {
      InsertionParameters temp = parserAndValidator.getInsertIntoTableColumnsWithValuesParameters(
          query);
      String tableName = temp.getTableName();
      ArrayList<String> columns = temp.getColumnsNames();
      ArrayList<Object> values = temp.getValues();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())
          && tablesNamesAndColumnsCount.get(tableName.toLowerCase()) >= columns.size()) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
        Object[] dummyRow = new Object[columnsNames.length];
        for (int counter = 0; counter < columns.size(); counter++) {
          boolean found = false;
          for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
            if (columns.get(counter).equalsIgnoreCase(columnsNames[counter2])) {
              dummyRow[counter2] = values.get(counter);
              found = true;
            }
          }
          if (!found) {
            throw new SQLException("column not found name = " + columns.get(counter));
          }
        }
        currentTableRows.add(dummyRow);
        Object[][] newTable = new Object[currentTableRows.size()][];
        for (int counteri = 0; counteri < currentTableRows.size(); counteri++) {
          newTable[counteri] = currentTableRows.get(counteri);
        }
        saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
            newTable, tableName.toLowerCase());
        return 1;
      }
    } else if (parserAndValidator.queryIsInsertIntoTableValues(query)) {
      InsertionParameters temp = parserAndValidator.getInsertIntoTableValuesParameters(query);
      String tableName = temp.getTableName();
      ArrayList<Object> values = temp.getValues();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())
          && tablesNamesAndColumnsCount.get(tableName.toLowerCase()) == values.size()) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
        Object[] columns = new Object[values.size()];
        for (int counter = 0; counter < values.size(); counter++) {
          columns[counter] = values.get(counter);
        }
        currentTableRows.add(columns);

        Object[][] newTable = new Object[currentTableRows.size()][];
        for (int counter = 0; counter < currentTableRows.size(); counter++) {
          newTable[counter] = currentTableRows.get(counter);
        }
        saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
            newTable, tableName.toLowerCase());
        return 1;
      }

    }
    return 0;
  }

  /**
   * Carry delete query.
   *
   * @param query the query
   * @return the int
   * @throws SQLException the SQL exception
   */
  private int carryDeleteQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsConditionalDelete(query)) {
      ConditionalDeleteParameters temp = parserAndValidator.getConditionalDeleteParameters(query);
      String tableName = temp.getTableName();
      String columnToCompareTo = temp.getColumnToCompareToName();
      String compareChar = temp.getComparingChar();
      String value = temp.getValueToCompareTo();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
        int index = -1;
        for (int counter = 0; counter < columnsNames.length; counter++) {
          if (columnsNames[counter].equalsIgnoreCase(columnToCompareTo)) {
            index = counter;
            break;
          }
        }
        if (index == -1) {
          throw new SQLException("column with name " + columnToCompareTo + " not found ");
        }

        String typeOfColumnToDelete = columnsDataTypes[index];
        if (typeOfColumnToDelete.equalsIgnoreCase("varchar")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((String) currentTableRows.get(counter)[index]).equalsIgnoreCase(value)) {
              currentTableRows.remove(counter);
              counter--;
            }
          }
          Object[][] newTable = new Object[currentTableRows.size()][];
          for (int counter = 0; counter < currentTableRows.size(); counter++) {
            newTable[counter] = currentTableRows.get(counter);
          }
          saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
              newTable, tableName.toLowerCase());
          return tempTable.length - currentTableRows.size();
        } else {
          int compareInt = Integer.parseInt(value);
          int comparingFlag;
          if (compareChar.equals("<")) {
            comparingFlag = -1;
          } else if (compareChar.equals("=")) {
            comparingFlag = 0;
          } else {
            comparingFlag = 1;
          }
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            int dummy = (Integer) currentTableRows.get(counter)[index];
            switch (comparingFlag) { // less than
            case -1: {
              if (dummy < compareInt) {
                currentTableRows.remove(counter);
                counter--;
              }
              break;
            }
            case 0: {
              if (dummy == compareInt) {
                currentTableRows.remove(counter);
                counter--;
              }
              break;
            }
            case 1: { // greater than
              if (dummy > compareInt) {
                currentTableRows.remove(counter);
                counter--;
              }
              break;
            }
            default:
              break;
            }

          }
          Object[][] newTable = new Object[currentTableRows.size()][];
          for (int counter = 0; counter < currentTableRows.size(); counter++) {
            newTable[counter] = currentTableRows.get(counter);
          }
          saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
              newTable, tableName.toLowerCase());
          return tempTable.length - currentTableRows.size();
        }
      }

    } else if (parserAndValidator.queryIsDeleteAll(query)) {
      String tableName = parserAndValidator.getTableNameToDeleteAll(query);
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        int size = tempTable.length;
        Object[][] newTable = new Object[1][columnsNames.length];
        saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
            newTable, tableName.toLowerCase());
        return size - 1;
      }

    }
    return 0;
  }

  /**
   * Carry update query.
   *
   * @param query the query
   * @return the int
   * @throws SQLException the SQL exception
   */
  private int carryUpdateQuery(String query) throws SQLException {
    if (parserAndValidator.queryIsConditionalUpdate(query)) {
      UpdateParameters parameters = parserAndValidator.getUpdateParameters(query);
      ArrayList<MyEntry<String, String>> columnsAndValues = parameters
          .getRequiredColumnsNamesAndValues();
      String[] columnsToChange = new String[columnsAndValues.size()];
      String[] newValues = new String[columnsAndValues.size()];
      int dummyCounter = 0;
      for (MyEntry<String, String> dummyEntry : columnsAndValues) {
        columnsToChange[dummyCounter] = dummyEntry.getFirst();
        newValues[dummyCounter] = dummyEntry.getSecond();
        dummyCounter++;
      }
      String tableName = parameters.getTableName();
      String columnToCompareWithName = parameters.getColumnToCompareToName();
      String compareChar = parameters.getComparingChar();
      String value = parameters.getValueToCompareTo();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
      } else {
        throw new SQLException("no table with such a name");
      }
      int[] index = new int[columnsToChange.length];
      Arrays.fill(index, -1);
      for (int counter = 0; counter < columnsToChange.length; counter++) {
        for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
          if (columnsNames[counter2].equalsIgnoreCase(columnsToChange[counter])) {
            index[counter] = counter2;
          }
        }
        if (index[counter] == -1) {
          throw new SQLException("no column with such a name exists" + columnsToChange[counter]);
        }
      }
      int columnToCompareWithIndex = -1;
      for (int counter = 0; counter < columnsNames.length; counter++) {
        if (columnsNames[counter].equalsIgnoreCase(columnToCompareWithName)) {
          columnToCompareWithIndex = counter;
          break;
        }
      }
      if (columnToCompareWithIndex == -1) {
        throw new SQLException("no column with such a name exists" + columnToCompareWithName);
      }
      String typeOfColumnToCompareWith = columnsDataTypes[columnToCompareWithIndex];
      if (typeOfColumnToCompareWith.equalsIgnoreCase("varChar")) {
        int edited = 0;
        for (int counter = 1; counter < currentTableRows.size(); counter++) {
          if (((String) currentTableRows.get(counter)[columnToCompareWithIndex]).equalsIgnoreCase(
              value)) {
            edited++;
            Object[] dummy = currentTableRows.get(counter);
            for (int counter2 = 0; counter2 < columnsToChange.length; counter2++) {
              dummy[index[counter2]] = newValues[counter2];
            }
            currentTableRows.set(counter, dummy);
          }
        }
        Object[][] finalTable = new Object[currentTableRows.size()][];
        for (int counter = 0; counter < currentTableRows.size(); counter++) {
          finalTable[counter] = currentTableRows.get(counter);
        }
        saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
            finalTable, tableName.toLowerCase());
        return edited;
      } else {
        int compareInt = Integer.parseInt(value);
        int edited = 0;
        if (compareChar.equals("<")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) < compareInt) {
              edited++;
              Object[] dummy = currentTableRows.get(counter);
              for (int counter2 = 0; counter2 < columnsToChange.length; counter2++) {
                dummy[index[counter2]] = newValues[counter2];
              }
              currentTableRows.set(counter, dummy);
            }
          }
        } else if (compareChar.equals("=")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) == compareInt) {
              edited++;
              Object[] dummy = currentTableRows.get(counter);
              for (int counter2 = 0; counter2 < columnsToChange.length; counter2++) {
                dummy[index[counter2]] = newValues[counter2];
              }
              currentTableRows.set(counter, dummy);
            }
          }
        } else if (compareChar.equals(">")) {
          for (int counter = 1; counter < currentTableRows.size(); counter++) {
            if (((Integer) currentTableRows.get(counter)[columnToCompareWithIndex]) > compareInt) {
              edited++;
              Object[] dummy = currentTableRows.get(counter);
              for (int counter2 = 0; counter2 < columnsToChange.length; counter2++) {
                dummy[index[counter2]] = newValues[counter2];
              }
              currentTableRows.set(counter, dummy);
            }
          }
        }
        Object[][] finalTable = new Object[currentTableRows.size()][];
        for (int counter = 0; counter < currentTableRows.size(); counter++) {
          finalTable[counter] = currentTableRows.get(counter);
        }
        saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
            finalTable, tableName.toLowerCase());
        return edited;
      }
    } else if (parserAndValidator.queryIsUpdateWithoutCondition(query)) {
      UpdateParameters parameters = parserAndValidator.getUpdateWithoutConditionParameters(query);
      ArrayList<MyEntry<String, String>> columnsAndValues = parameters
          .getRequiredColumnsNamesAndValues();
      String[] columnsToChange = new String[columnsAndValues.size()];
      String[] newValues = new String[columnsAndValues.size()];
      int dummyCounter = 0;
      for (MyEntry<String, String> dummyEntry : columnsAndValues) {
        columnsToChange[dummyCounter] = dummyEntry.getFirst();
        newValues[dummyCounter] = dummyEntry.getSecond();
        dummyCounter++;
      }
      String tableName = parameters.getTableName();
      if (tablesNamesAndColumnsCount.containsKey(tableName.toLowerCase())) {
        Object[][] tempTable = saveAndLoadHandler.readXml(currentDataBaseDirectory, tableName
            .toLowerCase());
        columnsNames = saveAndLoadHandler.getFieldsNames();
        columnsDataTypes = saveAndLoadHandler.getFieldsTypes();
        currentTableRows.clear();
        for (int counter = 0; counter < tempTable.length; counter++) {
          currentTableRows.add(tempTable[counter]);
        }
      } else {
        throw new SQLException("no table with such a name");
      }
      int[] index = new int[columnsToChange.length];
      Arrays.fill(index, -1);
      for (int counter = 0; counter < columnsToChange.length; counter++) {
        for (int counter2 = 0; counter2 < columnsNames.length; counter2++) {
          if (columnsNames[counter2].equalsIgnoreCase(columnsToChange[counter])) {
            index[counter] = counter2;
          }
        }
        if (index[counter] == -1) {
          throw new SQLException("no column with such a name exists" + columnsToChange[counter]);
        }
      }
      int edited = 0;
      for (int counter = 1; counter < currentTableRows.size(); counter++) {
        edited++;
        Object[] dummy = currentTableRows.get(counter);
        for (int counter2 = 0; counter2 < columnsToChange.length; counter2++) {
          dummy[index[counter2]] = newValues[counter2];
        }
        currentTableRows.set(counter, dummy);
      }
      Object[][] finalTable = new Object[currentTableRows.size()][];
      for (int counter = 0; counter < currentTableRows.size(); counter++) {
        finalTable[counter] = currentTableRows.get(counter);
      }
      saveAndLoadHandler.writeXml(columnsNames, columnsDataTypes, currentDataBaseDirectory,
          finalTable, tableName.toLowerCase());
      return edited;
    }
    return 0;
  }
}
