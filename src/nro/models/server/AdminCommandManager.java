package nro.models.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import nro.models.boss.Boss_Manager.BossManager;
import nro.models.data.LocalManager;
import nro.models.utils.Logger;

public class AdminCommandManager implements Runnable {
    
    private static AdminCommandManager instance;
    private boolean isRunning = false;
    
    public static AdminCommandManager gI() {
        if (instance == null) {
            instance = new AdminCommandManager();
        }
        return instance;
    }
    
    @Override
    public void run() {
        isRunning = true;
        while (isRunning && ServerManager.isRunning) {
            try {
                processCommands();
                Thread.sleep(2000); // Quét mỗi 2 giây
            } catch (Exception e) {
                Logger.error("Error AdminCommandManager: " + e.getMessage());
            }
        }
    }
    
    private void processCommands() {
        try (Connection con = LocalManager.gI().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM admin_command WHERE status = 0");
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String commandName = rs.getString("command_name");
                String commandValue = rs.getString("command_value");
                
                try {
                    handleCommand(commandName, commandValue);
                    updateCommandStatus(id, 1);
                } catch (Exception e) {
                    Logger.error("Lỗi thực thi lệnh: " + commandName + " - " + e.getMessage());
                    updateCommandStatus(id, 2); // 2: Lỗi
                }
            }
        } catch (Exception e) {
        }
    }
    
    private void handleCommand(String commandName, String commandValue) {
        if (commandName == null) return;
        
        switch (commandName.toUpperCase()) {
            case "SPAWN_BOSS":
                try {
                    int bossId = Integer.parseInt(commandValue);
                    BossManager.gI().createBoss(bossId);
                    Logger.success("Admin Panel gọi Spawn Boss ID: " + bossId);
                } catch (NumberFormatException e) {
                    Logger.error("Invalid boss ID: " + commandValue);
                }
                break;
            default:
                Logger.error("Unknown command from admin panel: " + commandName);
                break;
        }
    }
    
    private void updateCommandStatus(int id, int status) {
        try (Connection con = LocalManager.gI().getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE admin_command SET status = ? WHERE id = ?")) {
            ps.setInt(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
        }
    }
    
}
