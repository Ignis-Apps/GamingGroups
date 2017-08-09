package de.js_labs.gaminggroups;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 707;

    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseCrash firebaseCrash;
    private FirebaseAuth firebaseAuth;

    private Toolbar toolbar;
    private NavigationView navigationView;
    public ViewGroup appBarMain;
    public View contentMyGroups;
    public View contentSearch;
    public View contentDonate;

    private FloatingActionButton fab;

    private GroupsAdapter groupsAdapter;
    private ListView groupsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirebase();

        checkUser();

        setupNavigation();
    }

    private void setupFirebase() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void checkUser() {
        if (firebaseAuth.getCurrentUser() != null) {

        } else {
            startSignInActivity();
        }
    }

    private void setupNavigation(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        appBarMain = (ViewGroup) findViewById(R.id.appBarMainRl);
        contentMyGroups = inflater.inflate(R.layout.content_main_mygroups, (ViewGroup) findViewById(R.id.contentMyGroupsRl));
        contentSearch = inflater.inflate(R.layout.content_main_search, (ViewGroup) findViewById(R.id.contentSearchRl));
        contentDonate = inflater.inflate(R.layout.content_main_donate, (ViewGroup) findViewById(R.id.contentDonateRl));
        appBarMain.addView(contentMyGroups);

        Group[] groups = {new Group("Unkown Players", "efsbdidfabdfabasdfjabfdjadfsjadfsbjkadfsabdfjkslfabdjksdsbfjklbfjkladsbjkldfbjksdfbjkdfsbjkdfsbjkdfsfbjks", "Beschreibung"), new Group("Sick Players", "Wir sind absolut krass ;)))))", "Beschreibung"), new Group("Bad Players", "Wir sind krasser krass bliblablub", "Beschreibung"), new Group("Unkown Players", "efsbdidfabdfabasdfjabfdjadfsjadfsbjkadfsabdfjkslfabdjksdsbfjklbfjkladsbjkldfbjksdfbjkdfsbjkdfsbjkdfsfbjks", "Beschreibung"), new Group("Sick Players", "Wir sind absolut krass ;)))))", "Beschreibung")};
        groupsAdapter = new GroupsAdapter(this, groups);

        groupsListView = (ListView) findViewById(R.id.listViewGroups);
        groupsListView.setAdapter(groupsAdapter);
        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final Group item = (Group) parent.getItemAtPosition(position);
                groupsAdapter.notifyDataSetChanged();
                //listViewAction(position);
            }

        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbar("Adding new Group...");
            }
        });

    }

    private void startSignInActivity(){
        AuthUI.SignInIntentBuilder intentBuilder = AuthUI.getInstance().createSignInIntentBuilder();
        intentBuilder.setAvailableProviders(
                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()));
        intentBuilder.setPrivacyPolicyUrl("https://js-labs.jimdo.com/privacy-policy/");
        intentBuilder.setTheme(R.style.AppTheme);
        intentBuilder.setLogo(R.drawable.auth_logo);
        startActivityForResult(intentBuilder.build(), RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == ResultCodes.OK) {
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    showAlertDialog(getStringByResId(R.string.dialog_sign_in_required), getStringByResId(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startSignInActivity();
                        }
                    });
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showAlertDialog(getStringByResId(R.string.dialog_connection_required), getStringByResId(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    return;
                }

                showAlertDialog(getStringByResId(R.string.dialog_unknown_error), getStringByResId(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUser();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mygroups) {
            appBarMain.removeView(contentMyGroups);
            appBarMain.removeView(contentSearch);
            appBarMain.removeView(contentDonate);
            appBarMain.addView(contentMyGroups);
            fab.show();
        } else if (id == R.id.nav_search) {
            appBarMain.removeView(contentMyGroups);
            appBarMain.removeView(contentSearch);
            appBarMain.removeView(contentDonate);
            appBarMain.addView(contentSearch);
            fab.hide();
        } else if (id == R.id.nav_donate) {
            appBarMain.removeView(contentMyGroups);
            appBarMain.removeView(contentSearch);
            appBarMain.removeView(contentDonate);
            appBarMain.addView(contentDonate);
            fab.hide();
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_account) {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ///////////////////////////////////////////////////////////
    ///////////              TOOLS                 ////////////
    ///////////////////////////////////////////////////////////

    private String getStringByResId(int ResId){
        return getResources().getString(ResId);
    }

    private void showSnackbar(String message){
        Snackbar.make(fab, message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    private void showAlertDialog(String message, String postivBtn, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(postivBtn, onClickListener)
                .setCancelable(false);
        builder.create().show();
    }

    ///////////////////////////////////////////////////////////
    ///////////        ADAPTER / CLASSES           ////////////
    ///////////////////////////////////////////////////////////

    public class GroupsAdapter extends ArrayAdapter<Group> {
        private final Context context;
        private final Group[] groups;

        public GroupsAdapter(Context context, Group[] groups) {
            super(context, -1, groups);
            this.context = context;
            this.groups = groups;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItemView = inflater.inflate(R.layout.listview_item_mygroups, parent, false);
            TextView textViewName = (TextView) listItemView.findViewById(R.id.textViewName);
            TextView textViewMotto = (TextView) listItemView.findViewById(R.id.textViewMotto);
            ImageView imageView = (ImageView) listItemView.findViewById(R.id.icon);
            textViewName.setText(groups[position].name);
            textViewMotto.setText(groups[position].motto);

            return listItemView;
        }
    }
}
