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
        if (iter.hasNext()) sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(", ");
            sb.append(iter.next().toString());
        }
        sb.append(")");
        return sb.toString();
    }

    private List<String> fetchPermsOfPlayer(UUID uuid)
    {
        Connection connection = connect("jdbc:mysql://127.0.0.1:3306/zPermissions");
        String uuidString = "\"" + uuid.toString().replace("-", "") + "\"";
        Statement stmt = null;
        ResultSet result = null;
        Set<Integer> entityIds = new HashSet<Integer>();
        List<String> perms = new ArrayList<String>();
        try {
            // get entity row (entities)
            stmt = connection.createStatement();
            result = stmt.executeQuery("SELECT * FROM entities WHERE name = " + uuidString);
            while (result.next()) {
                entityIds.add(result.getInt("id"));
            }
            stmt.close();
            // get all memberships (memberships => entities)
            stmt = connection.createStatement();
            result = stmt.executeQuery("SELECT * FROM memberships WHERE member = " + uuidString);
            while (result.next()) {
                entityIds.add(result.getInt("group_id"));
            }
            stmt.close();
            // get all inheritances (inheritances => entities)
            HashSet<Integer> tmpIds = new HashSet<Integer>();
            tmpIds.addAll(entityIds);
            while (!tmpIds.isEmpty()) {
                stmt = connection.createStatement();
                result = stmt.executeQuery("SELECT * FROM inheritances WHERE child_id IN " + sqlSet(tmpIds));
                tmpIds.clear();
                while (result.next()) {
                    tmpIds.add(result.getInt("parent_id"));
                }
                entityIds.addAll(tmpIds);
                stmt.close();
            }
            // get all entries (entries)
            stmt = connection.createStatement();
            result = stmt.executeQuery("SELECT * FROM entries WHERE world_id IS NULL AND region_id IS NULL AND entity_id IN " + sqlSet(entityIds));
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
