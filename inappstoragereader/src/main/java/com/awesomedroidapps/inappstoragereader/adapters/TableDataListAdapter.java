package com.awesomedroidapps.inappstoragereader.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awesomedroidapps.inappstoragereader.R;
import com.awesomedroidapps.inappstoragereader.views.TableDataItemViewHolder;
import com.awesomedroidapps.inappstoragereader.views.TableItemViewHolder;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by anshul on 15/1/17.
 */

public class TableDataListAdapter extends RecyclerView.Adapter {

  private List<String> tableDataList;
  private WeakReference<Activity> activityWeakReference;

  public TableDataListAdapter(List tableDataList, Activity activity) {
    this.tableDataList = tableDataList;
    this.activityWeakReference = new WeakReference<Activity>(activity);
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.view_table_data_item, parent, false);
    TableDataItemViewHolder viewHolder = new TableDataItemViewHolder(view, tableDataList.size(),
        view.getContext());
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (!(holder instanceof TableDataItemViewHolder)) {
      return;
    }
    ((TableDataItemViewHolder) holder).updateTableDataItem(position,tableDataList.get(position));
  }

  @Override
  public int getItemCount() {
    return tableDataList.size();
  }
}