package assignment2;

import java.util.*;

public class CompliantNode implements Node {

    private Set<Transaction> pendingTransactions;
    private Set<Integer> followees;
    private boolean[] blacklist;
    private Set<Transaction> validTransactions;

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;

        pendingTransactions = new HashSet<>();
        followees = new HashSet<>();
        validTransactions = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        this.blacklist = new boolean[followees.length];
        for (int i = 0; i < followees.length; i++) {
            if (followees[i]) this.followees.add(i);
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = new HashSet<>(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        return new HashSet<>(validTransactions);
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        Map<Transaction, Integer> transactionCount = new HashMap<>();

        for (Candidate c : candidates) {
            if (blacklist[c.sender]) continue;
            transactionCount.put(c.tx, transactionCount.getOrDefault(c.tx, 0) + 1);
        }

        int threshold = 1;
        validTransactions.clear();

        for (Map.Entry<Transaction, Integer> entry : transactionCount.entrySet()) {
            if (entry.getValue() >= threshold) {
                validTransactions.add(entry.getKey());
            }
        }
    }
}
