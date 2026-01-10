package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import Data.Loan;

public class LoansAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_REQUEST = 1;
    private static final int TYPE_ACTIVE = 2;

    private List<Object> items;
    private LoanActionListener listener;

    public interface LoanActionListener {
        void onApprove(Loan loan);

        void onReject(Loan loan);

        void onRemind(Loan loan);
    }

    public LoansAdapter(List<Object> items, LoanActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String)
            return TYPE_HEADER;
        if (item instanceof Loan) {
            Loan loan = (Loan) item;
            if (Loan.STATUS_PENDING.equals(loan.getStatus()))
                return TYPE_REQUEST;
            return TYPE_ACTIVE;
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            // Customize header slightly
            TextView tv = view.findViewById(android.R.id.text1);
            tv.setTextColor(parent.getContext().getResources().getColor(R.color.deep_blue));
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setPadding(32, 16, 16, 16);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_REQUEST) {
            return new RequestViewHolder(inflater.inflate(R.layout.item_loan_request, parent, false));
        } else {
            return new ActiveViewHolder(inflater.inflate(R.layout.item_loan_active, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof RequestViewHolder) {
            ((RequestViewHolder) holder).bind((Loan) items.get(position));
        } else if (holder instanceof ActiveViewHolder) {
            ((ActiveViewHolder) holder).bind((Loan) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(android.R.id.text1);
        }

        void bind(String title) {
            tvTitle.setText(title);
        }
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvLoanReason, tvAmount;
        Button btnReject, btnApprove;

        RequestViewHolder(View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvLoanReason = itemView.findViewById(R.id.tvLoanReason);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }

        void bind(Loan loan) {
            tvMemberName.setText(loan.getMemberName());
            tvLoanReason.setText(loan.getReason());
            tvAmount.setText(String.format(Locale.getDefault(), "UGX %,.0f", loan.getAmount()));

            btnApprove.setOnClickListener(v -> listener.onApprove(loan));
            btnReject.setOnClickListener(v -> listener.onReject(loan));
        }
    }

    class ActiveViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvDueDate, tvTotalDue, tvRepaidAmount;
        ProgressBar progressBar;

        ActiveViewHolder(View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvTotalDue = itemView.findViewById(R.id.tvTotalDue);
            tvRepaidAmount = itemView.findViewById(R.id.tvRepaidAmount);
            progressBar = itemView.findViewById(R.id.progressBarRepayment);
        }

        void bind(Loan loan) {
            tvMemberName.setText(loan.getMemberName());
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            tvDueDate.setText("Due: " + (loan.getDueDate() != null ? sdf.format(loan.getDueDate()) : "N/A"));
            tvTotalDue.setText(String.format(Locale.getDefault(), "UGX %,.0f", loan.getTotalDue()));

            int progress = loan.getRepaymentProgress();
            progressBar.setProgress(progress);
            tvRepaidAmount.setText(
                    String.format(Locale.getDefault(), "Paid: UGX %,.0f (%d%%)", loan.getRepaidAmount(), progress));

            itemView.findViewById(R.id.tvRepaidAmount).setOnClickListener(v -> listener.onRemind(loan));
        }
    }
}
