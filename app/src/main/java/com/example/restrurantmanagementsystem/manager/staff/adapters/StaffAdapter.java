package com.example.restrurantmanagementsystem.manager.staff.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.User;
import com.example.restrurantmanagementsystem.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private Context context;
    private List<User> staffList;
    private OnStaffClickListener listener;

    public interface OnStaffClickListener {
        void onDeleteClick(User user);
    }

    public StaffAdapter(Context context, OnStaffClickListener listener) {
        this.context = context;
        this.staffList = new ArrayList<>();
        this.listener = listener;
    }

    public void setStaffList(List<User> staffList) {
        this.staffList = staffList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        User user = staffList.get(position);
        holder.tvName.setText(user.name);
        holder.tvEmail.setText(user.email);
        holder.tvRole.setText(user.role);

        // Set role-based icon and color
        if (Constants.ROLE_WAITER.equals(user.role)) {
            holder.tvRoleIcon.setText("W");
            holder.llRoleBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_primary));
            holder.tvRole.setBackgroundResource(R.drawable.badge_role_waiter);
        } else if (Constants.ROLE_CHEF.equals(user.role)) {
            holder.tvRoleIcon.setText("C");
            holder.llRoleBadge.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_orange));
            holder.tvRole.setBackgroundResource(R.drawable.badge_role_chef);
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole, tvRoleIcon;
        LinearLayout llRoleBadge;
        ImageButton btnDelete;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStaffName);
            tvEmail = itemView.findViewById(R.id.tvStaffEmail);
            tvRole = itemView.findViewById(R.id.tvStaffRole);
            tvRoleIcon = itemView.findViewById(R.id.tvRoleIcon);
            llRoleBadge = itemView.findViewById(R.id.llRoleBadge);
            btnDelete = itemView.findViewById(R.id.btnDeleteStaff);
        }
    }
}