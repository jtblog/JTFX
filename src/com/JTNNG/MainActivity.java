package com.JTNNG;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.fxcore2.*;

public class MainActivity extends ListActivity implements OnItemClickListener {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.main_list_item, this.getResources().getStringArray(
                        R.array.tables));
        this.setListAdapter(adapter);
        this.getListView().setOnItemClickListener(this);
		SharedObjects.getInstance().AccBal(((O2GAccountTableRow)((O2GAccountsTable) SharedObjects.getInstance()
			.getSession().getTableManager().getTable(O2GTableType.ACCOUNTS)).getGenericRow(0)).getBalance());
getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedObjects.getInstance().getSession()
                                        .logout();
                                MainActivity.this.finish();
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();

        alert.show();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        TextView textView = (TextView) arg1;
        String sTableName = textView.getText().toString();
        Class<?> activityClass = this.getActivityClassForTable(sTableName);
        if (activityClass != null) {
            Intent intent = new Intent(this, activityClass);
            this.startActivity(intent);
        }
    }

    private Class<?> getActivityClassForTable(String sTableName) {
        if (sTableName.compareTo("Offers") == 0) {
            return OffersActivity.class;
        } else if (sTableName.compareTo("Accounts") == 0) {
            return AccountsActivity.class;
        } else if (sTableName.compareTo("Orders") == 0) {
            return OrdersActivity.class;
        } else if (sTableName.compareTo("Trades") == 0) {
            return TradesActivity.class;
        } else if (sTableName.compareTo("Closed Trades") == 0) {
            return ClosedTradesActivity.class;
        } else if (sTableName.compareTo("Summary") == 0) {
            return SummaryActivity.class;
        } else {
            return null;
        }

    }

}