package com.rideshare.rideshare_backend.datastructure;

import com.rideshare.rideshare_backend.model.Driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeohashTrie {
    private TrieNode root;
    private int totalDrivers;

    /**
     * Internal node structure for the Trie
     */
    private static class TrieNode {
        Map<Character, TrieNode> children;
        List<Driver> drivers;

        public TrieNode() {
            this.children = new HashMap<>();
            this.drivers = new ArrayList<>();
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public boolean hasDrivers() {
            return !drivers.isEmpty();
        }
    }

    public GeohashTrie() {
        this.root = new TrieNode();
        this.totalDrivers = 0;
    }

    public void insert(String geohash, Driver driver) {
        if (geohash == null || geohash.isEmpty()) {
            throw new IllegalArgumentException("Geohash cannot be null or empty");
        }

        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }

        TrieNode current = root;

        for (char ch : geohash.toCharArray()) {
            current.children.putIfAbsent(ch, new TrieNode());
            current = current.children.get(ch);
        }

        current.drivers.add(driver);
        totalDrivers++;
    }

    public List<Driver> searchByPrefix(String prefix) {
        List<Driver> results = new ArrayList<>();

        if (prefix == null || prefix.isEmpty()) {
            collectAllDrivers(root, results);
            return results;
        }

        TrieNode current = root;

        for (char ch : prefix.toCharArray()) {
            TrieNode child = current.children.get(ch);
            if (child == null) {
                return results;
            }
            current = child;
        }

        collectAllDrivers(current, results);

        return results;
    }

    private void collectAllDrivers(TrieNode node, List<Driver> results) {
        if (node == null) {
            return;
        }

        results.addAll(node.drivers);

        for (TrieNode child : node.children.values()) {
            collectAllDrivers(child, results);
        }
    }

    public boolean delete(String geohash, Driver driver) {
        if (geohash == null || geohash.isEmpty() || driver == null) {
            return false;
        }

        return deleteHelper(root, geohash, 0, driver);
    }

    private boolean deleteHelper(TrieNode node, String geohash, int index, Driver driver) {
        if (index == geohash.length()) {
            boolean removed = node.drivers.remove(driver);

            if (removed) {
                totalDrivers--;
            }

            return removed;
        }

        char ch = geohash.charAt(index);
        TrieNode child = node.children.get(ch);

        if (child == null) {
            return false;
        }

        boolean removed = deleteHelper(child, geohash, index + 1, driver);

        if (!child.hasDrivers() && !child.hasChildren()) {
            node.children.remove(ch);
        }

        return removed;
    }

    /**
     * Update driver's location (remove from old geohash, insert at new geohash)
     *
     * @param oldGeohash Previous geohash location
     * @param newGeohash New geohash location
     * @param driver Driver to update
     * @return true if update successful
     */
    public boolean updateLocation(String oldGeohash, String newGeohash, Driver driver) {
        boolean removed = delete(oldGeohash, driver);

        if (!removed) {
            insert(newGeohash, driver);
            return true;
        }

        insert(newGeohash, driver);
        return true;
    }

    public int getTotalDrivers() {
        return totalDrivers;
    }

    public boolean isEmpty() {
        return totalDrivers == 0;
    }

    public void clear() {
        this.root = new TrieNode();
        this.totalDrivers = 0;
    }

    public TrieStats getStats() {
        TrieStats stats = new TrieStats();
        stats.totalDrivers = this.totalDrivers;
        stats.totalNodes = countNodes(root);
        stats.maxDepth = getMaxDepth(root, 0);
        return stats;
    }

    private int countNodes(TrieNode node) {
        if (node == null) {
            return 0;
        }
        int count = 1; // Count current node
        for (TrieNode child : node.children.values()) {
            count += countNodes(child);
        }
        return count;
    }

    private int getMaxDepth(TrieNode node, int currentDepth) {
        if (node == null) {
            return currentDepth;
        }
        int maxDepth = currentDepth;
        for (TrieNode child : node.children.values()) {
            int childDepth = getMaxDepth(child, currentDepth + 1);
            maxDepth = Math.max(maxDepth, childDepth);
        }
        return maxDepth;
    }

    public static class TrieStats {
        public int totalDrivers;
        public int totalNodes;
        public int maxDepth;

        @Override
        public String toString() {
            return "TrieStats{" +
                    "totalDrivers=" + totalDrivers +
                    ", totalNodes=" + totalNodes +
                    ", maxDepth=" + maxDepth +
                    '}';
        }
    }
}
