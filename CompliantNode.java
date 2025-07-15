import java.util.*;

public class CompliantNode implements Node {

    private Set<Transaction> pendingTransactions;
    private Set<Integer> followees;
    private boolean[] blacklist;
    private Set<Transaction> validTransactions;
    private int currentRound;
    private int numRounds;
    
    // Minimal tracking for obviously dead nodes
    private Set<Integer> activeFollowees;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.numRounds = numRounds;
        this.currentRound = 0;
        
        pendingTransactions = new HashSet<>();
        followees = new HashSet<>();
        validTransactions = new HashSet<>();
        activeFollowees = new HashSet<>();
    }

    public void setFollowees(boolean[] followees) {
        this.blacklist = new boolean[followees.length];
        for (int i = 0; i < followees.length; i++) {
            if (followees[i]) {
                this.followees.add(i);
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
        
        // Track which followees are active (have ever sent anything)
        for (Candidate c : candidates) {
            if (followees.contains(c.sender)) {
                activeFollowees.add(c.sender);
            }
        }
        
        // Very simple blacklisting: only blacklist if they've never sent anything by round 3
        if (currentRound >= 3) {
            for (int followee : followees) {
                if (!activeFollowees.contains(followee)) {
                    blacklist[followee] = true;
                }
            }
        }
        
        // Count all trusted followees
        int trustedCount = 0;
        for (int followee : followees) {
            if (!blacklist[followee]) {
                trustedCount++;
            }
        }
        
        // Very permissive threshold - accept almost anything
        int threshold = 1;
        
        // Count votes for each transaction
        Map<Transaction, Integer> votes = new HashMap<>();
        for (Candidate c : candidates) {
            if (followees.contains(c.sender) && !blacklist[c.sender]) {
                votes.put(c.tx, votes.getOrDefault(c.tx, 0) + 1);
            }
        }
        
        // Add all transactions that meet the minimal threshold
        for (Map.Entry<Transaction, Integer> entry : votes.entrySet()) {
            if (entry.getValue() >= threshold) {
                validTransactions.add(entry.getKey());
            }
        }
    }
}