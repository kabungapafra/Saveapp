package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.ui.adapters.GuarantorRosterAdapter;
import com.example.save.ui.adapters.GuarantorSelectedAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddGuarantorFragment extends Fragment {

    private ImageView backButton;
    private EditText etSearch;
    private RecyclerView rvSelectedGuarantors;
    private RecyclerView rvGroupMembers;
    private TextView tvTotalMembers;
    private MaterialButton btnConfirm;
    private TextView tvConfirmCount;

    private GuarantorSelectedAdapter selectedAdapter;
    private GuarantorRosterAdapter rosterAdapter;

    private List<Member> allMembers = new ArrayList<>();
    private Set<String> selectedMemberIds = new HashSet<>();
    private List<Member> selectedMembersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_guarantor, container, false);
        initViews(view);
        setupRecyclerViews();
        setupListeners();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MembersViewModel membersViewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        
        // Mocking the data for initial prototype states if needed
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null && !members.isEmpty()) {
                allMembers = new ArrayList<>(members);
                tvTotalMembers.setText(allMembers.size() + " members");
                filterMembers("");
            } else {
                // Fallback dummy data if DB is empty
                createDummyData();
            }
        });
    }

    private void initViews(View view) {
        backButton = view.findViewById(R.id.backButton);
        etSearch = view.findViewById(R.id.etSearch);
        rvSelectedGuarantors = view.findViewById(R.id.rvSelectedGuarantors);
        rvGroupMembers = view.findViewById(R.id.rvGroupMembers);
        tvTotalMembers = view.findViewById(R.id.tvTotalMembers);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        tvConfirmCount = view.findViewById(R.id.tvConfirmCount);
    }

    private void setupRecyclerViews() {
        selectedAdapter = new GuarantorSelectedAdapter(new GuarantorSelectedAdapter.OnRemoveClickListener() {
            @Override
            public void onRemoveClick(Member member) {
                toggleMemberSelection(member);
            }

            @Override
            public void onAddSlotClick() {
                // Focus on search to add
                etSearch.requestFocus();
            }
        });
        rvSelectedGuarantors.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedGuarantors.setAdapter(selectedAdapter);

        rosterAdapter = new GuarantorRosterAdapter(new GuarantorRosterAdapter.OnMemberClickListener() {
            @Override
            public void onMemberClick(Member member) {
                toggleMemberSelection(member);
            }
        });
        rvGroupMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroupMembers.setAdapter(rosterAdapter);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnConfirm.setOnClickListener(v -> {
            if (selectedMembersList.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one guarantor", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract IDs
            ArrayList<String> ids = new ArrayList<>();
            for (Member m : selectedMembersList) {
                ids.add(m.getId());
            }

            // Pass data back via FragmentResult
            Bundle result = new Bundle();
            result.putStringArrayList("selected_guarantor_ids", ids);
            getParentFragmentManager().setFragmentResult("guarantor_request", result);
            getParentFragmentManager().popBackStack();
        });
    }

    private void filterMembers(String query) {
        String lowerQuery = query.toLowerCase().trim();
        List<Member> filtered = new ArrayList<>();
        
        for (Member member : allMembers) {
            // Check if name matches query
            if (member.getName().toLowerCase().contains(lowerQuery)) {
                // Also, we optionally hide members from the list if they are currently selected?
                // The specs said "clicking adds avatar to top row... if deselected removes".
                // In many designs, they stay in the list but are checked. The prompt says: "If selected -> fills circle with blue checkmark".
                // So we keep them in the list.
                filtered.add(member);
            }
        }
        rosterAdapter.setMembers(filtered);
    }

    private void toggleMemberSelection(Member member) {
        if (selectedMemberIds.contains(member.getId())) {
            selectedMemberIds.remove(member.getId());
            // Remove from ordered list
            for (int i = 0; i < selectedMembersList.size(); i++) {
                if (selectedMembersList.get(i).getId().equals(member.getId())) {
                    selectedMembersList.remove(i);
                    break;
                }
            }
        } else {
            selectedMemberIds.add(member.getId());
            selectedMembersList.add(member);
        }
        
        // Update Adapters
        rosterAdapter.setSelectedMemberIds(selectedMemberIds);
        selectedAdapter.setMembers(selectedMembersList);
        
        updateConfirmButton();
    }

    private void updateConfirmButton() {
        int count = selectedMembersList.size();
        tvConfirmCount.setText(String.valueOf(count));
        
        if (count > 0) {
            btnConfirm.setEnabled(true);
            tvConfirmCount.setVisibility(View.VISIBLE);
        } else {
            btnConfirm.setEnabled(false);
            tvConfirmCount.setVisibility(View.INVISIBLE); // hidden when 0 or just text=0? The prompt says active when >= 1.
        }
    }

    private void createDummyData() {
        Member m1 = new Member("Marcus Thorne", "MEMBER", true, "0711111111", "marcus@example.com");
        m1.setId("m1");
        allMembers.add(m1);

        Member m2 = new Member("David Kim", "MEMBER", true, "0722222222", "david@example.com");
        m2.setId("m2");
        allMembers.add(m2);

        Member m3 = new Member("Jasmine Lee", "MEMBER", true, "0733333333", "jasmine@example.com");
        m3.setId("m3");
        allMembers.add(m3);

        Member m4 = new Member("Samuel Rivera", "MEMBER", true, "0744444444", "samuel@example.com");
        m4.setId("m4");
        m4.setCreditScore(80); // Ensure eligible
        allMembers.add(m4);
        
        Member bea = new Member("Beatrice Walsh", "MEMBER", true, "0755555555", "bea@example.com");
        bea.setId("m5");
        bea.setCreditScore(40); // Force LOW SCORE
        allMembers.add(bea);
        
        tvTotalMembers.setText(allMembers.size() + " members");
        filterMembers("");
    }
}
