package com.awesomedroidapps.inappstoragereader.views;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.awesomedroidapps.inappstoragereader.Constants;
import com.awesomedroidapps.inappstoragereader.DatabaseQueryCommandType;
import com.awesomedroidapps.inappstoragereader.ErrorType;
import com.awesomedroidapps.inappstoragereader.R;
import com.awesomedroidapps.inappstoragereader.SqliteDatabaseReader;
import com.awesomedroidapps.inappstoragereader.Utils;
import com.awesomedroidapps.inappstoragereader.entities.QueryDatabaseRequest;
import com.awesomedroidapps.inappstoragereader.entities.TableInfo;
import com.awesomedroidapps.inappstoragereader.helpers.GeneralSqliteHelper;
import com.awesomedroidapps.inappstoragereader.interfaces.ColumnSelectListener;
import com.awesomedroidapps.inappstoragereader.interfaces.ErrorMessageInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity for querying the database.
 * Created by anshul on 27/2/17.
 */

public class QueryDatabaseActivity extends AppCompatActivity implements
    View.OnClickListener, ErrorMessageInterface, ColumnSelectListener,
    AdapterView.OnItemSelectedListener, QueryDatabaseView {

  private Button submitQueryButton;
  private TextView errorMessageTextView;
  private Spinner queryTypeSpinner;
  private TextView fromTableTextView, updateTableTextView, insertTableTextView;
  private Button selectedColumnsButton, whereClauseButton, setClauseButton, valuesClauseButton;
  ArrayList<String> querySpinnerArrayList = new ArrayList<>();
  private EditText rawQueryEditText;
  private TableInfo tableInfo;
  private QueryDatabaseRequest queryDatabaseRequest;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.com_awesomedroidapps_inappstoragereader_activity_query_database);
    submitQueryButton = (Button) findViewById(R.id.com_awesomedroidapps_inappstoragereader_submit);
    errorMessageTextView = (TextView) findViewById(R.id.error_message_textview);
    submitQueryButton.setOnClickListener(this);

    // tableColumnsSpinner = (AppCompatSpinner) findViewById(R.id.spinner_database_table_columns);
    fromTableTextView = (TextView) findViewById(R.id.textview_select_query_table_name);
    updateTableTextView = (TextView) findViewById(R.id.textview_update_query_table_name);
    insertTableTextView = (TextView) findViewById(R.id.textview_insert_query_table_name);
    rawQueryEditText = (EditText) findViewById(R.id
        .com_awesomedroidapps_inappstoragereader_query_editText);

    queryTypeSpinner = (AppCompatSpinner) findViewById(R.id
        .spinner_database_query_command);
    queryTypeSpinner.setOnItemSelectedListener(this);
    selectedColumnsButton = (Button) findViewById(R.id.button_database_table_columns);
    whereClauseButton = (Button) findViewById(R.id.button_where_cause);
    setClauseButton = (Button) findViewById(R.id.set_clause);
    valuesClauseButton = (Button) findViewById(R.id.button_values_cause);
    selectedColumnsButton.setOnClickListener(this);
    whereClauseButton.setOnClickListener(this);
    setClauseButton.setOnClickListener(this);
    valuesClauseButton.setOnClickListener(this);

    queryDatabaseRequest = new QueryDatabaseRequest();
    readBundle();
    initInitialUI();
  }

  private void readBundle() {
    //Read the bundle
    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      tableInfo = (TableInfo) bundle.get(Constants.BUNDLE_TABLE_INFO);
    }
  }

  private void initInitialUI() {
    for (DatabaseQueryCommandType sharedPreferenceDataType : DatabaseQueryCommandType.values()) {
      querySpinnerArrayList.add(sharedPreferenceDataType.getCommand());
    }
    ArrayAdapter adapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
        querySpinnerArrayList);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    queryTypeSpinner.setAdapter(adapter);
    onSelectCommandSelected();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.com_awesomedroidapps_inappstoragereader_query, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    if (item.getItemId() == R.id.com_awesomedroidapps_inappstoragereader_refresh) {
      onSelectCommandSelected();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onStart() {
    super.onStart();
  }


  @Override
  public void onClick(View view) {
    if (view == submitQueryButton) {
      queryDatabase();
    } else if (view == selectedColumnsButton) {
      launchColumnsSelectionDialog();
    } else if (view == whereClauseButton) {
      launchActivityForWhereClauseAndContentValues(Constants.REQUEST_CODE_WHERE_CLAUSE);
    } else if (view == setClauseButton) {
      launchActivityForWhereClauseAndContentValues(Constants.REQUEST_CODE_SET_CLAUSE);
    }else if(view == valuesClauseButton){
      launchActivityForWhereClauseAndContentValues(Constants.REQUEST_CODE_VALUES_CLAUSE);
    }
  }

  private void launchActivityForWhereClauseAndContentValues(int requestCode) {
    Intent intent = new Intent(QueryDatabaseActivity.this, ClauseActivity.class);
    Bundle bundle = new Bundle();
    bundle.putSerializable(Constants.BUNDLE_TABLE_INFO, tableInfo);
    bundle.putInt(Constants.BUNDLE_REQUEST_CODE, requestCode);
    intent.putExtras(bundle);
    startActivityForResult(intent, requestCode);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) {
      return;
    }

    if (requestCode == Constants.REQUEST_CODE_WHERE_CLAUSE) {
      handleWhereClauseResult(data);
    } else if (requestCode == Constants.REQUEST_CODE_SET_CLAUSE) {
      handleContentValuesResult(data);
    }else if(requestCode== Constants.REQUEST_CODE_VALUES_CLAUSE){
      handleInsertValuesResult(data);
    }
  }

  private void handleWhereClauseResult(Intent data) {
    String str = data.getStringExtra(Constants.BUNDLE_WHERE_CLAUSE);
    queryDatabaseRequest.setWhereClause(str);
    str = new StringBuilder(Constants.WHERE_CLAUSE).append(Constants
        .SPACE).append(str).toString();
    whereClauseButton.setText(str);
  }

  private void handleContentValuesResult(Intent data) {
    ContentValues contentValues = data.getExtras().getParcelable(Constants.BUNDLE_CONTENT_VALUES);
    queryDatabaseRequest.setContentValues(contentValues);
    String str = data.getStringExtra(Constants.SET_CLAUSE);
    str = new StringBuilder(Constants.SET_CLAUSE).append(Constants
        .SPACE).append(str).toString();
    setClauseButton.setText(str);
  }

  private void handleInsertValuesResult(Intent data){
    ContentValues contentValues = data.getExtras().getParcelable(Constants.BUNDLE_CONTENT_VALUES);
    queryDatabaseRequest.setContentValues(contentValues);
    String str = data.getStringExtra(Constants.INSERT_CLAUSE);
    valuesClauseButton.setText(str);
  }

  private void launchColumnsSelectionDialog() {
    String[] columnNames = SqliteDatabaseReader.getColumnNames(this, tableInfo.getDatabaseName(),
        tableInfo.getTableName());
    boolean[] previouslySelectedColumns = null;
    if (!Constants.ASTERIK.equals(selectedColumnsButton.getText().toString())) {
      String selectedColumnsStr = selectedColumnsButton.getText().toString();
      List<String> selectedColumns = GeneralSqliteHelper.getListFromString(selectedColumnsStr);
      previouslySelectedColumns = GeneralSqliteHelper.getCheckedArray(columnNames, selectedColumns);
    }
    TableColumnsDialog tableColumnsDialog = TableColumnsDialog.newInstance(columnNames,
        previouslySelectedColumns, this);
    tableColumnsDialog.show(getFragmentManager(), "columnsDialog");
  }

  /**
   * This method will query the database.
   */
  private void queryDatabase() {
    //String query = queryDatabaseEditText.getText().toString();

    String queryType = (String) queryTypeSpinner.getSelectedItem();
    DatabaseQueryCommandType databaseQueryCommandType = DatabaseQueryCommandType.getCommand
        (queryType);

    if (databaseQueryCommandType == null) {
      return;
    }

    if (queryDatabaseRequest == null) {
      queryDatabaseRequest = new QueryDatabaseRequest();
    }

    String query = Constants.EMPTY_STRING;
    switch (databaseQueryCommandType) {
      case SELECT:
        query = getSelectQuery();
        queryDatabaseRequest.setSelectQuery(query);
        break;
      case UPDATE:
        break;
      case DELETE:
        break;
      case INSERT:
        query = getInsertQuery();
        break;
      case RAW_QUERY:
        query = getRawQuery();
        queryDatabaseRequest.setRawQuery(query);
        break;
    }
    queryDatabaseRequest.setDatabaseQueryCommandType(databaseQueryCommandType);


    Intent intent = new Intent(this, QueryResultActivity.class);
    Bundle bundle = new Bundle();
    bundle.putString(Constants.BUNDLE_RAW_QUERY, query);
    bundle.putString(Constants.BUNDLE_DATABASE_NAME, tableInfo.getDatabaseName());
    bundle.putString(Constants.BUNDLE_TABLE_NAME,tableInfo.getTableName());
    bundle.putParcelable(Constants.BUNDLE_CONTENT_VALUES, queryDatabaseRequest.getContentValues());
    queryDatabaseRequest.setContentValues(null);
    bundle.putSerializable(Constants.BUNDLE_QUERY_REQUEST, queryDatabaseRequest);
    intent.putExtras(bundle);
    startActivity(intent);
  }

  @Override
  public void handleError(ErrorType errorType) {

  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    String queryCommand = querySpinnerArrayList.get(position);
    DatabaseQueryCommandType command = DatabaseQueryCommandType.getCommand(queryCommand);
    switch (command) {
      case SELECT:
        onSelectCommandSelected();
        break;
      case UPDATE:
        onUpdateCommandSelected();
        break;
      case DELETE:
        onDeleteCommandSelected();
        break;
      case INSERT:
        onInsertCommandSelected();
        break;
      case RAW_QUERY:
        onRawQueryCommandSelected();
        break;
    }
  }

  private String getSelectQuery() {
    String queryType = queryTypeSpinner.getSelectedItem().toString();
    String queryColumns = selectedColumnsButton.getText().toString();
    String queryTableName = fromTableTextView.getText().toString();
    String queryWhereClause = whereClauseButton.getText().toString();

    if (Constants.WHERE_CLAUSE.equals(queryWhereClause.trim())) {
      queryWhereClause = Constants.EMPTY_STRING;
    }

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(queryType)
        .append(Constants.SPACE)
        .append(queryColumns)
        .append(Constants.SPACE)
        .append(queryTableName)
        .append(Constants.SPACE).append(queryWhereClause);

    String query = stringBuilder.toString().trim();
    return query;
  }

  private String getUpdateQuery() {

    String queryType = queryTypeSpinner.getSelectedItem().toString();
    String queryTableName = updateTableTextView.getText().toString();
    String querySetClause = setClauseButton.getText().toString();
    String queryWhereClause = whereClauseButton.getText().toString();


    if (Constants.WHERE_CLAUSE.equals(queryWhereClause.trim())) {
      queryWhereClause = Constants.EMPTY_STRING;
    }

    if (Constants.SET_CLAUSE.endsWith(querySetClause.trim())) {
      querySetClause = Constants.EMPTY_STRING;
    }

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(queryType)
        .append(Constants.SPACE)
        .append(queryTableName)
        .append(Constants.SPACE)
        .append(querySetClause)
        .append(Constants.SPACE).append(queryWhereClause);

    String query = stringBuilder.toString().trim();
    return query;

  }

  private String getDeleteQuery() {
    return Constants.EMPTY_STRING;

  }

  private String getInsertQuery() {
    return Constants.EMPTY_STRING;

  }

  private String getRawQuery() {
    String rawQuery = rawQueryEditText.getText().toString();
    return rawQuery;
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

  @Override
  public void onSelectCommandSelected() {
    queryDatabaseRequest.setDatabaseQueryCommandType(DatabaseQueryCommandType.SELECT);
    insertTableTextView.setVisibility(View.GONE);
    updateTableTextView.setVisibility(View.GONE);
    setClauseButton.setVisibility(View.GONE);
    selectedColumnsButton.setVisibility(View.VISIBLE);
    fromTableTextView.setVisibility(View.VISIBLE);
    fromTableTextView.setText(Constants.FROM_PREFIX + Constants.SPACE + tableInfo.getTableName());
    whereClauseButton.setText(Constants.WHERE_CLAUSE);
    selectedColumnsButton.setText(Constants.ASTERIK);
    rawQueryEditText.setVisibility(View.GONE);
  }

  @Override
  public void onUpdateCommandSelected() {
    queryDatabaseRequest.setDatabaseQueryCommandType(DatabaseQueryCommandType.UPDATE);
    insertTableTextView.setVisibility(View.GONE);
    fromTableTextView.setVisibility(View.GONE);
    updateTableTextView.setVisibility(View.VISIBLE);
    updateTableTextView.setText(Constants.SPACE + tableInfo.getTableName());
    selectedColumnsButton.setVisibility(View.GONE);
    setClauseButton.setVisibility(View.VISIBLE);
    setClauseButton.setText(Constants.SET_CLAUSE + Constants.SPACE);
    rawQueryEditText.setVisibility(View.GONE);
  }

  @Override
  public void onDeleteCommandSelected() {
    queryDatabaseRequest.setDatabaseQueryCommandType(DatabaseQueryCommandType.DELETE);
    insertTableTextView.setVisibility(View.GONE);
    selectedColumnsButton.setVisibility(View.GONE);
    setClauseButton.setVisibility(View.GONE);
    whereClauseButton.setVisibility(View.VISIBLE);
    fromTableTextView.setVisibility(View.VISIBLE);
    rawQueryEditText.setVisibility(View.GONE);
  }

  @Override
  public void onInsertCommandSelected() {
    rawQueryEditText.setVisibility(View.GONE);
    insertTableTextView.setVisibility(View.VISIBLE);
    valuesClauseButton.setText(Constants.VALUES);
    selectedColumnsButton.setVisibility(View.GONE);
    setClauseButton.setVisibility(View.GONE);
    fromTableTextView.setVisibility(View.GONE);
    updateTableTextView.setVisibility(View.GONE);
    insertTableTextView.setText(Constants.INTO_PREFIX + Constants.SPACE + tableInfo.getTableName());
  }

  @Override
  public void onRawQueryCommandSelected() {
    whereClauseButton.setVisibility(View.GONE);
    insertTableTextView.setVisibility(View.GONE);
    setClauseButton.setVisibility(View.GONE);
    selectedColumnsButton.setVisibility(View.GONE);
    updateTableTextView.setVisibility(View.GONE);
    fromTableTextView.setVisibility(View.GONE);
    rawQueryEditText.setVisibility(View.VISIBLE);
  }

  @Override
  public void onColumnsSelected(String columns) {
    if (Utils.isEmpty(columns)) {
      selectedColumnsButton.setText(Constants.ASTERIK);
    }
    selectedColumnsButton.setText(columns);
  }
}
