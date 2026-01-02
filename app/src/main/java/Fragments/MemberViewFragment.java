package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import Data.MemberRepository;
import Models.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberViewFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvMemberCount;
    private MemberAdapter adapter;
    private List<Member> memberList;
    private MemberRepository memberRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_view, container, false);

        memberRepository = MemberRepository.getInstance();

        initializeViews(view);
        setupRecyclerView();
        loadMemberData();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.membersRecyclerView);
        tvMemberCount = view.findViewById(R.id.tvMemberCount);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        memberList = new ArrayList<>();
        adapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(adapter);
    }

    private void loadMemberData() {
        memberList.clear();
        memberList.addAll(memberRepository.getAllMembers());
        updateMemberCount();
        adapter.notifyDataSetChanged();
    }

    private void updateMemberCount() {
        if (tvMemberCount != null) {
            tvMemberCount.setText(memberList.size() + " Active Members");
        }
    }

    // --- Simplified Read-Only Adapter ---
    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private List<Member> list;

        MemberAdapter(List<Member> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_simple, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            Member member = list.get(position);
            holder.tvName.setText(member.getName());
            holder.tvRole.setText(member.getRole());

            // Status Indicator Color
            int color = member.isActive() ? holder.itemView.getContext().getColor(android.R.color.holo_green_dark)
                    : holder.itemView.getContext().getColor(android.R.color.darker_gray);
            holder.statusView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole;
            View statusView;

            MemberViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvRole = itemView.findViewById(R.id.tvMemberRole);
                statusView = itemView.findViewById(R.id.statusIndicator);
            }
        }
    }
}
