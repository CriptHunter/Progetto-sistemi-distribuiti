package Messages;

public enum Header {
    NET_ENTRANCE,     //annunio entrata nella rete
    NET_ENTRANCE_ACK, //messaggio net_entrance ricevuto
    NET_EXIT,         //annuncio uscita dalla rete
    NET_EXIT_ACK,     //messaggio net_exit ricevuto
    LOCAL_STAT,       //nuova statistica locale
    GLOBAL_STAT,      //nuova statistica globale
    BOOST_REQUEST,
    BOOST_OK

}