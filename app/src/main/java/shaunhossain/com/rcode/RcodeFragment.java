package shaunhossain.com.rcode;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;


public class RcodeFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    View view;
    FloatingTextButton gen,loc;

    public RcodeFragment() {
        // Required empty public constructor
    }


    public static RcodeFragment newInstance() {
        RcodeFragment fragment = new RcodeFragment ( );
        Bundle args = new Bundle ( );
        fragment.setArguments (args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate (R.layout.fragment_rcode,container,false);
        gen = (FloatingTextButton) view.findViewById(R.id.gen);
        loc = (FloatingTextButton) view.findViewById(R.id.loc);

        gen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gcode = new Intent (getActivity(), MainActivityGenerator.class);
                startActivityForResult(gcode, 0);
            }
        });

        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent lcode = new Intent (getActivity(), MainActivityLocator.class);
                startActivityForResult(lcode, 0);
            }
        });


        return view;
    }

    @Override
    public void onClick(View view) {

    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }
}
