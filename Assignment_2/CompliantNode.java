import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private boolean[] followees;
    private Set<Transaction> pendingTransactions;
    private boolean[] blacklist;// a node share the same blacklist among different rounds


    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        this.followees = followees;
        blacklist = new boolean[followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        Set<Transaction> toFollowers = new HashSet<>(pendingTransactions);
        pendingTransactions.clear();
        return toFollowers;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
    	// A candidate, who does not send tx as a followee, should be in blacklist
        for(int i = 0; i < this.followees.length; ++i)
        {
        	if(followees[i])
        	{
        		boolean in = false;
        		for(Candidate can: candidates)
        		{
        			if(can.sender == i)
        			{
        				in = true;
        				break;
        			}
        		}
        		if(!in)
        		{
        			blacklist[i] = true;
        		}
        	}
        }
        for(Candidate can: candidates)
        {
        	if(!blacklist[can.sender])
        	{
        		this.pendingTransactions.add(can.tx);
        	}
        }
    }
}
