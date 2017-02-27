package comic.systems.ppefood;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.InputStream;
import java.net.URL;

public class FragmentNavigationDrawer extends Fragment implements View.OnClickListener {

    private LinearLayout menuCmp;
    private LinearLayout menuCmd;
    private LinearLayout menuPrd;
    private LinearLayout menuMag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View leMenu = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        return leMenu;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        menuCmp = (LinearLayout) view.findViewById(R.id.menuCompte);
        menuCmd = (LinearLayout) view.findViewById(R.id.menuCommandes);
        menuPrd = (LinearLayout) view.findViewById(R.id.menuProduits);
        menuMag = (LinearLayout) view.findViewById(R.id.menuMagasins);

        menuCmp.setOnClickListener(this);
        menuCmd.setOnClickListener(this);
        menuPrd.setOnClickListener(this);
        menuMag.setOnClickListener(this);

        menuCmp.setTag("monCompte");
        menuCmd.setTag("mesCommandes");
        menuPrd.setTag("mesProduits");
        menuMag.setTag("magasins");
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(), "Menu -> " + v.getTag(), Toast.LENGTH_LONG).show();
    }
}