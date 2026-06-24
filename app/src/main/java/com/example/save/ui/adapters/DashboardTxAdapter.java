package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.TransactionEntity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardTxAdapter extends RecyclerView.Adapter<DashboardTxAdapter.TxViewHolder> {

    private List<TransactionEntity> items = new ArrayList<>();
    private final NumberFormat ugFmt;
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM", Locale.getDefault());

    public DashboardTxAdapter() {
        ugFmt = NumberFormat.getNumberInstance(new Locale("en", "UG"));
        ugFmt.setGroupingUsed(true);
    }

    public void setItems(List<TransactionEntity> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tx_row, parent, false);
        return new TxViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TxViewHolder h, int position) {
        TransactionEntity tx = items.get(position);
        boolean isCredit = isIncoming(tx);

        // Arrow icon: green pointing inward (135°) for credit, red pointing outward (-45°) for debit
        h.iconBg.setBackgroundResource(R.drawable.bg_tx_icon_box);
        if (isCredit) {
            h.iconBg.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFDCFCE7));
            h.icon.setRotation(135f);
            h.icon.setImageTintList(
                    android.content.res.ColorStateList.valueOf(0xFF16A34A));
        } else {
            h.iconBg.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFFE4E4));
            h.icon.setRotation(-45f);
            h.icon.setImageTintList(
                    android.content.res.ColorStateList.valueOf(0xFFDC2626));
        }

        // Title: type · memberName
        String type = formatType(tx.getType());
        String name = tx.getMemberName();
        h.title.setText(name != null && !name.isEmpty() ? type + " · " + name : type);

        // Subtitle: relative date + time
        h.subtitle.setText(formatDateTime(tx.getDate()));

        // Amount: + green for incoming, - red for outgoing
        h.amount.setText((isCredit ? "+ UGX " : "- UGX ") + ugFmt.format(Math.abs(tx.getAmount())));
        h.amount.setTextColor(isCredit ? 0xFF16A34A : 0xFFDC2626);

        h.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private boolean isIncoming(TransactionEntity tx) {
        if (tx.getType() != null) {
            switch (tx.getType().toUpperCase()) {
                case "CONTRIBUTION":
                case "LOAN_REPAYMENT":
                    return true;
                case "PAYOUT":
                case "LOAN":
                case "WITHDRAWAL":
                case "DISBURSEMENT":
                    return false;
            }
        }
        // Fall back to the model field if type is unknown
        return tx.isPositive();
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String formatType(String raw) {
        if (raw == null) return "Transaction";
        switch (raw.toUpperCase()) {
            case "CONTRIBUTION": return "Contribution";
            case "LOAN_REPAYMENT": return "Loan Repayment";
            case "LOAN": return "Loan";
            case "PAYOUT": return "Payout Received";
            case "WITHDRAWAL": return "Withdrawal";
            default: return raw.replace("_", " ");
        }
    }

    private String formatDateTime(Date date) {
        if (date == null) return "—";
        Calendar now = Calendar.getInstance();
        Calendar txCal = Calendar.getInstance();
        txCal.setTime(date);

        int diffDays = (int) ((now.getTimeInMillis() - txCal.getTimeInMillis()) / (1000 * 60 * 60 * 24));

        String time = timeFmt.format(date);
        if (diffDays == 0) return "Today, " + time;
        if (diffDays == 1) return "Yesterday, " + time;
        return dateFmt.format(date) + ", " + time;
    }

    static class TxViewHolder extends RecyclerView.ViewHolder {
        FrameLayout iconBg;
        ImageView icon;
        TextView title, subtitle, amount;
        View divider;

        TxViewHolder(@NonNull View v) {
            super(v);
            iconBg   = v.findViewById(R.id.fvRowIconBg);
            icon     = v.findViewById(R.id.ivRowIcon);
            title    = v.findViewById(R.id.tvRowTitle);
            subtitle = v.findViewById(R.id.tvRowSubtitle);
            amount   = v.findViewById(R.id.tvRowAmount);
            divider  = v.findViewById(R.id.viewRowDivider);
        }
    }
}
