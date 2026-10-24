package org.edupage.brighperc.a48175.chargehub;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSlots;
    private SlotAdapter slotAdapter;
    private List<Slot> slotList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewSlots = findViewById(R.id.recyclerViewSlots);

        // Set 3 columns for the grid
        recyclerViewSlots.setLayoutManager(new GridLayoutManager(this, 3));

        // Generate 30 empty slots
        slotList = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            slotList.add(new Slot(i, false));
        }

        // Initialize adapter with click listener
        slotAdapter = new SlotAdapter(slotList, slot -> {
            if (slot.isOccupied()) {
                Toast.makeText(MainActivity.this, "Slot " + slot.getSlotNumber() + " selected for Checkout", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Slot " + slot.getSlotNumber() + " selected for Check-in", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewSlots.setAdapter(slotAdapter);
    }
}