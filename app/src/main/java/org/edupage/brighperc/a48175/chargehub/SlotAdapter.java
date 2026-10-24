package org.edupage.brighperc.a48175.chargehub;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private List<Slot> slotList;
    private OnSlotClickListener listener;

    public interface OnSlotClickListener {
        void onSlotClick(Slot slot);
    }

    public SlotAdapter(List<Slot> slotList, OnSlotClickListener listener) {
        this.slotList = slotList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        Slot slot = slotList.get(position);
        holder.textSlotNumber.setText("Slot " + slot.getSlotNumber());

        if (slot.isOccupied()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // Light Green
            holder.textSlotStatus.setText("CHARGING");
            holder.textSlotStatus.setTextColor(Color.parseColor("#2E7D32")); // Dark Green
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Light Grey
            holder.textSlotStatus.setText("EMPTY");
            holder.textSlotStatus.setTextColor(Color.parseColor("#757575")); // Dark Grey
        }

        holder.itemView.setOnClickListener(v -> listener.onSlotClick(slot));
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textSlotNumber, textSlotStatus;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            textSlotNumber = itemView.findViewById(R.id.textSlotNumber);
            textSlotStatus = itemView.findViewById(R.id.textSlotStatus);
        }
    }
}