package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.PlayerState;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) {
        ArrayList<Short> death = new ArrayList<>();
        ArrayList<Short> kills = new ArrayList<>();
        AtomicReference<Integer> first = new AtomicReference<>(0);
        // Create a new listener (using a lambda for this example)
        GSIListener listener = (state, context) -> {
            state.getAllPlayers().ifPresent(Player -> {
                if(first.get() == 0){
                    for (Map.Entry<PlayerSteamID, PlayerState> entry : Player.entrySet()) {
                        PlayerSteamID k = entry.getKey();
                        PlayerState v = entry.getValue();
                        death.add(first.get(), Short.valueOf(v.getStatistics().getDeathCount()));
                        kills.add(first.get(), Short.valueOf(v.getStatistics().getKillCount()));
                        first.getAndSet(first.get() + 1);
                    }
                }
                int loop = 0;
                for (Map.Entry<PlayerSteamID, PlayerState> entry : Player.entrySet()) {
                    PlayerSteamID k = entry.getKey();
                    PlayerState v = entry.getValue();
                    short kill = v.getStatistics().getKillCount();
                    if(kills.get(loop) != kill){
                        int deathloop = 0;
                        for (Map.Entry<PlayerSteamID, PlayerState> eentry : Player.entrySet()) {
                            PlayerSteamID key = eentry.getKey();
                            PlayerState value = eentry.getValue();

                            if (death.get(deathloop) != value.getStatistics().getDeathCount()) {
                                System.out.println(death.get(deathloop));
                                System.out.println(value.getStatistics().getDeathCount());
                                System.out.println(v.getName() + " killed " + value.getName());
                                System.out.println("");
                                death.set(deathloop, value.getStatistics().getDeathCount());
                                kills.set(loop,kill);
                            }
                        }
                    }
                    loop++;

                }
            });
        };

// Configure server
        GSIServer server = new GSIServer.Builder(1337)        // Port 1337, on all network interfaces
                .requireAuthToken("password", "Q79v5tcxVQ8u") // Require the specified password
                .registerListener(listener)                   // Alternatively, you can call this on the GSIServer dynamically
                .build();

// Start server
        try {
            server.start(); // Start the server (runs in a separate thread)
            System.out.println("Server started. Listening for state data...");
        } catch (IOException e) {
            System.out.println("Could not start server.");
        }
    }


}
