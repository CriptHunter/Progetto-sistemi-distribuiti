package p2pNetwork;

public enum Status {
    WORKING, //stato base della casa
    EXITING, //la casa sta uscendo dalla rete, aspetta gli ack dalle altre case
    MAKING_GLOBAL_STAT //la casa sta producendo una statistica globale
}

