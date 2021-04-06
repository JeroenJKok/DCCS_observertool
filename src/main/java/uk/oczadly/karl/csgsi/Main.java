package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.PhaseCountdownState;
import uk.oczadly.karl.csgsi.state.PlayerState;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main{
    public static void main(String[] args) {
        HashMap<PlayerSteamID, Short> KillMap = new HashMap<>();
        HashMap<PlayerSteamID, Short> DeathMap = new HashMap<>();
        HashMap<Integer, String> eventMap = new HashMap<>();
        final int[] gameTime = {0};
        Timer timer = new Timer();
        System.out.println(LocalTime.now().toSecondOfDay());
        AtomicInteger first = new AtomicInteger(0);
        AtomicBoolean round = new AtomicBoolean(false);
        // Create a new listener (using a lambda for this example)
        GSIListener listener = (state, context) -> {
            state.getAllPlayers().ifPresent(Player -> {
                if(first.get() == 0){
                    System.out.println("-----------------------ROUND 1-----------------------");
                    for (Map.Entry<PlayerSteamID, PlayerState> entry : Player.entrySet()) {
                        PlayerSteamID k = entry.getKey();
                        PlayerState v = entry.getValue();
                        KillMap.put(k,v.getStatistics().getKillCount());
                        DeathMap.put(k,v.getStatistics().getDeathCount());
                        first.getAndIncrement();
                    }
                }
                for(Map.Entry<PlayerSteamID, Short> map : KillMap.entrySet()){
                    PlayerSteamID key = map.getKey();
                    Short value = map.getValue();
                    if(Player.get(key).getStatistics().getKillCount() > value){
                        KillMap.put(key,Player.get(key).getStatistics().getKillCount());
                        for(Map.Entry<PlayerSteamID, Short> map2 : DeathMap.entrySet()){
                            PlayerSteamID k = map2.getKey();
                            Short v = map2.getValue();
                            if(Player.get(k).getStatistics().getDeathCount() > v){
                                DeathMap.put(k,Player.get(k).getStatistics().getDeathCount());
                                PhaseCountdownState phase = state.getPhaseCountdowns().get();
                                int time = (int)Math.round(phase.getRemainingTime());
                                int minutes = time/60;
                                int seconds = time-minutes*60;
                                String zero = "";
                                if(minutes == 0 && seconds <= 7){
                                    time = (int)Math.round(context.getPreviousState().getPhaseCountdowns().get().getRemainingTime());
                                    minutes = time/60;
                                    seconds = time-minutes*60;
                                }
                                if(String.valueOf(seconds).length() == 1){
                                    zero = "0";
                                }
                                eventMap.put(gameTime[0]+10,"("+minutes+":"+zero+seconds+")"+Player.get(key).getName()+" Killed "+Player.get(k).getName());
                                System.out.println("("+minutes+":"+zero+seconds+")"+Player.get(key).getName()+" Killed "+Player.get(k).getName());
                                break;
                            }
                        }
                    }
                }
            });
            state.getRound().ifPresent(Round ->{
                if(Round.getPhase().getString().equals("over") && round.get()){
                    int number = state.getMap().get().getRoundNumber()+1;
                    System.out.println("");
                    System.out.println("-----------------------ROUND "+number+"-----------------------");
                    round.set(false);
                }
                if(Round.getPhase().getString().equals("freezetime") && !round.get()){
                    round.set(true);
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



