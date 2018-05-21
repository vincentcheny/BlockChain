import java.util.ArrayList;


public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        //2-存储已经使用过的utxo，避免双发花问题.
        ArrayList<UTXO> utxoUsed = new ArrayList<UTXO>();

        double sumOutput = 0;
        double sumInput = 0;

        //遍历交易中的input实现签名的逐一校验
        for (int i = 0; i < inputs.size() ; i++){
            //根据input中存储的 上一笔交易的哈希值 在UTXOPool 中找到对应的output
            UTXO u = new UTXO(inputs.get(i).prevTxHash, inputs.get(i).outputIndex);
            Transaction.Output output = utxoPool.getTxOutput(u);
            //1
            if(output==null){return false;}
            //2
            if(!Crypto.verifySignature(output.address,
                    tx.getRawDataToSign(i),
                    inputs.get(i).signature)){
                return false;
            }
            //3
            if(utxoUsed.contains(u))return false;
            utxoUsed.add(u);
            sumInput += output.value;
        }

        if (outputs == null) {
            return false;
        }

        for (int i = 0; i < outputs.size(); i++) {
            //4
            if(outputs.get(i).value<0)return false;
            sumOutput += outputs.get(i).value;
        }
        //5
        if(sumInput < sumOutput){return false;}

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> txs = new ArrayList<>();
        boolean foundtx = false;
        //1-存在互相依赖不可以单独校验一次
        do {
            foundtx = false;
            for (Transaction tx : possibleTxs) {
                if(txs.contains(tx))continue;
                if (isValidTx(tx)) {
                    foundtx = true;
                    txs.add(tx);
                    // 从UTXOPool中移除已花费的 即所有input
                    for (Transaction.Input in : tx.getInputs()) {
                        UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                        utxoPool.removeUTXO(utxo);
                    }
                    // 向UTXOPool 中添加新的utxo 即output
                    int idx = 0;
                    for (Transaction.Output out : tx.getOutputs()) {
                        UTXO utxo = new UTXO(tx.getHash(), idx++);
                        utxoPool.addUTXO(utxo, out);
                    }
                }
            }
        }while (foundtx);
        return txs.toArray(new Transaction[txs.size()]);
    }


}