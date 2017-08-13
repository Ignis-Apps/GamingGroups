package de.js_labs.gaminggroups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import de.js_labs.gaminggroups.elements.Group;
import de.js_labs.gaminggroups.elements.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ChildEventListener {
    public static final int RC_SIGN_IN = 707;
    public static final String LOG_TAG = "jslabslog";
    public static final String FIREBASE_DATABASE_REF_GROUPS = "groups";
    public static final String FIREBASE_DATABASE_REF_USERS = "users";
    public static final String FIREBASE_DATABASE_REF_INVITES = "invites";

    public static boolean loading = false;

    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentFireabseUser;
    private User currentUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference groupsRef;
    private DatabaseReference userRef;

    private Toolbar toolbar;
    private NavigationView navigationView;
    public ViewGroup appBarMain;
    public View contentMyGroups;
    public View contentSearch;
    public View contentDonate;

    private ProgressDialog progressDialog;

    private FloatingActionButton fab;

    private GroupsAdapter groupsAdapter;
    private ListView groupsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirebase();

        setupNavigation();
    }

    private void setupFirebase() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setAnalyticsCollectionEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        groupsRef = firebaseDatabase.getReference(FIREBASE_DATABASE_REF_GROUPS);
        userRef = firebaseDatabase.getReference(FIREBASE_DATABASE_REF_USERS);

        /*ArrayList<String> users = new ArrayList<String>();
        users.add("player1");
        users.add("player2");
        users.add("Tinolus");
        users.add("linusbau");
        users.add(firebaseAuth.getCurrentUser().getUid());

        ArrayList<String> admins = new ArrayList<String>();
        admins.add("thecooladmin");
        admins.add("thestupidadmin");

        ArrayList<String> onlineMembers = new ArrayList<String>();
        onlineMembers.add("thecooladmin");
        onlineMembers.add("player2");

        groupsRef.child("testgroup").setValue(new Group("Sick Players", "Krasse DUDDDDEEESSSS :D", "TEST", users, admins, onlineMembers));*/
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
        loadUserData();
    }

    private void loadUserData() {
        if (firebaseAuth.getCurrentUser() != null) {
            if(!loading){
                loading = true;
                progressDialog= ProgressDialog.show(this, "",
                        getStringByResId(R.string.fui_progress_dialog_loading), true);

                currentFireabseUser =  firebaseAuth.getCurrentUser();
                currentUser = new User();
                DatabaseReference currentUserRef = userRef.child(currentFireabseUser.getUid());

                groupsAdapter = new GroupsAdapter(this, new ArrayList<Group>());
                groupsListView.setAdapter(groupsAdapter);
                currentUserRef.child(FIREBASE_DATABASE_REF_GROUPS).addChildEventListener(this);
                currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {if(dataSnapshot.getChildrenCount() == 0){progressDialog.hide();loading = false;}}
                    @Override
                    public void onCancelled(DatabaseError databaseError) {FirebaseCrash.report(databaseError.toException());}
                });
            }
        } else {
            startSignInActivity();
        }
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

    private void startSignInActivity(){
        Log.d(LOG_TAG, "startSignInActivity()");
        AuthUI.SignInIntentBuilder intentBuilder = AuthUI.getInstance().createSignInIntentBuilder();
        intentBuilder.setAvailableProviders(
                Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                        new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build()));
        intentBuilder.setPrivacyPolicyUrl("https://gaming-groups.jimdo.com/privacy-policy/");
        intentBuilder.setTosUrl("https://gaming-groups.jimdo.com/terms-of-service/");
        intentBuilder.setTheme(R.style.AppTheme);
        intentBuilder.setLogo(R.drawable.auth_logo);
        startActivityForResult(intentBuilder.build(), RC_SIGN_IN);
    }

    ///////////////////////////////////////////////////////////
    ///////////          DATABASE WORK             ////////////
    ///////////////////////////////////////////////////////////

    private void addGroupToList(String groupID, final int index){
        DatabaseReference currentGroup = groupsRef.child(groupID);
        currentGroup.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, dataSnapshot.getValue(Group.class).motto + " + index: " + index);
                groupsAdapter.insert(dataSnapshot.getValue(Group.class), index);
                groupsAdapter.notifyDataSetChanged();
                progressDialog.hide();
                loading = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                FirebaseCrash.report(databaseError.toException());
            }
        });
    }

    private void removeGroupFromList(int groupIndex){
        groupsAdapter.remove(groupsAdapter.getItem(groupIndex));
        groupsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        addGroupToList((String) dataSnapshot.getValue(), Integer.parseInt(dataSnapshot.getKey()));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        removeGroupFromList(Integer.parseInt(dataSnapshot.getKey()));
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

    @Override
    public void onCancelled(DatabaseError databaseError) {
        FirebaseCrash.report(databaseError.toException());
    }

    ///////////////////////////////////////////////////////////
    ///////////        ADAPTER / CLASSES           ////////////
    ///////////////////////////////////////////////////////////

    public class GroupsAdapter extends ArrayAdapter<Group> {
        private final Context context;
        public final ArrayList<Group> groups;

        public GroupsAdapter(Context context, ArrayList<Group> groups) {
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
            TextView textViewMemberCount = (TextView) listItemView.findViewById(R.id.textViewMembers);
            TextView textViewMemberOnlineCount = (TextView) listItemView.findViewById(R.id.textViewOnlineMembers);
            textViewMemberCount.setText((groups.get(position).users.size() + groups.get(position).admins.size()) + " " + getStringByResId(R.string.list_members));
            textViewMemberOnlineCount.setText(getStringByResId(R.string.list_dot) + " " + groups.get(position).onlineMembers.size() + " " + getStringByResId(R.string.list_members_online));
            textViewName.setText(groups.get(position).name);
            textViewMotto.setText(groups.get(position).motto);

            return listItemView;
        }
    }
}
