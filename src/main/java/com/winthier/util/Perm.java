package com.winthier.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class Perm
{
    final Map<UUID, List<String>> cache = new HashMap<UUID, List<String>>();
    final UtilMod mod;
    static Perm instance;

    Perm(UtilMod mod)
    {
        instance = this;
        this.mod = mod;
    }

    void clearPlayer(UUID uuid) {
        cache.remove(uuid);
    }

    static Perm instance()
    {
        return instance;
    }

    public boolean playerHasPermission(UUID uuid, String permission)
    {
        return permsOfPlayer(uuid).contains(permission);
    }

    List<String> permsOfPlayer(UUID uuid)
    {
        List<String> result = cache.get(uuid);
        if (result == null) {
            result = fetchPermsOfPlayer(uuid);
            cache.put(uuid, result);
        }
        return result;
    }

    Properties info() {
        Properties info = new Properties();
        info.put("user", "readonly");
        info.put("password", "theyseemerollin");
        info.put("autoReconnect", "false");
        info.put("maxReconnects", "1");
        return info;
    }

    Connection connect(String url) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return null;
        }
        try {
            return DriverManager.getConnection(url, info());
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return null;
        }
    }

    String sqlSet(Collection<? extends Object> set) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        Iterator<? extends Object> iter = set.iterator();
        if (iter.hasNext()) sb.append("'" + iter.next().toString() + "'");
        while (iter.hasNext()) {
            sb.append(", ");
            sb.append("'" + iter.next().toString() + "'");
        }
        sb.append(")");
        return sb.toString();
    }

    private List<String> fetchPermsOfPlayer(UUID uuid) {
        Connection connection = connect("jdbc:mysql://127.0.0.1:3306/Perm");
        String uuidString = "\"" + uuid.toString() + "\"";
        Statement stmt = null;
        ResultSet result = null;
        Set<String> groups = new HashSet<String>();
        Map<String, String> parents = new HashMap<String, String>();
        List<String> perms = new ArrayList<String>();
        try {
            // get group memberships
            stmt = connection.createStatement();
            result = stmt.executeQuery("SELECT * FROM members WHERE member = " + uuidString);
            while (result.next()) {
                groups.add(result.getString("group").toLowerCase());
            }
            stmt.close();
            // get all memberships (memberships => entities)
            stmt = connection.createStatement();
            result = stmt.executeQuery("SELECT * FROM groups");
            while (result.next()) {
                String child = result.getString("key");
                String parent = result.getString("parent");
                if (!result.wasNull()) {
                    parents.put(child, parent);
                }
            }
            stmt.close();
            // Inherit
            boolean foundSomething;
            do {
                foundSomething = false;
                for (String group: new ArrayList<String>(groups)) {
                    String parent = parents.get(group);
                    if (parent != null && !groups.contains(parent)) {
                        groups.add(parent);
                        foundSomething = true;
                    }
                }
            } while (foundSomething);
            // get all entries (entries)
            stmt = connection.createStatement();
            result = stmt.executeQuery("SELECT * FROM permissions WHERE is_group = 1 AND value = 1 AND entity IN " + sqlSet(groups));
            while (result.next()) {
                perms.add(result.getString("permission"));
            }
            stmt.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return perms;
    }
}
