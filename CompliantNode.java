import java.util.*;

public class CompliantNode implements Node {

    private Set<Transaction> pendingTransactions;
    private Set<Integer> followees;
    private boolean[] blacklist;
    private Set<Transaction> validTransactions;
    private int currentRound;
    private int numRounds;
    
    // Track rounds where each node sent nothing (to detect dead nodes)
    private Map<Integer, Integer> silentRounds;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.numRounds = numRounds;
        this.currentRound = 0;
        
        pendingTransactions = new HashSet<>();
        followees = new HashSet<>();
        validTransactions = new HashSet<>();
        silentRounds = new HashMap<>();
    }

    public void setFollowees(boolean[] followees) {
        this.blacklist = new boolean[followees.length];
        for (int i = 0; i < followees.length; i++) {
            if (followees[i]) {
                this.followees.add(i);
                this.silentRounds.put(i, 0);
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = new HashSet<>(pendingTransactions);
        this.validTransactions = new HashSet<>(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        return new HashSet<>(validTransactions);
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        currentRound++;
        
        // Track which followees sent proposals this round
        Set<Integer> activeSenders = new HashSet<>();
        for (Candidate c : candidates) {
            if (followees.contains(c.sender)) {
                activeSenders.add(c.sender);
            }
        }
        
        // Update silent round counters and blacklist consistently silent nodes
        for (int followee : followees) {
            if (activeSenders.contains(followee)) {
                silentRounds.put(followee, 0);
            } else {
                int silent = silentRounds.get(followee) + 1;
                silentRounds.put(followee, silent);
                
                // Blacklist nodes that have been silent for too many consecutive rounds
                if (silent >= 4) {
                    blacklist[followee] = true;
                }
            }
        }
        
        // Count votes for each transaction from non-blacklisted followees
        Map<Transaction, Integer> votes = new HashMap<>();
        for (Candidate c : candidates) {
            if (followees.contains(c.sender) && !blacklist[c.sender]) {
                votes.put(c.tx, votes.getOrDefault(c.tx, 0) + 1);
            }
        }
        
        // Calculate threshold: require at least 2 votes unless we have very few followees
        int trustedFolloweeCount = 0;
        for (int followee : followees) {
            if (!blacklist[followee]) {
                trustedFolloweeCount++;
            }
        }
        
        int threshold;
        if (trustedFolloweeCount <= 3) {
            threshold = 1;  // If very few trusted followees, accept single votes
        } else if (trustedFolloweeCount <= 10) {
            threshold = 2;  // Small network, require 2 votes
        } else {
            threshold = Math.max(2, trustedFolloweeCount / 5);  // Larger network, ~20% agreement
        }
        
        // Add transactions that meet the threshold
        for (Map.Entry<Transaction, Integer> entry : votes.entrySet()) {
            if (entry.getValue() >= threshold) {
                validTransactions.add(entry.getKey());
            }
        }
    }
}