package tk.pathfinder.UI;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import tk.pathfinder.Map.Api;
import tk.pathfinder.R;

/**
 * A fragment for displaying a map search result.
 */
public class MapResult extends Fragment {

    private int id;
    private String name;

    private MapResultListener mListener;

    public MapResult() {
        // Required empty public constructor
    }


    public static MapResult newInstance(Api.MapQueryResult map) {
        MapResult fragment = new MapResult();
        Bundle args = new Bundle();
        args.putInt("id", map.getId());
        args.putString("name", map.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getInt("id");
            name = getArguments().getString("name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_result, container, false);
        EditText label = v.findViewById(R.id.map_name_label);
        label.setText(name);
        FrameLayout layout = v.findViewById(R.id.map_result_container);
        layout.setOnClickListener(x -> mListener.onMapSelected(id));
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapResultListener) {
            mListener = (MapResultListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MapResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface MapResultListener {
        void onMapSelected(int id);
    }
}