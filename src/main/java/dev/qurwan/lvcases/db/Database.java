package dev.qurwan.lvcases.db;

import dev.qurwan.lvcases.LvCases;

import java.io.*;
import java.util.Map;

public class Database {
    private final File file;
    public Database(File file)
    {
        this.file = file;
    }
    public void load()
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String nickname = parts[0];
                    String data = parts[1];
                    LvCases.getUsers().put(nickname, data);
                }
            }
        } catch (IOException ignored) {}
    }
    public void save()
    {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        for (Map.Entry<String, String> entry : LvCases.getUsers().entrySet()) {
            writer.write(entry.getKey() + ":" + entry.getValue());
            writer.newLine();
        }
    } catch (IOException ignored) {}
    }

    public static int count(String player, String type) {
        String cases = LvCases.getUsers().get(player);
        if (cases == null || type == null || cases.isEmpty() || type.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = cases.indexOf(type);
        while (index != -1) {
            count++;
            index = cases.indexOf(type, index + 1);
        }
        return count;
    }

    public static void give(String player, String type)
    {
        String cases = LvCases.getUsers().get(player);
        LvCases.getUsers().put(player, cases + ";" + type);
    }

    public static void remove(String player, String type)
    {
        String cases = LvCases.getUsers().get(player).replaceFirst(type, "");
        LvCases.getUsers().put(player, cases);
    }

}
