package uk.oczadly.karl.csgsi;

import uk.oczadly.karl.csgsi.state.PhaseCountdownState;
import uk.oczadly.karl.csgsi.state.PlayerState;
import uk.oczadly.karl.csgsi.state.components.EnumValue;
import uk.oczadly.karl.csgsi.state.components.PlayerSteamID;
import uk.oczadly.karl.csgsi.state.components.Team;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        HashMap<PlayerSteamID, Short> KillMap = new HashMap<>();
        HashMap<PlayerSteamID, Short> DeathMap = new HashMap<>();
        HashMap<Integer, String> eventMap = new HashMap<>();
        final int[] gameTime = {0};
        Timer timer = new Timer();
        int killDelay = 100;
        AtomicInteger killDif = new AtomicInteger();
        final int[] roundNumber = {1};
        System.out.println(LocalTime.now().toSecondOfDay());
        AtomicInteger first = new AtomicInteger(0);
        final AtomicBoolean[] round = {new AtomicBoolean(false)};
        // Create a new listener (using a lambda for this example)
        GSIListener listener = (state, context) -> {
            state.getAllPlayers().ifPresent(Player -> {
                if (first.get() == 0) {
                    eventMap.put(gameTime[0] + killDelay,"-----------------------ROUND " + roundNumber[0] + "-----------------------");
                    for (Map.Entry<PlayerSteamID, PlayerState> entry : Player.entrySet()) {
                        PlayerSteamID k = entry.getKey();
                        PlayerState v = entry.getValue();
                        KillMap.put(k, v.getStatistics().getKillCount());
                        DeathMap.put(k, v.getStatistics().getDeathCount());
                        first.getAndIncrement();
                    }
                }
                for (Map.Entry<PlayerSteamID, Short> map : KillMap.entrySet()) {
                    PlayerSteamID key = map.getKey();
                    Short value = map.getValue();
                    if (Player.get(key).getStatistics().getKillCount() != value) {
                        killDif.set(Player.get(key).getStatistics().getKillCount() - value);
                        System.out.println(killDif.get());
                        KillMap.put(key, Player.get(key).getStatistics().getKillCount());
                        if (killDif.get() > 0){
                            for (Map.Entry<PlayerSteamID, Short> map2 : DeathMap.entrySet()) {
                                PlayerSteamID k = map2.getKey();
                                Short v = map2.getValue();
                                if (Player.get(k).getStatistics().getDeathCount() > v) {
                                    DeathMap.put(k, Player.get(k).getStatistics().getDeathCount());
                                    PhaseCountdownState phase = state.getPhaseCountdowns().get();
                                    int time = (int) Math.round(phase.getRemainingTime());
                                    int minutes = time / 60;
                                    int seconds = time - minutes * 60;
                                    String zero = "";
                                    if (minutes == 0 && seconds <= 7) {
                                        time = (int) Math.round(context.getPreviousState().getPhaseCountdowns().get().getRemainingTime());
                                        minutes = time / 60;
                                        seconds = time - minutes * 60;
                                    }
                                    if (String.valueOf(seconds).length() == 1) {
                                        zero = "0";
                                    }
                                    int currtime = gameTime[0] + killDelay;
                                    if (eventMap.containsKey(currtime)) {
                                        currtime++;
                                    }
                                    eventMap.put(currtime,
                                            "(" + minutes + ":" + zero + seconds + ")" +
                                                    "(" + Player.get(key).getObserverSlot() + ")" + Player.get(key).getName() +
                                                    " Killed " + "(" + Player.get(k).getObserverSlot() + ")" + Player.get(k).getName() +
                                                    " With " + context.getPreviousState().getAllPlayers().get().get(key).getInventory().getActiveItem().getWeapon());
                                    System.out.println(eventMap.get(currtime));
                                    killDif.set(killDif.get() - 1);
                                    if (killDif.get() < 0) {
                                        break;
                                    }
                                }
                            }
                        } else{
                            KillMap.put(key, Player.get(key).getStatistics().getKillCount());
                            for (Map.Entry<PlayerSteamID, Short> map2 : DeathMap.entrySet()) {
                                PlayerSteamID tkKey = map2.getKey();
                                Short tkValue = map2.getValue();
                                    if (Player.get(tkKey).getStatistics().getDeathCount() > tkValue){
                                        DeathMap.put(tkKey, Player.get(tkKey).getStatistics().getDeathCount());
                                        PhaseCountdownState phase = state.getPhaseCountdowns().get();
                                        int time = (int) Math.round(phase.getRemainingTime());
                                        int minutes = time / 60;
                                        int seconds = time - minutes * 60;
                                        String zero = "";
                                        if (minutes == 0 && seconds <= 7) {
                                            time = (int) Math.round(context.getPreviousState().getPhaseCountdowns().get().getRemainingTime());
                                            minutes = time / 60;
                                            seconds = time - minutes * 60;
                                        }
                                        if (String.valueOf(seconds).length() == 1) {
                                            zero = "0";
                                        }
                                        int currtime = gameTime[0] + killDelay;
                                        if (eventMap.containsKey(currtime)) {
                                            currtime++;
                                        }
                                        eventMap.put(currtime,
                                                "(" + minutes + ":" + zero + seconds + ")" +
                                                        "(" + Player.get(key).getObserverSlot() + ")" + Player.get(key).getName() +
                                                        " Killed " + "(" + Player.get(tkKey).getObserverSlot() + ")" + Player.get(tkKey).getName() +
                                                        " With " + context.getPreviousState().getAllPlayers().get().get(key).getInventory().getActiveItem().getWeapon());
                                        System.out.println(eventMap.get(currtime));
                                    }
                                }
                                if (killDif.get() < 0) {
                                    break;
                                }
                            }
                        }

                }
            });
            state.getRound().ifPresent(Round -> {
                roundNumber[0] = state.getMap().get().getRoundNumber()+1;
                if (Round.getPhase().getString().equals("over") && round[0].get()) {
                    eventMap.put(gameTime[0]+killDelay+10,"-----------------------ROUND " + roundNumber[0] + "-----------------------");
                    round[0].set(false);
                }
                if (Round.getPhase().getString().equals("freezetime") && !round[0].get()) {
                    round[0].set(true);
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


        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (eventMap.containsKey(gameTime[0])) {
                    System.out.println(eventMap.get(gameTime[0]));

                }
                gameTime[0]++;
            }
        };
//        timer.scheduleAtFixedRate(task,1000,1000);
    }

}